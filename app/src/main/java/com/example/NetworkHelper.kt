package com.example

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkHelper {
    suspend fun sendKickRequest(tc: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val urlString = "http://192.168.1.8:2081/kick?tc={$tc}"
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.useCaches = false
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Result.success("Đã gửi.")
                } else {
                    Result.failure(IOException("Mã lỗi: $responseCode"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}
