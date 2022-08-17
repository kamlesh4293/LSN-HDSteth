package com.example.example

import com.google.gson.annotations.SerializedName


data class ResponseMessage (

  @SerializedName("status" ) var status : String? = null,
  @SerializedName("code"   ) var code   : Int?    = null,
  @SerializedName("desc"   ) var desc   : String? = null

)