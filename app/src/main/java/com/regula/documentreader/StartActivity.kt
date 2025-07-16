package com.regula.documentreader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.regula.common.utils.CameraUtil
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.exception.InitException
import android.util.Log

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // Initialisation du FaceSDK
        FaceSDK.Instance().initialize(this) { status: Boolean, exception: InitException? ->
            if (status) {
                Log.d("FaceSDK", "Initialisation réussie")
            } else {
                Log.e("FaceSDK", "Erreur initialisation: ${exception?.message}")
            }
        }

        // Lancement activité suivante selon le device
        if (CameraUtil.isWP7Device()) {
            startActivity(Intent(this@StartActivity, DeviceActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            startActivity(Intent(this@StartActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }


    private fun loadLicense(): ByteArray {
        assets.open("Regula/regula.license").use { inputStream ->
            return inputStream.readBytes()
        }
    }
}
