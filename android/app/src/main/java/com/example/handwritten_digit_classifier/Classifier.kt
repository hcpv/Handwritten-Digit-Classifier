package com.example.handwritten_digit_classifier

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Classifier(private val context: Context) {

    companion object {
        private const val TAG = "DigitClassifier"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 10
    }

    private var interpreter: Interpreter? = null
    var isInitialized: Boolean = false

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var imgWidth: Int = 0
    private var imgHeight: Int = 0
    private var modelSize: Int = 0

    fun initialize(): Task<Void> {
        return call(executorService, Callable<Void> {
            initializeInterpreter()
            null
        }
        )
    }
    private fun loadModelFile(assets: AssetManager, filename: String): ByteBuffer {
        val file = assets.openFd(filename)
        val inputStream = FileInputStream(file.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = file.startOffset
        val declaredLength = file.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    private fun initializeInterpreter() {
        val assets = context.assets
        val model = loadModelFile(assets, "mnist.tflite")
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)
        var count = interpreter.inputTensorCount
        print("Number of input tensor : $count")
        val inputShape = interpreter.getInputTensor(0).shape()
        imgWidth = inputShape[1]
        imgHeight = inputShape[2]
        modelSize = FLOAT_TYPE_SIZE * imgWidth * imgHeight * PIXEL_SIZE

        this.interpreter = interpreter
        isInitialized = true

        Log.d(TAG,"Initialised tflite interpreter")

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imgWidth*imgHeight)
        if (bitmap != null) {
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        }

        for (x in pixels) {
            val r = Color.red(x)
            val g = Color.green(x)
            val b = Color.blue(x)

            val normalizedPixel = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixel)
        }
        return byteBuffer
    }

    private fun classify(bitmap: Bitmap): String{
        check(isInitialized) {"tflite interpreter is not initialised"}

        val resizedimg = Bitmap.createScaledBitmap(bitmap,imgWidth,imgHeight,true)
        val byteBuffer = convertBitmapToByteBuffer(resizedimg)
        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT)}
        interpreter?.run(byteBuffer, output)

        for (x in output)
        {
            println("$x")
        }
        val result = output[0]
        val maxInd = result.indices.maxBy{result[it]} ?: -1
        val resultString = "Prediction Result: %d\nConfidence: %2f".format(maxInd, result[maxInd])

        return resultString
    }

    fun classifyAsync(bitmap: Bitmap):Task<String>{
        return call(executorService,Callable<String>{classify(bitmap)})
    }

    fun close(){
        call(executorService, Callable {
            interpreter?.close()
            Log.d(TAG,"Closed tflite interpreter")
            null
        })
    }
}

