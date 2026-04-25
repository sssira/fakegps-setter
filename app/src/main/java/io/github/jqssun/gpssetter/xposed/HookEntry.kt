package io.github.jqssun.gpssetter.xposed

import android.content.Context
import android.content.Intent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.jqssun.gpssetter.BuildConfig

class HookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        
        // 1. Hook untuk Grab Driver (Auto Kill)
        if (lpparam.packageName == "com.grabtaxi.driver2") {
            try {
                XposedHelpers.findAndHookMethod(
                    "com.grab.driver.vibrator.VibratorController", // Sesuaikan Class ini via JADX jika gagal
                    lpparam.classLoader,
                    "getGrabVibratorMethod",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val context = XposedHelpers.callStaticMethod(
                                XposedHelpers.findClass("android.app.ActivityThread", null),
                                "currentApplication"
                            ) as? Context ?: return

                            val intent = Intent("com.sssira.fakegps.STOP_SIGNAL")
                            intent.setPackage("io.github.jqssun.gpssetter")
                            context.sendBroadcast(intent)
                        }
                    }
                )
            } catch (e: Throwable) {
                // Log jika method tidak ditemukan
            }
        }

        // 2. Hook untuk deteksi module di aplikasi sendiri
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "io.github.jqssun.gpssetter.ui.viewmodel.MainViewModel",
                lpparam.classLoader,
                "updateXposedState",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = null
                    }
                }
            )
        }

        // 3. Jalankan hook bawaan repo (Location Hook)
        LocationHook.initHooks(lpparam)
    }
}
