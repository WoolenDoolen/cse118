/*
#######################################################################
#
# Copyright (C) 2022-2026 David C. Harrison. All right reserved.
#
# You may not use, distribute, publish, or modify this code without
# the express written permission of the copyright holder.
#
#######################################################################
*/

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PostfixCalculatorTest {
  private val calculator = PostfixCalculator()

  @Test
  internal fun `adds numbers`() {
    assertEquals(11.0, calculator.parse("5 6 +"))
    assertEquals(0.3, calculator.parse("0.1 0.2 +"), 0.000001)
  }

  @Test
  internal fun `subtracts in the right order`() {
    assertEquals(-5.0, calculator.parse("4 9 -"))
    assertEquals(-3.0, calculator.parse("-5 -2 -"))
  }

  @Test
  internal fun `multiplies negative numbers and decimals`() {
    assertEquals(-10.0, calculator.parse("2.5 -4 *"))
    assertEquals(6.0, calculator.parse("-2 -3 *"))
  }

  @Test
  internal fun `divides without rounding to an integer`() {
    assertEquals(3.5, calculator.parse("7 2 /"))
    assertEquals(0.25, calculator.parse("2 8 /"))
  }

  @Test
  internal fun `raises a number to a power`() {
    assertEquals(8.0, calculator.parse("2 3 ^"))
    assertEquals(3.0, calculator.parse("9 0.5 ^"))
    assertEquals(0.25, calculator.parse("2 -2 ^"))
    assertEquals(1.0, calculator.parse("5 0 ^"))
  }

  @Test
  internal fun `evaluates expressions with several operators`() {
    assertEquals(170.0, calculator.parse("5 6 2 ^ 2 - *"))
    assertEquals(14.0, calculator.parse("5 1 2 + 4 * + 3 -"))
    assertEquals(4.0, calculator.parse("20 2 3 + /"))
  }

  @Test
  internal fun `returns a single number`() {
    assertEquals(-3.5, calculator.parse("-3.5"))
    assertEquals(0.0, calculator.parse("0"))
  }

  @Test
  internal fun `accepts extra whitespace`() {
    assertEquals(11.0, calculator.parse("  \t5   6\n+  \r\n"))
  }

  @Test
  internal fun `uses double arithmetic for division by zero`() {
    assertEquals(Double.POSITIVE_INFINITY, calculator.parse("1 0 /"))
    assertEquals(Double.NEGATIVE_INFINITY, calculator.parse("-1 0 /"))
    assertTrue(calculator.parse("0 0 /").isNaN())
  }

  @Test
  internal fun `rejects missing operands`() {
    for (expression in listOf("", " \t\n ", "+", "1 +", "1 2 + +")) {
      assertFailsWith<IllegalArgumentException> {
        calculator.parse(expression)
      }
    }
  }

  @Test
  internal fun `rejects unknown operators and invalid numbers`() {
    assertFailsWith<IllegalArgumentException> { calculator.parse("1 2 %") }
    assertFailsWith<IllegalArgumentException> { calculator.parse("1 2 nope") }
  }

  @Test
  internal fun `rejects extra operands`() {
    assertFailsWith<IllegalArgumentException> { calculator.parse("1 2") }
    assertFailsWith<IllegalArgumentException> { calculator.parse("1 2 3 +") }
  }
}
