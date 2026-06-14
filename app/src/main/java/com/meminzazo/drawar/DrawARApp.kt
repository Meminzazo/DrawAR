package com.meminzazo.drawar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader

@HiltAndroidApp
class DrawARApp : Application(){
    override fun onCreate() {
        super.onCreate()

        OpenCVLoader.initLocal()
    }
}

