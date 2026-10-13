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
package edu.ucsc.cse118.composeremote.repo

import com.google.gson.Gson
import edu.ucsc.cse118.composeremote.data.BallClub
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BallClubRepo {
    private val gson = Gson()
    private val baseUrl = "https://cse118.com/api/v2/ballclub"

    suspend fun getClubs(): List<BallClub> = withContext(Dispatchers.IO) {
        gson.fromJson(getJson(baseUrl), Array<BallClub>::class.java).toList()
    }

    suspend fun getClub(code: String): BallClub = withContext(Dispatchers.IO) {
        val encodedCode = URLEncoder.encode(code, "UTF-8")
        gson.fromJson(getJson("$baseUrl/$encodedCode"), BallClub::class.java)
    }

    private fun getJson(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Ball club request failed (HTTP $status)")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
