package com.jassemdev.pdfboxsample

import android.app.Application
import timber.log.Timber

class PdfBoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}