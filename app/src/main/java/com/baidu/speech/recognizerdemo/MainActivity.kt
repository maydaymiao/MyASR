package com.baidu.speech.recognizerdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.aip.asrwakeup3.core.recog.IStatus
import com.baidu.aip.asrwakeup3.core.recog.IStatus.*
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener
import com.baidu.aip.asrwakeup3.core.wakeup.MyWakeup
import com.baidu.aip.asrwakeup3.core.wakeup.listener.IWakeupListener
import com.baidu.aip.asrwakeup3.core.wakeup.listener.RecogWakeupListener
import com.baidu.speech.asr.SpeechConstant
import com.baidu.speech.recognizerdemo.logic.model.SpeechParse
import com.baidu.speech.recognizerdemo.util.Audio.MyAudioRecord
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.set


class MainActivity : AppCompatActivity(),IStatus {

    //    lateinit var asr: EventManager
    lateinit var myWakeup: MyWakeup
    lateinit var handler: Handler
    lateinit var myRecognizer: MyRecognizer
    lateinit var audioRecordDemo: MyAudioRecord
    private val backTrackInMs = 1500
    private val TAG = "myTag"
    private var flag = false
    private var updateVolume = 1
    private var curVolume = 0F
    private var possibleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = @SuppressLint("HandlerLeak")
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                handleMsg(msg)
            }
        }

        val recogListener: IRecogListener = MessageStatusRecogListener(handler)
        myRecognizer = MyRecognizer(this, recogListener)

        val listener: IWakeupListener = RecogWakeupListener(handler)
        myWakeup = MyWakeup(this, listener)

        start()
//        initView()
        initPermission()

        audioRecordDemo = MyAudioRecord(handler)
        audioRecordDemo.getNoiseLevel()

        siriWaveView.startAnim();


    }

    private fun handleMsg(msg: Message){
//        msg.obj?.let {
//            Log.d(TAG, msg.obj.toString())
//        }
        when(msg.what){
            updateVolume -> {
                curVolume = msg.obj as Float
                siriWaveView.setVolume(curVolume);
                Log.d(TAG, curVolume.toString())
            }
            STATUS_READY -> Log.d(TAG, "唤醒词识别后，实际句子说话前的处理。。。 ")
            STATUS_WAKEUP_SUCCESS -> {
                textView.text = "诶，我在。 请问您要找谁？"
                flag = false
                possibleName = ""
                setSpeechParam()
                clearSpeechText()
            }
            STATUS_FINISHED -> {
//                修改Core里MessageStatusRecogListener里的onAsrFinalResult和onAsrFinish
                val result = msg.obj.toString()
                Log.d(TAG, result)
                try {
                    val speechParse = Gson().fromJson<SpeechParse>(result, SpeechParse::class.java)
                    val rawText = speechParse.rawText
                    speechParse.results?.let { results ->
                        for (result in results) {
                            val domain = result.domain
                            when (domain) {
                                "person" -> {
                                    val name = result.slots.userPersonName.first().word
                                    Log.d(TAG, "person：${name}")
                                    flag = true
                                    textView.text = name
                                    break
                                }
                                "contact" -> {
                                    val name = result.slots.userContact.first().word
                                    Log.d(TAG, "contact：${name}")
                                    textView.text = name
                                    flag = true
                                    break
                                }
                                "instruction" -> {
                                    val intent = result.intent
                                    when (intent) {
                                        "YES" -> {
                                            flag = true
                                            Log.d(TAG, "${possibleName}")
                                            textView.text = possibleName
                                            break
                                        }
                                    }
                                }
                                "robot" -> {
                                    val intent = result.intent
                                    when (intent) {
                                        "ROBOT_DENY" -> {
                                            flag = true
                                            Log.d(TAG, "对不起，小浦还没学会，请尝试说：小浦小浦，我要找XXX")
                                            textView.text = "对不起，小浦还没学会，请尝试说：小浦小浦，我要找XXX"
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!flag) {
                        val index = rawText.indexOf("找")
                        if (index > 0) {
                            flag = true
                            val findName = rawText.substring(index + 1, rawText.length - 1)
                            Log.d(TAG, "通过找关键词：$findName")
                            textView.text = findName
                        } else {
                            possibleName = rawText.substring(0, rawText.length - 1)
                            Log.d(TAG, "对不起，小浦无法判断您说的是一个人名，请问您是要找: ${possibleName}吗？")
                            textView.text = "对不起，小浦无法判断您说的是一个人名，请问您是要找: ${possibleName}吗？"
                            setSpeechParam()

                        }
                    }
                    clearSpeechText()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun clearSpeechText(){
        GlobalScope.launch {
            delay(8000)
            textView.text = " "
        }
    }

    private fun setSpeechParam(){
        val params: MutableMap<String, Any> = java.util.LinkedHashMap()
        params[SpeechConstant.ACCEPT_AUDIO_VOLUME] = false
        params[SpeechConstant.VAD] = SpeechConstant.VAD_DNN
        params[SpeechConstant.PID] = 15373
        params[SpeechConstant.LMID] = 11383
        if (backTrackInMs == 0) {
            params[SpeechConstant.AUDIO_MILLS] = System.currentTimeMillis() - backTrackInMs
        }
        myRecognizer.cancel()
        myRecognizer.start(params)
    }

    private fun start() {
//        以下是唤醒后识别的代码
        val params: MutableMap<String, Any> = HashMap()
        params[SpeechConstant.WP_WORDS_FILE] = "assets:///WakeUp.bin"
        myWakeup.start(params)

    }

    private fun stop(){
        myWakeup.stop()
//        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
    }


    private fun initView() {
        btn_start.setOnClickListener{
            start()
        }
        btn_stop.setOnClickListener{
            stop()
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private fun initPermission() {
        val permissions = arrayOf<String>(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val toApplyList = ArrayList<String>()
        for (perm in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                            this,
                            perm
                    )
            ) {
                toApplyList.add(perm)
                //进入到这里代表没有权限
            }
        }
        val tmpList = arrayOfNulls<String>(toApplyList.size)
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        myRecognizer.release()
    }

}