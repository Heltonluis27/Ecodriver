package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ConversionMode {
    L_100KM_TO_KML,
    KML_TO_L_100KM
}

enum class AppTab {
    CALCULATOR,
    HISTORY,
    STATIONS
}

data class GasStation(
    val id: String,
    val name: String,
    val price: Double
)

data class SavedTrip(
    val id: String,
    val date: String,
    val distance: String,
    val consumption: String,
    val modeLabel: String,
    val cost: Double,
    val stationName: String,
    val liters: Double
)

data class CalculatorUiState(
    val conversionMode: ConversionMode = ConversionMode.L_100KM_TO_KML,
    val consumptionInput: String = "8.0", // default L/100km matching example user fuel
    val fuelPriceInput: String = "5.89",   // default average fuel price context
    val distanceInput: String = "86",      // default distance requested by user: 86km
    val currentTab: AppTab = AppTab.CALCULATOR,
    val stations: List<GasStation> = listOf(
        GasStation("1", "Posto Shell Central", 5.89),
        GasStation("2", "Posto Ipiranga Trevo", 5.75),
        GasStation("3", "Posto Petrobras Lago", 6.10)
    ),
    val selectedStationId: String = "1",
    val savedTrips: List<SavedTrip> = emptyList()
) {
    // Selected gas station
    val selectedStation: GasStation?
        get() = stations.find { it.id == selectedStationId }

    // Parsed numeric values or null
    val consumptionVal: Double?
        get() = parseDouble(consumptionInput)

    val fuelPriceVal: Double?
        get() = selectedStation?.price ?: parseDouble(fuelPriceInput)

    val distanceVal: Double?
        get() = parseDouble(distanceInput)

    // Calculated efficiency
    val calculatedEfficiency: Double?
        get() {
            val value = consumptionVal ?: return null
            if (value <= 0) return null
            return when (conversionMode) {
                ConversionMode.L_100KM_TO_KML -> 100.0 / value
                ConversionMode.KML_TO_L_100KM -> 100.0 / value
            }
        }

    // Trip fuel consumed in Liters
    val tripFuelConsumedLiters: Double?
        get() {
            val dist = distanceVal ?: return null
            val cons = consumptionVal ?: return null
            if (dist < 0 || cons <= 0) return null

            return when (conversionMode) {
                ConversionMode.L_100KM_TO_KML -> {
                    // C liters per 100 km -> (dist * cons) / 100
                    (dist * cons) / 100.0
                }
                ConversionMode.KML_TO_L_100KM -> {
                    // cons is km / liter -> dist / cons
                    dist / cons
                }
            }
        }

    // Trip cost in currency
    val tripCost: Double?
        get() {
            val liters = tripFuelConsumedLiters ?: return null
            val price = fuelPriceVal ?: return null
            if (price < 0) return null
            return liters * price
        }

    // Assessment info of the fuel efficiency rating
    val efficiencyRating: FuelEfficiencyRating
        get() {
            val kml = when (conversionMode) {
                ConversionMode.L_100KM_TO_KML -> calculatedEfficiency
                ConversionMode.KML_TO_L_100KM -> consumptionVal
            } ?: return FuelEfficiencyRating.UNKNOWN

            return when {
                kml >= 14.0 -> FuelEfficiencyRating.EXCELLENT
                kml >= 10.0 -> FuelEfficiencyRating.MODERATE
                kml > 0.0 -> FuelEfficiencyRating.EXPENSIVE
                else -> FuelEfficiencyRating.UNKNOWN
            }
        }

    private fun parseDouble(input: String): Double? {
        val sanitized = input.replace(',', '.').trim()
        return sanitized.toDoubleOrNull()
    }
}

enum class FuelEfficiencyRating(val label: String, val description: String) {
    EXCELLENT("Econômico 🟢", "Excelente autonomia! Pouco consumo por km."),
    MODERATE("Médio 🟡", "Consumo moderado. Ideal para trechos mistos."),
    EXPENSIVE("Alto Consumo 🔴", "Eficiência reduzida. Preste atenção aos gastos!"),
    UNKNOWN("Indefinido", "Informe dados válidos acima para avaliar.")
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun updateConsumption(value: String) {
        _uiState.update { it.copy(consumptionInput = value) }
    }

    fun updateFuelPrice(value: String) {
        _uiState.update { it.copy(fuelPriceInput = value) }
    }

    fun updateDistance(value: String) {
        _uiState.update { it.copy(distanceInput = value) }
    }

    fun toggleConversionMode() {
        _uiState.update {
            val nextMode = if (it.conversionMode == ConversionMode.L_100KM_TO_KML) {
                ConversionMode.KML_TO_L_100KM
            } else {
                ConversionMode.L_100KM_TO_KML
            }
            // Swap inputs appropriately or clean to default reasonable values
            val parsedCurrent = it.consumptionVal
            val nextInput = if (parsedCurrent != null && parsedCurrent > 0) {
                // If it was 8 L/100km, the next converted represents 12.5 km/L.
                // Let's do a smart swap to keep user input in sync!
                val converted = 100.0 / parsedCurrent
                String.format("%.1f", converted).replace(',', '.')
            } else {
                it.consumptionInput
            }
            it.copy(
                conversionMode = nextMode,
                consumptionInput = nextInput
            )
        }
    }

    fun selectDistancePreset(presetKm: Int) {
        _uiState.update { it.copy(distanceInput = presetKm.toString()) }
    }

    fun switchTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectGasStation(id: String) {
        _uiState.update { state ->
            val station = state.stations.find { it.id == id }
            val updatedPriceInput = station?.price?.toString() ?: state.fuelPriceInput
            state.copy(
                selectedStationId = id,
                fuelPriceInput = updatedPriceInput
            )
        }
    }

    fun addGasStation(name: String, price: Double) {
        _uiState.update { state ->
            val newId = java.util.UUID.randomUUID().toString()
            val newStation = GasStation(newId, name, price)
            val updatedList = state.stations + newStation
            state.copy(
                stations = updatedList,
                selectedStationId = if (state.stations.isEmpty()) newId else state.selectedStationId
            )
        }
    }

    fun deleteGasStation(id: String) {
        _uiState.update { state ->
            val updatedList = state.stations.filter { it.id != id }
            val nextSelectedId = if (state.selectedStationId == id) {
                updatedList.firstOrNull()?.id ?: ""
            } else {
                state.selectedStationId
            }
            state.copy(
                stations = updatedList,
                selectedStationId = nextSelectedId
            )
        }
    }

    fun saveCurrentTrip() {
        _uiState.update { state ->
            val cost = state.tripCost ?: return@update state
            val liters = state.tripFuelConsumedLiters ?: return@update state
            val stationName = state.selectedStation?.name ?: "Posto Manual"
            val distance = state.distanceInput
            val consumption = state.consumptionInput
            val modeLabel = if (state.conversionMode == ConversionMode.L_100KM_TO_KML) "L/100km" else "km/L"

            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val newTrip = SavedTrip(
                id = java.util.UUID.randomUUID().toString(),
                date = dateStr,
                distance = distance,
                consumption = consumption,
                modeLabel = modeLabel,
                cost = cost,
                stationName = stationName,
                liters = liters
            )
            state.copy(savedTrips = listOf(newTrip) + state.savedTrips)
        }
    }

    fun deleteSavedTrip(id: String) {
        _uiState.update { state ->
            state.copy(savedTrips = state.savedTrips.filter { it.id != id })
        }
    }
}
