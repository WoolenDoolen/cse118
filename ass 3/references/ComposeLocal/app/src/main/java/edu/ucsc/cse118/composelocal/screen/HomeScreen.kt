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
package edu.ucsc.cse118.composelocal.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.dp


import edu.ucsc.cse118.composelocal.model.LocalClubViewModel
import edu.ucsc.cse118.composelocal.card.HomeCard

@Composable
fun HomeScreen(onNavigateToDetails: () -> Unit) {
    val clubViewModel = LocalClubViewModel.current

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        itemsIndexed(clubViewModel.clubs) { index, _ ->
            HomeCard(
                clubIndex = index,
                onClick = onNavigateToDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}
