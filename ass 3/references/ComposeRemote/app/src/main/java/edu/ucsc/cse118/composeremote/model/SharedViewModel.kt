/*
#######################################################################
#
# Copyright (C) 2022-2026 David C. Harrison. All rights reserved.
#
# You may not use, distribute, publish, or modify this code without
# the express written permission of the copyright holder.
#
#######################################################################
*/
package edu.ucsc.cse118.composeremote.model

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import edu.ucsc.cse118.composeremote.data.BallClub
import edu.ucsc.cse118.composeremote.repo.BallClubRepo

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BallClubRepo()

    var clubs by mutableStateOf<List<BallClub>>(emptyList())
        private set

    var error by mutableStateOf<String?>(null)

    var selectedClub by mutableStateOf<BallClub?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                clubs = repo.getClubs()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Unable to load ball clubs"
            }
        }
    }

    fun selectClub(club: BallClub) {
        selectedClub = club
        error = null
        viewModelScope.launch {
            try {
                selectedClub = repo.getClub(club.code)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Unable to load ball club details"
            }
        }
    }
}

// Shared by the navigation title, home screen, and detail screen.
val LocalClubViewModel = compositionLocalOf<SharedViewModel> {
    error("Club ViewModel has not been provided")
}
