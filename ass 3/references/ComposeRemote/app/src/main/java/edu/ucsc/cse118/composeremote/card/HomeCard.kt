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
package edu.ucsc.cse118.composeremote.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import edu.ucsc.cse118.composeremote.model.LocalClubViewModel

@Composable
fun HomeCard(clubIndex: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val clubViewModel = LocalClubViewModel.current
    val club = clubViewModel.clubs[clubIndex]
    Card(onClick = {
        clubViewModel.selectClub(club)
        onClick()
    }, modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = club.stadium.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
