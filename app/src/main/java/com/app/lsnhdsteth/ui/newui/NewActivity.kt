package com.app.lsnhdsteth.ui.newui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.lsnhdsteth.databinding.ActivityNewBinding
import com.app.lsnhdsteth.model.ResponseCheckDeviceData
import com.app.lsnhdsteth.network.NetworkConnectivity
import com.app.lsnhdsteth.network.Status
import com.app.lsnhdsteth.network.isConnected
import com.app.lsnhdsteth.ui.DialogView
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.app.lsnhdsteth.utils.Utility
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewActivity : AppCompatActivity() {

    var TAG = "NewActivity : "

    lateinit var binding: ActivityNewBinding
    lateinit var viewModel: NewViewModel

    var dialog : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initXml()
//        initObserver()
//        checkStoragePermission()
    }

    // init Xml
    private fun initXml() {
        // remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // screen always on mode
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // init binding
        binding = ActivityNewBinding.inflate(layoutInflater)
        // init viewmodel
        viewModel = ViewModelProvider(this).get(NewViewModel::class.java)
        setContentView(binding.root)

        viewModel.internet = isConnected
        viewModel.connectionLiveData = NetworkConnectivity(application)
        viewModel.pref = MySharePrefernce(this)

        viewModel.device_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)+"123"

    }

    // check permissions
    private fun checkStoragePermission() {
        // check phone state and storage permission
        if(Utility.checkPermissionGranted(this)){
            Utility.showToast(this,"permission granted")
            checkDeviceversion()
            @RequiresApi(Build.VERSION_CODES.R)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var hasPermission = hasAllFilesPermission()
                if (!hasPermission){
                    takePermissions()
                }
            }
        }else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Utility.STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun takePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(java.lang.String.format("Package:%s",getPackageName()))
                startActivityForResult(intent, 100)
            } catch (exception: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 101)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"
                ),
                101
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasAllFilesPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        return false
    }

    private fun initObserver() {

        if(this.isConnected)binding.ivNoInternet.visibility = View.GONE
        else binding.ivNoInternet.visibility = View.VISIBLE

        // device registred API observer
        viewModel.device_register_api_result.observe(this, Observer { response ->
            if (response.status == Status.SUCCESS) {
                var device_obj = Gson().fromJson(response.data, ResponseCheckDeviceData::class.java)
                if (device_obj.desc.equals("device not found")) {
                    viewModel.is_device_registered = false
                    Utility.showToast(this,"No Device Available")
                    if(dialog==null) dialog = DialogView.showNotRegisterDialog(this,viewModel.device_id)
                    if (!dialog!!.isShowing) dialog?.show()
                } else {
                    viewModel.is_device_registered = true
                    viewModel.submitDeviceInfo(this)
                    Utility.showToast(this,"Device Available")
                    if (dialog!=null && dialog!!.isShowing){
                        dialog?.dismiss()
                    }
                    // check version change
                    var devcie_api_version = device_obj.desc.toInt()
                    var content_api_version = viewModel.pref?.getVersionOfConytentAPI()
                    if (devcie_api_version != content_api_version) {
                        viewModel.fetchContentData()
                    }

                }
            } else viewModel.is_device_registered = false
        })

        // content result API observer
        viewModel.content_api_result.observe(this, Observer { response ->
            if (response.status == Status.SUCCESS) {
                try {
                    viewModel.pref?.setLocalStorage(response.data!!)
                    startPlayingContent()
                } catch (ex: Exception) {
                    Log.d(TAG, "initObserver: $ex")
                }
            }
        })
    }

    private fun startPlayingContent() {

    }

    // check device version
    private fun checkDeviceversion() {
        viewModel.isDeviceRegistered()
        Utility.showToast(this,"check version api  calling")
        CoroutineScope(IO).launch {
            delay(30000)
            CoroutineScope(Main).launch {
                checkDeviceversion()
            }
        }.cancel()
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Utility.STORAGE_PERMISSION_CODE && grantResults.size == 2){
            Utility.showToast(this,"permission granted callback")
            checkDeviceversion()
        }
    }

}