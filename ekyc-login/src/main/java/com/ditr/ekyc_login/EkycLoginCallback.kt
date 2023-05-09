package com.ditr.ekyc_login

import com.ditr.ekyc_login.model.EkycLoginResult

interface EkycLoginCallback {
    fun onSuccess(result: EkycLoginResult)
    fun onCancel()
    fun onError(throwable: Throwable)
}