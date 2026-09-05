package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.auth.AuthRepository
import com.example.data.db.BookmarkEntity
import com.example.data.db.DhikrHistoryEntity
import com.example.data.db.PrayerLogEntity
import com.example.data.model.CalcMethod
import com.example.data.model.DhikrPreset
import com.example.data.model.Madhab
import com.example.data.model.PrayerName
import com.example.data.model.QuranLastRead
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.repository.AppRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dedicated Firestore Synchronization Manager.
 * Handles bidirectional cloud synchronization for authenticated users while
 * maintaining local storage as the immediate, offline-first source of truth.
 *
 * Guest users remain strictly local (no reads, writes, or snapshot listeners).
 */
class FirestoreSyncManager(
    private val context: Context,
    private val repository: AppRepository,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    companion object {
        private const val TAG = "FirestoreSyncManager"
        @Volatile
        private var INSTANCE: FirestoreSyncManager? = null

        fun getInstance(
            context: Context,
            repository: AppRepository,
            authRepository: AuthRepository
        ): FirestoreSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreSyncManager(
                    context.applicationContext,
                    repository,
                    authRepository
                ).also { INSTANCE = it }
            }
        }
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow<Long?>(null)
    val lastSyncedTime: StateFlow<Long?> = _lastSyncedTime.asStateFlow()

    // Flag to prevent listener -> local write -> cloud write -> listener feedback loops
    val isSyncingFromRemote = AtomicBoolean(false)

    @Volatile
    private var activeUserId: String? = null
    private val listenerRegistrations = mutableListOf<ListenerRegistration>()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var recoveryJob: kotlinx.coroutines.Job? = null

    init {
        // Link to repository so repository knows when remote sync is active
        repository.syncManager = this
        startAuthObservation()
    }

    /**
     * Observes Firebase Auth state changes.
     * When user is authenticated, starts synchronization.
     * When user signs out, immediately terminates listeners, stops active sync, and resets to guest mode.
     */
    private fun startAuthObservation() {
        scope.launch {
            authRepository.currentUser.collect { user ->
                handleUserChanged(user)
            }
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) return
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Network available callback received. Triggering sync recovery.")
                    triggerNetworkRecovery()
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Network lost callback received.")
                    val isConnected = com.example.data.util.NetworkUtils.isNetworkAvailable(context)
                    if (!isConnected) {
                        _syncState.value = SyncState.Offline
                    }
                }
            }
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
            networkCallback = callback
            Log.d(TAG, "Registered NetworkCallback for automatic sync recovery.")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network callback: ${e.message}")
        }
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let { callback ->
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                connectivityManager?.unregisterNetworkCallback(callback)
                Log.d(TAG, "Unregistered NetworkCallback.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to unregister network callback: ${e.message}")
            }
        }
        networkCallback = null
        recoveryJob?.cancel()
        recoveryJob = null
    }

    /**
     * Coalesces rapid network callbacks and automatically retries synchronization when network returns.
     */
    fun triggerNetworkRecovery() {
        val uid = activeUserId ?: return
        recoveryJob?.cancel()
        recoveryJob = scope.launch {
            kotlinx.coroutines.delay(500L) // Debounce 500ms
            val currentUid = activeUserId ?: return@launch
            if (!com.example.data.util.NetworkUtils.isNetworkAvailable(context)) {
                _syncState.value = SyncState.Offline
                return@launch
            }
            if (_syncState.value == SyncState.Syncing) {
                Log.d(TAG, "Sync already in progress, skipping recovery attempt.")
                return@launch
            }

            Log.d(TAG, "Executing automatic sync recovery for $currentUid")
            _syncState.value = SyncState.Syncing
            try {
                performInitialMerge(currentUid)
                val now = System.currentTimeMillis()
                _syncState.value = SyncState.Synced
                _lastSyncedTime.value = now
                prefs.edit().putLong("last_synced_timestamp_$currentUid", now).apply()
                Log.d(TAG, "Automatic sync recovery completed successfully for $currentUid")
            } catch (e: Exception) {
                Log.e(TAG, "Automatic sync recovery failed: ${e.message}", e)
                _syncState.value = SyncState.Offline
            }
        }
    }

    private fun handleUserChanged(user: FirebaseUser?) {
        val newUid = user?.uid
        if (newUid == activeUserId) return

        if (newUid == null) {
            // User signed out -> Return to guest/local-only mode
            stopSync()
            unregisterNetworkCallback()
            activeUserId = null
            _syncState.value = SyncState.Idle
            _lastSyncedTime.value = null
            Log.d(TAG, "User signed out. Firestore sync stopped, guest mode active.")
        } else {
            // New authenticated user -> Stop previous if any, then start sync for this UID
            stopSync()
            registerNetworkCallback()
            activeUserId = newUid

            val savedTs = prefs.getLong("last_synced_timestamp_$newUid", 0L)
            if (savedTs > 0L) {
                _lastSyncedTime.value = savedTs
            }

            Log.d(TAG, "User signed in ($newUid). Initializing Firestore sync.")
            scope.launch {
                try {
                    _syncState.value = SyncState.Syncing
                    attachSnapshotListeners(newUid)
                    try {
                        performInitialMerge(newUid)
                    } catch (e: Exception) {
                        Log.w(TAG, "Initial merge finished with offline/partial results: ${e.message}")
                    }

                    if (com.example.data.util.NetworkUtils.isNetworkAvailable(context)) {
                        _syncState.value = SyncState.Synced
                        val now = System.currentTimeMillis()
                        _lastSyncedTime.value = now
                        prefs.edit().putLong("last_synced_timestamp_$newUid", now).apply()
                    } else {
                        _syncState.value = SyncState.Offline
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing sync for $newUid", e)
                    _syncState.value = SyncState.Offline
                }
            }
        }
    }

    /**
     * Cancels all active Firestore listeners and clears registered state.
     */
    @Synchronized
    fun stopSync() {
        unregisterNetworkCallback()
        for (listener in listenerRegistrations) {
            try {
                listener.remove()
            } catch (e: Exception) {
                Log.w(TAG, "Error removing Firestore listener", e)
            }
        }
        listenerRegistrations.clear()
    }

    /**
     * Deletes all cloud data for the specified user ID from Firestore.
     */
    suspend fun deleteUserCloudData(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userDoc = firestore.collection("users").document(uid)
            val subcollections = listOf("prayer_logs", "bookmarks", "dhikr_history", "data")

            for (subcol in subcollections) {
                val snapshot = userDoc.collection(subcol).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            }
            userDoc.delete().await()
            Log.d(TAG, "Successfully deleted all user cloud data for UID: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user cloud data for UID $uid", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // INITIAL MERGE ENGINE (FIRST LOGIN / FIRST SYNC)
    // =========================================================================

    suspend fun performInitialMerge(uid: String) = withContext(Dispatchers.IO) {
        isSyncingFromRemote.set(true)
        try {
            try { mergePrayerLogs(uid) } catch (e: Exception) { Log.w(TAG, "Prayer logs merge error: ${e.message}") }
            try { mergeBookmarks(uid) } catch (e: Exception) { Log.w(TAG, "Bookmarks merge error: ${e.message}") }
            try { mergeDhikrHistory(uid) } catch (e: Exception) { Log.w(TAG, "Dhikr history merge error: ${e.message}") }
            try { mergeQada(uid) } catch (e: Exception) { Log.w(TAG, "Qada merge error: ${e.message}") }
            try { mergeQuranProgress(uid) } catch (e: Exception) { Log.w(TAG, "Quran progress merge error: ${e.message}") }
            try { mergeTasbeehState(uid) } catch (e: Exception) { Log.w(TAG, "Tasbeeh state merge error: ${e.message}") }
            try { mergePreferences(uid) } catch (e: Exception) { Log.w(TAG, "Preferences merge error: ${e.message}") }

            Log.d(TAG, "Initial bidirectional merge completed for $uid")
        } finally {
            isSyncingFromRemote.set(false)
        }
    }

    private suspend fun mergePrayerLogs(uid: String) {
        val col = firestore.collection("users").document(uid).collection("prayer_logs")
        val remoteDocs = try {
            col.get().await().documents
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch remote prayer logs", e)
            emptyList()
        }

        val remoteMap = remoteDocs.associateBy { it.id }
        val localLogs = repository.getAllRawPrayerLogsDirect().associateBy { it.date }

        // Process all dates found either locally or remotely
        val allDates = localLogs.keys + remoteMap.keys
        for (date in allDates) {
            val local = localLogs[date]
            val remoteDoc = remoteMap[date]

            if (local == null && remoteDoc != null) {
                // Exists only in cloud -> Apply to local
                val remoteEntity = docToPrayerLog(remoteDoc)
                repository.applyPrayerLogFromRemote(remoteEntity)
            } else if (local != null && remoteDoc == null) {
                // Exists only locally -> Upload to cloud
                col.document(date).set(prayerLogToMap(local)).await()
            } else if (local != null && remoteDoc != null) {
                // Exists in both -> Conflict resolution by updatedAt (LWW)
                val remoteUpdatedAt = remoteDoc.getLong("updatedAt") ?: 0L
                if (remoteUpdatedAt > local.updatedAt) {
                    val remoteEntity = docToPrayerLog(remoteDoc)
                    repository.applyPrayerLogFromRemote(remoteEntity)
                } else if (local.updatedAt > remoteUpdatedAt) {
                    col.document(date).set(prayerLogToMap(local)).await()
                }
            }
        }
    }

    private suspend fun mergeBookmarks(uid: String) {
        val col = firestore.collection("users").document(uid).collection("bookmarks")
        val remoteDocs = try {
            col.get().await().documents
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch remote bookmarks", e)
            emptyList()
        }

        val remoteMap = remoteDocs.associateBy { it.id }
        val localBookmarks = repository.getAllRawBookmarksDirect().associateBy { "${it.surahNumber}_${it.verseNumber}" }

        val allKeys = localBookmarks.keys + remoteMap.keys
        for (key in allKeys) {
            val local = localBookmarks[key]
            val remoteDoc = remoteMap[key]

            if (local == null && remoteDoc != null) {
                val entity = docToBookmark(remoteDoc)
                repository.applyBookmarkFromRemote(entity)
            } else if (local != null && remoteDoc == null) {
                col.document(key).set(bookmarkToMap(local)).await()
            } else if (local != null && remoteDoc != null) {
                val remoteUpdatedAt = remoteDoc.getLong("updatedAt") ?: 0L
                if (remoteUpdatedAt > local.updatedAt) {
                    val entity = docToBookmark(remoteDoc)
                    repository.applyBookmarkFromRemote(entity)
                } else if (local.updatedAt > remoteUpdatedAt) {
                    col.document(key).set(bookmarkToMap(local)).await()
                }
            }
        }
    }

    private suspend fun mergeDhikrHistory(uid: String) {
        val col = firestore.collection("users").document(uid).collection("dhikr_history")
        val remoteDocs = try {
            col.get().await().documents
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch remote dhikr history", e)
            emptyList()
        }

        val remoteMap = remoteDocs.associateBy { it.id }
        val localList = repository.getAllDhikrHistoryDirect()
        val localMap = localList.associateBy { it.syncId }

        // Insert remote entries missing locally
        for (remoteDoc in remoteDocs) {
            val syncId = remoteDoc.getString("syncId") ?: remoteDoc.id
            if (!localMap.containsKey(syncId)) {
                val entity = docToDhikrHistory(remoteDoc)
                repository.applyDhikrHistoryFromRemote(entity)
            }
        }

        // Upload local entries missing remotely
        for (local in localList) {
            if (!remoteMap.containsKey(local.syncId)) {
                col.document(local.syncId).set(dhikrHistoryToMap(local)).await()
            }
        }
    }

    private suspend fun mergeQada(uid: String) {
        val docRef = firestore.collection("users").document(uid).collection("data").document("qada")
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        val prayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        val cloudData = snapshot?.data ?: emptyMap<String, Any?>()

        var hasLocalWin = false
        for (prayer in prayers) {
            val pName = prayer.name.lowercase()
            val localTs = repository.getQadaTimestamp(prayer)
            val cloudTs = (cloudData["${pName}UpdatedAt"] as? Number)?.toLong() ?: 0L

            if (cloudTs > localTs) {
                // Cloud wins for this prayer
                val count = (cloudData["${pName}Count"] as? Number)?.toInt() ?: 0
                val ever = (cloudData["${pName}EverAdded"] as? Boolean) ?: (count > 0)
                repository.applyQadaFromRemote(prayer, count, ever, cloudTs)
            } else if (localTs > cloudTs || snapshot?.exists() != true) {
                hasLocalWin = true
            }
        }

        if (hasLocalWin || snapshot?.exists() != true) {
            // Push merged state to cloud
            val map = buildQadaMap()
            docRef.set(map, SetOptions.merge()).await()
        }
    }

    private suspend fun mergeQuranProgress(uid: String) {
        val docRef = firestore.collection("users").document(uid).collection("data").document("quran_progress")
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        val localTs = repository.getQuranProgressTimestamp()
        val cloudTs = snapshot?.getLong("updatedAt") ?: 0L

        if (snapshot != null && snapshot.exists() && cloudTs > localTs) {
            // Apply cloud to local
            val lastRead = parseLastReadFromDoc(snapshot)
            val recentlyRead = parseRecentlyReadFromDoc(snapshot)
            val goal = snapshot.getLong("dailyGoal")?.toInt() ?: 0
            repository.applyQuranProgressFromRemote(lastRead, recentlyRead, goal, cloudTs)
        } else {
            // Upload local to cloud
            docRef.set(buildQuranProgressMap(), SetOptions.merge()).await()
        }
    }

    private suspend fun mergeTasbeehState(uid: String) {
        val docRef = firestore.collection("users").document(uid).collection("data").document("tasbeeh_state")
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        val localTs = repository.getTasbeehStateTimestamp()
        val cloudTs = snapshot?.getLong("updatedAt") ?: 0L

        if (snapshot != null && snapshot.exists()) {
            if (cloudTs > localTs) {
                // Remote is newer: apply remote custom presets and targets
                val presets = parseCustomPresets(snapshot.getString("customPresetsJson"))
                val targets = parseCustomTargets(snapshot.getString("customTargetsJson"))
                val counts = parseStringIntMap(snapshot.get("activeCounts"))
                val targetMap = parseStringIntMap(snapshot.get("activeTargets"))
                repository.applyTasbeehStateFromRemote(presets, targets, counts, targetMap, cloudTs)
            } else if (localTs > cloudTs) {
                docRef.set(buildTasbeehStateMap(), SetOptions.merge()).await()
            }
        } else {
            docRef.set(buildTasbeehStateMap(), SetOptions.merge()).await()
        }
    }

    private suspend fun mergePreferences(uid: String) {
        val docRef = firestore.collection("users").document(uid).collection("data").document("preferences")
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        val localTs = repository.getPreferencesTimestamp()
        val cloudTs = snapshot?.getLong("updatedAt") ?: 0L

        if (snapshot != null && snapshot.exists() && cloudTs > localTs) {
            repository.applyPreferencesFromRemote(snapshot.data ?: emptyMap(), cloudTs)
        } else {
            docRef.set(buildPreferencesMap(), SetOptions.merge()).await()
        }
    }

    // =========================================================================
    // REAL-TIME SNAPSHOT LISTENERS
    // =========================================================================

    @Synchronized
    private fun attachSnapshotListeners(uid: String) {
        val userDoc = firestore.collection("users").document(uid)

        // 1. Prayer Logs Listener
        val prayerLogsReg = userDoc.collection("prayer_logs").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    for (docChange in snapshot.documentChanges) {
                        val doc = docChange.document
                        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
                        val local = repository.getRawPrayerLogForDateDirect(doc.id)
                        if (local == null || remoteUpdatedAt > local.updatedAt) {
                            val entity = docToPrayerLog(doc)
                            repository.applyPrayerLogFromRemote(entity)
                        }
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(prayerLogsReg)

        // 2. Bookmarks Listener
        val bookmarksReg = userDoc.collection("bookmarks").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    for (docChange in snapshot.documentChanges) {
                        val doc = docChange.document
                        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
                        val surah = doc.getLong("surahNumber")?.toInt() ?: 0
                        val verse = doc.getLong("verseNumber")?.toInt() ?: 0
                        if (surah > 0 && verse > 0) {
                            val local = repository.getRawBookmarkDirect(surah, verse)
                            if (local == null || remoteUpdatedAt > local.updatedAt) {
                                val entity = docToBookmark(doc)
                                repository.applyBookmarkFromRemote(entity)
                            }
                        }
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(bookmarksReg)

        // 3. Dhikr History Listener
        val dhikrHistoryReg = userDoc.collection("dhikr_history").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    for (docChange in snapshot.documentChanges) {
                        val doc = docChange.document
                        val syncId = doc.getString("syncId") ?: doc.id
                        val existing = repository.getDhikrHistoryBySyncId(syncId)
                        if (existing == null) {
                            val entity = docToDhikrHistory(doc)
                            repository.applyDhikrHistoryFromRemote(entity)
                        }
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(dhikrHistoryReg)

        // 4. Qada Document Listener
        val qadaReg = userDoc.collection("data").document("qada").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    val data = snapshot.data ?: return@launch
                    val prayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
                    for (prayer in prayers) {
                        val pName = prayer.name.lowercase()
                        val remoteTs = (data["${pName}UpdatedAt"] as? Number)?.toLong() ?: 0L
                        val localTs = repository.getQadaTimestamp(prayer)
                        if (remoteTs > localTs) {
                            val count = (data["${pName}Count"] as? Number)?.toInt() ?: 0
                            val ever = (data["${pName}EverAdded"] as? Boolean) ?: (count > 0)
                            repository.applyQadaFromRemote(prayer, count, ever, remoteTs)
                        }
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(qadaReg)

        // 5. Quran Progress Listener
        val quranReg = userDoc.collection("data").document("quran_progress").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    val remoteUpdatedAt = snapshot.getLong("updatedAt") ?: 0L
                    val localTs = repository.getQuranProgressTimestamp()
                    if (remoteUpdatedAt > localTs) {
                        val lastRead = parseLastReadFromDoc(snapshot)
                        val recentlyRead = parseRecentlyReadFromDoc(snapshot)
                        val goal = snapshot.getLong("dailyGoal")?.toInt() ?: 0
                        repository.applyQuranProgressFromRemote(lastRead, recentlyRead, goal, remoteUpdatedAt)
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(quranReg)

        // 6. Preferences Listener
        val prefsReg = userDoc.collection("data").document("preferences").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    val remoteUpdatedAt = snapshot.getLong("updatedAt") ?: 0L
                    val localTs = repository.getPreferencesTimestamp()
                    if (remoteUpdatedAt > localTs) {
                        repository.applyPreferencesFromRemote(snapshot.data ?: emptyMap(), remoteUpdatedAt)
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(prefsReg)

        // 7. Tasbeeh State Listener
        val tasbeehReg = userDoc.collection("data").document("tasbeeh_state").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (activeUserId != uid || isSyncingFromRemote.get()) return@addSnapshotListener

            scope.launch {
                isSyncingFromRemote.set(true)
                try {
                    val remoteUpdatedAt = snapshot.getLong("updatedAt") ?: 0L
                    val localTs = repository.getTasbeehStateTimestamp()
                    if (remoteUpdatedAt > localTs) {
                        val presets = parseCustomPresets(snapshot.getString("customPresetsJson"))
                        val targets = parseCustomTargets(snapshot.getString("customTargetsJson"))
                        val counts = parseStringIntMap(snapshot.get("activeCounts"))
                        val targetMap = parseStringIntMap(snapshot.get("activeTargets"))
                        repository.applyTasbeehStateFromRemote(presets, targets, counts, targetMap, remoteUpdatedAt)
                    }
                } finally {
                    isSyncingFromRemote.set(false)
                }
            }
        }
        listenerRegistrations.add(tasbeehReg)
    }

    // =========================================================================
    // OUTBOUND SYNC TRIGGERS (ASYNCHRONOUS, NON-BLOCKING)
    // =========================================================================

    fun notifyPrayerLogChanged(log: PrayerLogEntity) {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                firestore.collection("users").document(uid)
                    .collection("prayer_logs").document(log.date)
                    .set(prayerLogToMap(log))
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for prayer log ${log.date}", e)
            }
        }
    }

    fun notifyBookmarkChanged(bookmark: BookmarkEntity) {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                val docId = "${bookmark.surahNumber}_${bookmark.verseNumber}"
                firestore.collection("users").document(uid)
                    .collection("bookmarks").document(docId)
                    .set(bookmarkToMap(bookmark))
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for bookmark", e)
            }
        }
    }

    fun notifyDhikrHistoryAdded(entry: DhikrHistoryEntity) {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                firestore.collection("users").document(uid)
                    .collection("dhikr_history").document(entry.syncId)
                    .set(dhikrHistoryToMap(entry))
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for dhikr history", e)
            }
        }
    }

    fun notifyQadaChanged(prayerName: PrayerName) {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                val map = buildQadaMap()
                firestore.collection("users").document(uid)
                    .collection("data").document("qada")
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for Qada", e)
            }
        }
    }

    fun notifyQuranProgressChanged() {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                val map = buildQuranProgressMap()
                firestore.collection("users").document(uid)
                    .collection("data").document("quran_progress")
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for Quran progress", e)
            }
        }
    }

    fun notifyPreferencesChanged() {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                val map = buildPreferencesMap()
                firestore.collection("users").document(uid)
                    .collection("data").document("preferences")
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for preferences", e)
            }
        }
    }

    fun notifyTasbeehStateChanged() {
        val uid = activeUserId ?: return
        if (isSyncingFromRemote.get()) return

        scope.launch {
            try {
                val map = buildTasbeehStateMap()
                firestore.collection("users").document(uid)
                    .collection("data").document("tasbeeh_state")
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed outbound sync for tasbeeh state", e)
            }
        }
    }

    // =========================================================================
    // MAPPER & SERIALIZATION HELPERS
    // =========================================================================

    private fun prayerLogToMap(log: PrayerLogEntity): Map<String, Any?> {
        return mapOf(
            "date" to log.date,
            "fajrCompleted" to log.fajrCompleted,
            "dhuhrCompleted" to log.dhuhrCompleted,
            "asrCompleted" to log.asrCompleted,
            "maghribCompleted" to log.maghribCompleted,
            "ishaCompleted" to log.ishaCompleted,
            "fajrMissed" to log.fajrMissed,
            "dhuhrMissed" to log.dhuhrMissed,
            "asrMissed" to log.asrMissed,
            "maghribMissed" to log.maghribMissed,
            "ishaMissed" to log.ishaMissed,
            "fajrNote" to log.fajrNote,
            "dhuhrNote" to log.dhuhrNote,
            "asrNote" to log.asrNote,
            "maghribNote" to log.maghribNote,
            "ishaNote" to log.ishaNote,
            "fajrQadaAdded" to log.fajrQadaAdded,
            "dhuhrQadaAdded" to log.dhuhrQadaAdded,
            "asrQadaAdded" to log.asrQadaAdded,
            "maghribQadaAdded" to log.maghribQadaAdded,
            "ishaQadaAdded" to log.ishaQadaAdded,
            "updatedAt" to log.updatedAt,
            "isDeleted" to log.isDeleted
        )
    }

    private fun docToPrayerLog(doc: DocumentSnapshot): PrayerLogEntity {
        return PrayerLogEntity(
            date = doc.id,
            fajrCompleted = doc.getBoolean("fajrCompleted") ?: false,
            dhuhrCompleted = doc.getBoolean("dhuhrCompleted") ?: false,
            asrCompleted = doc.getBoolean("asrCompleted") ?: false,
            maghribCompleted = doc.getBoolean("maghribCompleted") ?: false,
            ishaCompleted = doc.getBoolean("ishaCompleted") ?: false,
            fajrMissed = doc.getBoolean("fajrMissed") ?: false,
            dhuhrMissed = doc.getBoolean("dhuhrMissed") ?: false,
            asrMissed = doc.getBoolean("asrMissed") ?: false,
            maghribMissed = doc.getBoolean("maghribMissed") ?: false,
            ishaMissed = doc.getBoolean("ishaMissed") ?: false,
            fajrNote = doc.getString("fajrNote"),
            dhuhrNote = doc.getString("dhuhrNote"),
            asrNote = doc.getString("asrNote"),
            maghribNote = doc.getString("maghribNote"),
            ishaNote = doc.getString("ishaNote"),
            fajrQadaAdded = doc.getBoolean("fajrQadaAdded") ?: false,
            dhuhrQadaAdded = doc.getBoolean("dhuhrQadaAdded") ?: false,
            asrQadaAdded = doc.getBoolean("asrQadaAdded") ?: false,
            maghribQadaAdded = doc.getBoolean("maghribQadaAdded") ?: false,
            ishaQadaAdded = doc.getBoolean("ishaQadaAdded") ?: false,
            updatedAt = doc.getLong("updatedAt") ?: 0L,
            isDeleted = doc.getBoolean("isDeleted") ?: false
        )
    }

    private fun bookmarkToMap(bookmark: BookmarkEntity): Map<String, Any?> {
        return mapOf(
            "surahNumber" to bookmark.surahNumber,
            "verseNumber" to bookmark.verseNumber,
            "surahNameEnglish" to bookmark.surahNameEnglish,
            "surahNameArabic" to bookmark.surahNameArabic,
            "verseTextArabic" to bookmark.verseTextArabic,
            "verseTextTranslation" to bookmark.verseTextTranslation,
            "createdAt" to bookmark.timestamp,
            "updatedAt" to bookmark.updatedAt,
            "isDeleted" to bookmark.isDeleted
        )
    }

    private fun docToBookmark(doc: DocumentSnapshot): BookmarkEntity {
        return BookmarkEntity(
            surahNumber = doc.getLong("surahNumber")?.toInt() ?: 0,
            verseNumber = doc.getLong("verseNumber")?.toInt() ?: 0,
            surahNameEnglish = doc.getString("surahNameEnglish") ?: "",
            surahNameArabic = doc.getString("surahNameArabic") ?: "",
            verseTextArabic = doc.getString("verseTextArabic") ?: "",
            verseTextTranslation = doc.getString("verseTextTranslation") ?: "",
            timestamp = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: 0L,
            isDeleted = doc.getBoolean("isDeleted") ?: false
        )
    }

    private fun dhikrHistoryToMap(entry: DhikrHistoryEntity): Map<String, Any?> {
        return mapOf(
            "syncId" to entry.syncId,
            "dhikrName" to entry.dhikrName,
            "arabicText" to entry.arabicText,
            "countCompleted" to entry.countCompleted,
            "target" to entry.target,
            "timestamp" to entry.timestamp
        )
    }

    private fun docToDhikrHistory(doc: DocumentSnapshot): DhikrHistoryEntity {
        return DhikrHistoryEntity(
            syncId = doc.getString("syncId") ?: doc.id,
            dhikrName = doc.getString("dhikrName") ?: "",
            arabicText = doc.getString("arabicText") ?: "",
            countCompleted = doc.getLong("countCompleted")?.toInt() ?: 0,
            target = doc.getLong("target")?.toInt() ?: 0,
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun buildQadaMap(): Map<String, Any?> {
        val prayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        val map = mutableMapOf<String, Any?>()
        var maxTs = 0L

        for (prayer in prayers) {
            val pName = prayer.name.lowercase()
            val count = repository.getQadaCount(prayer)
            val ever = repository.hasEverHadQada(prayer)
            val ts = repository.getQadaTimestamp(prayer)
            if (ts > maxTs) maxTs = ts

            map["${pName}Count"] = count
            map["${pName}EverAdded"] = ever
            map["${pName}UpdatedAt"] = ts
        }
        map["updatedAt"] = maxTs
        return map
    }

    private fun buildQuranProgressMap(): Map<String, Any?> {
        val lastRead = repository.lastReadPosition.value
        val recentlyRead = repository.recentlyReadList.value
        val goal = repository.getDailyQuranGoal()
        val ts = repository.getQuranProgressTimestamp()

        val recentlyReadJson = JSONArray().apply {
            for (item in recentlyRead) {
                put(JSONObject().apply {
                    put("surahNumber", item.surahNumber)
                    put("surahNameEnglish", item.surahNameEnglish)
                    put("surahNameArabic", item.surahNameArabic)
                    put("verseNumber", item.verseNumber)
                    put("verseIndex", item.verseIndex)
                    put("timestamp", item.timestamp)
                })
            }
        }.toString()

        return mapOf(
            "lastReadSurah" to (lastRead?.surahNumber ?: 0),
            "lastReadSurahEn" to (lastRead?.surahNameEnglish ?: ""),
            "lastReadSurahAr" to (lastRead?.surahNameArabic ?: ""),
            "lastReadVerse" to (lastRead?.verseNumber ?: 0),
            "lastReadVerseIndex" to (lastRead?.verseIndex ?: 0),
            "lastReadTimestamp" to (lastRead?.timestamp ?: 0L),
            "dailyGoal" to goal,
            "recentlyReadJson" to recentlyReadJson,
            "updatedAt" to ts
        )
    }

    private fun parseLastReadFromDoc(doc: DocumentSnapshot): QuranLastRead? {
        val surah = doc.getLong("lastReadSurah")?.toInt() ?: 0
        val verse = doc.getLong("lastReadVerse")?.toInt() ?: 0
        if (surah <= 0 || verse <= 0) return null
        return QuranLastRead(
            surahNumber = surah,
            surahNameEnglish = doc.getString("lastReadSurahEn") ?: "",
            surahNameArabic = doc.getString("lastReadSurahAr") ?: "",
            verseNumber = verse,
            verseIndex = doc.getLong("lastReadVerseIndex")?.toInt() ?: 0,
            timestamp = doc.getLong("lastReadTimestamp") ?: 0L
        )
    }

    private fun parseRecentlyReadFromDoc(doc: DocumentSnapshot): List<QuranLastRead> {
        val jsonStr = doc.getString("recentlyReadJson") ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<QuranLastRead>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuranLastRead(
                        surahNumber = obj.getInt("surahNumber"),
                        surahNameEnglish = obj.getString("surahNameEnglish"),
                        surahNameArabic = obj.getString("surahNameArabic"),
                        verseNumber = obj.getInt("verseNumber"),
                        verseIndex = obj.getInt("verseIndex"),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildTasbeehStateMap(): Map<String, Any?> {
        val customPresets = repository.customDhikrs.value
        val customTargets = repository.customTargets.value
        val ts = repository.getTasbeehStateTimestamp()

        val presetsJson = JSONArray().apply {
            for (p in customPresets) {
                put(JSONObject().apply {
                    put("id", p.id)
                    put("nameEnglish", p.nameEnglish)
                    put("nameArabic", p.nameArabic)
                    put("translation", p.translation)
                    put("defaultTarget", p.defaultTarget)
                })
            }
        }.toString()

        val targetsJson = JSONArray(customTargets).toString()

        val allPresets = repository.DHIKR_PRESETS + customPresets
        val countsMap = mutableMapOf<String, Int>()
        val targetsMap = mutableMapOf<String, Int>()
        for (p in allPresets) {
            countsMap[p.id] = repository.getDhikrCount(p.id)
            targetsMap[p.id] = repository.getDhikrTarget(p.id)
        }

        return mapOf(
            "customPresetsJson" to presetsJson,
            "customTargetsJson" to targetsJson,
            "activeCounts" to countsMap,
            "activeTargets" to targetsMap,
            "updatedAt" to ts
        )
    }

    private fun parseCustomPresets(json: String?): List<DhikrPreset> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<DhikrPreset>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DhikrPreset(
                        id = obj.getString("id"),
                        nameEnglish = obj.getString("nameEnglish"),
                        nameArabic = obj.optString("nameArabic", ""),
                        translation = obj.optString("translation", ""),
                        defaultTarget = obj.optInt("defaultTarget", 33),
                        isCustom = true
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseCustomTargets(json: String?): List<Int> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Int>()
            for (i in 0 until array.length()) {
                list.add(array.getInt(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseStringIntMap(obj: Any?): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        if (obj is Map<*, *>) {
            for ((k, v) in obj) {
                if (k is String && v is Number) {
                    map[k] = v.toInt()
                }
            }
        }
        return map
    }

    private fun buildPreferencesMap(): Map<String, Any?> {
        val smartManager = SmartPrayerNotificationManager(context)
        val nafl = repository.naflPreferences.value
        val home = repository.homeFeaturesPreferences.value

        val naflJson = JSONObject().apply {
            put("tahajjud", nafl.tahajjudEnabled)
            put("ishraq", nafl.ishraqEnabled)
            put("duha", nafl.duhaEnabled)
            put("awwabin", nafl.awwabinEnabled)
            put("order", JSONArray(nafl.naflOrder))
        }.toString()

        val homeJson = JSONObject().apply {
            put("continueReading", home.continueReadingEnabled)
            put("rightNow", home.rightNowEnabled)
            put("tonight", home.tonightEnabled)
            put("nextOpportunity", home.nextOpportunityEnabled)
            put("prayerPrep", home.prayerPrepEnabled)
            put("weeklyOverview", home.weeklyOverviewEnabled)
            put("moments", home.momentsEnabled)
            put("quietMode", home.quietModeEnabled)
            put("prayerJourney", home.prayerJourneyEnabled)
            put("recentlyRead", home.recentlyReadEnabled)
            put("quranLens", home.quranLensEnabled)
            put("nightIsComing", home.nightIsComingEnabled)
        }.toString()

        val prayerToggles = JSONObject().apply {
            for (p in listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")) {
                put(p, smartManager.isPrayerEnabled(p))
            }
        }.toString()

        return mapOf(
            "calcMethod" to repository.calcMethod.value.name,
            "madhab" to repository.madhab.value.name,
            "appearanceMode" to repository.appearanceMode.value.name,
            "timeFormat" to repository.timeFormat.value.name,
            "hijriDateMethod" to repository.hijriDateMethod.value.name,
            "customHijriOffset" to repository.customHijriOffset.value,
            "tasbeehSound" to repository.tasbeehSound.value.id,
            "vibrationEnabled" to repository.vibrationEnabled.value,
            "naflPreferencesJson" to naflJson,
            "homeFeaturesJson" to homeJson,
            "homeFeatureOrder" to home.featureOrder.joinToString(","),
            "notificationsSmartEnabled" to smartManager.isSmartNotificationsEnabled,
            "notificationsPrayerTimeEnabled" to smartManager.isPrayerTimeNotificationsEnabled,
            "notificationsPreReminderMins" to smartManager.preReminderOffset.minutes,
            "notificationsContextualEnabled" to smartManager.isContextualRemindersEnabled,
            "notificationsNaflEnabled" to smartManager.isNaflOpportunitiesEnabled,
            "notificationsPrayerTogglesJson" to prayerToggles,
            "selectedCity" to (repository.selectedCity.value?.cityName ?: ""),
            "bookmarkedDuaIds" to JSONArray((context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE).getStringSet("bookmarked_dua_ids", emptySet()) ?: emptySet()).toList()).toString(),
            "updatedAt" to repository.getPreferencesTimestamp()
        )
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Synced : SyncState()
    object Offline : SyncState()
    data class Error(val message: String) : SyncState()
}
