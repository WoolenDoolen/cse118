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
package edu.ucsc.cse118.composelocal.model

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.ucsc.cse118.composelocal.data.BallClub
import edu.ucsc.cse118.composelocal.repo.BallClubRepo

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BallClubRepo(application)

    val clubs: List<BallClub>
        get() = repo.clubs

    var selectedClub by mutableStateOf<BallClub?>(null)
        private set

    fun selectClub(club: BallClub) {
        selectedClub = club
    }
}

// Shared by the navigation title, home screen, and detail screen.
val LocalClubViewModel = compositionLocalOf<SharedViewModel> {
    error("Club ViewModel has not been provided")
}
