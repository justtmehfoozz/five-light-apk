package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityLocation
import com.example.data.model.Mosque
import com.example.data.model.PrayerItem
import com.example.data.repository.MosqueRepository
import com.example.data.util.LocationHelper
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// FiveLight Custom Minimalist Map Styles (Dark & Light)
// ---------------------------------------------------------------------------
private const val DARK_MAP_STYLE = """
[
  {
    "elementType": "geometry",
    "stylers": [{"color": "#141518"}]
  },
  {
    "elementType": "labels.icon",
    "stylers": [{"visibility": "off"}]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{"color": "#727782"}]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{"color": "#141518"}]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{"color": "#2a2c33"}]
  },
  {
    "featureType": "poi",
    "stylers": [{"visibility": "off"}]
  },
  {
    "featureType": "road",
    "elementType": "geometry.fill",
    "stylers": [{"color": "#202227"}]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [{"color": "#8c919a"}]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{"color": "#2c2f37"}]
  },
  {
    "featureType": "transit",
    "stylers": [{"visibility": "off"}]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{"color": "#0c0d10"}]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{"color": "#4a505b"}]
  }
]
"""

private const val LIGHT_MAP_STYLE = """
[
  {
    "elementType": "geometry",
    "stylers": [{"color": "#f5f4ef"}]
  },
  {
    "elementType": "labels.icon",
    "stylers": [{"visibility": "off"}]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{"color": "#63615a"}]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{"color": "#f5f4ef"}]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{"color": "#dcdacf"}]
  },
  {
    "featureType": "poi",
    "stylers": [{"visibility": "off"}]
  },
  {
    "featureType": "road",
    "elementType": "geometry.fill",
    "stylers": [{"color": "#ffffff"}]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [{"color": "#e6e4dc"}]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.fill",
    "stylers": [{"color": "#eceae2"}]
  },
  {
    "featureType": "transit",
    "stylers": [{"visibility": "off"}]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{"color": "#dbe3ed"}]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{"color": "#768597"}]
  }
]
"""

private enum class SheetState {
    PEEK,
    HALF,
    EXPANDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueFinderScreen(
    onBack: () -> Unit,
    selectedCity: CityLocation? = null,
    nextPrayer: PrayerItem? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val repository = remember { MosqueRepository(context) }

    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
    }

