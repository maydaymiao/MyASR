package com.baidu.speech.recognizerdemo.util.mqtt

import android.content.Context
import android.util.Log
import com.baidu.speech.recognizerdemo.util.BaseApplication
import com.baidu.speech.recognizerdemo.util.BaseApplication.Companion.CLIENT_ID
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttManager(context: Context) {

    private var mqttClient: MqttAndroidClient

    companion object {
        const val SERVER_URI = "tcp://49.4.124.65:1883"
        const val USERNAME = "admin"
        const val PASSWORD = "spdb_2020"
        const val TAG = "MQTTManager"
    }

    init {
        mqttClient = MqttAndroidClient(BaseApplication.context, SERVER_URI, CLIENT_ID)
        mqttClient.setCallback(object : MqttCallback {
            //            receive new messages from broker
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(MqttManager.TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            //            lost the connection to broker
            override fun connectionLost(cause: Throwable?) {
                Log.d(MqttManager.TAG, "Connection lost ${cause.toString()}")
            }

            //            complete message delivery to the broker
            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        connect()
    }

    fun setCallback(callback: MqttCallbackExtended){
        mqttClient.setCallback(callback)
    }

    fun connect() {

        val options = MqttConnectOptions()
        options.userName = USERNAME
        options.password = PASSWORD.toCharArray()
        options.isAutomaticReconnect = true
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean{
        return mqttClient.isConnected
    }

    fun destroy(){
        mqttClient.unregisterResources()
        mqttClient.disconnect()
    }
}