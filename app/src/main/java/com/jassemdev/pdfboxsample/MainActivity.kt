package com.jassemdev.pdfboxsample

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.ibm.icu.text.ArabicShaping
import com.ibm.icu.text.ArabicShapingException
import com.ibm.icu.text.Bidi
import com.jassemdev.boxable.BaseTable
import com.jassemdev.boxable.HorizontalAlignment
import com.jassemdev.boxable.VerticalAlignment
import com.jassemdev.boxable.utils.FontUtils
import com.jassemdev.boxable.utils.PDStreamUtils
import com.jassemdev.boxable.utils.PageContentStreamOptimized
import com.jassemdev.pdfboxsample.databinding.ActivityMainBinding
import com.tom_roush.harmony.awt.AWTColor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val filename: String = "sample_pdf"
    private val appExecutors by lazy { AppExecutors() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PDFBoxResourceLoader.init(applicationContext)

        with(binding) {
            generatePdfBtn.setOnClickListener {
                createPdf(filename)
            }

            printPdfBtn.setOnClickListener {
                printPdf(filename)
            }

            renderPdfBtn.setOnClickListener {
                renderPdfFile(filename)
            }
        }
    }

    private fun createPdf(filename: String) {
        binding.pdfImage.setImageDrawable(null)
        binding.progressCircularV.isVisible = true
        appExecutors.diskIO.execute {
            val path = "${filesDir.absolutePath}/$filename.pdf"
            val file = File(path)
            if (file.exists()) file.delete()

            val document = PDDocument()
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            val arabicRaw = "جملة بالعربي لتجربة الكلاس ١٣٥"
            val arabicFont = PDType0Font.load(document, assets.open("NotoNaskhArabic-Regular.ttf"))
            val arabicFontBold = PDType0Font.load(document, assets.open("NotoNaskhArabic-Bold.ttf"))
            val arabicFont1 = PDType0Font.load(document, assets.open("arial.ttf"))
            val arabicFont2 = PDType0Font.load(document, assets.open("Arial-Bold.ttf"))
            val arabicFontSize = 15F
            val arabicText = bidiReorder(arabicRaw)

            val pageHeight = page.mediaBox.height.toInt().toFloat()
            val pageWidth = page.mediaBox.width.toInt().toFloat()

            var pageUsed = pageHeight
            val font = PDType1Font.HELVETICA
            val fontBold = PDType1Font.HELVETICA_BOLD

            val arabicFontHeight =
                arabicFont.fontDescriptor.fontBoundingBox.height.div(1000).times(arabicFontSize)

            val imageStream = assets.open("sample-logo.jpg")

            try {
                val contentStream = PDPageContentStream(document, page)

                Timber.d("createPdf: pageHeight: %f", pageHeight)
                Timber.d("createPdf: pageWidth: %f", pageWidth)
                contentStream.beginText()
//            contentStream.setNonStrokingColor(15 / 255F, 38 / 255F, 192 / 255F)
//            contentStream.setNonStrokingColor(AWTColor.BLACK)
//            contentStream.setNonStrokingColor(PDColor(floatArrayOf(247 / 255F, 247 / 255F, 87F / 255F), PDDeviceRGB.INSTANCE))
                contentStream.setFont(fontBold, 18F)
                contentStream.setLeading(19F)
                contentStream.newLineAtOffset(20F, pageHeight - getFontHeight(fontBold, 18F))
                contentStream.showText("MY COMPANY")
                pageUsed -= getFontHeight(fontBold, 18F)

                contentStream.newLine()
                contentStream.setLeading(16F)
                contentStream.setFont(fontBold, 15F)
                contentStream.showText("TRADING & CO.")
                pageUsed -= getFontHeight(fontBold, 15F)

                contentStream.newLine()
                contentStream.setLeading(13F)
                contentStream.setFont(font, 12F)
                contentStream.showText("RIYADH...KSA")
                pageUsed -= getFontHeight(font, 12F)

                contentStream.newLine()
                contentStream.showText("VAT: 12334353454364")
                pageUsed -= getFontHeight(font, 12F)

                contentStream.newLine()
                contentStream.showText("C.R: 12334353")
                pageUsed -= getFontHeight(font, 12F)

                contentStream.newLine()
                contentStream.endText()

                /*val para = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum"
                val paragraph = Paragraph(para, font, 11F, pageWidth - 30F, HorizontalAlignment.LEFT).apply {
                    lineSpacing = 1.5F
                }
                paragraph.write(PageContentStreamOptimized(contentStream), 10F, pageUsed)
    //            contentStream.showText(paragraph.text)
                Timber.d("para height: ${paragraph.height}")
                pageUsed -= paragraph.height
                Timber.d("pageUsed: $pageUsed")
                contentStream.newLine()
                contentStream.endText()*/

                val image = JPEGFactory.createFromStream(document, imageStream)
                contentStream.drawImage(image, pageWidth / 2.5F, pageHeight - 100F, 100F, 100F)
                Timber.d("imageHeight: ${image.height}")
                Timber.d("pageUsed i: $pageUsed")

                // Arabic Render
//            contentStream.beginText()
                contentStream.setLeading(19F)
//            contentStream.newLine()
//            contentStream.setNonStrokingColor(AWTColor.BLACK)
//            contentStream.setFont(arabicFontBold, 18F)
//            contentStream.setRenderingMode(RenderingMode.FILL_STROKE)
//            contentStream.setStrokingColor(AWTColor.BLACK)
                val px = (pageWidth - 30F) - getStringWidth(arabicText, arabicFontBold, 18F)
                val py = pageHeight - getFontHeight(arabicFontBold, 18F)
                Timber.d("arab px: $px")
//            contentStream.newLineAtOffset(
//                (pageWidth - 30F) - getStringWidth(arabicText, arabicFontBold, 18F),
//                pageHeight - getFontHeight(arabicFontBold, 18F)
//            )
//            contentStream.showText(arabicText)
                PDStreamUtils.write(
                    PageContentStreamOptimized(contentStream),
                    arabicText,
                    arabicFontBold,
                    18F,
                    px,
                    pageHeight,
                    AWTColor.BLACK
                )
                contentStream.endText()

//            contentStream.newLine()
                contentStream.setLeading(16F)
//            contentStream.setFont(arabicFontBold, 15F)
                val px1 = (pageWidth - 30F) - getStringWidth(arabicText, arabicFontBold, 15F)
                Timber.d("arab px1: $px1")
//            contentStream.newLineAtOffset(
//                (pageWidth - 30F) - getStringWidth(arabicText, arabicFontBold, 15F),
//                pageHeight - getFontHeight(arabicFontBold, 15F)
//            )
//            contentStream.setNonStrokingColor(AWTColor.BLACK)
//            contentStream.setRenderingMode(RenderingMode.FILL_STROKE)
//            contentStream.setStrokingColor(AWTColor.BLACK)
//            contentStream.newLineAtOffset((pageWidth - 30F) - arabicTextLength, pageUsed)
//            contentStream.showText(arabicText)
                PDStreamUtils.write(
                    PageContentStreamOptimized(contentStream),
                    arabicText,
                    arabicFontBold,
                    15F,
                    px1,
                    (pageHeight - FontUtils.getDescent(arabicFontBold, 18F) - FontUtils.getHeight(
                        arabicFontBold,
                        18F
                    )),
                    AWTColor.BLACK
                )
                contentStream.endText()

                val arabAddr = bidiReorder("رقم...الضريبي")
                val px2 = (pageWidth - 30F) - getStringWidth(arabAddr, arabicFont, 12F)
                contentStream.setLeading(13F)
                PDStreamUtils.write(
                    PageContentStreamOptimized(contentStream),
                    arabAddr,
                    arabicFont,
                    12F,
                    px2,
                    (pageHeight - FontUtils.getDescent(
                        arabicFontBold,
                        (16F + 19F)
                    ) - FontUtils.getHeight(arabicFontBold, (16F + 19F))),
                    AWTColor.BLACK
                )
                contentStream.endText()

                val arabVat = bidiReorder("رقم الضريبي:١١٢٣٤٥٦٧٨")
                val px3 = (pageWidth - 30F) - getStringWidth(arabVat, arabicFont, 12F)
                contentStream.setLeading(13F)
                PDStreamUtils.write(
                    PageContentStreamOptimized(contentStream),
                    arabVat,
                    arabicFont,
                    12F,
                    px3,
                    (pageHeight - FontUtils.getDescent(
                        arabicFontBold,
                        (16F + 19F + 13F)
                    ) - FontUtils.getHeight(arabicFontBold, (16F + 19F + 13F))),
                    AWTColor.BLACK
                )
                contentStream.endText()


                /*
                *   Simple Table
                * */
                contentStream.setRenderingMode(RenderingMode.FILL)
                val table = BaseTable(
                    pageUsed - 30F,
                    pageHeight - 11F,
                    10F,
                    pageWidth - 50F,
                    20F,
                    document,
                    page,
                    true,
                    true
                )

                val headerRow = table.createRow(15F)
                headerRow.apply {
                    createCell(
                        10F,
                        "Sl.No<br>${bidiReorder("رقم")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                    createCell(
                        30F,
                        "Description<br>${bidiReorder("اسم المنتج")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                    createCell(
                        10F,
                        "Qty<br>${bidiReorder("كمية")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                    createCell(
                        10F,
                        "Unit<br>${bidiReorder("وحدة")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                    createCell(
                        20F,
                        "Price<br>${bidiReorder("اسعر الوحده")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                    createCell(
                        20F,
                        "Total<br>${bidiReorder("اسعر الإجمالي")}",
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE
                    ).apply {
                        this.font = arabicFont2
                    }
                }
                table.addHeaderRow(headerRow)

                for (i in 0..100) {
                    val row = table.createRow(12F)

                    row.apply {
                        createCell(
                            10F,
                            "${i + 1}",
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE
                        )
                        createCell(
                            30F,
                            "Data 2 $arabicText",
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP
                        ).apply {
                            this.font = arabicFont1
                        }
                        val lis = "<ul><li>One</li><li>Two</li><li>Three</li><li>Four</li></ul>"
                        createCell(
                            10F,
                            "${i + 1}",
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE
                        )
                        createCell(10F, "PC", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
                        createCell(
                            20F,
                            "${i * 10}",
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE
                        )
                        createCell(
                            20F,
                            "${(i + 1) * (i * 10)}",
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE
                        )
                    }
                    Timber.d("rowHeight: ${row.height}")
//                val row1 = table.createRow(8F)
//                row1.apply {
//                    lineSpacing = 0F
//                    createCell(10F, "").setTopBorderStyle(LineStyle(AWTColor.WHITE, 0F))
//                    createCell(20F, "").setTopBorderStyle(LineStyle(AWTColor.WHITE, 0F))
//                    createCell(30F, arabicText).apply {
//                        setTopBorderStyle(LineStyle(AWTColor.WHITE, 0F))
//                        this.font = arabicFont
//                    }
//                    createCell(40F, "").setTopBorderStyle(LineStyle(AWTColor.WHITE, 0F))
//                }
//                Timber.d("rowHeight 1: ${row1.height}")
                }

                table.draw()

                contentStream.close()
                document.save(path)
                document.close()
                appExecutors.mainThread().execute {
                    binding.progressCircularV.isVisible = false
                    Snackbar.make(binding.root, "Pdf Generated" , Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Timber.e(e, "createPdf: Exception thrown while creating PDF")
            }
        }
    }

    private fun getFontHeight(font: PDFont, fontSize: Float): Float {
        return font.fontDescriptor.fontBoundingBox.height.div(1000).times(fontSize)
    }

    private fun getStringWidth(text: String, font: PDFont, fontSize: Float): Float {
        return font.getStringWidth(text).div(1000F).times(fontSize)
    }

    private fun printPdf(filename: String) {
        val path = "${filesDir.absolutePath}/$filename.pdf"

        val file = File(path)

        if (file.exists()) {
            Snackbar.make(binding.root, "File is present", Snackbar.LENGTH_SHORT).show()
            openPdfViewer(filename, path)
//            Handler().postDelayed({
//                file.delete()
//            }, 2000)
        } else {
            Snackbar.make(binding.root, "File is Not present", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun renderPdfFile(filename: String) {
        binding.progressCircularV.isVisible = true
        appExecutors.diskIO.execute {
            val path = "${filesDir.absolutePath}/$filename.pdf"
            val file = File(path)
            if (file.exists()) {
                try {
                    val document = PDDocument.load(file)
                    val renderer = PDFRenderer(document)

                    val pageImage = renderer.renderImage(0, 1F, ImageType.RGB)
                    val imagePath = "${filesDir.absolutePath}/render.jpg"
                    val imageFile = File(imagePath)
                    val fileOutputStream = FileOutputStream(imageFile)
                    pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.close()

                    appExecutors.mainThread().execute {
                        binding.progressCircularV.isVisible = false
                        binding.pdfImage.setImageBitmap(pageImage)
                    }

                } catch (e: IOException) {
                    Timber.e(e, "renderPdfFile: Exception thrown while rendering file")
                }
            }
        }
    }

    private fun bidiReorder(text: String): String {
        return try {
            val bidi = Bidi(
                ArabicShaping(ArabicShaping.LETTERS_SHAPE).shape(text),
                Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT
            )
            bidi.reorderingMode = 0
            bidi.writeReordered(2)
        } catch (ase3: ArabicShapingException) {
            text
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}