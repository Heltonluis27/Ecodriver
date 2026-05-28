package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

import androidx.compose.runtime.setValue
import androidx.compose.foundation.horizontalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { 
                        GeometricBottomNavigation(
                            currentTab = state.currentTab,
                            onTabSelect = { viewModel.switchTab(it) }
                        ) 
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        ConsumptionDashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsumptionDashboardScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    when (state.currentTab) {
        AppTab.CALCULATOR -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Header Section (Geometric Balance style matches the top navbar)
                GeometricHeader()

                // Mode Switching Selector
                GeometricModeSelector(
                    mode = state.conversionMode,
                    onModeToggle = { viewModel.toggleConversionMode() }
                )

                // CARD 1: Consumption Conversion Card (White with gray border, rounded 28dp)
                GeometricConversionCard(
                    state = state,
                    onConsumptionChange = { viewModel.updateConsumption(it) },
                    focusManager = focusManager
                )

                // CARD 2: Trip Cost Calculator Card (Solid Dark blue 0xFF001D35, rounded 28dp)
                GeometricCostCalculatorCard(
                    state = state,
                    onPriceChange = { viewModel.updateFuelPrice(it) },
                    onDistanceChange = { viewModel.updateDistance(it) },
                    onPresetSelect = { viewModel.selectDistancePreset(it) },
                    onStationSelect = { viewModel.selectGasStation(it) },
                    onSaveTrip = { viewModel.saveCurrentTrip() },
                    onSwitchToStations = { viewModel.switchTab(AppTab.STATIONS) },
                    focusManager = focusManager
                )

                // Auxiliary context tips info
                GeometricInfoSection()
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        AppTab.STATIONS -> {
            GeometricStationsScreen(
                state = state,
                onAddStation = { name, price -> viewModel.addGasStation(name, price) },
                onDeleteStation = { viewModel.deleteGasStation(it) },
                onSelectStation = { viewModel.selectGasStation(it) }
            )
        }
        AppTab.HISTORY -> {
            GeometricHistoryScreen(
                state = state,
                onDeleteTrip = { viewModel.deleteSavedTrip(it) }
            )
        }
    }
}

