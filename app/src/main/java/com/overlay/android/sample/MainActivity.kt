package com.overlay.android.sample

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobile.aibuysdk.SdkLifecycleInitializer
import com.mobile.aibuysdk.view.overlayview.OverlayView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SdkLifecycleInitializer.initialize(this)
        val uidFromExtra = intent.getStringExtra(EXTRA_UID)
        if (UidContainer.uid.isBlank() && uidFromExtra.isNullOrBlank()) {
            finish()
        } else if (savedInstanceState == null || UidContainer.new) {
            if (uidFromExtra.isNullOrBlank().not()) {
                UidContainer.uid = uidFromExtra.orEmpty()
            }
            UidContainer.new = false
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ContentFragment.newInstance()).commit()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        findViewById<OverlayView>(R.id.overlay).onUserInteraction()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (UidContainer.uid.isBlank()) {
            startActivity(Intent(this, StartActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SdkLifecycleInitializer.uninitializeActivityResources()
    }

    companion object {
        const val EXTRA_UID = "extra_uid"
    }
}