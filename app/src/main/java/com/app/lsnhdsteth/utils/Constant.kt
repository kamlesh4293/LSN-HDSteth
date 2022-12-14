package com.app.lsnhdsteth.utils

import android.os.Environment
import java.io.File

class Constant {

    // release key  and pass - Lsquared

    // vivo - cd7598568b48f67
    // realme - 899077be05e0bc19
    // honour - 6d3ccf7cadabff68
    // samsung testing - 8f888a7c1385f848O
    // mahesh sir device- c0de742442c77ccc

    // ankit mob - d06d4e782d67ca3f
    // emu-android17 - 7f256e818ca50e01

    // oneplus - 8e3771365ee4892a  --  a0724b17a562897f


    companion object{

        // environment - RC
//        const val BASE_URL = "https://rc.lsquared.com/"
//        const val BASE_FILE_URL = BASE_URL+"rc-lsquared-hub/"

        // environment - US
//        const val BASE_URL = "https://us.lsquared.com/"
//        const val BASE_FILE_URL = BASE_URL+"lsquared-hub/"

        // environment - US New
//        const val BASE_URL = "https://usnew.lsquared.com/"
//        const val BASE_FILE_URL = BASE_URL+"lsquared-hub/"

        // enviroment - hub
        const val BASE_URL = "https://hub.lsquared.com/"
        const val BASE_FILE_URL = "https://hub.lsquared.com/lsquared-hub/"


        // environment - Dev
//        const val BASE_URL = "https://dev2.lsquared.com/"
//        const val BASE_FILE_URL = "https://dev2.lsquared.com/dev-lsquared-hub/"




        // refresh from
        const val REFRESH_FROM_CONTENT = "refresh_from_content"
        const val REFRESH_FROM_NODEVICE = "refresh_from_nodevice"
        const val REFRESH_FROM_WAITING = "refresh_from_waiting"
        const val REFRESH_FROM_CHANGE_INTERNET = "refresh_from_change_internet"
        const val REFRESH_FROM_BACKGROUND = "refresh_from_background"

        // HDSTETH
        const val API_UPLOAD_ZIP_FILE = BASE_URL+ "api/v1/hdsteth/Zip" // widget id
        const val API_RECORD_LIST = BASE_URL+ "api/v1/hdsteth/rec/"
        const val API_SEND_EMAIL = BASE_URL+ "api/v1/hdsteth/mail" // widget id
        const val API_SEND_ADVERT_CLICK = BASE_URL+ "api/v1/hdsteth/advtClick" // widget id


        // content type
        const val CONTENT_IMAGE = "image"
        const val CONTENT_VECTOR = "vector"
        const val CONTENT_POWERPOINT = "powerpoint"
        const val CONTENT_WORD = "word"
        const val CONTENT_VIDEO = "video"
        const val CONTENT_WEB = "webPage"
        const val CONTENT_HD_STETH = "HDSteth"
            // target
        const val TARGET_CONTENT_CONTENT = "con"
        const val TARGET_CONTENT_URL = "url"

        // api response check
        const val DEVICE_NOT_FOUND = "device not found"

        // new changes
        const val APP_MODE_NODEVICE = "mode_nodevice"
        const val APP_MODE_DEVICE_AVAILABLE = "device_available"
        const val APP_MODE_DOWNLOADING = "mode_downloading"
        const val APP_MODE_CONTENT = "mode_content"


        // FOR New Activity
        // APIS
        const val API_DEVICE_VERSION = BASE_URL+"api/v1/feed/deviceversion/"
        const val API_CONTENT = Constant.BASE_FILE_URL+"feed/json/"
        const val API_SUBMIT_INFO = Constant.BASE_URL+"api/v1/feed/setDeviceInfo"

    }

}