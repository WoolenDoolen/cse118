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

import java.time.YearMonth

class CalendarArray {
  /**
   * See assignment specification.
   */
  fun generate(month: YearMonth): Array<IntArray> {
    val first = month.atDay(1)
    val start = first.minusDays((first.dayOfWeek.value % 7).toLong())
    return Array(6) { row ->
      IntArray(7) { column ->
        start.plusDays((row * 7 + column).toLong()).dayOfMonth
      }
    }
  }
}
