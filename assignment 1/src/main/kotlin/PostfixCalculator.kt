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

import java.util.ArrayDeque
import kotlin.math.pow

class PostfixCalculator {
  /**
   * See assignment specification.
   */
  fun parse(expression: String): Double {
    val stack = ArrayDeque<Double>()
    for (token in expression.trim().split(Regex("\\s+"))) {
      val number = token.toDoubleOrNull()
      if (number != null) {
        stack.addLast(number)
      } else {
        require(stack.size >= 2) { "Not enough operands" }
        val right = stack.removeLast()
        val left = stack.removeLast()
        val result = when (token) {
          "+" -> left + right
          "-" -> left - right
          "*" -> left * right
          "/" -> left / right
          "^" -> left.pow(right)
          else -> throw IllegalArgumentException("Unknown operator: $token")
        }
        stack.addLast(result)
      }
    }
    require(stack.size == 1) { "Expected one result" }
    return stack.removeLast()
  }
}