    var hasLocationPermission by remember {
        mutableStateOf(LocationHelper.hasLocationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
    }

    // Default coordinates based on GPS or User's Selected City
    val userLat = remember(hasLocationPermission, selectedCity) {
        if (hasLocationPermission) {
            val loc = LocationHelper.getLastKnownLocation(context)
            loc?.latitude ?: selectedCity?.latitude ?: 21.4225
        } else {
            selectedCity?.latitude ?: 21.4225
        }
    }

    val userLng = remember(hasLocationPermission, selectedCity) {
        if (hasLocationPermission) {
            val loc = LocationHelper.getLastKnownLocation(context)
            loc?.longitude ?: selectedCity?.longitude ?: 39.8262
        } else {
            selectedCity?.longitude ?: 39.8262
        }
    }

    val userLatLng = remember(userLat, userLng) { LatLng(userLat, userLng) }

    // Map & Search state
    var selectedRadiusMeters by remember { mutableStateOf(5000.0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Data State
    var mosques by remember { mutableStateOf<List<Mosque>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMosque by remember { mutableStateOf<Mosque?>(null) }
    var searchCenter by remember { mutableStateOf(userLatLng) }
    var showSearchThisArea by remember { mutableStateOf(false) }

    // Map Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 14f)
    }

    // Detect when user pans camera far away (>1.2km) to show "Search this area"
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            val dist = FloatArray(1)
            Location.distanceBetween(
                searchCenter.latitude,
                searchCenter.longitude,
                target.latitude,
                target.longitude,
                dist
            )
            showSearchThisArea = dist[0] > 1200f
        }
    }

    // Fetch Mosques function
    fun fetchMosques(center: LatLng, radius: Double, query: String) {
        coroutineScope.launch {
            isLoading = true
            searchCenter = center
            showSearchThisArea = false
            val results = repository.getNearbyMosques(
                centerLat = center.latitude,
                centerLng = center.longitude,
                radiusMeters = radius,
                searchQuery = query
            )
            mosques = results
            isLoading = false
            if (selectedMosque == null || !results.contains(selectedMosque)) {
                selectedMosque = results.firstOrNull()
            }
        }
    }

    // Debounced query execution when searchQuery changes
    LaunchedEffect(searchQuery, selectedRadiusMeters) {
        if (searchQuery.isNotEmpty()) {
            delay(350)
        }
        fetchMosques(searchCenter, selectedRadiusMeters, searchQuery)
    }

    // Initial load
    LaunchedEffect(userLat, userLng) {
        fetchMosques(LatLng(userLat, userLng), selectedRadiusMeters, searchQuery)
    }

    // Markers state and lifecycle-safe initialization
    var isMapReady by remember { mutableStateOf(false) }
    var defaultMarkerBitmap by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var selectedMarkerBitmap by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Safe background MapsInitializer
    LaunchedEffect(Unit) {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                context.applicationContext,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) {
                isMapReady = true
            }
        } catch (e: Exception) {
            android.util.Log.w("MosqueFinderScreen", "MapsInitializer: ${e.message}")
        }
    }

    // Only generate custom BitmapDescriptor when Maps SDK is confirmed ready
    LaunchedEffect(isMapReady, isDark) {
        if (isMapReady) {
            defaultMarkerBitmap = createMosqueMarkerBitmap(isDark = isDark, isSelected = false)
            selectedMarkerBitmap = createMosqueMarkerBitmap(isDark = isDark, isSelected = true)
        }
    }

    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("mosque_finder_screen")
    ) {
        val screenHeight = maxHeight
        val screenHeightPx = with(density) { screenHeight.toPx() }

        val peekHeightDp = 112.dp
        val peekHeightPx = with(density) { peekHeightDp.toPx() }
        val halfHeightDp = screenHeight * 0.46f
        val halfHeightPx = with(density) { halfHeightDp.toPx() }
        val expandedHeightDp = screenHeight * 0.76f
        val expandedHeightPx = with(density) { expandedHeightDp.toPx() }

        // Sheet animated height
        val sheetHeight = remember { Animatable(peekHeightPx) }
        var currentSheetState by remember { mutableStateOf(SheetState.PEEK) }

        fun animateSheetTo(targetState: SheetState) {
            currentSheetState = targetState
            val targetPx = when (targetState) {
                SheetState.PEEK -> peekHeightPx
                SheetState.HALF -> halfHeightPx
                SheetState.EXPANDED -> expandedHeightPx
            }
            coroutineScope.launch {
                sheetHeight.animateTo(
                    targetValue = targetPx,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
                )
            }
        }

        fun centerOnMosque(mosque: Mosque, expandSheet: Boolean = true) {
            selectedMosque = mosque
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(mosque.latitude, mosque.longitude),
                        15.5f
                    ),
                    durationMs = 500
                )
            }
            if (expandSheet && currentSheetState == SheetState.PEEK) {
                animateSheetTo(SheetState.HALF)
            }
        }

        fun centerOnUser() {
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(userLatLng, 14.5f),
                    durationMs = 500
                )
            }
        }

        // =======================================================================
        // 1. FULL-SCREEN GOOGLE MAP (THE HERO)
        // =======================================================================
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isMapReady = true },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions(if (isDark) DARK_MAP_STYLE else LIGHT_MAP_STYLE)
            )
        ) {
            mosques.forEach { mosque ->
                val isSelected = selectedMosque?.id == mosque.id
                Marker(
                    state = rememberMarkerState(
                        key = "${mosque.id}_$isSelected",
                        position = LatLng(mosque.latitude, mosque.longitude)
                    ),
                    title = mosque.name,
                    snippet = mosque.formattedDistance,
                    icon = if (isSelected) selectedMarkerBitmap else defaultMarkerBitmap,
                    zIndex = if (isSelected) 2f else 1f,
                    onClick = {
                        centerOnMosque(mosque, expandSheet = true)
                        true
                    }
                )
            }
        }

        // =======================================================================
        // 2. FLOATING TOP CONTROLS (HEADER & SEARCH)
        // =======================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xEE141518) else Color(0xEEF8F6F1),
                border = BorderStroke(1.dp, Color.semanticBorder.copy(alpha = 0.8f)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSearchActive) {
                    // Active Search Input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search mosque name or road...",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mosque_search_input")
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Standard Floating Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("mosque_finder_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Mosques Near You",
                                fontFamily = SerifHeaderFont,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hasLocationPermission) "Locating near you" else (selectedCity?.cityName ?: "Showing nearby"),
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Search Trigger
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("open_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Floating "Search this area" Pill
            AnimatedVisibility(
                visible = showSearchThisArea,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xF0202227) else Color(0xF8FFFFFF),
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .clickable {
                            val target = cameraPositionState.position.target
                            fetchMosques(target, selectedRadiusMeters, searchQuery)
                        }
                        .testTag("search_this_area_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Search this area",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                }
            }
        }

        // =======================================================================
        // 3. FLOATING MAP CONTROLS (Right Side, Above Bottom Sheet)
        // =======================================================================
        val currentSheetHeightDp = with(density) { sheetHeight.value.toDp() }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = currentSheetHeightDp + 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Radius Filter Quick Pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xEE16171B) else Color(0xEEF8F6F1),
                border = BorderStroke(1.dp, Color.semanticBorder),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .clickable { showFilterSheet = true }
                    .testTag("open_filter_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(selectedRadiusMeters / 1000).toInt()} km",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            // Recenter My Location FAB
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0xEE16171B) else Color(0xEEF8F6F1),
                border = BorderStroke(1.dp, Color.semanticBorder),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        if (!hasLocationPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            centerOnUser()
                        }
                    }
                    .testTag("recenter_location_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "My Location",
                        tint = textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // =======================================================================
        // 4. OVERLAYING DRAGGABLE MOSQUE BOTTOM SHEET
        // =======================================================================
        val sheetColor = if (isDark) Color(0xF2121316) else Color(0xF4F9F8F4)

        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = sheetColor,
            border = BorderStroke(1.dp, Color.semanticBorder.copy(alpha = 0.7f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(currentSheetHeightDp)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newHeight = (sheetHeight.value - dragAmount)
                                    .coerceIn(peekHeightPx, expandedHeightPx)
                                sheetHeight.snapTo(newHeight)
                            }
                        },
                        onDragEnd = {
                            val current = sheetHeight.value
                            val target = when {
                                current < (peekHeightPx + halfHeightPx) / 2 -> SheetState.PEEK
                                current < (halfHeightPx + expandedHeightPx) / 2 -> SheetState.HALF
                                else -> SheetState.EXPANDED
                            }
                            animateSheetTo(target)
                        }
                    )
                }
                .testTag("mosque_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.semanticBorder)
                    )
                }

                // Compact Sheet Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentSheetState == SheetState.PEEK) {
                                animateSheetTo(SheetState.HALF)
                            } else {
                                animateSheetTo(SheetState.PEEK)
                            }
                        }
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mosques Near You",
                            fontFamily = SerifHeaderFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        val subtitleText = buildString {
                            append("${mosques.size} nearby")
                            if (nextPrayer != null) {
                                append(" • Next: ${nextPrayer.name.displayName} ${nextPrayer.timeFormatted}")
                            }
                        }

                        Text(
                            text = subtitleText,
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Quick radius badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.semanticSurfaceElevated,
                        border = BorderStroke(1.dp, Color.semanticBorder),
                        modifier = Modifier
                            .clickable { showFilterSheet = true }
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${(selectedRadiusMeters / 1000).toInt()} km",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.semanticBorder.copy(alpha = 0.5f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                // Mosque List Rows (Continuous editorial layout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = textPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (mosques.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No mosques found",
                                fontFamily = SerifHeaderFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try increasing the radius or searching a different area",
                                fontFamily = SpaceGrotesk,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mosques, key = { it.id }) { mosque ->
                                MosqueEditorialRow(
                                    mosque = mosque,
                                    isSelected = selectedMosque?.id == mosque.id,
                                    onClick = { centerOnMosque(mosque, expandSheet = false) },
                                    onDirections = { openDirections(context, mosque) },
                                    isDark = isDark
                                )
                                HorizontalDivider(
                                    color = Color.semanticBorder.copy(alpha = 0.35f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =======================================================================
        // 5. RADIUS / FILTER MODAL BOTTOM SHEET
        // =======================================================================
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = if (isDark) Color(0xFF16171A) else Color(0xFFFAF9F5),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.semanticBorder)
                        )
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = "Search Radius",
                        fontFamily = SerifHeaderFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Choose search distance from current area",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val radiusOptions = listOf(
                        1000.0 to "1 km • Walking distance",
                        3000.0 to "3 km • Local neighborhood",
                        5000.0 to "5 km • City district",
                        10000.0 to "10 km • Wider area",
                        25000.0 to "25 km • Extended region"
                    )

                    radiusOptions.forEach { (radius, label) ->
                        val isSelected = selectedRadiusMeters == radius
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedRadiusMeters = radius
                                    showFilterSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) textPrimary else textSecondary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Mosque Compact Editorial Row (Restrained, Continuous Surface)
