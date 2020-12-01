package com.baidu.speech.recognizerdemo.logic.model


import com.google.gson.annotations.SerializedName

data class UserPersonName(
    @SerializedName("norm")
    val norm: String,
    @SerializedName("word")
    val word: String
)