@Composable
fun GeometricHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "EcoDrive",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = "Conversor de Consumo",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFFDDE2F9),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = Color(0xFF1B1B1F),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun GeometricModeSelector(
    mode: ConversionMode,
    onModeToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onModeToggle() }
            .testTag("mode_toggle_container"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, Color(0xFFE1E2EC)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MODO DE CONVERSÃO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF005AC1),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (mode == ConversionMode.L_100KM_TO_KML) {
                        "Liters / 100km ➔ km / L"
                    } else {
                        "km / L ➔ Liters / 100km"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (mode == ConversionMode.L_100KM_TO_KML) {
                        "Conversão padrão europeia para o rendimento brasileiro"
                    } else {
                        "Conversão de km/L para litros consumidos por 100km"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFDDE2F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⇄",
                    fontSize = 20.sp,
                    color = Color(0xFF001D35),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GeometricConversionCard(
    state: CalculatorUiState,
    onConsumptionChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val isKmlMode = state.conversionMode == ConversionMode.KML_TO_L_100KM

    val inputLabel = if (isKmlMode) {
        "Consumo Atual (km / L)"
    } else {
        "Consumo Atual (L / 100km)"
    }

    val inputSuffix = if (isKmlMode) "km/L" else "L/100"
    val resultLabel = "Equivale a:"
    val resultSuffix = if (isKmlMode) "L/100km" else "km/L"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conversion_section_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, Color(0xFFE1E2EC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header for card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDDE2F9), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "⚡", fontSize = 16.sp, color = Color(0xFF001D35))
                }
                Text(
                    text = "Conversão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C1E)
                )
            }

            // Text Input matching the Geometric balance styling with label nested visually
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = inputLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF005AC1),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                OutlinedTextField(
                    value = state.consumptionInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                            onConsumptionChange(newValue)
                        }
                    },
                    suffix = { Text(inputSuffix, fontWeight = FontWeight.SemiBold, color = Color(0xFF44474E)) },
                    placeholder = { Text("Ex: 8.0") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                    isError = state.consumptionInput.isNotEmpty() && state.consumptionVal == null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF005AC1),
                        unfocusedBorderColor = Color(0xFF005AC1).copy(alpha = 0.5f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consumption_input"),
                    trailingIcon = {
                        if (state.consumptionInput.isNotEmpty()) {
                            IconButton(onClick = { onConsumptionChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpar campo",
                                    tint = Color(0xFF44474E)
                                )
                            }
                        }
                    }
                )
            }

            if (state.consumptionInput.isNotEmpty() && state.consumptionVal == null) {
                Text(
                    text = "Insira um número decimal válido (Ex: 8.2 ou 8,2)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBA1A1A),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Sync Arrow Representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⇅",
                    fontSize = 20.sp,
                    color = Color(0xFF44474E),
                    fontWeight = FontWeight.Bold
                )
            }

            // Results sub-panel inside conversion card (light container, text summary on the right)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F9), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resultLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF44474E),
                        fontWeight = FontWeight.Medium
                    )

                    val computed = state.calculatedEfficiency
                    if (computed != null) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%,.2f", computed),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color(0xFF1A1C1E),
                                modifier = Modifier.testTag("conversion_result")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resultSuffix,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF44474E),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Aguardando...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF44474E).copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricCostCalculatorCard(
    state: CalculatorUiState,
    onPriceChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onPresetSelect: (Int) -> Unit,
    onStationSelect: (String) -> Unit,
    onSaveTrip: () -> Unit,
    onSwitchToStations: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cost_calculator_section"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001D35) // Deep solid Dark blue from Geometric theme
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header section inside card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF004786), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "🪙", fontSize = 16.sp, color = Color(0xFFD1E4FF))
                }
                Text(
                    text = "Estimativa de Custo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFFFFF)
                )
            }

            // Grid layout columns for Fuel Price and Trip Distance inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Posto Selecionado (Puxado automaticamente)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "POSTO SELECIONADO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFFD1E4FF),
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF004786), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFD1E4FF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⛽", fontSize = 16.sp)
                            Column {
                                val stationName = state.selectedStation?.name ?: "Nenhum Posto"
                                val priceFormatted = state.selectedStation?.price?.let {
                                    String.format(Locale.getDefault(), "R$ %,.2f/L", it)
                                } ?: "---"
                                
                                Text(
                                    text = stationName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = priceFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD1E4FF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Distancia km Card Input
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "DISTÂNCIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFFD1E4FF),
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    OutlinedTextField(
                        value = state.distanceInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                                onDistanceChange(newValue)
                            }
                        },
                        suffix = {
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        },
                        placeholder = { Text("86", color = Color.White.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF004786),
                            unfocusedContainerColor = Color(0xFF004786),
                            focusedBorderColor = Color(0xFFD1E4FF),
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_distance_input")
                    )
                }
            }

            // Quick distance toggle bars including 86km
            GeometricPresetRow(
                selectedDistance = state.distanceInput,
                onPresetClick = onPresetSelect
            )

            // Dynamic horizontal scroll row of gas stations
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Postos Cadastrados (Selecione um):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (state.stations.isEmpty()) {
                    Text(
                        text = "Nenhum posto cadastrado na aba 'Postos'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.stations.forEach { station ->
                            val isSelected = state.selectedStationId == station.id
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .background(
                                        color = if (isSelected) Color(0xFFD1E4FF) else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF005AC1) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onStationSelect(station.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = station.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = if (isSelected) Color(0xFF001D35) else Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "R$ %.2f/L", station.price),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF004786) else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Dynamic blue result block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD1E4FF), RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .animateContentSize()
            ) {
                if (state.stations.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CADASTRE UM POSTO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF001D35)
                        )
                        Text(
                            text = "Adicione postos na aba 'Postos' com seus respectivos preços para calcular estimativas precisas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF001D35).copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF001D35))
                                .clickable { onSwitchToStations() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "⛽", fontSize = 14.sp)
                                Text(
                                    text = "Ir para Cadastro de Postos",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    val totalCost = state.tripCost
                    val litConsumed = state.tripFuelConsumedLiters

                    if (totalCost != null && litConsumed != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GASTO TOTAL ESTIMADO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF001D35)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "R$",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF001D35),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%,.2f", totalCost),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 35.sp,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = Color(0xFF001D35),
                                    modifier = Modifier.testTag("calculated_cost_result")
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%,.1f Litros consumidos", litConsumed),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF001D35).copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val currentStationName = state.selectedStation?.name ?: "Posto de Combustível"
                            Text(
                                text = "Para andar ${state.distanceInput}km a ${state.consumptionInput} ${if (state.conversionMode == ConversionMode.L_100KM_TO_KML) "L/100km" else "km/L"} no $currentStationName",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF001D35).copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF001D35))
                                    .clickable { onSaveTrip() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "💾", fontSize = 14.sp)
                                    Text(
                                        text = "Salvar Viagem no Histórico",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Aguardando cálculo...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF001D35).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricPresetRow(
    selectedDistance: String,
    onPresetClick: (Int) -> Unit
) {
    val presets = listOf(10, 50, 86, 120, 300)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Distância Rápida:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { km ->
                val isSelected = selectedDistance == km.toString()
                val isUserSpecial = km == 86

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = when {
                                isSelected -> Color(0xFFD1E4FF)
                                isUserSpecial -> Color(0xFF004786)
                                else -> Color.White.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isUserSpecial && !isSelected) Color(0xFFD1E4FF) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onPresetClick(km) }
                        .heightIn(min = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${km}km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSelected -> Color(0xFF001D35)
                            isUserSpecial -> Color(0xFFD1E4FF)
                            else -> Color.White.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GeometricInfoSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, Color(0xFFE1E2EC))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "ℹ️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Como ler o Consumo?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005AC1)
                )
            }
            Text(
                text = "Medições em Litros a cada 100km (L/100km) são o inverso de km/L. Quanto menor o valor em L/100km, mais eficiente e econômico é o seu carro! A calculadora estima seus gastos reais instantaneamente.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun GeometricBottomNavigation(
    currentTab: AppTab,
    onTabSelect: (AppTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F9)),
        border = BorderStroke(1.dp, Color(0xFFDDE2F9)),
        color = Color(0xFFF3F4F9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: Calculadora
            val isCalcActive = currentTab == AppTab.CALCULATOR
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTabSelect(AppTab.CALCULATOR) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 32.dp)
                        .background(
                            if (isCalcActive) Color(0xFFD1E4FF) else Color.Transparent, 
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧮", fontSize = 16.sp)
                }
                Text(
                    text = "Calculadora",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCalcActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCalcActive) Color(0xFF001D35) else Color(0xFF44474E)
                )
            }

            // Option 2: Histórico
            val isHistoryActive = currentTab == AppTab.HISTORY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTabSelect(AppTab.HISTORY) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 32.dp)
                        .background(
                            if (isHistoryActive) Color(0xFFD1E4FF) else Color.Transparent, 
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🕒", fontSize = 16.sp)
                }
                Text(
                    text = "Histórico",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isHistoryActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHistoryActive) Color(0xFF001D35) else Color(0xFF44474E)
                )
            }

            // Option 3: Postos
            val isStationsActive = currentTab == AppTab.STATIONS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTabSelect(AppTab.STATIONS) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 32.dp)
                        .background(
                            if (isStationsActive) Color(0xFFD1E4FF) else Color.Transparent, 
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⛽", fontSize = 16.sp)
                }
                Text(
                    text = "Postos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isStationsActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isStationsActive) Color(0xFF001D35) else Color(0xFF44474E)
                )
            }
        }
    }
}

