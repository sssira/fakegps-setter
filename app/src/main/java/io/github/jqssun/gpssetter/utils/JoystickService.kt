package io.github.jqssun.gpssetter.utils

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.jqssun.gpssetter.R
import kotlin.math.cos
import kotlin.math.sin
import android.content.BroadcastReceiver
import android.content.IntentFilter

class JoystickService : Service(),View.OnTouchListener,View.OnClickListener {

    private var wm: WindowManager? = null
    private var mJoystickContainerView: View? = null
    private var mJoystickView: JoystickView? = null
    private var mJoystickLayoutParams: WindowManager.LayoutParams? = null
    private var lat : Double = PrefManager.getLat
    private var lon : Double = PrefManager.getLng

	private val stopReceiver = object : BroadcastReceiver() {
	    override fun onReceive(context: Context, intent: Intent) {
	        if (intent.action == "com.sssira.fakegps.STOP_SIGNAL") {
	            // Pake PrefManager bawaan repo lo
	            if (PrefManager.get().getBoolean("auto_kill_grab", false)) {
	                stopSelf()
	            }
	        }
	    }
	}
	

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        // Register receiver untuk dengerin sinyal dari Xposed
        val filter = android.content.IntentFilter("com.sssira.fakegps.STOP_SIGNAL")
        registerReceiver(stopReceiver, filter)
        wm =  getSystemService(WINDOW_SERVICE) as WindowManager
        val mInflater :LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mJoystickContainerView = mInflater.inflate(R.layout.joystick, null as ViewGroup?) as View
        mJoystickView = mJoystickContainerView!!.findViewById(R.id.joystickView_right)
        mJoystickView?.setOnTouchListener { v, event ->
            if (event.action == 1){
                try {
                    lat = PrefManager.getLat
                    lon = PrefManager.getLng
                    updateLocation(lat, lon)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            false
        }
        // mJoystickView?.setOnMoveListener { angle, strength ->
        mJoystickView?.setOnMoveListener { angle, strength, event ->
            val radians = Math.toRadians(angle.toDouble())
            try {
                val factorX: Double = cos(radians) / 100000.0 * (strength / 30)
                val factorY: Double = sin(radians) / 100000.0 * (strength / 30)
                lon = PrefManager.getLng + factorX
                lat = PrefManager.getLat + factorY
                updateLocation(lat, lon)

            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        mJoystickLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        mJoystickLayoutParams?.let {
            it.gravity = Gravity.LEFT
        }

        wm!!.addView(mJoystickContainerView,mJoystickLayoutParams)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
    	// Jangan lupa di unregister biar gak memory leak
 		try {
    	    unregisterReceiver(stopReceiver)
    	} catch (e: Exception) {}
    	
    	if (this::mJoystickContainerView.isInitialized && mJoystickContainerView != null) {
    	        wm?.removeView(mJoystickContainerView)
    	        this.mJoystickContainerView = null
    	}
    	    
        super.onDestroy()
        if (this.mJoystickContainerView != null) {
            this.wm!!.removeView(mJoystickContainerView);
            this.mJoystickContainerView = null;
        }
    }

    private fun updateLocation(lat : Double,lon : Double){
        PrefManager.update(start = PrefManager.isStarted, la = lat, ln = lon)

    }

}
