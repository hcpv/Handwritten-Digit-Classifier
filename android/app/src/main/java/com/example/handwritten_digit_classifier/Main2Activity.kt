package com.example.handwritten_digit_classifier

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class Main2Activity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    companion object {

        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_REQUEST = 1
    }

    private var classifier = Classifier(this)
    private var cameraView: CameraBridgeViewBase? = null
    private var imageView: ImageView? = null
    private var textLabelPrediction: TextView? = null
    private var textLabelConfifdence: TextView? = null
    private var textPrediction: TextView? = null
    private var textConfifdence: TextView? = null
    private var detectButton: Button? = null
    private var clearButton: Button? = null
    private val baseLoaderCallback = object : BaseLoaderCallback(this){
        override fun onManagerConnected(status: Int) {
            when(status)
            {
                LoaderCallbackInterface.SUCCESS ->{
                    cameraView?.enableView()
                }
                else -> super.onManagerConnected(status)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        cameraView = findViewById<JavaCameraView>(R.id.camera)
        cameraView?.visibility = SurfaceView.VISIBLE
        cameraView?.setCvCameraViewListener(this)
        cameraView?.setMaxFrameSize(3000,3000)
        imageView = findViewById(R.id.imageview)
        clearButton = findViewById(R.id.btn_clear)
        detectButton = findViewById(R.id.btn_detect)
        textLabelPrediction = findViewById(R.id.label_prediction)
        textLabelConfifdence = findViewById(R.id.label_confidence)
        textPrediction = findViewById(R.id.prediction)
        textConfifdence = findViewById(R.id.confidence)
        clearButton?.setOnClickListener {
            imageView?.visibility = SurfaceView.INVISIBLE
            textConfifdence?.visibility = SurfaceView.INVISIBLE
            textPrediction?.visibility = SurfaceView.INVISIBLE
            textLabelConfifdence?.visibility = SurfaceView.INVISIBLE
            textLabelPrediction?.visibility = SurfaceView.INVISIBLE
        }
        classifier.initialize().addOnFailureListener{e-> Log.e(TAG,"Error to setting up classifier",e)}

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val message = "Camera permission granted"
                    Log.d(TAG, message)
                    //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                } else {
                    val message = "Camera permission was not granted"
                    Log.e(TAG, message)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Log.e(TAG, "Unexpected permission request")
            }
        }
    }
    override fun onPause() {
        super.onPause()
        if (cameraView != null)
            cameraView!!.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraView != null)
            cameraView!!.disableView()
        classifier.close()
    }
    override fun onCameraViewStarted(width: Int, height: Int){

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        var mat1: Mat = Mat()
        mat1 = inputFrame.rgba()
        var size = mat1.size()
        var len = size.width
        var width =size.height
        var w = 500.0
        var h = 500.0
        var x = len/2 - w/2
        var y = width/2 - w/2
        Imgproc.rectangle(mat1, Point(x,y),Point(x+w,y+h), Scalar(25.0,118.0,210.0),2)
        detectButton?.setOnClickListener {
            imageView?.visibility = SurfaceView.VISIBLE
            textConfifdence?.visibility = SurfaceView.VISIBLE
            textPrediction?.visibility = SurfaceView.VISIBLE
            textLabelConfifdence?.visibility = SurfaceView.VISIBLE
            textLabelPrediction?.visibility = SurfaceView.VISIBLE
            var mat2 = imageProcessing(mat1, x, y, w, h)
            size = mat2.size()
            var img: Bitmap = Bitmap.createBitmap(size.width.toInt(), size.height.toInt(),Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat2,img)
            imageView?.setImageBitmap(img)
            classifyimage(img)
        }
        return mat1
    }
    private fun imageProcessing(mat: Mat, x: Double, y: Double, w: Double,h: Double):Mat{
        var roi = Rect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
        var mat2 = Mat(mat,roi)
        var mat3 = Mat()
        Imgproc.cvtColor(mat2,mat3,Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mat3,mat2,Size(35.0,35.0),0.0)
        Imgproc.threshold(mat2,mat3,70.0,255.0,Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU)
        var matrix = Imgproc.getRotationMatrix2D(Point(w/2,h/2),270.0,1.0)
        Imgproc.warpAffine(mat3,mat3,matrix,Size(w,h))
        Imgproc.resize(mat3,mat3,Size(28.0,28.0))
        return mat3
    }
    private fun classifyimage(bitmap: Bitmap) {

        if((bitmap!=null) && (classifier.isInitialized)){
            classifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { (resultText1,resultText2) ->
                    var result = resultText2
                    var prediction = result?.substring(0,1)
                    var confidence = result?.substring(2,9)
                    textPrediction?.setText(prediction)
                    textConfifdence?.setText(confidence)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG,"Error while classifying",e)
                }
        }

    }
}
