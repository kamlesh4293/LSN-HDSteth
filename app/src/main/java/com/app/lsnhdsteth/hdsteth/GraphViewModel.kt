package com.app.lsnhdsteth.hdsteth

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.app.lsnhdsteth.model.ResponseJsonData
import com.app.lsnhdsteth.network.ApiResponse
import com.app.lsnhdsteth.network.Status
import com.app.lsnhdsteth.utils.Constant
import com.app.lsnhdsteth.utils.DataManager.Companion.zipFolder
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.app.lsnhdsteth.utils.Utility.Companion.getCurrentdate
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File


class GraphViewModel : ViewModel() {

    var TAG = "GraphViewModel"

    // 1. upload zip file
    private val _upload_file_data = MutableLiveData<ApiResponse>()
    val _upload_file_api_result : LiveData<ApiResponse> get() = _upload_file_data

    // 2. fetch records
    private val _record_data = MutableLiveData<ApiResponse>()
    val _record_api_result : LiveData<ApiResponse> get() = _record_data

    // 3. send E-mail
    private val _send_email_data = MutableLiveData<ApiResponse>()
    val _email_api_result : LiveData<ApiResponse> get() = _send_email_data


    // 1. upload zip file
    fun uploadZipFile(pref: MySharePrefernce,path: String,dr_name:String, online_report: Boolean) {
        var zipfile:File? = null
        if(online_report){
            var file_path = File(path).parentFile.parentFile.absolutePath
            val file = File(file_path)
            zipfile = zipFolder(file)
        }else{
            zipfile = File(path)
            Log.d("TAG", "uploadZipFile: ${zipfile?.absolutePath}")
            Log.d("TAG", "uploadZipFileName: ${zipfile?.name}")
        }

        val response: String = pref.getJsonData()
        val did: String = pref.getStringData(MySharePrefernce.KEY_DEVICE_ID)
        val mac: String = pref.getStringData(MySharePrefernce.KEY_MAC_ID)
        val server_id: String = pref.getStringData(MySharePrefernce.KEY_SERVER_ID)
        val widget_id: String = pref.getStringData(MySharePrefernce.KEY_WIDGET_ID)
        val (device) = Gson().fromJson(response, ResponseJsonData::class.java)


        if(online_report){
            AndroidNetworking.upload(Constant.API_UPLOAD_ZIP_FILE)
                .addMultipartFile("file", zipfile)
                .addMultipartParameter("dId",device[0].id.toString())
                .addMultipartParameter("dateTime", getCurrentdate().replace("T", " "))
                .addMultipartParameter("mac", device[0].mac)
                .addMultipartParameter("dr", dr_name)
                .addMultipartParameter("widgetId", widget_id)
                .addMultipartParameter("serverId", server_id)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    // do anything with progress
                }
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        // do anything with response
                        var res = response.toString()
                        Log.d("onResponse", response.toString())
                        if(online_report){
                            var file_path = File(path).parentFile.parentFile.absolutePath
                            if(File(file_path).exists()) File(file_path).delete()
                        }
                        if(zipfile!!.exists())zipfile.delete()
                        if(online_report)_upload_file_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                    }

                    override fun onError(error: ANError) {
                        var error = error.toString()
                        Log.d("onError", error.toString())
                        // handle error
                        _upload_file_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                    }
                })
        }else{
            var filename = zipfile?.name?.replace(".zip","")
            var date_time = filename?.replace("_",":")?.replace("T"," ")

            AndroidNetworking.upload(Constant.API_UPLOAD_ZIP_FILE)
                .addMultipartFile("file", zipfile)
                .addMultipartParameter("dateTime", date_time)
                .addMultipartParameter("dr", dr_name)
                .addMultipartParameter("mac", device[0].mac)
                .addMultipartParameter("dId",device[0].id.toString())
                .addMultipartParameter("widgetId", widget_id)
                .addMultipartParameter("serverId", server_id)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    // do anything with progress
                }
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        // do anything with response
                        var res = response.toString()
                        Log.d("onResponse", response.toString())
                        if(online_report){
                            var file_path = File(path).parentFile.parentFile.absolutePath
                            if(File(file_path).exists()) File(file_path).delete()
                        }
                        if(zipfile!!.exists())zipfile.delete()
                        if(online_report)_upload_file_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                    }

                    override fun onError(error: ANError) {
                        var error = error.toString()
                        Log.d("onError", error.toString())
                        // handle error
                        _upload_file_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                    }
                })
        }
    }

    // 2. fetch Records
    fun fetchRecords(id: String,date: String,widget_id: String) {
        Log.d(TAG, "fetchRecords: ${Constant.API_RECORD_LIST + "$id/$date/$widget_id"}")
        AndroidNetworking.get(Constant.API_RECORD_LIST+"$id/$date/$widget_id")
            .setTag("uploadTest")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("onResponse", response.toString())
                    _record_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                }

                override fun onError(error: ANError) {
                    Log.d("onError", error.toString())
                    // handle error
                    _record_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                }
            })
    }

    // 3. send e-mail
    fun sendEmail(id: String,email: String,pref: MySharePrefernce) {

        val did: String = pref.getStringData(MySharePrefernce.KEY_DEVICE_ID)
        val mac: String = pref.getStringData(MySharePrefernce.KEY_MAC_ID)
        val server_id: String = pref.getStringData(MySharePrefernce.KEY_SERVER_ID)
        val widget_id: String = pref.getStringData(MySharePrefernce.KEY_WIDGET_ID)

        Log.d(TAG,
            "sendEmail: upload Data - email -" +
                    " $email, id - $id, mac - $mac, did - $did, widget - $widget_id, server - $server_id"
        )

        AndroidNetworking.post(Constant.API_SEND_EMAIL)
            .addBodyParameter("email",email)
            .addBodyParameter("id",id)
            .addBodyParameter("mac", mac)
            .addBodyParameter("dId",did)
            .addBodyParameter("widgetId", widget_id)
            .addBodyParameter("serverId", server_id)
            .setTag("uploadTest")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("onResponse", response.toString())
                    _send_email_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                }

                override fun onError(error: ANError) {
                    Log.d("onError", error.toString())
                    // handle error
                    _send_email_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                }
            })
    }

    // 4. send advert click
    fun sendAdvertClick(dev_id: String,adv_id: String,target_key: String,target_id: String,pref: MySharePrefernce) {
        val mac: String = pref.getStringData(MySharePrefernce.KEY_MAC_ID)
        val widget_id: String = pref.getStringData(MySharePrefernce.KEY_WIDGET_ID)
        val server_id: String = pref.getStringData(MySharePrefernce.KEY_SERVER_ID)

        Log.d(TAG, "sendAdvertClick: $dev_id, $adv_id, $target_id")
        AndroidNetworking.post(Constant.API_SEND_ADVERT_CLICK)
            .addBodyParameter("dId",dev_id)
            .addBodyParameter("advtId",adv_id)
            .addBodyParameter(target_key,target_id)
            .addBodyParameter("mac", mac)
            .addBodyParameter("widgetId", widget_id)
            .addBodyParameter("serverId", server_id)
            .setTag("uploadTest")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("advert click onResponse", response.toString())
//                    _send_email_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                }

                override fun onError(error: ANError) {
                    Log.d("advert click onError", error.toString())
                    // handle error
//                    _send_email_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                }
            })
    }

}