package com.app.percentagechartview.callback

interface ProgressTextFormatter {
    fun provideFormattedText(progress: Float): CharSequence
}