@Composable
fun GeometricStationsScreen(
    state: CalculatorUiState,
    onAddStation: (String, Double) -> Unit,
    onDeleteStation: (String) -> Unit,
    onSelectStation: (String) -> Unit
) {
    var nameInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var priceInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Form Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFDDE2F9), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Text(text = "⛽", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Postos de Combustível",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Gerencie os postos onde costuma abastecer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E)
                )
            }
        }

        // Add Station Card (white visually matching the Conversion Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2EC))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CADASTRAR NOVO POSTO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005AC1)
                )

                // Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nome do Posto") },
                    placeholder = { Text("Ex: Posto BR Trevo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF005AC1),
                        unfocusedBorderColor = Color(0xFF005AC1).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Price Input
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^[0-9]*[.,]?[0-9]*$"))) {
                            priceInput = newValue
                        }
                    },
                    label = { Text("Preço do Litro (Gasolina/Etanol)") },
                    prefix = { Text("R$ ", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("Ex: 5.89") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF005AC1),
                        unfocusedBorderColor = Color(0xFF005AC1).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Button
                val isEnabled = nameInput.isNotBlank() && priceInput.isNotBlank() && priceInput.replace(',', '.').toDoubleOrNull() != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = if (isEnabled) Color(0xFF005AC1) else Color(0xFF005AC1).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = isEnabled) {
                            val parsedPrice = priceInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                            onAddStation(nameInput.trim(), parsedPrice)
                            nameInput = ""
                            priceInput = ""
                            focusManager.clearFocus()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Adicionar à Lista",
                        color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List Header
        Text(
            text = "SEUS POSTOS CADASTRADOS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF44474E),
            modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 8.dp)
        )

        // Gas Stations listing
        if (state.stations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F9), RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum posto cadastrado ainda.\nCadastre acima para usar no cálculo rápido!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44474E).copy(alpha = 0.7f)
                )
            }
        } else {
            state.stations.forEach { station ->
                val isSelected = state.selectedStationId == station.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectStation(station.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFD1E4FF) else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF005AC1) else Color(0xFFE1E2EC)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSelected) Color(0xFF005AC1) else Color(0xFFF3F4F9),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSelected) "⭐️" else "⛽",
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF001D35) else Color(0xFF1A1C1E)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "Preço: R$ %,.2f", station.price),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF004786) else Color(0xFF44474E)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF001D35), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ATIVO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDeleteStation(station.id) }
                        ) {
                            Text(text = "❌", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricHistoryScreen(
    state: CalculatorUiState,
    onDeleteTrip: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFDDE2F9), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Text(text = "🕒", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Histórico de Viagens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Acompanhe suas estimativas e gastos salvos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E)
                )
            }
        }

        if (state.savedTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F9), RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma viagem salva no histórico ainda.\nSimule seus gastos na Calculadora e toque em\n'Salvar Viagem no Histórico'.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44474E).copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        } else {
            state.savedTrips.forEach { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE1E2EC))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row of Card: Station and Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = trip.stationName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF005AC1)
                                )
                                Text(
                                    text = trip.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF44474E).copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { onDeleteTrip(trip.id) }) {
                                Text(text = "🗑️", fontSize = 16.sp)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE1E2EC).copy(alpha = 0.5f))

                        // Score Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "DISTÂNCIA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF44474E)
                                    )
                                    Text(
                                        text = "${trip.distance} km",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1C1E)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "CONSUMO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF44474E)
                                    )
                                    Text(
                                        text = "${trip.consumption} ${trip.modeLabel}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1C1E)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "CONSUMIDO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF44474E)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%,.1f L", trip.liters),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1C1E)
                                    )
                                }
                            }

                            // Cost Badge
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD1E4FF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "R$ %,.2f", trip.cost),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF001D35)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeometricDashboardPreview() {
    MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ConsumptionDashboardScreen()
        }
    }
}
