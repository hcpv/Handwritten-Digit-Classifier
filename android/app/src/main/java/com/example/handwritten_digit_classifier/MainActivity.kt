package com.example.handwritten_digit_classifier

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import com.divyanshu.draw.widget.DrawView
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }
    private var drawView: DrawView? = null
    private var clearButton: Button? = null
    private var predictedTextView: TextView? = null
    private var classifier = Classifier(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawView = findViewById(R.id.draw_view)
        drawView?.setStrokeWidth(70.0f)
        drawView?.setColor(Color.WHITE)
        drawView?.setBackgroundColor(Color.BLACK)
        clearButton = findViewById(R.id.clear_button)
        predictedTextView =findViewById(R.id.predicted_text)

        clearButton?.setOnClickListener {
            drawView?.clearCanvas()
            predictedTextView?.text = "Please draw a digit"
        }

        classifier.initialize().addOnFailureListener{e-> Log.e(TAG,"Error to setting up classifier",e)}

        drawView?.setOnTouchListener { _, event ->
            drawView?.onTouchEvent(event)
            if(event.action == MotionEvent.ACTION_UP)
            {
                classifyDrawing()
            }
            true
        }
    }

    private fun classifyDrawing() {
        val bitmap = drawView?.getBitmap()

        if((bitmap!=null) && (classifier.isInitialized)){
            classifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { resultText -> predictedTextView?.text = resultText }
                .addOnFailureListener { e ->
                    predictedTextView?.text = "Error"
                    Log.e(TAG,"Error while classifying",e)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
