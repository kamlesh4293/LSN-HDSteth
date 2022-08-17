package com.example.example

import com.google.gson.annotations.SerializedName


data class Rec (

  @SerializedName("id"   ) var id   : Int?    = null,
  @SerializedName("time" ) var time : String? = null,
  @SerializedName("dir"  ) var dir  : String? = null,
  @SerializedName("noOfPages"  ) var noOfPages  : Int? = null,
  @SerializedName("isAudio"  ) var isAudio  : Boolean? = false

)