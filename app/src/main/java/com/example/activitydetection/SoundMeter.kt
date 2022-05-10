package com.example.activitydetection

import android.media.MediaRecorder
import java.io.IOException

class SoundMeter {
    private var mRecorder: MediaRecorder? = null
    private var mEMA = 0.0
    @Throws(IOException::class)
    fun start() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder!!.setOutputFile("/dev/null")
            mRecorder!!.prepare()
            mRecorder!!.start()
            mEMA = 0.0
        }
    }

    fun stop() {
        if (mRecorder != null) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
        }
    }

    val amplitude: Double
        get() = if (mRecorder != null) mRecorder!!.maxAmplitude / 2700.0 else 0.0
    val amplitudeEMA: Double
        get() {
            val amp = amplitude
            mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA
            return mEMA
        }

    companion object {
        private const val EMA_FILTER = 0.6
    }
}