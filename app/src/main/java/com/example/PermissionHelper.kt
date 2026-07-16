package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build

object PermissionHelper {
    /**
     * Checks if the SYSTEM_ALERT_WINDOW permission is granted.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Creates an Intent to launch the overlay permission settings page for this app.
     */
    fun getOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
}
