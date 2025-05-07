package com.example.neversnooze

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neversnooze.ml.AutoModel2
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import android.widget.TextView
import android.content.Intent
import android.os.Vibrator

//private val targetObjects = listOf("chair", "toilet", "sink", "laptop", "keyboard", "refrigerator")
//private val targetObjects = listOf("chair", "laptop", "keyboard", "tv", "mouse", "cell phone")
private lateinit var targetLabel: String
private var detected = false
private var cameraCaptureSession: CameraCaptureSession? = null

class ObjectDetection : AppCompatActivity() {

    lateinit var labels:List<String>
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    val paint = Paint()
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler:Handler
    lateinit var cameraManager: CameraManager
    lateinit  var textureView: TextureView
    lateinit var model:AutoModel2
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.cancel()
        enableEdgeToEdge()
        setContentView(R.layout.object_detection)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        detected = false // ← Reset here
        get_permission()

        labels = FileUtil.loadLabels(this, "labels.txt")

        //targetLabel = targetObjects.random() // Pick random target
        targetLabel = intent.getStringExtra("TARGET_OBJECT") ?: "chair" // ✅ Default fallback
        val promptText = findViewById<TextView>(R.id.promptText)
        promptText.text = "Find a $targetLabel!"

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = AutoModel2.newInstance(this)

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!

                // Creates inputs for reference.
                var image = TensorImage.fromBitmap(bitmap)
                
                image = imageProcessor.process(image)

                // Runs model inference and gets result.
                val outputs = model.process(image)
//                val detectionResult = outputs.detectionResultList.get(0)

                // Gets result from DetectionResult.
//                val location = detectionResult.locationAsRectF;
//                val category = detectionResult.categoryAsString;
//                val score = detectionResult.scoreAsFloat;

                val locations = outputs.locationAsTensorBuffer.floatArray
                val classes = outputs.categoryAsTensorBuffer.floatArray
                val scores = outputs.scoreAsTensorBuffer.floatArray
                val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer
                
                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)

                val h = mutable.height
                val w = mutable.width

                paint.textSize = h/15f
                paint.strokeWidth = h/85f
                var x = 0
                scores.forEachIndexed{index, fl ->
                    x = index
                    x *= 4
                    if(fl > 0.7){
                        paint.setColor(colors.get(index))
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(locations.get(x+1)*w, locations.get(x)*h, locations.get(x+3)*w, locations.get(x+2)*h), paint)
                        paint.style = Paint.Style.FILL
                        // canvas.drawText(labels.get(classes.get(index).toInt())+" "+fl.toString(), locations.get(x+1)*w, locations.get(x)*h, paint)

                        val detectedLabel = labels.getOrNull(classes.get(index).toInt()) ?: ""
                        canvas.drawText("$detectedLabel $fl", locations.get(x+1)*w, locations.get(x)*h, paint)

                        // Check match and move to Congrats screen once
                        if (!detected && detectedLabel.equals(targetLabel, ignoreCase = true)) {
                            detected = true
                            // Forwards all alarm details (for snoozing)
                            val congrats = Intent(
                                this@ObjectDetection,
                                CongratulationsActivity::class.java
                            ).apply {
                                putExtras(intent.extras ?: Bundle())
                                putExtra("TARGET_OBJECT", targetLabel)
                            }
                            startActivity(congrats)
                            finish() // closes camera screen
                        }


                    }
                }
                imageView.setImageBitmap(mutable)
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()

        try {
            cameraCaptureSession?.stopRepeating()
            cameraCaptureSession?.close()
            cameraDevice.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                var surfaceTexture = textureView.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object:CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession?.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {

                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }

    fun get_permission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            get_permission()
        }
    }
}