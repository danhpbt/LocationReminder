package com.udacity.project4.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun isPermissionGranted(context : Context, permission: String): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    context,
                    permission)
    } else {
        true
    }
}