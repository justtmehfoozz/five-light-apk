package com.example.ui.screens

import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.data.model.NaflPreferences
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.Madhab
import com.example.data.model.TasbeehSound
import com.example.data.model.TimeFormat
import com.example.ui.theme.SerifHeaderFont

enum class PreferencesSubScreen {
    MAIN,
    CITY,
    CALC_METHOD,
    MADHAB,
    HIJRI_METHOD,
    TASBEEH_SOUND,
    REMINDERS,
    NAFL_PRAYERS,
    HOME_FEATURES,
    CREDITS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    sheetState: SheetState,
    citiesList: List<CityLocation>,
    selectedCity: CityLocation,
    onSelectCity: (CityLocation) -> Unit,
    selectedCalcMethod: CalcMethod,
    onSelectCalcMethod: (CalcMethod) -> Unit,
    selectedMadhab: Madhab,
    onSelectMadhab: (Madhab) -> Unit,
    selectedTimeFormat: TimeFormat,
    onSelectTimeFormat: (TimeFormat) -> Unit,
    selectedAppearanceMode: AppearanceMode,
    onSelectAppearanceMode: (AppearanceMode) -> Unit,
    selectedTasbeehSound: TasbeehSound,
    onSelectTasbeehSound: (TasbeehSound) -> Unit,
    selectedHijriMethod: com.example.data.model.HijriDateMethod = com.example.data.model.HijriDateMethod.REGIONAL_INDIA,
    onSelectHijriMethod: (com.example.data.model.HijriDateMethod) -> Unit = {},
    customHijriOffset: Int = 0,
    onUpdateCustomHijriOffset: (Int) -> Unit = {},
    vibrationEnabled: Boolean = true,
    onToggleVibration: (Boolean) -> Unit = {},
    naflPreferences: NaflPreferences = NaflPreferences(),
    onUpdateNaflPreference: (tahajjud: Boolean, ishraq: Boolean, duha: Boolean, awwabin: Boolean) -> Unit = { _, _, _, _ -> },
    homeFeaturesPreferences: com.example.data.model.HomeFeaturesPreferences = com.example.data.model.HomeFeaturesPreferences(),
    onUpdateHomeFeaturesPreference: (
        continueReading: Boolean,
        rightNow: Boolean,
        tonight: Boolean,
        nextOpportunity: Boolean,
        prayerPrep: Boolean,
        weeklyOverview: Boolean,
        moments: Boolean,
        quietMode: Boolean,
        prayerJourney: Boolean,
        recentlyRead: Boolean,
        quranLens: Boolean,
        nightIsComing: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onUpdateHomeFeatureOrder: (List<String>) -> Unit = {},
    onResetHomeFeatureOrder: () -> Unit = {},
    onUpdateNaflOrder: (List<String>) -> Unit = {},
    onResetNaflOrder: () -> Unit = {},
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit = {}
) {
    var activeSubScreen by remember { mutableStateOf(PreferencesSubScreen.MAIN) }
    val context = LocalContext.current

    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    val soundIdMap = remember(soundPool, context) {
        val map = mutableMapOf<TasbeehSound, Int>()
        TasbeehSound.entries.forEach { sound ->
            sound.resId?.let { resId ->
                try {
                    val soundId = soundPool.load(context, resId, 1)
                    map[sound] = soundId
                } catch (_: Exception) {}
            }
        }
        map
    }

    DisposableEffect(soundPool) {
        onDispose {
            try {
                soundPool.release()
            } catch (_: Exception) {}
        }
    }

    val isDark = isAppInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val settingsPredictiveState = rememberPredictiveBackState()
        RegisterPredictiveBackHandler(
            enabled = activeSubScreen != PreferencesSubScreen.MAIN,
            backState = settingsPredictiveState,
            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = {
                    if (targetState != PreferencesSubScreen.MAIN) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "preferencesDrillDown"
            ) { subScreen ->
                when (subScreen) {
                    PreferencesSubScreen.MAIN -> {
                        MainPreferencesView(
                            selectedAppearanceMode = selectedAppearanceMode,
                            onSelectAppearanceMode = onSelectAppearanceMode,
                            selectedCity = selectedCity,
                            selectedCalcMethod = selectedCalcMethod,
                            selectedMadhab = selectedMadhab,
                            selectedHijriMethod = selectedHijriMethod,
                            selectedTimeFormat = selectedTimeFormat,
                            onSelectTimeFormat = onSelectTimeFormat,
                            selectedTasbeehSound = selectedTasbeehSound,
                            vibrationEnabled = vibrationEnabled,
                            onToggleVibration = onToggleVibration,
                            naflPreferences = naflPreferences,
                            onNavigateTo = { activeSubScreen = it },
                            onDismiss = onDismiss
                        )
                    }

                    PreferencesSubScreen.CITY -> {
                        var searchQuery by remember { mutableStateOf("") }
                        val filteredCities = remember(citiesList, searchQuery) {
                            if (searchQuery.isBlank()) {
                                citiesList
                            } else {
                                val query = searchQuery.trim()
                                citiesList.filter {
                                    it.cityName.contains(query, ignoreCase = true) ||
                                    it.countryName.contains(query, ignoreCase = true)
                                }
                            }
                        }

                        SubScreenLayout(
                            title = "City Location",
                            onBack = {
                                searchQuery = ""
                                activeSubScreen = PreferencesSubScreen.MAIN
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("city_search_input"),
                                    placeholder = {
                                        Text(
                                            text = "Search city or country...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Close,
                                                    contentDescription = "Clear search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.semanticSurfaceElevated,
                                        unfocusedContainerColor = Color.semanticSurfaceElevated,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                if (filteredCities.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No cities found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredCities, key = { it.cityName }) { city ->
                                            val isSelected = city.cityName == selectedCity.cityName
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSelectCity(city)
                                                        searchQuery = ""
                                                        activeSubScreen = PreferencesSubScreen.MAIN
                                                    }
                                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = city.cityName,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = city.countryName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                RadioButton(
                                                    selected = isSelected,
                                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                        selectedColor = MaterialTheme.colorScheme.primary,
                                                        unselectedColor = Color.semanticStrongBorder
                                                    ),
                                                    onClick = {
                                                        onSelectCity(city)
                                                        searchQuery = ""
                                                        activeSubScreen = PreferencesSubScreen.MAIN
                                                    },
                                                    modifier = Modifier.testTag("city_${city.cityName.lowercase()}")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PreferencesSubScreen.CALC_METHOD -> {
                        SubScreenLayout(
                            title = "Calculation Method",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(CalcMethod.entries, key = { it.name }) { method ->
                                    val isSelected = method == selectedCalcMethod
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSelectCalcMethod(method)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = method.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        RadioButton(
                                            selected = isSelected,
                                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = Color.semanticStrongBorder
                                            ),
                                            onClick = {
                                                onSelectCalcMethod(method)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            },
                                            modifier = Modifier.testTag("calc_method_${method.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PreferencesSubScreen.MADHAB -> {
                        SubScreenLayout(
                            title = "Asr Calculation (Madhab)",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(Madhab.entries, key = { it.name }) { m ->
                                    val isSelected = m == selectedMadhab
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSelectMadhab(m)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = m.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        RadioButton(
                                            selected = isSelected,
                                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = Color.semanticStrongBorder
                                            ),
                                            onClick = {
                                                onSelectMadhab(m)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            },
                                            modifier = Modifier.testTag("madhab_${m.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PreferencesSubScreen.HIJRI_METHOD -> {
                        SubScreenLayout(
                            title = "Hijri Date Convention",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Text(
                                        text = "Select local or regional moon-sighting convention:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(com.example.data.model.HijriDateMethod.entries, key = { it.name }) { method ->
                                    val isSelected = method == selectedHijriMethod
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectHijriMethod(method)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            }
                                            .testTag("hijri_method_${method.name.lowercase()}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.semanticSurfaceElevated
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.semanticBorder
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = method.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = method.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PreferencesSubScreen.TASBEEH_SOUND -> {
                        SubScreenLayout(
                            title = "Tasbeeh Sound",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(TasbeehSound.entries, key = { it.name }) { sound ->
                                    val isSelected = sound == selectedTasbeehSound
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSelectTasbeehSound(sound)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = Color.semanticStrongBorder
                                            ),
                                            onClick = {
                                                onSelectTasbeehSound(sound)
                                                activeSubScreen = PreferencesSubScreen.MAIN
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sound.displayName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (sound.resId != null) {
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val soundId = soundIdMap[sound]
                                                        if (soundId != null && soundId > 0) {
                                                            soundPool.play(soundId, 0.9f, 0.9f, 1, 0, 1.0f)
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.PlayCircle,
                                                    contentDescription = "Preview ${sound.displayName}",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PreferencesSubScreen.REMINDERS -> {
                        SmartPrayerNotificationsSubScreen(
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN },
                            onSettingsChanged = onSettingsChanged
                        )
                    }

                    PreferencesSubScreen.NAFL_PRAYERS -> {
                        val naflItems = naflPreferences.naflOrder.map { naflId ->
                            when (naflId.uppercase()) {
                                "TAHAJJUD" -> com.example.ui.components.ReorderableItemData(
                                    id = "TAHAJJUD",
                                    title = "Tahajjud",
                                    subtitle = "Night prayer (Last third of the night)",
                                    isEnabled = naflPreferences.tahajjudEnabled
                                )
                                "ISHRAQ" -> com.example.ui.components.ReorderableItemData(
                                    id = "ISHRAQ",
                                    title = "Ishraq",
                                    subtitle = "Post-sunrise solar window",
                                    isEnabled = naflPreferences.ishraqEnabled
                                )
                                "DUHA" -> com.example.ui.components.ReorderableItemData(
                                    id = "DUHA",
                                    title = "Duha / Chasht",
                                    subtitle = "Forenoon voluntary prayer",
                                    isEnabled = naflPreferences.duhaEnabled
                                )
                                "AWWABIN" -> com.example.ui.components.ReorderableItemData(
                                    id = "AWWABIN",
                                    title = "Awwabin",
                                    subtitle = "Post-Maghrib voluntary window",
                                    isEnabled = naflPreferences.awwabinEnabled
                                )
                                else -> com.example.ui.components.ReorderableItemData(
                                    id = naflId,
                                    title = naflId,
                                    subtitle = "",
                                    isEnabled = false
                                )
                            }
                        }

                        SubScreenLayout(
                            title = "Nafl Prayers",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select voluntary prayers to display on your Home screen.",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )

                                    TextButton(onClick = { onResetNaflOrder() }) {
                                        Text(
                                            text = "Reset order",
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                com.example.ui.components.ReorderableSettingsList(
                                    items = naflItems,
                                    onToggleItem = { id, isChecked ->
                                        val tahajjud = if (id == "TAHAJJUD") isChecked else naflPreferences.tahajjudEnabled
                                        val ishraq = if (id == "ISHRAQ") isChecked else naflPreferences.ishraqEnabled
                                        val duha = if (id == "DUHA") isChecked else naflPreferences.duhaEnabled
                                        val awwabin = if (id == "AWWABIN") isChecked else naflPreferences.awwabinEnabled
                                        onUpdateNaflPreference(tahajjud, ishraq, duha, awwabin)
                                    },
                                    onReorderComplete = { newOrder ->
                                        onUpdateNaflOrder(newOrder)
                                    }
                                )
                            }
                        }
                    }

                    PreferencesSubScreen.HOME_FEATURES -> {
                        val homeFeatureItems = homeFeaturesPreferences.featureOrder.map { featId ->
                            when (featId.uppercase()) {
                                "RIGHT_NOW" -> com.example.ui.components.ReorderableItemData(
                                    id = "RIGHT_NOW",
                                    title = "Right Now",
                                    subtitle = "Current worship focus",
                                    isEnabled = homeFeaturesPreferences.rightNowEnabled
                                )
                                "NEXT_OPPORTUNITY" -> com.example.ui.components.ReorderableItemData(
                                    id = "NEXT_OPPORTUNITY",
                                    title = "Next Worship Opportunity",
                                    subtitle = "Upcoming voluntary worship windows",
                                    isEnabled = homeFeaturesPreferences.nextOpportunityEnabled
                                )
                                "PRAYER_PREP" -> com.example.ui.components.ReorderableItemData(
                                    id = "PRAYER_PREP",
                                    title = "Prayer Preparation",
                                    subtitle = "Preparation checklist before prayer",
                                    isEnabled = homeFeaturesPreferences.prayerPrepEnabled
                                )
                                "NAFL_PRAYERS" -> com.example.ui.components.ReorderableItemData(
                                    id = "NAFL_PRAYERS",
                                    title = "Nafl Prayers",
                                    subtitle = "Voluntary prayers section",
                                    isEnabled = naflPreferences.isAnyEnabled
                                )
                                "TONIGHT" -> com.example.ui.components.ReorderableItemData(
                                    id = "TONIGHT",
                                    title = "Tonight / Night is Coming",
                                    subtitle = "Night prayer and Tahajjud window",
                                    isEnabled = homeFeaturesPreferences.tonightEnabled
                                )
                                "CONTINUE_READING" -> com.example.ui.components.ReorderableItemData(
                                    id = "CONTINUE_READING",
                                    title = "Continue Reading",
                                    subtitle = "Resume Quran reading position",
                                    isEnabled = homeFeaturesPreferences.continueReadingEnabled
                                )
                                "RECENTLY_READ" -> com.example.ui.components.ReorderableItemData(
                                    id = "RECENTLY_READ",
                                    title = "Recently Read History",
                                    subtitle = "Last 7 read Quran locations",
                                    isEnabled = homeFeaturesPreferences.recentlyReadEnabled
                                )
                                "MOMENTS" -> com.example.ui.components.ReorderableItemData(
                                    id = "MOMENTS",
                                    title = "FiveLight Moments",
                                    subtitle = "Friday and contextual reflection moments",
                                    isEnabled = homeFeaturesPreferences.momentsEnabled
                                )
                                "WEEKLY_OVERVIEW" -> com.example.ui.components.ReorderableItemData(
                                    id = "WEEKLY_OVERVIEW",
                                    title = "Weekly Overview",
                                    subtitle = "7-day prayer tracking grid",
                                    isEnabled = homeFeaturesPreferences.weeklyOverviewEnabled
                                )
                                else -> com.example.ui.components.ReorderableItemData(
                                    id = featId,
                                    title = featId,
                                    subtitle = "",
                                    isEnabled = true
                                )
                            }
                        }

                        SubScreenLayout(
                            title = "Home Features & Density",
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Toggle and drag handle to reorder Home screen sections.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )

                                    TextButton(onClick = { onResetHomeFeatureOrder() }) {
                                        Text(
                                            text = "Reset order",
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                com.example.ui.components.ReorderableSettingsList(
                                    items = homeFeatureItems,
                                    onToggleItem = { id, isChecked ->
                                        when (id.uppercase()) {
                                            "CONTINUE_READING" -> onUpdateHomeFeaturesPreference(
                                                isChecked, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "RIGHT_NOW" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, isChecked, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "TONIGHT" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, isChecked,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "NEXT_OPPORTUNITY" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                isChecked, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "PRAYER_PREP" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, isChecked,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "NAFL_PRAYERS" -> {
                                                onUpdateNaflPreference(isChecked, isChecked, isChecked, isChecked)
                                            }
                                            "WEEKLY_OVERVIEW" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                isChecked, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "MOMENTS" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, isChecked,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                homeFeaturesPreferences.recentlyReadEnabled, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                            "RECENTLY_READ" -> onUpdateHomeFeaturesPreference(
                                                homeFeaturesPreferences.continueReadingEnabled, homeFeaturesPreferences.rightNowEnabled, homeFeaturesPreferences.tonightEnabled,
                                                homeFeaturesPreferences.nextOpportunityEnabled, homeFeaturesPreferences.prayerPrepEnabled,
                                                homeFeaturesPreferences.weeklyOverviewEnabled, homeFeaturesPreferences.momentsEnabled,
                                                homeFeaturesPreferences.quietModeEnabled, homeFeaturesPreferences.prayerJourneyEnabled,
                                                isChecked, homeFeaturesPreferences.quranLensEnabled,
                                                homeFeaturesPreferences.nightIsComingEnabled
                                            )
                                        }
                                    },
                                     onReorderComplete = { newOrder ->
                                        onUpdateHomeFeatureOrder(newOrder)
                                    }
                                )
                            }
                        }
                    }

                    PreferencesSubScreen.CREDITS -> {
                        CreditsSubScreen(
                            onBack = { activeSubScreen = PreferencesSubScreen.MAIN }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainPreferencesView(
    selectedAppearanceMode: AppearanceMode,
    onSelectAppearanceMode: (AppearanceMode) -> Unit,
    selectedCity: CityLocation,
    selectedCalcMethod: CalcMethod,
    selectedMadhab: Madhab,
    selectedHijriMethod: com.example.data.model.HijriDateMethod = com.example.data.model.HijriDateMethod.REGIONAL_INDIA,
    selectedTimeFormat: TimeFormat,
    onSelectTimeFormat: (TimeFormat) -> Unit,
    selectedTasbeehSound: TasbeehSound,
    vibrationEnabled: Boolean = true,
    onToggleVibration: (Boolean) -> Unit = {},
    naflPreferences: NaflPreferences = NaflPreferences(),
    onNavigateTo: (PreferencesSubScreen) -> Unit,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.headlineLarge.copy(fontFamily = SerifHeaderFont),
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_settings_btn")
            ) {
                Text(
                    "Done",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // GENERAL Group
            item {
                MenuGroupCard(title = "GENERAL") {
                    Box(modifier = Modifier.padding(16.dp)) {
                        AppearanceSegmentedControl(
                            selectedMode = selectedAppearanceMode,
                            onModeSelected = onSelectAppearanceMode,
                            showTitle = false
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time Format",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedTimeFormat.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val timeFormatItems = remember {
                            listOf(
                                PillItem(TimeFormat.TWELVE_HOUR, "12h"),
                                PillItem(TimeFormat.TWENTY_FOUR_HOUR, "24h")
                            )
                        }

                        SpringPillSelector(
                            items = timeFormatItems,
                            selectedItem = selectedTimeFormat,
                            onItemSelected = onSelectTimeFormat,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "City Location",
                        value = selectedCity.fullDisplayName,
                        onClick = { onNavigateTo(PreferencesSubScreen.CITY) },
                        testTag = "pref_row_city"
                    )
                }
            }

            // PRAYER Group
            item {
                val context = LocalContext.current
                val reminderManager = remember(context) { com.example.data.reminder.SmartPrayerNotificationManager(context) }
                val summaryText = reminderManager.getSummaryText()

                MenuGroupCard(title = "PRAYER") {
                    GroupedMenuRow(
                        label = "Prayer Notifications",
                        value = summaryText,
                        onClick = { onNavigateTo(PreferencesSubScreen.REMINDERS) },
                        testTag = "pref_row_reminders"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "Calculation Method",
                        value = selectedCalcMethod.displayName,
                        onClick = { onNavigateTo(PreferencesSubScreen.CALC_METHOD) },
                        testTag = "pref_row_calc_method"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "Asr Calculation (Madhab)",
                        value = selectedMadhab.displayName,
                        onClick = { onNavigateTo(PreferencesSubScreen.MADHAB) },
                        testTag = "pref_row_madhab"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "Hijri Date Convention",
                        value = selectedHijriMethod.displayName,
                        onClick = { onNavigateTo(PreferencesSubScreen.HIJRI_METHOD) },
                        testTag = "pref_row_hijri_method"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "Nafl Prayers",
                        value = if (naflPreferences.isAnyEnabled) "${naflPreferences.enabledCount} Enabled" else "Off",
                        onClick = { onNavigateTo(PreferencesSubScreen.NAFL_PRAYERS) },
                        testTag = "pref_row_nafl"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GroupedMenuRow(
                        label = "Home Cards & Density",
                        value = "Customize",
                        onClick = { onNavigateTo(PreferencesSubScreen.HOME_FEATURES) },
                        testTag = "pref_row_home_features"
                    )
                }
            }

            // TASBEEH Group
            item {
                MenuGroupCard(title = "TASBEEH & HAPTICS") {
                    GroupedMenuRow(
                        label = "Tap Sound",
                        value = selectedTasbeehSound.displayName,
                        onClick = { onNavigateTo(PreferencesSubScreen.TASBEEH_SOUND) },
                        testTag = "pref_row_tasbeeh_sound"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vibration Feedback",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (vibrationEnabled) "Haptics enabled" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = onToggleVibration,
                            modifier = Modifier.testTag("pref_vibration_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.semanticAccentForeground,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.semanticSecondaryText,
                                uncheckedTrackColor = Color.semanticControl,
                                uncheckedBorderColor = Color.semanticBorder
                            )
                        )
                    }
                }
            }

            // ABOUT Group
            item {
                MenuGroupCard(title = "ABOUT") {
                    GroupedMenuRow(
                        label = "Credits",
                        value = "Meet the people behind FiveLight",
                        onClick = { onNavigateTo(PreferencesSubScreen.CREDITS) },
                        testTag = "pref_row_credits"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CreditsSubScreen(
    onBack: () -> Unit
) {
    SubScreenLayout(
        title = "Credits",
        onBack = onBack
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 16.dp)
                .testTag("credits_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FiveLight",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = SerifHeaderFont,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Built with care for the Ummah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "DEVELOPED BY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Mehfooz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version ${com.example.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MenuGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.semanticSurfaceElevated),
            border = BorderStroke(1.dp, Color.semanticBorder)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun GroupedMenuRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SubScreenLayout(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifHeaderFont),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

data class PillItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null
)

private val SPRING_STRETCH_START = spring<Float>(
    dampingRatio = 0.68f,
    stiffness = 260f
)

private val SPRING_STRETCH_END = spring<Float>(
    dampingRatio = 0.52f,
    stiffness = 260f
)

@Composable
fun <T> SpringPillSelector(
    items: List<PillItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val durationScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            durationScale == 0f
        } catch (e: Exception) {
            false
        }
    }

    val selectedIndex = items.indexOfFirst { it.value == selectedItem }.coerceAtLeast(0)

    val containerBg = Color.semanticControl
    val activePillBg = Color.semanticPrimaryAccent
    val activeContentColor = Color.semanticAccentForeground
    val inactiveContentColor = Color.semanticSecondaryText

    val targetIdx = selectedIndex.toFloat()

    val animatedStartIndex by animateFloatAsState(
        targetValue = targetIdx,
        animationSpec = if (isReducedMotion) snap() else SPRING_STRETCH_START,
        label = "pillStart"
    )

    val animatedEndIndex by animateFloatAsState(
        targetValue = targetIdx,
        animationSpec = if (isReducedMotion) snap() else SPRING_STRETCH_END,
        label = "pillEnd"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .background(containerBg)
            .border(
                width = 1.dp,
                color = Color.semanticBorder,
                shape = CircleShape
            )
            .padding(3.dp)
    ) {
        val totalWidth = maxWidth
        val count = items.size.coerceAtLeast(1)
        val segmentWidth = totalWidth / count

        val startVal = minOf(animatedStartIndex, animatedEndIndex)
        val endVal = maxOf(animatedStartIndex, animatedEndIndex)

        val leftPos = (startVal * segmentWidth.value).dp
        val rightPos = ((endVal + 1f) * segmentWidth.value).dp
        val indicatorWidth = (rightPos - leftPos).coerceAtLeast(segmentWidth)

        Box(
            modifier = Modifier
                .offset(x = leftPos)
                .width(indicatorWidth)
                .height(36.dp)
                .clip(CircleShape)
                .background(activePillBg)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.value == selectedItem
                val contentColor = if (isSelected) activeContentColor else inactiveContentColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable { onItemSelected(item.value) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceSegmentedControl(
    selectedMode: AppearanceMode,
    onModeSelected: (AppearanceMode) -> Unit,
    showTitle: Boolean = true,
    modifier: Modifier = Modifier
) {
    val modes = remember {
        listOf(
            PillItem(AppearanceMode.SYSTEM, "System", Icons.Outlined.Smartphone),
            PillItem(AppearanceMode.LIGHT, "Light", Icons.Outlined.LightMode),
            PillItem(AppearanceMode.DARK, "Dark", Icons.Outlined.DarkMode)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showTitle) {
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        SpringPillSelector(
            items = modes,
            selectedItem = selectedMode,
            onItemSelected = onModeSelected
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Choose how FiveLight looks. System follows your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NaflToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontFamily = SpaceGrotesk,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.semanticAccentForeground,
                checkedTrackColor = Color.semanticPrimaryAccent,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.semanticSecondaryText,
                uncheckedTrackColor = Color.semanticControl,
                uncheckedBorderColor = Color.semanticBorder
            )
        )
    }
}
