package com.ditr.ekyc_login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ditr.ekyc_login.constants.StringConstants
import com.ditr.ekyc_login.helper.GenerateCode
import com.ditr.ekyc_login.helper.GetAppInfo
import com.ditr.ekyc_login.model.EkycLoginResult
import kotlin.math.ceil

class EkycLoginButton(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatButton(context, attrs, 0) {

    private var externalOnClickListener: OnClickListener? = null
    private var textButton = resources.getString(R.string.com_ditr_ekyc_login_button_login)
    private var overrideCompoundPaddingLeft = 0
    private var overrideCompoundPaddingRight = 0
    private var overrideCompoundPadding = false
    private lateinit var popupWindows: PopupWindow
    private lateinit var popupView: RelativeLayout

    private lateinit var getAppInfo: GetAppInfo
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var resultCallback: EkycLoginCallback? = null

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    init {
        context as FragmentActivity
        setupLauncher()
        configureButton()
        setLoadingAlert(context)
        setupOnClickListener(context)
    }

    override fun onDraw(canvas: Canvas) {
        val centered = this.gravity and Gravity.CENTER_HORIZONTAL != 0
        if (centered) {
            // if the text is centered, we need to adjust the frame for the titleLabel based on the
            // size of the text in order to keep the text centered in the button without adding
            // extra blank space to the right when unnecessary
            // 1. the text fits centered within the button without colliding with the image
            //    (imagePaddingWidth)
            // 2. the text would run into the image, so adjust the insets to effectively left align
            //    it (textPaddingWidth)
            val compoundPaddingLeft = compoundPaddingLeft
            val compoundPaddingRight = compoundPaddingRight
            val compoundDrawablePadding = compoundDrawablePadding
            val textX = compoundPaddingLeft + compoundDrawablePadding
            val textContentWidth = width - textX - compoundPaddingRight
            val textWidth = measureTextWidth(text.toString())
            val textPaddingWidth = (textContentWidth - textWidth) / 2
            val imagePaddingWidth = (compoundPaddingLeft - paddingLeft) / 2
            val inset = textPaddingWidth.coerceAtMost(imagePaddingWidth)
            overrideCompoundPaddingLeft = compoundPaddingLeft - inset
            overrideCompoundPaddingRight = compoundPaddingRight + inset
            overrideCompoundPadding = true
        }
        super.onDraw(canvas)
        overrideCompoundPadding = false
    }

    override fun getCompoundPaddingLeft(): Int {
        return if (overrideCompoundPadding) overrideCompoundPaddingLeft
        else super.getCompoundPaddingLeft()
    }

    override fun getCompoundPaddingRight(): Int {
        return if (overrideCompoundPadding) overrideCompoundPaddingRight
        else super.getCompoundPaddingRight()
    }

    private fun measureTextWidth(text: String?): Int {
        return ceil(paint.measureText(text).toDouble()).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun configureButton() {
        val paddingVertical = 20
        val paddingHorizontal = 30
        text = textButton
        gravity = Gravity.CENTER
        background =
            ContextCompat.getDrawable(context, R.drawable.com_ditr_ekyc_login_default_style_login)
        setTextColor(Color.WHITE)
        setPadding(paddingVertical, paddingHorizontal, paddingVertical, paddingHorizontal)
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(
                context,
                R.drawable.com_ditr_ekyc_login_ic_face
            ),
            null, null, null,
        )
    }

    override fun setOnClickListener(l: OnClickListener?) {
        externalOnClickListener = l
    }

    // customize onclick to the ekyc button
    private fun setupOnClickListener(context: FragmentActivity) {
        // set the listener on super so that consumers can set another listener that this will
        // forward to
        super.setOnClickListener {
            try {
                // show loading alert after the button was click
                showAlert()

                // get app information
                getAppInfo = GetAppInfo(context)

                // generate verify and challenge code
                codeVerifier = GenerateCode.codeVerifier()
                codeChallenge = GenerateCode.codeChallenge(codeVerifier)

                // intent to our ekyc application
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getAppInfo.getUri(codeChallenge))
                )
                launcher.launch(intent)
            } catch (ignored: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.resources.getString(R.string.com_ditr_ekyc_link_download_app))
                    )
                )
                context.finish()
            } catch (error: Exception) {
                popupWindows.dismiss()
                resultCallback?.onError(error)
            }
        }
    }

    // set up a launcher to launch ekyc application
    private fun setupLauncher() {
        launcher = (context as FragmentActivity).registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            getLoginMethodHandlerCallback()
        )
    }

    // get result after back from ekyc application
    private fun getLoginMethodHandlerCallback(): (ActivityResult) -> Unit =
        { result: ActivityResult ->
            popupWindows.dismiss()
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data == null){
                    throw Exception(StringConstants.ERROR_CANNOT_GET_DATA)
                }
                val token = result.data!!.getStringExtra(StringConstants.TOKEN)
                resultCallback?.onSuccess(EkycLoginResult(token!!, codeVerifier))
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                resultCallback?.onCancel()
            }
        }

    // setup alert
    @SuppressLint("InflateParams")
    private fun setLoadingAlert(context: FragmentActivity) {
        popupView = context.layoutInflater.inflate(
            R.layout.com_ditr_ekyc_login_loading_layout, null, false
        ) as RelativeLayout
        popupWindows = PopupWindow(context)
        popupWindows.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.com_ditr_ekyc_login_popup
            )
        )
        popupWindows.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindows.height = WindowManager.LayoutParams.MATCH_PARENT
        popupWindows.contentView = popupView
    }

    // show alert when user click the button
    private fun showAlert() {
        popupWindows.showAtLocation(this, Gravity.CENTER, 0, 0)
    }

    // register callback function to the ekyc button to get result back after click the button
    fun registerCallback(callback: EkycLoginCallback) {
        this.resultCallback = callback
    }
}