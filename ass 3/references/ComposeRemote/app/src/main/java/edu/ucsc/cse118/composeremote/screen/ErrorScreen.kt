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
package edu.ucsc.cse118.composeremote.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.ucsc.cse118.composeremote.model.LocalClubViewModel

@Composable
fun ErrorScreen() {
    Text(
        text = LocalClubViewModel.current.error.orEmpty(),
        modifier = Modifier.padding(16.dp)
    )
}
