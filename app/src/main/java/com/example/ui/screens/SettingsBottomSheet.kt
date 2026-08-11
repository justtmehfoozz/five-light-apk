package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.Madhab
import com.example.ui.theme.SerifHeaderFont

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
    selectedAppearanceMode: AppearanceMode,
    onSelectAppearanceMode: (AppearanceMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
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
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Appearance Section
                item {
                    AppearanceSegmentedControl(
                        selectedMode = selectedAppearanceMode,
                        onModeSelected = onSelectAppearanceMode
                    )
                }

                // City Selector Section
                item {
                    Column {
                        Text(
                            text = "City Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        citiesList.forEach { city ->
                            val isSelected = city.cityName == selectedCity.cityName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectCity(city) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = city.fullDisplayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectCity(city) },
                                    modifier = Modifier.testTag("city_${city.cityName.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // Calculation Method Section
                item {
                    Column {
                        Text(
                            text = "Calculation Method",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CalcMethod.entries.forEach { method ->
                            val isSelected = method == selectedCalcMethod
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectCalcMethod(method) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = method.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectCalcMethod(method) },
                                    modifier = Modifier.testTag("calc_method_${method.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // Asr Madhab Section
                item {
                    Column {
                        Text(
                            text = "Asr Calculation (Madhab)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Madhab.entries.forEach { m ->
                            val isSelected = m == selectedMadhab
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectMadhab(m) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = m.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectMadhab(m) },
                                    modifier = Modifier.testTag("madhab_${m.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun AppearanceSegmentedControl(
    selectedMode: AppearanceMode,
    onModeSelected: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = AppearanceMode.entries
    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val containerBg = if (isDark) Color(0x0FFFFFFF) else Color(0x0D000000)
    val indicatorBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFFFFFFF)
    val activeTextColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1815)
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animatedStartIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorStart"
    )

    val animatedEndIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorEnd"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "APPEARANCE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(containerBg)
                .padding(4.dp)
        ) {
            val totalWidth = maxWidth
            val segmentWidth = totalWidth / 3f

            val startVal = minOf(animatedStartIndex, animatedEndIndex)
            val endVal = maxOf(animatedStartIndex, animatedEndIndex)

            val leftPos = (startVal * segmentWidth.value).dp
            val rightPos = ((endVal + 1f) * segmentWidth.value).dp
            val indicatorWidth = (rightPos - leftPos).coerceAtLeast(segmentWidth)

            Box(
                modifier = Modifier
                    .offset(x = leftPos)
                    .width(indicatorWidth)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(indicatorBg)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                modes.forEach { mode ->
                    val isSelected = mode == selectedMode
                    val icon = when (mode) {
                        AppearanceMode.SYSTEM -> Icons.Outlined.Smartphone
                        AppearanceMode.LIGHT -> Icons.Outlined.LightMode
                        AppearanceMode.DARK -> Icons.Outlined.DarkMode
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(CircleShape)
                            .clickable { onModeSelected(mode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.label,
                                tint = if (isSelected) activeTextColor else inactiveTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) activeTextColor else inactiveTextColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Choose how FiveLight looks. System follows your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
