package com.jassemdev.pdfboxsample

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import timber.log.Timber

class PdfUtils {

    companion object {
        fun splitRight(text: String, regex: String, limit: Int): List<String> {
            var string = text
            val result: MutableList<String> = ArrayList()
            var temp = arrayOf<String>()
            for (i in 1 until limit) {
                if (string.matches(Regex(".*$regex.*"))) {
                    temp = string.split(modifyRegex(regex)).toTypedArray()
                    result.add(temp[1])
                    string = temp[0]
                }
            }
            if (temp.isNotEmpty()) {
                result.add(temp[0])
            }
            result.reverse()
            Timber.d("reverseText: $result")
            return result
        }

        private fun modifyRegex(regex: String): String {
            return "$regex(?!.*$regex.*$)"
        }
    }
}

fun Activity.openPdfViewer(filename: String, path: String) {
    val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "${getString(R.string.app_name)} Document"
    val printAttributes = PrintAttributes.Builder().apply {
        setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
        setColorMode(PrintAttributes.COLOR_MODE_COLOR)
    }.build()

    printManager.print(
        jobName,
        PrintDocAdapter(this, filename, path),
        printAttributes
    )
}