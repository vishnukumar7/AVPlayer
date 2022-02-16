package com.app.avplayer.external

interface HourglassListener {

    fun onTimerTick(timeRemaining: Long)

    fun onTimerFinish()
}