package com.app.lsnhdsteth.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.app.lsnhdsteth.model.ResponseJsonData
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import android.R.string




class MySharePrefernce(ctx: Context) {

    companion object{
        const val MY_PREFERNCE_NAME = "my_preference"

        const val KEY_JSON_DATA = "json_data"
        const val KEY_VERSION_API_VERSION = "device_version"
        const val KEY_CONTENT_API_VERSION = "content_version"

        const val KEY_CONTENT_REFRESH = "data_refresh"
        const val KEY_SCREENSHOT_INTERVAL = "screen_shot_interval"
        const val KEY_REFRESH_INTERNET = "refresh_internet"
        const val KEY_REPORT_DATA = "report_data"

        const val KEY_IDEAL_TIME = "ideal_time"
        const val KEY_RECORD_TIME = "record_time"
        const val KEY_LANGUAGE = "lanhuage"
        const val KEY_TIME_FORMAT = "time_format"
        const val KEY_CONNECTED_DEVICE_ADDRESS = "connected_devcice_address"


    }

    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    init {
        pref = ctx.getSharedPreferences(MY_PREFERNCE_NAME, Context.MODE_PRIVATE)
        editor = pref?.edit()
    }

    // put string data
    fun putStringData(key:String,value : String){
        editor?.putString(key,value)
        editor?.commit()
    }

    // get int data
    fun getStringData(key: String):String{
        return pref?.getString(key,"")!!
    }

    // put int data
    fun putIntData(key:String,value : Int){
        editor?.putInt(key,value)
        editor?.commit()
    }

    // get int data
    fun getIntData(key: String):Int{
        return pref?.getInt(key,0)!!
    }

    // put boolean data
    fun putBooleanData(key:String,value : Boolean){
        editor?.putBoolean(key,value)
        editor?.commit()
    }

    // get boolean data
    fun getBooleanData(key: String):Boolean{
        return pref?.getBoolean(key,false)!!
    }



    // json data
    fun putJsonData(data : String){
        editor?.putString(KEY_JSON_DATA,data)
        editor?.commit()
    }

    fun getJsonData():String{
        return pref?.getString(KEY_JSON_DATA,"")!!
    }


    // version api version
    fun putVersionFromDeviceAPI(version : Int){
        editor?.putInt(KEY_VERSION_API_VERSION,version)
        editor?.commit()
    }
    fun getVersionOfDeviceAPI():Int{
        return pref?.getInt(KEY_VERSION_API_VERSION,0)!!
    }

    // content api version
    fun putVersionFromContentAPI(version : Int){
        editor?.putInt(KEY_CONTENT_API_VERSION,version)
        editor?.commit()
    }

    fun getVersionOfConytentAPI():Int{
        return pref?.getInt(KEY_CONTENT_API_VERSION,0)!!
    }

    // data refresh
    fun setDataRefresh(isRefresh: String){
        editor?.putString(KEY_CONTENT_REFRESH,isRefresh)
        editor?.commit()
    }

    fun checkRefreshData():String{
        return pref?.getString(KEY_CONTENT_REFRESH,"")!!
    }

    // submit report
    fun storeReportdata(new_data :String){

        Log.d("TAG", "storeReportdata: ${new_data.toString()}")
        var stored_data = pref?.getString(KEY_REPORT_DATA,"")
        if(stored_data.equals(""))editor?.putString(KEY_REPORT_DATA,new_data)
        else {
            var string_builder = StringBuilder()
            string_builder.append(stored_data)
            string_builder.append(new_data)
            editor?.putString(KEY_REPORT_DATA,string_builder.toString())
        }
            editor?.commit()
    }

    // get report data
    fun getStoreReportdata() : String{
        return pref?.getString(KEY_REPORT_DATA,"").toString()
    }

    // clear data
    fun clearReportdata(){
        editor?.putString(KEY_REPORT_DATA,"")
        editor?.commit()
    }


    fun createReport(id:String,duration:Double){
        var report = StringBuilder()

        val parts = id.split("-").toTypedArray()
        report.append("${parts[2]},")

//        var id1 = id.substring(id.lastIndexOf("-"))
//        report.append("${id1.substring(1,id1.length)},")

        report.append("${duration.toInt()}000,")

        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = SimpleDateFormat("hh:mm:ss")
        val todayDate = Date()
        val thisDate: String = currentDate.format(todayDate)
        val thisTime: String = currentTime.format(todayDate)
        report.append("$thisDate")
        report.append("T$thisTime,")
        report.append("${parts[0]},")
        report.append("${parts[1]}\n")
        storeReportdata(report.toString())
    }

    fun setLocalStorage(response: String) {
        if (response != null && !response.equals("")) {
            var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
            if(data_obj.device[0] != null){
                putVersionFromContentAPI(data_obj.device[0].version)
                putJsonData(response)
            }
        }
    }

}