package com.overlay.android.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.overlay.android.sample.MainActivity.Companion.EXTRA_UID

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val etVideoUid = findViewById<EditText>(R.id.uidVideo)
        findViewById<Button>(R.id.openVideoButton).setOnClickListener {
            UidContainer.uid = etVideoUid.text.toString().replace("\\s".toRegex(), "")
            UidContainer.isRealtime = findViewById<CheckBox>(R.id.realtimeCheckbox).isChecked
            UidContainer.isLinkoutInBrowser = findViewById<CheckBox>(R.id.browserCheckbox).isChecked
            UidContainer.jsonCustomizationURL =
                findViewById<EditText>(R.id.customizationUrl).text.toString().ifBlank { null }
            UidContainer.new = true
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_UID, UidContainer.uid)
            })
        }
    }
}
