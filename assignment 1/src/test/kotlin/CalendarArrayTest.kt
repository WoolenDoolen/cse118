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
import java.time.YearMonth
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CalendarArrayTest {
  private val calendar = CalendarArray()

  @Test
  internal fun `april matches the assignment example`() {
    checkMonth(2023, 4,
      intArrayOf(26, 27, 28, 29, 30, 31, 1),
      intArrayOf(2, 3, 4, 5, 6, 7, 8),
      intArrayOf(9, 10, 11, 12, 13, 14, 15),
      intArrayOf(16, 17, 18, 19, 20, 21, 22),
      intArrayOf(23, 24, 25, 26, 27, 28, 29),
      intArrayOf(30, 1, 2, 3, 4, 5, 6))
  }

  @Test
  internal fun `a month starting on sunday begins with day one`() {
    checkMonth(2023, 1,
      intArrayOf(1, 2, 3, 4, 5, 6, 7),
      intArrayOf(8, 9, 10, 11, 12, 13, 14),
      intArrayOf(15, 16, 17, 18, 19, 20, 21),
      intArrayOf(22, 23, 24, 25, 26, 27, 28),
      intArrayOf(29, 30, 31, 1, 2, 3, 4),
      intArrayOf(5, 6, 7, 8, 9, 10, 11))
  }

  @Test
  internal fun `march includes the end of a non leap february`() {
    checkMonth(2023, 3,
      intArrayOf(26, 27, 28, 1, 2, 3, 4),
      intArrayOf(5, 6, 7, 8, 9, 10, 11),
      intArrayOf(12, 13, 14, 15, 16, 17, 18),
      intArrayOf(19, 20, 21, 22, 23, 24, 25),
      intArrayOf(26, 27, 28, 29, 30, 31, 1),
      intArrayOf(2, 3, 4, 5, 6, 7, 8))
  }

  @Test
  internal fun `february has 29 days in a leap year`() {
    checkMonth(2024, 2,
      intArrayOf(28, 29, 30, 31, 1, 2, 3),
      intArrayOf(4, 5, 6, 7, 8, 9, 10),
      intArrayOf(11, 12, 13, 14, 15, 16, 17),
      intArrayOf(18, 19, 20, 21, 22, 23, 24),
      intArrayOf(25, 26, 27, 28, 29, 1, 2),
      intArrayOf(3, 4, 5, 6, 7, 8, 9))
  }

  @Test
  internal fun `a century divisible by 400 is a leap year`() {
    checkMonth(2000, 2,
      intArrayOf(30, 31, 1, 2, 3, 4, 5),
      intArrayOf(6, 7, 8, 9, 10, 11, 12),
      intArrayOf(13, 14, 15, 16, 17, 18, 19),
      intArrayOf(20, 21, 22, 23, 24, 25, 26),
      intArrayOf(27, 28, 29, 1, 2, 3, 4),
      intArrayOf(5, 6, 7, 8, 9, 10, 11))
  }

  @Test
  internal fun `other centuries are not leap years`() {
    checkMonth(2100, 2,
      intArrayOf(31, 1, 2, 3, 4, 5, 6),
      intArrayOf(7, 8, 9, 10, 11, 12, 13),
      intArrayOf(14, 15, 16, 17, 18, 19, 20),
      intArrayOf(21, 22, 23, 24, 25, 26, 27),
      intArrayOf(28, 1, 2, 3, 4, 5, 6),
      intArrayOf(7, 8, 9, 10, 11, 12, 13))
  }

  @Test
  internal fun `december continues into the next year`() {
    checkMonth(2023, 12,
      intArrayOf(26, 27, 28, 29, 30, 1, 2),
      intArrayOf(3, 4, 5, 6, 7, 8, 9),
      intArrayOf(10, 11, 12, 13, 14, 15, 16),
      intArrayOf(17, 18, 19, 20, 21, 22, 23),
      intArrayOf(24, 25, 26, 27, 28, 29, 30),
      intArrayOf(31, 1, 2, 3, 4, 5, 6))
  }

  private fun checkMonth(year: Int, month: Int, vararg expected: IntArray) {
    val days = calendar.generate(YearMonth.of(year, month))
    assertEquals(6, days.size)
    for (row in expected.indices) {
      assertContentEquals(expected[row], days[row], "Row $row of $year-$month")
    }
  }
}
