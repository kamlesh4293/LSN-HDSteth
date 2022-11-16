package com.app.lsnhdsteth.model

import com.google.gson.annotations.SerializedName

data class Ss (

    @SerializedName("id") val id : String,
    @SerializedName("fileName") val fileName : String,
    @SerializedName("src") val src : String,
    @SerializedName("type") val type : String,
    @SerializedName("filesize") val filesize: Int,
    @SerializedName("d") val d : Double,    // duration
    @SerializedName("fd") val fd : Double,  // actual duration
    @SerializedName("downloadable") val downloadable : Boolean,
    @SerializedName("active") val active : Int,
    )

