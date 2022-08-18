package com.app.lsnhdsteth.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.*
import com.app.lsnhdsteth.network.ApiInterface
import com.app.lsnhdsteth.network.ApiResponse
import com.app.lsnhdsteth.network.Status
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.*
import com.androidnetworking.error.ANError
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.StringRequestListener
import com.app.lsnhdsteth.utils.*
import java.io.File
import com.test.RetrofitClient
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    var internet = false
    var device_id = ""
    var is_device_registered = false
    var is_deviceinfo_submitted = false
    var is_devicereport_submitted = false

    var delay = 30000
    var temp_delay = 60000
    var screen_delay = 300


    // 1. device register
    private val _device_register_data = MutableLiveData<ApiResponse>()
    val device_register_api_result : LiveData<ApiResponse> get() = _device_register_data

    // 2. content data
    private val _content_data = MutableLiveData<ApiResponse>()
    val content_api_result : LiveData<ApiResponse> get() = _content_data

    // 3. device info
    private val _deviceinfo_data = MutableLiveData<ApiResponse>()
    val devcieinfo_api_result : LiveData<ApiResponse> get() = _deviceinfo_data

    // 4. temp data submit
    private val _temprature_data = MutableLiveData<ApiResponse>()
    val temprature_api_result : LiveData<ApiResponse> get() = _temprature_data

    // 5. post screen shot
    private val _screenshot_data = MutableLiveData<ApiResponse>()
    val screenshot_api_result : LiveData<ApiResponse> get() = _screenshot_data


    // 1 check device is registered
    fun isDeviceRegistered() {
        var url = Constant.BASE_URL+"api/v1/feed/deviceversion/$device_id"
        if(internet){
            viewModelScope.launch(Dispatchers.IO) {
                ApiInterface.create().checkIsDeviceRegister(url)
                    .enqueue( object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>,response: retrofit2.Response<ResponseBody>) {
                            if(response?.body() != null){
                                var res = response?.body()!!.string()
                                _device_register_data.postValue(ApiResponse(Status.SUCCESS,res,"success"))
                            }else{
                                _device_register_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                            }
                        }
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            _device_register_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                        }
                    })
            }
        }else{
            _device_register_data.postValue(ApiResponse(Status.NO_INTERNET,null,""))
        }
    }


    // 2 fetch content data
    fun fetchContentData(){
        if(internet){
            var url = Constant.BASE_FILE_URL+"feed/json/${device_id}.json";
//            var url = "https://dev2.lsquared.com/dev-lsquared-hub/feed/json/${device_id}.json"
            Log.d("TAG", "fetchContentData: $url")
            viewModelScope.launch(Dispatchers.IO) {
                ApiInterface.create().fetchPlayingContent(url)
                .enqueue( object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>,response: retrofit2.Response<ResponseBody>) {
                        if(response?.body() != null){
                            var res = response?.body()!!.string()
                            Log.d("TAG", "fetchContentData: respone $res")
                            _content_data.postValue(ApiResponse(Status.SUCCESS,res,"success"))
                        }else{
                            _content_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        Log.d("res_error-",t.toString())
                        _content_data.postValue(ApiResponse(Status.FAILURE,null,t.toString()))
                    }
                })
            }
        }
    }

    // 3 submit device info
    fun submitDeviceInfo(ctx:Activity){
        // get device info
        var info = Utility.deviceInfoToJson(
                DeviceInfo.getDeviceId(ctx),
                DeviceInfo.getDeviceResolution(ctx),
                DeviceInfo.getDeviceName(),
                DeviceInfo.getLocalIpAddress(),
                DeviceInfo.getTotalDiscSize(),
                DeviceInfo.getUsedDiscSize(),
                DeviceInfo.getTotalRAMSize(ctx),
                DeviceInfo.getUsedRAMSize(ctx),
                DeviceInfo.getSerial(),
                DeviceInfo.getModelName(),
                DeviceInfo.getConnectedNetworkType(ctx),
                DeviceInfo.getDeviceVersion(),
                DeviceInfo.getWifiMacAddress(ctx)
            )

        if(internet && !is_deviceinfo_submitted && is_device_registered){
            Log.d("device_info_body-",info.toString())
            viewModelScope.launch(Dispatchers.IO) {
                AndroidNetworking.post(Constant.BASE_URL+"api/v1/feed/setDeviceInfo")
                    .addJSONObjectBody(info) // posting json
                    .setOkHttpClient(RetrofitClient.getOkhttpClient())
                    .setTag("test")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsString(object : StringRequestListener{
                        override fun onResponse(response: String?) {
                            Log.d("device_info_success-",response.toString())
                            _deviceinfo_data.postValue(ApiResponse(Status.SUCCESS,response,response!!))
                        }
                        override fun onError(anError: ANError?) {
                            Log.d("device_info_failed-",anError.toString())
                            _deviceinfo_data.postValue(ApiResponse(Status.FAILURE,anError.toString(),"failed"))
                        }
                    })
            }
        }
    }

    // 4 submit temprature data
    fun updateTempratureData(temp:String){
        if(internet && is_device_registered){
            viewModelScope.launch(Dispatchers.IO) {
                val retroInstance = ApiInterface.create()
                Log.d("Temp_api", "api/v1/feed/dt/$device_id/$temp/${Utility.getCurrentdate()}")
                var call  = retroInstance.postTemperature("api/v1/feed/dt/$device_id/$temp/${Utility.getCurrentdate()}")
                call.enqueue( object : Callback<ResponseBody> {

                    override fun onResponse(call: Call<ResponseBody>,response: retrofit2.Response<ResponseBody>) {
                        if(response?.body() != null){
                            var res = response?.body()!!.string()
                            _temprature_data.postValue(ApiResponse(Status.SUCCESS,res,"success"))
                        }else{
                            _temprature_data.postValue(ApiResponse(Status.ERROR,null,"error"))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        Log.d("res_error-",t.toString())
                        _temprature_data.postValue(ApiResponse(Status.FAILURE,null,t.toString()))
                    }
                })
            }
        }else{
            _temprature_data.postValue(ApiResponse(Status.NO_INTERNET,null,""))
        }
    }

    // 5 submit screen shot
    fun submitScreenShot(body: JSONObject){
        if(internet && is_device_registered){
            Log.d("device_screenshot_body-",body.toString())
            viewModelScope.launch(Dispatchers.IO) {
                    AndroidNetworking.post(Constant.BASE_URL+"api/v1/feed/deviceSaveScreenshotBase64")
                        .addJSONObjectBody(body) // posting json
                        .setOkHttpClient(RetrofitClient.getOkhttpClient())
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsString(object : StringRequestListener{
                            override fun onResponse(response: String?) {
                                Log.d("device_screen_success-",response.toString())
                                _screenshot_data.postValue(ApiResponse(Status.SUCCESS,response.toString(),"success"))
                            }
                            override fun onError(anError: ANError?) {
                                Log.d("device_screen_failed-",anError.toString())
                                _screenshot_data.postValue(ApiResponse(Status.ERROR,anError.toString(),"error"))
                            }
                        })
            }
        }
    }

    // 6 submit report
    fun submitRecords(device_id: String, data: String, pref: MySharePrefernce?){
        if(internet && is_device_registered && !data.equals("") && !is_devicereport_submitted){
            var body = Utility.getRecords(device_id,data)
            Log.d("record_body-",body.toString())
            viewModelScope.launch(Dispatchers.IO) {
                    AndroidNetworking.post(Constant.BASE_URL+"api/v1/feed/writePoPReport")
                        .addJSONObjectBody(body) // posting json
                        .setOkHttpClient(RetrofitClient.getOkhttpClient())
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsString(object : StringRequestListener{
                            override fun onResponse(response: String?) {
                                Log.d("device_report_success-",response.toString())
                                is_devicereport_submitted = true
                                pref?.clearReportdata()
                            }
                            override fun onError(anError: ANError?) {
                                Log.d("device_report_failed-",anError.toString())
                            }
                        })
            }
        }
    }

    // delete file
    fun deleteFiles(downloable_file: List<String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if(downloable_file !=null && downloable_file.size>0){
                var dir_files = DataManager.getAllDirectoryFiles()
                if(dir_files!=null &&dir_files.size>0){
                    for (i in 0..dir_files.size-1){
                        if(!downloable_file.contains(dir_files[i].name)){
                            val file = File(dir_files[i].path)
                            if(file.exists()){
                                var deleted = file.delete()
                                if(deleted)Log.d("Main View Model", "deleteFiles: ${file.path}")
                                else Log.d("Main View Model", "deleteFiles: Not Deleted")
                            }
                        }
                    }
                }
            }else{
                var dir_files = DataManager.getAllDirectoryFiles()
                if(dir_files!=null &&dir_files.size>0){
                    val file = File(dir_files[0].path)
                    if(file.exists())file.delete()
                }
            }
        }
    }

}