package com.baidu.speech.recognizerdemo.logic.model


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("domain")
    val domain: String,
    @SerializedName("intent")
    val intent: String,
    @SerializedName("score")
    val score: Double,
    @SerializedName("slots")
    val slots: Slots
)