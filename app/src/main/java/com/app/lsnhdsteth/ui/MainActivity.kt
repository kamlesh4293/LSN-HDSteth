package com.app.lsnhdsteth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.*
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.lsnhdsteth.R
import com.app.lsnhdsteth.network.Status
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import android.widget.TextView.BufferType
import android.view.KeyEvent
import com.app.lsnhdsteth.utils.*
import android.graphics.Bitmap
import android.graphics.Color
import java.io.*
import android.widget.RelativeLayout
import com.androidnetworking.AndroidNetworking
import eu.bolt.screenshotty.Screenshot
import eu.bolt.screenshotty.util.ScreenshotFileSaver
import android.media.MediaPlayer.OnPreparedListener
import android.provider.MediaStore
import android.database.Cursor
import android.media.MediaMetadataRetriever
import androidx.core.app.ActivityCompat
import android.os.BatteryManager
import java.util.*
import android.webkit.WebView
import androidx.core.content.FileProvider
import com.app.lsnhdsteth.network.isConnected
import org.json.JSONObject
import android.os.Bundle
import android.webkit.WebViewClient
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.androidnetworking.interfaces.DownloadProgressListener
import com.app.lsnhdsteth.model.*
import com.test.RetrofitClient
import android.text.format.DateFormat
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.app.lsnhdsteth.databinding.ActivityMainBinding
import com.app.lsnhdsteth.hdsteth.MainGraphActivity
import com.app.lsnhdsteth.network.NetworkConnectivity
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    var TAG = "MultiFrameMainActivity";

    // view list
    var layout_list : MutableList<LayoutView> = mutableListOf()
    var screen_layout_list : MutableList<LayoutView> = mutableListOf()

    //    var items: List<Item>? = null
    var multiframe_items: MutableList<MutableList<Item>> = mutableListOf()
    var items: MutableList<Item> = mutableListOf()


    var item_size = 0

    var current_size_list : MutableList<Int> = mutableListOf()
    var device_id = ""

    // downloading count
    var downloading = 0


    // share prefernce
    var pref: MySharePrefernce? = null


    // dialog
    var dialog: Dialog? = null

    // view  binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    // live data
    lateinit var connectionLiveData: NetworkConnectivity

    // for register device
    var handler: Handler = Handler()
    var runnable: Runnable? = null

    // for temperature
    var temp_handler: Handler = Handler()
    var temp_runnable: Runnable? = null

    // for screenshot
    var screenshot_handler: Handler = Handler()
    var screenshot_runnable: Runnable? = null

    var from_internet = false
    var from_background = false

    var temp = 0

    var downloable_file : List<Downloadable>? = null
    var delete_enabled = false

    // added
    var frame_clickable = false
    var advt = ""
    var dr_name = ""


    private val broadcastreceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        from_background = false
        initXml()
        initObserver()
        checkDeviceVersion()

        // broadcast reciver for temp
        var intentfilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(broadcastreceiver,intentfilter)

        binding.rlBackground.setOnClickListener { openGraphView() }
        binding.rootLayout.setOnClickListener { openGraphView() }
        binding.mainLayout.setOnClickListener { openGraphView() }
    }

    fun openGraphView(){
        if(frame_clickable)startActivity(Intent(this,MainGraphActivity::class.java)
            .putExtra("advt",advt)
            .putExtra("dr",dr_name)
        )
    }

    override fun onStop() {
        super.onStop()
        from_background = true
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ from_internet = true }, 2000)
        if(from_background){
            refreshPage(Constant.REFRESH_FROM_BACKGROUND)
        }

        // is device registered
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, viewModel.delay.toLong())
            checkDeviceVersion()
        }.also { runnable = it }, viewModel.delay.toLong())

        // temp handler
        temp_handler.postDelayed(Runnable {
            temp_handler.postDelayed(temp_runnable!!, viewModel.temp_delay.toLong())
            viewModel.updateTempratureData(temp.toString())
        }.also { temp_runnable = it }, viewModel.temp_delay.toLong())


        // screen shot
        screenshot_handler.postDelayed(Runnable {
            screenshot_handler.postDelayed(
                screenshot_runnable!!,
                viewModel.screen_delay * 1000.toLong()
            )
            Log.d("secreen handler", "running")
            var response = pref?.getJsonData()
            if(response != null && response != ""){
                var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
                if(data_obj.device[0].screenshotUpload.equals("On") && screen_layout_list != null && screen_layout_list.size>0) {
                    var screen_img_list = mutableListOf<String>()

                    for(i in 0..screen_layout_list.size-1){
                        var layout = layout_list[i]
                        if(layout.imageView?.visibility == View.GONE &&
                            layout.videoView?.visibility == View.GONE &&
                            layout.webView?.visibility == View.GONE ) continue
                        if(layout_list[i].videoView?.visibility == View.VISIBLE){
                            val currentPosition: Int? = layout_list[i].videoView?.getCurrentPosition() //in millisecond
                            val pos = currentPosition?.times(1000) //unit in microsecond
                            val bmFrame = layout_list[i].myMediaMetadataRetriever?.getFrameAtTime(pos!!.toLong())
                            screen_layout_list[i].imageView?.setImageBitmap(bmFrame)
                            continue
                        }
                        var file = screenshot(layout_list[i].relative_layout!!,"Screen_$i"+Utility.getCurrentdate())
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 1
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        options.inJustDecodeBounds = false
                        screen_layout_list[i].imageView?.setImageBitmap(BitmapFactory.decodeFile(file?.absolutePath,options))
                        screen_img_list.add(file?.absolutePath.toString())
                    }
                    binding.screenRootLayout.visibility = View.VISIBLE
                    Handler().postDelayed(
                        Runnable {
                            var file = screenshot(binding.screenRootLayout,"Screen_final_"+Utility.getCurrentdate())
                            screen_img_list.add(file?.absolutePath.toString())
                            if(file != null){
                                viewModel.submitScreenShot(Utility.getScreenshotJson(device_id,Utility.getFileToByte(file.absolutePath)))
                                if(screen_img_list!=null && screen_img_list?.size>0){
                                    for (i in 0..screen_img_list.size-1){
                                        if(File(screen_img_list[i]).exists())File(screen_img_list[i]).delete()
                                    }
                                }
                            }
                            binding.screenRootLayout.visibility = View.GONE
                        },1000
                    )
                }else{
                    var file = screenshot(binding.mainLayout,"Screen_final_"+Utility.getCurrentdate())
                    if(file != null){
                        viewModel.submitScreenShot(Utility.getScreenshotJson(device_id,Utility.getFileToByte(file.absolutePath)))
                        if(file.exists())file.delete()
                    }
                    binding.screenRootLayout.visibility = View.GONE
                }
            }
        }.also { screenshot_runnable = it }, viewModel.screen_delay * 100.toLong())

    }

    protected fun screenshot(view: RelativeLayout, filename: String): File? {
        val date = Date()
        // Here we are initialising the format of our image name
        val format = DateFormat.format("yyyy-MM-dd_hh:mm:ss", date)
        try {
            // Initialising the directory of storage
//            val dirpath = Environment.getExternalStorageDirectory().toString() + ""
            val dirpath = DataManager.getScreenShotDirectory()
            val file = File(dirpath)
            if (!file.exists()) {
                val mkdir = file.mkdir()
            }
            // File name
            val path = "$dirpath/$filename-$format.jpeg"
            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            val imageurl = File(path)
            val outputStream = FileOutputStream(imageurl)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
            return imageurl
        } catch (io: FileNotFoundException) {
            io.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    protected fun screenshot(view: ConstraintLayout, filename: String): File? {
        val date = Date()
        // Here we are initialising the format of our image name
        val format = DateFormat.format("yyyy-MM-dd_hh:mm:ss", date)
        try {
            // Initialising the directory of storage
//            val dirpath = Environment.getExternalStorageDirectory().toString() + ""
            val dirpath = DataManager.getScreenShotDirectory()
            val file = File(dirpath)
            if (!file.exists()) {
                val mkdir = file.mkdir()
            }
            // File name
            val path = "$dirpath/$filename-$format.jpeg"
            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            val imageurl = File(path)
            val outputStream = FileOutputStream(imageurl)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
            return imageurl
        } catch (io: FileNotFoundException) {
            io.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun stopPlayingVideoForScreen() {
        for (i in 0..layout_list.size-1){
            if(layout_list[i].videoView?.visibility == View.VISIBLE){
                val currentPosition: Int? = layout_list[i].videoView?.getCurrentPosition() //in millisecond
                val pos = currentPosition?.times(1000) //unit in microsecond
                val bmFrame = layout_list[i].myMediaMetadataRetriever?.getFrameAtTime(pos!!.toLong())
                layout_list[i].video_imageView?.visibility = View.VISIBLE
                layout_list[i].video_imageView?.setImageBitmap(bmFrame)
            }
        }
    }

    private fun resumPlayingVideoAfterScreen() {
        for (i in 0..layout_list.size-1){
            if(layout_list[i].video_imageView?.visibility == View.VISIBLE){
                layout_list[i].video_imageView?.visibility = View.GONE
            }
        }
    }

    fun writeToFile(screenshot: Screenshot): File {
        val fileSaver = ScreenshotFileSaver.create(Bitmap.CompressFormat.PNG)
        val targetFile = File(filesDir, "screenshot")
        fileSaver.saveToFile(targetFile, screenshot)
        return targetFile
    }

    private fun initXml() {
        // remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

//        window.decorView.systemUiVisibility = flags
        // screen always on mode
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContentView(binding.root)
        Utility.checkAllPermissionGranted(this)
        @RequiresApi(Build.VERSION_CODES.R)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var d=hasAllFilesPermission()
            Log.d("TAGXZ", d.toString())
            if (!d){
                takePermissions()
            }
        }

        // initialize netwrok lib
        AndroidNetworking.initialize(getApplicationContext())
        // live data initiate
        connectionLiveData = NetworkConnectivity(application)
        device_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//        device_id = "91581699f87d1663"
        viewModel.internet = isConnected
        viewModel.device_id = device_id


        // share preference
        pref = MySharePrefernce(this)
        // set screenshot interval
        if (pref?.getIntData(MySharePrefernce.KEY_SCREENSHOT_INTERVAL)!! != 0)
            viewModel.screen_delay = pref?.getIntData(MySharePrefernce.KEY_SCREENSHOT_INTERVAL)!!

        //refresh
        startApplication()
    }

    private fun startApplication() {
        var pref_from = pref?.checkRefreshData()
        if(pref_from != null && pref_from.equals("")){
            loadWaiting()
        }else if (pref_from != null && pref_from.equals(Constant.REFRESH_FROM_CONTENT)) {
            setJsonDataForPlaying()
        } else if (pref_from != null && pref_from.equals(Constant.REFRESH_FROM_NODEVICE)) {
            showNotRegisterDialog()
        } else if (pref_from != null && pref_from.equals(Constant.REFRESH_FROM_WAITING)) {
            loadWaiting()
        }else if(pref_from != null && pref_from.equals(Constant.REFRESH_FROM_CHANGE_INTERNET)){
            setJsonDataForPlaying()
        }else if(pref_from != null && pref_from.equals(Constant.REFRESH_FROM_BACKGROUND)){
            setJsonDataForPlaying()
        }
        delete_enabled = true
    }

    private fun initObserver() {

        if(!this.isConnected)internetOffON(false)

        // internet observer
        connectionLiveData.observe(this, Observer { isInternet ->
            viewModel.internet = isInternet
                internetOffON(isInternet)
            if(from_internet) {
                refreshPage(Constant.REFRESH_FROM_CHANGE_INTERNET)
            }
        })


        // device registred observer
        viewModel.device_register_api_result.observe(this, Observer { response ->
            if (response.status == Status.SUCCESS) {
                var device_obj = Gson().fromJson(response.data, ResponseCheckDeviceData::class.java)
                if (device_obj.desc.equals("device not found")) {
                    refreshPage(Constant.REFRESH_FROM_NODEVICE)
                } else {
                    viewModel.is_device_registered = true
                    viewModel.submitRecords(device_id,pref?.getStoreReportdata()!!,pref)
                    pref?.putVersionFromDeviceAPI(device_obj.desc.toInt())
                    hideDialog()
                    var device_version = pref?.getVersionOfDeviceAPI()
                    var content_version = pref?.getVersionOfConytentAPI()
                    if (device_version != content_version) {
                        viewModel.fetchContentData()
                    }
                }
            } else viewModel.is_device_registered = false

            if (response.status == Status.ERROR) {
                showSnackBar(getString(R.string.error_msg))
            }
            viewModel.submitDeviceInfo(this)
        })

        // content result observer
        viewModel.content_api_result.observe(this, Observer { response ->
            if (response.status == Status.SUCCESS) {
                try {
                    pref?.setLocalStorage(response.data!!)
                    setJsonDatawithMultiFrame()
                } catch (ex: Exception) {
                    Log.d(TAG, "initObserver: ${ex.toString()}")
                    refreshPage(Constant.REFRESH_FROM_WAITING)
                }
            }
        }
        )


        // submit device info
        viewModel.devcieinfo_api_result.observe(this, Observer { response ->

            if(response.status==Status.SUCCESS) viewModel.is_deviceinfo_submitted = true
//                showSnackBar("Device info Result - ${response.status}")
//                Log.d("device_info_result", response.message)
        })

        // submit temp
        viewModel.temprature_api_result.observe(this, Observer { response ->
//            showSnackBar("Device temp Result - ${response.status}")
        })

        // submit screenshot
        viewModel.screenshot_api_result.observe(this, Observer { response ->
        })

    }

    fun loadWaiting() {
        if(layout_list!=null &&layout_list.size>0){
            for (i in 0..layout_list.size-1)
                layout_list[i].relative_layout?.visibility = View.GONE
        }
        binding.rlBackground.visibility = View.VISIBLE
    }


    private fun setJsonDatawithMultiFrame() {

        var response = pref?.getJsonData()
        var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
        if(isConnected)startDowbloading1(data_obj)
        if (data_obj.device[0].screenshotUploadInterval != null) {
//            val am = getSystemService(ALARM_SERVICE) as AlarmManager
//            am.setTimeZone(data_obj.device[0].timeZone)

            pref?.putIntData(
                MySharePrefernce.KEY_SCREENSHOT_INTERVAL,
                data_obj.device[0].screenshotUploadInterval
            )
            if (data_obj.device[0].screenshotUploadInterval != null && data_obj.device[0].screenshotUploadInterval != 0)
                viewModel.screen_delay = data_obj.device[0].screenshotUploadInterval
        }

//        inactive_items = DataParsing.getInactiveItems(data_obj)
        var layout = data_obj.layout.get(0)
        if (layout.frame != null && layout.frame?.size > 0) {
            var frame = layout.frame
            for (i in 0..frame.size - 1) {
                if (frame.get(i).item != null && frame.get(i).item.size > 0) {
                    var items_array = frame.get(i).item
                    for (j in 0..items_array.size - 1) {
                        items.add(items_array[j])
                        item_size = item_size + 1
                    }
                }
            }
            Log.d(TAG, "setJsonDatawithMultiFrame: itemsize- ${items.size}")
            if (dialog != null && dialog?.isShowing == true) {
                dialog?.dismiss()
            }
            if(downloading==0)
                refreshPage(Constant.REFRESH_FROM_CONTENT)
        }else{
            refreshPage(Constant.REFRESH_FROM_WAITING)
        }
    }

    // set json data for playing
    private fun setJsonDataForPlaying() {
        var response = pref?.getJsonData()
        if (response != null && !response.equals("")) {
            var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
            downloable_file = data_obj.downloadable

            if(data_obj.layout == null) return
            var layout = data_obj.layout.get(0)
            if (layout.frame != null && layout.frame?.size > 0) {
                var list_frame = Utility.getFilterFrameList(layout.frame)
                for (i in 0..list_frame.size - 1) {
                    Log.d(TAG, "setJsonDataForPlaying: ZZZ: ${list_frame.get(i).z}")
                    // add create relative layout
                    var rl = RelativeLayout(this)

                    val params = RelativeLayout.LayoutParams(list_frame.get(i).w,list_frame.get(i).h)
                    rl.layoutParams = params
                    rl.x = list_frame.get(i).x.toFloat()
                    rl.y = list_frame.get(i).y.toFloat()


                    var screen_rl = RelativeLayout(this)
                    screen_rl.layoutParams = params
                    screen_rl.x = list_frame.get(i).x.toFloat()
                    screen_rl.y = list_frame.get(i).y.toFloat()

                    if(list_frame.get(i).bg.equals(""))rl.setBackgroundColor(Color.TRANSPARENT)
                    else rl.setBackgroundColor(Color.parseColor(list_frame.get(i).bg))


                    var video = VideoView(this)
                    video.layoutParams = params
                    video.visibility = View.GONE

                    var image = ImageView(this)
                    image.layoutParams = params
                    image.visibility = View.GONE

                    var video_image = ImageView(this)
                    image.layoutParams = params
                    image.visibility = View.GONE

                    var myMediaMetadataRetriever = MediaMetadataRetriever()


                    var web = WebView(this)
                    web.layoutParams = params
                    web.getSettings().setJavaScriptEnabled(true)
                    web.setWebViewClient(WebViewClient())
                    web.visibility = View.GONE

                    rl.addView(video)
                    rl.addView(image)
                    rl.addView(web)
                    rl.addView(video_image)

                    // screenshot frame
                    var screen_image = ImageView(this)
                    screen_image.layoutParams = params


                    screen_rl.addView(screen_image)

                    layout_list.add(LayoutView(rl,image,video_image,video,web,myMediaMetadataRetriever))
                    screen_layout_list.add(LayoutView(screen_rl,screen_image,null,null,null,null))
                    binding.rootLayout.addView(rl)
                    binding.screenRootLayout.addView(screen_rl)

                    var layout = data_obj.layout.get(0)
                    if (layout.frame != null && layout.frame?.size > 0) {
                        var frame = layout.frame
                        for (i in 0..frame.size - 1) {
                            if (frame.get(i).item != null && frame.get(i).item.size > 0) {
                                var items_array = frame.get(i).item
                                for (j in 0..items_array.size - 1) {
                                    items.add(items_array[j])
                                    item_size = item_size + 1
                                    if(items_array[j].type.equals("HDSteth")){
                                        frame_clickable =true
                                        advt = Gson().toJson(items_array[j])
                                        dr_name = items_array[j].dr
                                    }
                                }
                            }
                        }
                        if (dialog != null && dialog?.isShowing == true) {
                            dialog?.dismiss()
                        }
                    }else{
                        refreshPage(Constant.REFRESH_FROM_WAITING)
                    }



                    if (list_frame.get(i).item != null && list_frame.get(i).item.size > 0) {

                        var child_items: MutableList<Item> = mutableListOf()

                        var items_array = list_frame.get(i).item
                        for (j in 0..items_array.size - 1) {
                            items.add(items_array[j])
                            items.get(items.size-1).pos = i
                            child_items.add(items_array[j])
                            item_size = item_size + 1
                        }
                        multiframe_items?.add(child_items)
                        current_size_list.add(0)
                    }else {
                        multiframe_items?.add(mutableListOf())
                        current_size_list.add(0)
                    }
                }
                if (data_obj.device[0].screenshotUploadInterval != null) {
                    pref?.putIntData(
                        MySharePrefernce.KEY_SCREENSHOT_INTERVAL,
                        data_obj.device[0].screenshotUploadInterval
                    )
                    if (data_obj.device[0].screenshotUploadInterval != null && data_obj.device[0].screenshotUploadInterval != 0)
                        viewModel.screen_delay = data_obj.device[0].screenshotUploadInterval
                }
                if (dialog != null && dialog?.isShowing == true) {
                    dialog?.dismiss()
                }
                startPlayingContent()
            }else{
                refreshPage(Constant.REFRESH_FROM_WAITING)
            }
        }
    }

    fun startDowbloading1(dataJson: ResponseJsonData) {
        if(dataJson != null && dataJson.downloadable !=null && dataJson.downloadable.size>0){
            for(i in 0..dataJson.downloadable.size-1){
                Log.d(TAG, "startDowbloading1: ${dataJson.downloadable[i]}")
                if(!DataManager.fileIsExist(dataJson.downloadable[i])){
                    Log.d(TAG, "startDowbloading2: ${dataJson.downloadable[i]}")
                    downloadFIle(Constant.BASE_FILE_URL + dataJson.downloadable[i].src,dataJson.downloadable[i].name)
                }
            }
        }
    }

    private fun startPlayingContent() {

        if(multiframe_items != null && multiframe_items.size>0){
            for (i in 0..multiframe_items.size-1){
                if(multiframe_items.get(i) != null && multiframe_items.get(i).size>0){
                    if(current_size_list[i] < multiframe_items.get(i).size){

                        var file = multiframe_items[i].get(current_size_list[i]).fileName
                        val path = DataManager.getDirectory()+File.separator+ file

                        if(multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_HD_STETH){
                            playHdStethContent(multiframe_items[i].get(current_size_list[i]),i,0)
                        }
                        else if(multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_WEB) showWebView(i,multiframe_items.get(i)[current_size_list[i]])
                        else if(!File(path).exists()){
                            current_size_list[i] = current_size_list[i]+1
                            nextPlay(i)
                            return
                        }
                        else if(multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_IMAGE
                            || multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_VECTOR
                            || multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_POWERPOINT
                            || multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_WORD
                        ) loadImage(i)
                        else if(multiframe_items.get(i)[current_size_list[i]].type == Constant.CONTENT_VIDEO) playVideo(i,multiframe_items.get(i).size,multiframe_items[i].get(current_size_list[i]).duration)
                    }
                }
            }
        }
    }


    private fun showWebView(pos: Int, item: Item) {
        binding.rlBackground.visibility = View.GONE
        layout_list[pos].imageView?.visibility = View.GONE
        layout_list[pos].videoView?.visibility = View.GONE
        layout_list[pos].webView?.visibility = View.VISIBLE


        var settings = multiframe_items[pos].get(current_size_list[pos]).settings
        var setting_obj = JSONObject(settings)
        var reload = setting_obj.getString("reloadOpt")
        var reload_interval = setting_obj.getString("reload")
        if(reload.equals("n") || reload.equals("c") && reload_interval.toInt() >= multiframe_items[pos][current_size_list[pos]].duration.toInt()){
            try {
                layout_list[pos].webView?.loadUrl(multiframe_items[pos].get(current_size_list[pos]).src)
//                layout_list[pos].webView?.loadUrl("https://www.facebook.com/")
            } catch (e: java.lang.Exception) {
                Log.w(TAG, "setUpNavigationView", e)
            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(multiframe_items[pos][current_size_list[pos]].duration.toLong()))
                withContext(Dispatchers.Main) {
                    pref?.createReport(
                        multiframe_items[pos].get(current_size_list[pos]).id,
                        multiframe_items[pos].get(current_size_list[pos]).duration
                    )
                    current_size_list[pos] = current_size_list[pos]+1
                    nextPlay(pos)
                }
            }
        }else{
            reloadWebPage(layout_list[pos].webView!!,
                multiframe_items[pos][current_size_list[pos]].duration,
                reload_interval.toDouble(),
                item,pos
            )
        }
    }

    private fun reloadWebPage(webView: WebView,duration:Double,interval :Double,item:Item,pos:Int) {
        try {
            layout_list[pos].webView?.loadUrl(multiframe_items[pos].get(current_size_list[pos]).src)
            layout_list[pos].webView
        } catch (e: java.lang.Exception) {
            Log.w(TAG, "setUpNavigationView", e)
        }
        if(interval<duration){
            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(interval.toLong()))
                withContext(Dispatchers.Main) {
                    reloadWebPage(webView,duration-interval,interval,item,pos)
                }
            }
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(duration.toLong()))
                withContext(Dispatchers.Main) {
                    pref?.createReport(item.id,item.duration)
                    current_size_list[pos] = current_size_list[pos]+1
                    nextPlay(pos)
                }
            }
        }
    }

    private fun nextPlay(pos:Int){

        Log.d(TAG, "nextPlay: $pos ${SimpleDateFormat("hh:mm:ss").format(Date())}")

        if(current_size_list[pos] < multiframe_items.get(pos).size){
            var type = multiframe_items.get(pos)[current_size_list[pos]].type
            var file = multiframe_items[pos].get(current_size_list[pos]).fileName
            val path = DataManager.getDirectory()+File.separator+ file
            if(multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_HD_STETH){
                playHdStethContent(multiframe_items[pos].get(current_size_list[pos]),pos,0)
            }else if(multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_WEB) showWebView(pos,multiframe_items.get(pos)[current_size_list[pos]])
            else if(!File(path).exists()){
                current_size_list[pos] = current_size_list[pos]+1
                nextPlay(pos)
                return
            }else if(multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_IMAGE
                || multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_VECTOR
                || multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_POWERPOINT
                || multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_WORD
            ) loadImage(pos)
            else if(multiframe_items.get(pos)[current_size_list[pos]].type == Constant.CONTENT_VIDEO) playVideo(pos,multiframe_items.get(pos).size,multiframe_items[pos].get(current_size_list[pos]).duration)
        }else{
            current_size_list[pos] = 0
            nextPlay(pos)
        }
    }

    // load image
    private fun loadImage(pos:Int) {

        if(File("/storage/emulated/0/Android/data/com.app.lsnhdsteth/files/LSN-HDSteth/1552642904.2248-driving-shot-of-happy-female-friends-listening-music-and-paying-currency-while-traveling-in-taxi.mp4").exists()){
            Log.d(TAG, "loadImage: Video File Available")
        }else Log.d(TAG, "loadImage: Video File Not Available")

        binding.rlBackground.visibility = View.GONE
        layout_list[pos].imageView?.visibility = View.VISIBLE
        layout_list[pos].videoView?.visibility = View.GONE
        layout_list[pos].webView?.visibility = View.GONE

        var src = multiframe_items[pos].get(current_size_list[pos]).src


//        val file = Utility.getFileName(multiframe_items[pos].get(current_size_list[pos]))
        val file = multiframe_items[pos].get(current_size_list[pos]).fileName

        val path = DataManager.getDirectory()+File.separator+file


        if(Utility.isFileCompleteDownloaded(file,multiframe_items[pos].get(current_size_list[pos]).filesize)){

            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inJustDecodeBounds = false

            layout_list[pos].imageView?.setImageBitmap(BitmapFactory.decodeFile(path,options))
            layout_list[pos].imageView?.visibility = View.VISIBLE
            layout_list[pos].videoView?.visibility = View.GONE

//            Glide.with(applicationContext)
//                .load(BitmapFactory.decodeFile(path,options))
//                .into(layout_list[pos].imageView!!)

            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(multiframe_items[pos][current_size_list[pos]].duration.toLong()))
                withContext(Dispatchers.Main) {
                    pref?.createReport(
                        multiframe_items[pos].get(current_size_list[pos]).id,
                        multiframe_items[pos].get(current_size_list[pos]).duration
                    )
                    current_size_list[pos] = current_size_list[pos]+1
                    layout_list[pos].imageView?.setImageBitmap(null)
                    nextPlay(pos)
                }
            }
        }else{
            current_size_list[pos] = current_size_list[pos]+1
            nextPlay(pos)
        }
    }

    // play local storage video
    private fun playVideo(pos:Int,size: Int,duration: Double) {

        binding.rlBackground.visibility = View.GONE
        layout_list[pos].imageView?.visibility = View.GONE
        layout_list[pos].webView?.visibility = View.GONE
        layout_list[pos].videoView?.visibility = View.VISIBLE

        var item = multiframe_items[pos].get(current_size_list[pos])

        var file = multiframe_items[pos].get(current_size_list[pos]).fileName

        val path = DataManager.getDirectory()+File.separator+ file
        Log.d("file_path- ", path)

        if(Utility.isFileCompleteDownloadedForPlay(file,multiframe_items[pos].get(current_size_list[pos]).filesize,this)){

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                var uri = FileProvider.getUriForFile(this,packageName+".provider",File(path))
                layout_list[pos].myMediaMetadataRetriever!!.setDataSource(this, uri)
            }else{
                try {
                    layout_list[pos].myMediaMetadataRetriever!!.setDataSource(path, HashMap())
                }catch (e :RuntimeException ){
                    var uri = FileProvider.getUriForFile(this,packageName+".provider",File(path))
                    layout_list[pos].myMediaMetadataRetriever!!.setDataSource(this, uri)
                    e.printStackTrace();
                }
            }

        }


        if(Utility.isFileCompleteDownloadedForPlay(file,multiframe_items[pos].get(current_size_list[pos]).filesize,this)){
            Log.d("file_path_exist- ", path)
            layout_list[pos].videoView?.setVideoPath(path)
            layout_list[pos].videoView?.start()
            layout_list[pos].imageView?.visibility = View.GONE
            layout_list[pos].videoView?.visibility = View.VISIBLE


            var mc : MediaController? = MediaController(this)
            mc?.visibility = View.GONE
            layout_list[pos].videoView?.setMediaController(mc)
            var id = item.id
            var sound = item.sound
            if(sound.equals("no")){
                layout_list[pos].videoView?.setOnPreparedListener(
                    OnPreparedListener
                    { mp -> mp.setVolume(0f, 0f) })
            }else{
                layout_list[pos].videoView?.setOnPreparedListener(
                    OnPreparedListener
                    { mp -> mp.setVolume(100f, 100f) })
            }
            // new start
            if(duration<= item.actualDuration) {
                CoroutineScope(Dispatchers.IO).launch {
                    var item = multiframe_items[pos][current_size_list[pos]]
                    delay((duration*1000).toLong())
                    withContext(Dispatchers.Main) {
                        showToast("if playing")
                        pref?.createReport(
                            multiframe_items[pos].get(current_size_list[pos]).id,
                            multiframe_items[pos].get(current_size_list[pos]).duration
                        )
                        if(layout_list[pos].videoView?.isPlaying == true){
                            layout_list[pos].videoView?.stopPlayback()
                            layout_list[pos].videoView?.setVideoURI(null)
                        }
                        mc = null
                        current_size_list[pos] = current_size_list[pos]+1
                        nextPlay(pos)
                    }
                }
            }else{
                CoroutineScope(Dispatchers.IO).launch {
                    var item = multiframe_items[pos][current_size_list[pos]]
                    delay((item.actualDuration*1000).toLong())
                    withContext(Dispatchers.Main) {
                        showToast("else playing")
                        playVideo(pos,size,duration-item.actualDuration.toDouble())
                    }
                }
            }
        }else{
            current_size_list[pos] = current_size_list[pos]+1
            nextPlay(pos)
        }

    }


    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onPause() {
        super.onPause()
        from_internet = false
        handler.removeCallbacks(runnable!!)
        temp_handler.removeCallbacks(temp_runnable!!)
        screenshot_handler.removeCallbacks(screenshot_runnable!!)
    }

    private fun checkDeviceVersion() {
        if(packageManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,getPackageName())==PackageManager.PERMISSION_GRANTED)
            if(packageManager.checkPermission(Manifest.permission.READ_PHONE_STATE,getPackageName())==PackageManager.PERMISSION_GRANTED){
                if(delete_enabled && downloading==0) viewModel.deleteFiles(DataManager.getListDownloadable(downloable_file))
                if(isConnected)
                    if(downloading==0){
                        freeMemory()
                        viewModel.isDeviceRegistered()
                    }
            }
    }


    // dialog
    fun showNotRegisterDialog() {
        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(metrics)
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = Math.sqrt((xInches * xInches + yInches * yInches).toDouble())
        loadWaiting()
        if (dialog == null || dialog?.isShowing == false) {
            dialog = Dialog(this)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setCancelable(false)
            dialog?.setContentView(R.layout.dialog_not_register)
            dialog?.findViewById<TextView>(R.id.tv_dialog_deviceid)?.text = device_id
            dialog?.findViewById<TextView>(R.id.text_dia_desc)?.text =
                if (diagonalInches >= 6.5) "This media player is not registered to the L Squared Hub." // 6.5inch device or bigger
                else "This media player is not registered to the \nL Squared Hub."

            dialog?.findViewById<TextView>(R.id.tv_dialog_detail)
                ?.setText(Utility.getDetailsText(), BufferType.SPANNABLE)
            dialog?.show()
            dialog?.setOnKeyListener { arg0, keyCode, event -> // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog?.dismiss()
                    finish()
                }
                true
            }
        }
    }

    fun hideDialog() {
        if (dialog != null && dialog?.isShowing == true) {
            dialog?.dismiss()
            dialog = null
        }
    }

    // toast & snack bar
    fun showSnackBar(msg: Int) {
        Snackbar.make(
            findViewById<View>(android.R.id.content),
            getString(msg),
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun showSnackBar(msg: String) {
//        Snackbar.make(findViewById<View>(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }

    fun showToast(msg: String) {
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }


    // permission
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Utility.STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    checkDeviceVersion()
                }
            }
            101 ->{
            }
            100 ->{
            }
        }
    }


    fun refreshPage(from: String) {
        if(from.equals(Constant.REFRESH_FROM_CHANGE_INTERNET))pref?.putBooleanData(MySharePrefernce.KEY_REFRESH_INTERNET,false)
        if(from.equals(Constant.REFRESH_FROM_NODEVICE)){
            if(dialog==null){
                pref?.setDataRefresh(from)
                finish()
                startActivity(Intent(this,ContentPlayActivity::class.java))
            }
        }else if(from.equals(Constant.REFRESH_FROM_NODEVICE) && dialog!=null && dialog!!.isShowing){

        }else if(from.equals(Constant.REFRESH_FROM_BACKGROUND)){
            pref?.setDataRefresh(from)
            finish()
            startActivity(Intent(this,ContentPlayActivity::class.java))
        }else if(from.equals(Constant.REFRESH_FROM_CONTENT)){
            pref?.setDataRefresh(from)
            finish()
            startActivity(Intent(this,ContentPlayActivity::class.java))
        }
    }


    fun getFilePathToMediaID(songPath: String, context: Context): Long {
        var id: Long = 0
        val cr = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor: Cursor? = cr.query(uri, projection, "$selection=?", selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        return id
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasAllFilesPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        return false
    }

    //
    private fun takePermissions() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(java.lang.String.format("Package:%s",getPackageName()))
                startActivityForResult(intent, 100)
            } catch (exception: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 100)
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

    // downloading using network lib
    fun downloadFIle(url:String,fileName: String){

        binding.ivDownloading.visibility = View.VISIBLE
        binding.ivDownloading.visibility = View.VISIBLE
        downloading = downloading + 1
        AndroidNetworking.download(url, DataManager.getDirectory(), fileName)
            .setTag("downloadTest")
            .setOkHttpClient(RetrofitClient.getOkhttpClient())
            .setPriority(Priority.MEDIUM)
            .build()
            .setDownloadProgressListener(object : DownloadProgressListener {
                override fun onProgress(bytesDownloaded: Long, totalBytes: Long) {
                    // do anything with progress
                    Log.d("TAG", "onStart: $totalBytes/ $bytesDownloaded")
                    if(!binding.ivDownloading.isVisible){
                        binding.ivDownloading.visibility = View.VISIBLE
                    }
                }
            })
            .startDownload(object : DownloadListener {
                override fun onDownloadComplete() {
                    // do anything after completion
                    Log.d("TAG", "onSuccess: ")
                    downloading = downloading - 1
                    if (downloading == 0) {
                        Log.d("TAG", "all download complete: ")
                        binding.ivDownloading.visibility = View.GONE
                        refreshPage(Constant.REFRESH_FROM_CONTENT)
                    }
                }

                override fun onError(error: ANError?) {
                    // handle error
                    Log.d("TAG", "onError: ${error.toString()}")
                }
            })
    }

    fun freeMemory() {
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
    }

    private fun playHdStethContent(hd_item: Item,pos : Int,hd_pos : Int) {
        if(hd_item.ss != null){
            var conten_list = hd_item.ss
            if(conten_list.size > hd_pos){
                if(conten_list[hd_pos].type == Constant.CONTENT_IMAGE) loadHDStethImage(hd_item,pos,hd_pos)
                if(conten_list[hd_pos].type == Constant.CONTENT_VIDEO) playHDStethVideo(hd_item,pos,hd_pos,conten_list[hd_pos].duration)
            }else{
                current_size_list[pos] = current_size_list[pos]+1
                nextPlay(pos)
            }
        }
    }

    // load image
    private fun loadHDStethImage(hd_item: Item,pos : Int,hd_pos : Int) {

        binding.rlBackground.visibility = View.GONE
        layout_list[pos].imageView?.visibility = View.VISIBLE
        layout_list[pos].videoView?.visibility = View.GONE
        layout_list[pos].webView?.visibility = View.GONE

        var src = hd_item.ss[hd_pos]
        val file = hd_item.ss[hd_pos].fileName

        val path = DataManager.getDirectory()+File.separator+file


        if(Utility.isFileCompleteDownloaded(file,hd_item.ss[hd_pos].filesize)){

            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inJustDecodeBounds = false

            layout_list[pos].imageView?.setImageBitmap(BitmapFactory.decodeFile(path,options))
            layout_list[pos].imageView?.visibility = View.VISIBLE
            layout_list[pos].videoView?.visibility = View.GONE

//            Glide.with(applicationContext)
//                .load(BitmapFactory.decodeFile(path,options))
//                .into(layout_list[pos].imageView!!)

            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(hd_item.ss[hd_pos].duration.toLong()))
                withContext(Dispatchers.Main) {
                    pref?.createReport(hd_item.ss[hd_pos].id,hd_item.ss[hd_pos].duration)
                    playHdStethContent(hd_item,pos,hd_pos+1)
                }
            }
        }else playHdStethContent(hd_item,pos,hd_pos+1)
    }

    // play local storage video
    private fun playHDStethVideo(hd_item: Item,pos : Int,hd_pos : Int,duration: Double) {

        binding.rlBackground.visibility = View.GONE
        layout_list[pos].imageView?.visibility = View.GONE
        layout_list[pos].webView?.visibility = View.GONE
        layout_list[pos].videoView?.visibility = View.VISIBLE

        var item = hd_item.ss[hd_pos]
        var file = hd_item.ss[hd_pos].fileName

        val path = DataManager.getDirectory()+File.separator+ file
        Log.d("file_path- ", path)


        if(Utility.isFileCompleteDownloadedForPlay(file,hd_item.ss[hd_pos].filesize,this)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                var uri = FileProvider.getUriForFile(this,packageName+".provider",File(path))
                layout_list[pos].myMediaMetadataRetriever!!.setDataSource(this, uri)
            }else{
                try {
                    layout_list[pos].myMediaMetadataRetriever!!.setDataSource(path, HashMap())
                }catch (e :RuntimeException ){
                    var uri = FileProvider.getUriForFile(this,packageName+".provider",File(path))
                    layout_list[pos].myMediaMetadataRetriever!!.setDataSource(this, uri)
                    e.printStackTrace();
                }
            }
        }

        if(Utility.isFileCompleteDownloadedForPlay(file,hd_item.ss[hd_pos].filesize,this)){
            Log.d("file_path_exist- ", path)
            layout_list[pos].videoView?.setVideoPath(path)
            layout_list[pos].videoView?.start()
            layout_list[pos].imageView?.visibility = View.GONE
            layout_list[pos].videoView?.visibility = View.VISIBLE


            var mc : MediaController? = MediaController(this)
            mc?.visibility = View.GONE
            layout_list[pos].videoView?.setMediaController(mc)
            // new start
            CoroutineScope(Dispatchers.IO).launch {
                delay((duration*1000).toLong())
                withContext(Dispatchers.Main) {
                    showToast("if playing")
                    pref?.createReport(
                        hd_item.ss[hd_pos].id,
                        hd_item.ss[hd_pos].duration
                    )
                    if(layout_list[pos].videoView?.isPlaying == true){
                        layout_list[pos].videoView?.stopPlayback()
                        layout_list[pos].videoView?.setVideoURI(null)
                    }
                    mc = null
                    playHdStethContent(hd_item,pos,hd_pos+1)
                }
            }
        }else playHdStethContent(hd_item,pos,hd_pos+1)
    }

    fun internetOffON(internet:Boolean){
//        if(internet)Utility.showToast(this,"Internet")
//        else Utility.showToast(this,"No Internet")

        binding.ivDownloading.visibility = View.GONE
        if(internet)binding.ivNoInternet.visibility = View.GONE
        else binding.ivNoInternet.visibility = View.VISIBLE
    }


}
