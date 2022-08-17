package com.example.example

import com.google.gson.annotations.SerializedName


data class HdStethRecords (

  @SerializedName("info" ) var info : Info?          = Info(),
  @SerializedName("rec"  ) var rec  : ArrayList<Rec> = arrayListOf()

)