package com.ditr.ekyc_login.helper

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.ditr.ekyc_login.R
import com.ditr.ekyc_login.constants.StringConstants

class GetAppInfo(private val activity: FragmentActivity) {
    private val appResources: Resources = activity.resources

    // generate uri to launch ekyc application
    fun getUri(codeChallenge: String): String {
        try {
            return String.format(
                activity.getString(R.string.com_ditr_ekyc_uri),
                getClientID(), codeChallenge, getAppName()
            )
        } catch (error: Exception){
            throw Exception(error.message)
        }
    }

    // get application name
    private fun getAppName(): String? {
        return try {
            val appName: CharSequence = appResources.getText(
                appResources.getIdentifier(
                    StringConstants.APP_NAME,
                    "string", activity.packageName
                )
            )
            appName.toString()
        } catch (error: Exception) {
            throw Exception(StringConstants.ERROR_NOT_FOUND_APP_NAME)
        }
    }

    // get client id
    private fun getClientID(): String? {
        return try {
            val clientID: CharSequence = appResources.getText(
                appResources.getIdentifier(
                    StringConstants.CLIENT_ID,
                    "string", activity.packageName
                )
            )
            clientID.toString()
        } catch (error: Exception) {
            throw Exception(StringConstants.ERROR_NOT_FOUND_CLIENT_ID)
        }
    }
}