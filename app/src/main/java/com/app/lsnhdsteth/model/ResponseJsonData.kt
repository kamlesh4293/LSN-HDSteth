package com.app.lsnhdsteth.model

import com.google.gson.annotations.SerializedName

data class ResponseJsonData (

	@SerializedName("device") val device : List<Device>,
	@SerializedName("layout") val layout : List<Layout>,
	@SerializedName("downloadable") val downloadable : List<Downloadable>
)