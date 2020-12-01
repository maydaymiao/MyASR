package com.baidu.speech.recognizerdemo.logic.model


import com.google.gson.annotations.SerializedName

data class SpeechParse(
    @SerializedName("appid")
    val appid: Int,
    @SerializedName("encoding")
    val encoding: String,
    @SerializedName("err_no")
    val errNo: Int,
    @SerializedName("parsed_text")
    val parsedText: String,
    @SerializedName("raw_text")
    val rawText: String,
    @SerializedName("results")
    val results: List<Result>
)