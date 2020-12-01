package com.baidu.speech.recognizerdemo.util.Audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Message
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAudioRecord {
    private var handler: Handler? = null
    var mAudioRecord: AudioRecord? = null
    var isGetVoiceRun = false
    var volume = 0F

    companion object {
        private const val TAG = "AudioRecord"
        const val SAMPLE_RATE_IN_HZ = 8000
        val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)
    }

    constructor(handler: Handler?) {
        this.handler = handler
    }


    fun getNoiseLevel(){
            if (isGetVoiceRun) {
                Log.e(TAG, "还在录着呢")
                return
            }
            mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)
            if (mAudioRecord == null) {
                Log.e("sound", "mAudioRecord初始化失败")
            }
            isGetVoiceRun = true
            Thread {
                mAudioRecord!!.startRecording()
                val buffer = ShortArray(BUFFER_SIZE)
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    val r = mAudioRecord!!.read(buffer, 0, BUFFER_SIZE)
                    var v: Long = 0
                    // 将 buffer 内容取出。进行平方和运算
                    for (i in buffer.indices) {
                        v += (buffer[i] * buffer[i]).toLong()
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    val mean = v / r.toDouble()
                    // 分贝这里手动减10，否则白噪音太明显
                    volume = ((10 * Math.log10(mean))-10).toFloat()
//                    Log.d(TAG, "分贝值:$volume")
                    sendMessage(volume, 1)
                    // 大概一秒十次
//                    GlobalScope.launch {
//                        delay(1000)
//                    }
                }
                mAudioRecord!!.stop()
                mAudioRecord!!.release()
                mAudioRecord = null
            }.start()
        }

    private fun sendMessage(volume: Float, what: Int) {
        if (handler == null) {
            return
        }
        val msg = Message.obtain()
        msg.what = what
        msg.obj = volume
        handler!!.sendMessage(msg)
    }


}