package com.ditr.ekyc_login.model

class EkycLoginResult(val token: String, val codeVerifier: String) {
    override fun toString(): String {
        return "EkycResult(token=$token, codeVerify='$codeVerifier')"
    }
}