package com.app.lsnhdsteth.model

import com.google.gson.annotations.SerializedName

data class T (

    @SerializedName("tType") var tType : String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("fileName") var fileName: String? = null,
    @SerializedName("src") var src: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("filesize") var filesize: Int?    = null,
    @SerializedName("downloadable") var downloadable : String? = null,
    @SerializedName("active") var active: Int?    = null,
    @SerializedName("url") var url: String?    = null

)
