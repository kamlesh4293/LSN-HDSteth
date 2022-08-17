package com.app.lsnhdsteth.hdsteth

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.app.lsnhdsteth.model.ResponseJsonData
import com.app.lsnhdsteth.network.ApiResponse
import com.app.lsnhdsteth.network.Status
import com.app.lsnhdsteth.utils.*
import com.app.lsnhdsteth.utils.DataManager.Companion.zipFolder
import com.app.lsnhdsteth.utils.Utility.Companion.getCurrentdate
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File


class GraphViewModel : ViewModel() {

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
    fun uploadZipFile(pref: MySharePrefernce,path: String,dr_name:String, image: Boolean) {
        var file_path = ""
        if(image)file_path = File(path).parentFile.parentFile.absolutePath
        else file_path = path
        Log.d("TAG", "uploadZipFile: $file_path")
        val response: String = pref.getJsonData()
        val (device) = Gson().fromJson(response, ResponseJsonData::class.java)
        val file = File(file_path)
        val zipfile = zipFolder(file)
        AndroidNetworking.upload(Constant.API_UPLOAD_ZIP_FILE)
            .addMultipartFile("file", zipfile)
            .addMultipartParameter("dId",device[0].id.toString())
            .addMultipartParameter("dateTime", getCurrentdate().replace("T", " "))
            .addMultipartParameter("mac", device[0].mac)
            .addMultipartParameter("dr", dr_name)
            .setTag("uploadTest")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    // do anything with response
                    Log.d("onResponse", response.toString())
                    if(file.exists())file.delete()
                    if(zipfile!!.exists())zipfile.delete()
                    if(image)_upload_file_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                }

                override fun onError(error: ANError) {
                    Log.d("onError", error.toString())
                    // handle error
                    _upload_file_data.postValue(ApiResponse(Status.ERROR,error.toString(),"success"))
                }
            })
    }

    // 2. fetch Records
    fun fetchRecords(id: String,date: String) {
        AndroidNetworking.get(Constant.API_RECORD_LIST+"$id/$date")
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
    fun sendEmail(id: String,email: String) {
        AndroidNetworking.post(Constant.API_SEND_EMAIL)
            .addBodyParameter("email",email)
            .addBodyParameter("id",id)
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

}