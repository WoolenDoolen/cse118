/*
#######################################################################
#
# Copyright (C) 2026 David C. Harrison. All rights reserved.
#
# You may not use, distribute, publish, or modify this code without
# the express written permission of the copyright holder.
#
#######################################################################
*/
package edu.ucsc.cse118.composelocal.data

data class Stadium (
  val name: String,
  val location: String? = "Unknown",
  val capacity: Int? = 0,
  val built: Int? = 0
)
