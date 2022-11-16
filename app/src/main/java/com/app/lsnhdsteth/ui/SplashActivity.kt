package com.app.lsnhdsteth.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.lsnhdsteth.R
import com.app.lsnhdsteth.utils.DataManager
import com.app.lsnhdsteth.utils.MySharePrefernce

class SplashActivity : AppCompatActivity(){

    var pref: MySharePrefernce? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        pref = MySharePrefernce(this)
        DataManager.createReportFile(pref)
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}