// ---------------------------------------------------------------------------
@Composable
private fun MosqueEditorialRow(
    mosque: Mosque,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDirections: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color.semanticPrimaryText
    val textSecondary = Color.semanticSecondaryText

    val rowBg = if (isSelected) {
        if (isDark) Color(0xFF1E2025) else Color(0xFFEBE8DF)
    } else {
        Color.Transparent
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("mosque_row_${mosque.id}")
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mosque.name,
                fontFamily = SerifHeaderFont,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = mosque.address,
                fontFamily = SpaceGrotesk,
                fontSize = 12.sp,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (mosque.formattedDistance.isNotBlank()) {
                    Text(
                        text = mosque.formattedDistance,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }

                if (mosque.rating != null) {
                    Text(
                        text = "• ★ ${String.format(java.util.Locale.US, "%.1f", mosque.rating)}",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                if (mosque.isOpenNow == true) {
                    Text(
                        text = "• Open Now",
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Directions Action Button
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.semanticSurfaceElevated,
            border = BorderStroke(1.dp, Color.semanticBorder),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onDirections)
                .testTag("directions_button_${mosque.id}")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Directions",
                    tint = textPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Directions",
                    fontFamily = SpaceGrotesk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FiveLight Minimalist Custom Marker Icon Generator
// ---------------------------------------------------------------------------
private fun createMosqueMarkerBitmap(isDark: Boolean, isSelected: Boolean): BitmapDescriptor? {
    return try {
        val sizePx = if (isSelected) 64 else 52
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (isDark) {
            if (isSelected) 0xFFFFFFFF.toInt() else 0xFF23252A.toInt()
        } else {
            if (isSelected) 0xFF1C1D21.toInt() else 0xFFFFFFFF.toInt()
        }

        val strokeColor = if (isDark) {
            if (isSelected) 0xFFFFFFFF.toInt() else 0xFF4A4E58.toInt()
        } else {
            if (isSelected) 0xFF1C1D21.toInt() else 0xFFCCCCCC.toInt()
        }

        val dotColor = if (isDark) {
            if (isSelected) 0xFF1C1D21.toInt() else 0xFFFFFFFF.toInt()
        } else {
            if (isSelected) 0xFFFFFFFF.toInt() else 0xFF1C1D21.toInt()
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = bgColor
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = strokeColor
        }

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = dotColor
        }

        val radius = (sizePx / 2f) - 4f
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, bgPaint)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, strokePaint)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, if (isSelected) 9f else 6f, dotPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        android.util.Log.w("MosqueFinderScreen", "BitmapDescriptorFactory deferred: ${e.message}")
        null
    }
}

// ---------------------------------------------------------------------------
// Helper Intents
// ---------------------------------------------------------------------------
private fun openDirections(context: Context, mosque: Mosque) {
    try {
        val uri = Uri.parse("geo:${mosque.latitude},${mosque.longitude}?q=${Uri.encode("${mosque.latitude},${mosque.longitude}(${mosque.name})")}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${mosque.latitude},${mosque.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${mosque.latitude},${mosque.longitude}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
