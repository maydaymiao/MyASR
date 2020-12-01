package com.baidu.speech.recognizerdemo.logic.model


import com.google.gson.annotations.SerializedName

data class Slots(
    @SerializedName("user_person_name")
    val userPersonName: List<UserPersonName>,
    @SerializedName("user_contact")
    val userContact: List<UserContact>
)