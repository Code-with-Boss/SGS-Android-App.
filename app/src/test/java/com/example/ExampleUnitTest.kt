package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun feeOutstandingCalculation_isAccurate() {
    val monthlyFee = 2500.0
    val monthsPaid = 2
    val totalPaid = monthsPaid * monthlyFee
    val annualFee = 12 * monthlyFee
    val outstanding = annualFee - totalPaid
    assertEquals(5000.0, totalPaid, 0.01)
    assertEquals(25000.0, outstanding, 0.01)
  }
}
