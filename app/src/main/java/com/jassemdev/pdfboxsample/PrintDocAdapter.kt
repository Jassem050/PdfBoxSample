package com.jassemdev.pdfboxsample

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import kotlinx.coroutines.*
import java.io.*

class PrintDocAdapter @JvmOverloads constructor(
    private val context: Context,
    private val fileName: String,
    private val path: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : PrintDocumentAdapter() {

    private lateinit var file: File

    override fun onStart() {
        super.onStart()
        coroutineScope.launch(Dispatchers.IO) {
            file = File(path)
        }
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        coroutineScope.launch {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return@launch
            }

            withContext(Dispatchers.IO) {
                PrintDocumentInfo.Builder(fileName.split(".")[0])
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
            }.also { info ->
                callback.onLayoutFinished(info, true)
            }
        }

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {

        coroutineScope.launch {
            Log.d(TAG, "onWrite: m con: ${this.coroutineContext}")

            withContext(Dispatchers.IO) {
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = FileInputStream(file)
                    outputStream = FileOutputStream(destination.fileDescriptor)
                    inputStream.copyTo(outputStream)
                } catch (ex: Exception) {
                    callback.onWriteFailed(ex.message)
                    Log.e(TAG, "Could not write: ${ex.localizedMessage}")
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }

            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
            } else {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        }
    }

    private fun showPdf(fileDescriptor: FileDescriptor) {

    }

    companion object {
        private const val TAG = "PrintDocAdapter"
    }

    override fun onFinish() {
        super.onFinish()
        coroutineScope.cancel()
    }
}