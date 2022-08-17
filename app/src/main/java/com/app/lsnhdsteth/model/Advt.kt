package com.app.lsnhdsteth.model

import com.google.gson.annotations.SerializedName

data class Advt (

    @SerializedName("id") val id : String,
    @SerializedName("fileName") val fileName : String,
    @SerializedName("src") val src : String,
    @SerializedName("type") val type : String,
    @SerializedName("filesize") val filesize: Int,
    @SerializedName("duration") val duration : Double,
    @SerializedName("downloadable") val downloadable : Boolean,
    @SerializedName("active") val active : Int,
    @SerializedName("t") var t: ArrayList<T> = arrayListOf()

)

