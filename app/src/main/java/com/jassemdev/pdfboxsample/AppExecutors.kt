package com.jassemdev.pdfboxsample

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {
    val networkIO: Executor
    val diskIO: Executor
    private val mainThread: Executor

    init {
        networkIO = Executors.newSingleThreadExecutor()
        diskIO = Executors.newFixedThreadPool(3)
        mainThread = MainThreadExecutor()
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}