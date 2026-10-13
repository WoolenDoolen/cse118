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
package edu.ucsc.cse118.composelocal.repo

import android.content.Context
import com.google.gson.Gson
import edu.ucsc.cse118.composelocal.R
import edu.ucsc.cse118.composelocal.data.BallClub
import java.io.InputStreamReader

class BallClubRepo(context: Context) {
    val clubs: List<BallClub> = context.resources.openRawResource(R.raw.ball_clubs).use { stream ->
        InputStreamReader(stream).use { reader ->
            Gson().fromJson(reader, Array<BallClub>::class.java).toList()
        }
    }
}


