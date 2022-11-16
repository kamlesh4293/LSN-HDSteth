package com.app.lsnhdsteth.ui.newui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.app.lsnhdsteth.databinding.DialogDialogBinding
import com.app.lsnhdsteth.network.ApiInterface
import com.app.lsnhdsteth.network.ApiResponse
import com.app.lsnhdsteth.network.NetworkConnectivity
import com.app.lsnhdsteth.network.Status
import com.app.lsnhdsteth.utils.Constant
import com.app.lsnhdsteth.utils.DeviceInfo
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.app.lsnhdsteth.utils.Utility
import com.test.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback

class NewViewModel : ViewModel(){

    var TAG = "NewViewModel"
    var device_id = ""
    var internet = false
    var is_device_registered = false
    var is_deviceinfo_submitted = false

    // required
    var pref:MySharePrefernce? = null
    var connectionLiveData: NetworkConnectivity? = null


    // 1. device register
    private val _device_register_data = MutableLiveData<ApiResponse>()
    val device_register_api_result : LiveData<ApiResponse> get() = _device_register_data

    // 2. content data
    private val _content_data = MutableLiveData<ApiResponse>()
    val content_api_result : LiveData<ApiResponse> get() = _content_data

    // 3. device info
    private val _deviceinfo_data = MutableLiveData<ApiResponse>()
    val devcieinfo_api_result : LiveData<ApiResponse> get() = _deviceinfo_data


    // 1 check device is registered
    fun isDeviceRegistered() {
        var url = Constant.API_DEVICE_VERSION+"$device_id"
        if(internet){
            Log.d(TAG, "isDeviceRegistered: API - $url")
            viewModelScope.launch(Dispatchers.IO) {
                ApiInterface.create().checkIsDeviceRegister(url)
                    .enqueue( object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                            if(response?.body() != null){
                                var res = response?.body()!!.string()
                                Log.d(TAG, "isDeviceRegistered: API Response - $res")
                                _device_register_data.postValue(ApiResponse(Status.SUCCESS,res,"success"))
                            }else{
                                Log.d(TAG, "isDeviceRegistered: API Response - null")
                                _device_register_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                            }
                        }
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            Log.d(TAG, "isDeviceRegistered: API Response - error ${t.toString()}")
                            _device_register_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                        }
                    })
            }
        }else{
            _device_register_data.postValue(ApiResponse(Status.NO_INTERNET,null,""))
        }
    }

    // 2 fetch content data'
    fun fetchContentData(){
        if(internet){
            var url = Constant.API_CONTENT+"${device_id}.json";
            Log.d(TAG, "fetchContentData: API - $url")
            viewModelScope.launch(Dispatchers.IO) {
                ApiInterface.create().fetchPlayingContent(url)
                    .enqueue( object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                            if(response?.body() != null){
                                var res = response?.body()!!.string()
                                Log.d(TAG, "fetchContentData: API Response - $res")
                                _content_data.postValue(ApiResponse(Status.SUCCESS,res,"success"))
                            }else{
                                Log.d(TAG, "fetchContentData: API Response - null")
                                _content_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            Log.d(TAG, "fetchContentData: API Response - ${t.toString()}")
                            _content_data.postValue(ApiResponse(Status.FAILURE,null,t.toString()))
                        }
                    })
            }
        }
    }

    // 3 submit device info
    fun submitDeviceInfo(ctx: Activity){
        // get device info
        var info = DeviceInfo.deviceInfoData(ctx)
        var url = Constant.API_SUBMIT_INFO

        if(internet && !is_deviceinfo_submitted && is_device_registered){
            Log.d(TAG, "submitDeviceInfo: API - $url")
            Log.d(TAG, "submitDeviceInfo: API Body - ${info.toString()}")
            viewModelScope.launch(Dispatchers.IO) {
                AndroidNetworking.post(url)
                    .addJSONObjectBody(info) // posting json
                    .setOkHttpClient(RetrofitClient.getOkhttpClient())
                    .setTag("test")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsString(object : StringRequestListener {
                        override fun onResponse(response: String?) {
                            Log.d(TAG, "submitDeviceInfo: API Response - ${response.toString()}")
                            is_deviceinfo_submitted = true
                            _deviceinfo_data.postValue(ApiResponse(Status.SUCCESS,response,response!!))
                        }
                        override fun onError(anError: ANError?) {
                            Log.d(TAG, "submitDeviceInfo: API Response - error ${anError.toString()}")
                            _deviceinfo_data.postValue(ApiResponse(Status.FAILURE,anError.toString(),"failed"))
                        }
                    })
            }
        }
    }

}