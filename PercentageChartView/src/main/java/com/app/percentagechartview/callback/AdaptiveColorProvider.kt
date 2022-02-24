package com.app.percentagechartview.callback

interface AdaptiveColorProvider {
    fun provideProgressColor(progress: Float): Int {
        return -1
    }

    fun provideBackgroundColor(progress: Float): Int {
        return -1
    }

    fun provideTextColor(progress: Float): Int {
        return -1
    }

    fun provideBackgroundBarColor(progress: Float): Int {
        return -1
    }
}