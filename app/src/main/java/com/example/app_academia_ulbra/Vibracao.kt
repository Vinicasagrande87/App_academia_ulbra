package com.example.app_academia_ulbra

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// vibra o celular pelo tempo informado (VibratorManager só existe a partir do Android 12)
fun vibrar(context: Context, milissegundos: Long) {
    val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrador.hasVibrator()) {
        vibrador.vibrate(VibrationEffect.createOneShot(milissegundos, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
