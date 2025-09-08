package com.iamnaran.firefly.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.app.ShareCompat
import com.iamnaran.firefly.R

class Utilities {

    fun openAppSetting(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


    // Window utils
    @Composable
    fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

    @Composable
    fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

    private tailrec fun Context.getActivityWindow(): Window? =
        when (this) {
            is Activity -> window
            is ContextWrapper -> baseContext.getActivityWindow()
            else -> null
        }
}