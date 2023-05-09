package com.ditr.ekyc_login.helper

import android.util.Base64
import java.security.MessageDigest

object GenerateCode {

    // generate code verify for user to verify user code at server side
    fun codeVerifier(): String {
        try {
            val random43to128 = (43..128).random()
            val allowedCharSet =
                ('a'..'z') + ('A'..'Z') + ('0'..'9') + ('-') + ('.') + ('_') + ('~')
            return List(random43to128) { allowedCharSet.random() }.joinToString("")
        } catch (error: Exception) {
            throw Exception(error.message)
        }
    }

    // generate code challenge to get user code in ekyc app
    fun codeChallenge(codeVerifier: String): String {
        try {
            // try to generate challenge with S256
            val bytes: ByteArray = codeVerifier.toByteArray(Charsets.US_ASCII)
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(bytes, 0, bytes.size)
            val digest = messageDigest.digest()
            return Base64.encodeToString(
                digest,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
        } catch (error: Exception) {
            throw Exception(error.message)
        }
    }
}