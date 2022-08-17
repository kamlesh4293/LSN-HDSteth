package com.app.lsnhdsteth.model

import com.google.gson.annotations.SerializedName


data class Layout (

	@SerializedName("id") val id : Int,
	@SerializedName("w") val w : Int,
	@SerializedName("h") val h : Int,
	@SerializedName("bg") val bg : String,
	@SerializedName("frame") val frame : List<Frame>,
	@SerializedName("inactive") val inactive : List<Frame>

)