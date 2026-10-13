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
package edu.ucsc.cse118.composelocal.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

import edu.ucsc.cse118.composelocal.model.LocalClubViewModel

@Composable
fun DetailCard(modifier: Modifier = Modifier) {
    val club = LocalClubViewModel.current.selectedClub ?: return
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Stadium: ${club.stadium.name}")
            Text("Location: ${club.stadium.location ?: "Unknown"}")
            Text("Capacity: ${club.stadium.capacity?.let { String.format(Locale.US, "%,d", it) } ?: "Unknown"}")
            Text("Built: ${club.stadium.built ?: "Unknown"}")
        }
    }
}
