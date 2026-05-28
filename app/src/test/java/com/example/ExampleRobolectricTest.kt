package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Conversor de Consumo", appName)
  }

  @Test
  fun `convert L-100km to km-L correctly`() {
    val state = CalculatorUiState(
      conversionMode = ConversionMode.L_100KM_TO_KML,
      consumptionInput = "8.0",
      fuelPriceInput = "5.00",
      distanceInput = "100.0"
    )

    // 100 / 8.0 = 12.5 km/L
    assertEquals(12.5, state.calculatedEfficiency ?: 0.0, 0.01)

    // 100km at 8 L/100km = 8 Liters
    assertEquals(8.0, state.tripFuelConsumedLiters ?: 0.0, 0.01)

    // 8 Liters * R$ 5.00 = R$ 40.00
    assertEquals(40.0, state.tripCost ?: 0.0, 0.01)
  }

  @Test
  fun `convert km-L to L-100km correctly`() {
    val state = CalculatorUiState(
      conversionMode = ConversionMode.KML_TO_L_100KM,
      consumptionInput = "10.0",
      fuelPriceInput = "6.00",
      distanceInput = "50.0"
    )

    // 100 / 10.0 = 10.0 L/100km
    assertEquals(10.0, state.calculatedEfficiency ?: 0.0, 0.01)

    // 50km at 10.0 km/L = 5 Liters
    assertEquals(5.0, state.tripFuelConsumedLiters ?: 0.0, 0.01)

    // 5 Liters * R$ 6.00 = R$ 30.00
    assertEquals(30.0, state.tripCost ?: 0.0, 0.01)
  }
}
