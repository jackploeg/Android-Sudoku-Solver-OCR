package nl.trisol.sudokusolver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import nl.trisol.sudokusolver.R
import nl.trisol.sudokusolver.databinding.ActivityMainBinding
import nl.trisol.sudokusolver.imageproc.OCR
import nl.trisol.sudokusolver.imageproc.OpenCV
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.runBlocking
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
        private const val TAG = "SudokuSolver"

        private const val STATUS_NOTHING = -1
        private const val STATUS_PREVIEWING = 1
        private const val STATUS_PROCESS_IMAGE = 2
    }

    private lateinit var javaCameraView: JavaCameraView
    private lateinit var mRGBA: Mat

    private var STATUS = STATUS_PREVIEWING
    private var FLASH_STATE = false

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(viewBinding.root)

        // Activate the camera only if we have the necessary permission, otherwise require the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            activateCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        findViewById<Button>(R.id.camButton).setOnClickListener {
            STATUS = STATUS_PROCESS_IMAGE
        }

        Solver.init(this)

        /**
         * Turn flashlight on / off
         * https://answers.opencv.org/question/131404/opencv-android-implement-flashlight/
         */
        findViewById<Button>(R.id.flashLight).setOnClickListener {
            FLASH_STATE = !FLASH_STATE

            if (FLASH_STATE) {
                javaCameraView.turnOnFlashlight()
                it.setBackgroundResource(R.drawable.baseline_flash_on_24)
            } else {
                javaCameraView.turnOffFlashLight()
                it.setBackgroundResource(R.drawable.baseline_flash_off_24)
            }
        }

        findViewById<Button>(R.id.keyboard).setOnClickListener {
            startActivity(Intent(baseContext, SolverActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!grantResults.contains(PackageManager.PERMISSION_DENIED)) {
                activateCamera()
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.camera_permission_is_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Setup for the JavaCameraView
    private fun activateCamera() {
        javaCameraView = findViewById(R.id.javaCamView)

        javaCameraView.setCameraPermissionGranted()
        javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        javaCameraView.visibility = SurfaceView.VISIBLE
        javaCameraView.setCvCameraViewListener(this)
        javaCameraView.enableView()
    }

    private val mBaseLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.d(TAG, "OpenCV loaded!")
                    activateCamera()
                }

                else -> super.onManagerConnected(status)
            }
        }
    }

    // Initialize (Load) the OpenCV library
    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            mBaseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mBaseLoaderCallback)
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRGBA = Mat(width, height, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mRGBA.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
        if (STATUS == STATUS_PREVIEWING) {
            mRGBA = inputFrame!!.rgba()
            OpenCV.getSudokuBoundingRectangle(mRGBA, true)

            return mRGBA
        } else if (STATUS == STATUS_PROCESS_IMAGE) {
            // If the camera button was pressed prepare the image for processing
            mRGBA = inputFrame!!.rgba()

            // Step 1: Get the bounding rectangle's corner points
            val cntPts = OpenCV.getSudokuBoundingRectangle(mRGBA, false)

            // If the OpenCV.getSudokuBoundingRectangle returns null don't continue and return to previewing
            if (cntPts == null) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.no_board),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Step 2: Order the points clockwise
                val sortedPoints = OpenCV.orderContourPoints(cntPts)

                // Step 3: Crop the sudoku board out of the image and apply a perspective transform
                mRGBA = OpenCV.getCroppedSudokuBoard(mRGBA, sortedPoints)

                // Step 4: Convert the resulting Mat to an InputImage
                val bitmapOrig = SudokuUtils.matToBitmap(mRGBA)

                val height = bitmapOrig.height
                val width = bitmapOrig.width
                val scale = width.toDouble()/height.toDouble()
                val bitmap = Bitmap.createScaledBitmap(bitmapOrig, width, (height * scale).toInt(), false)

                var image = InputImage.fromBitmap(bitmap, 0)

                // Save the bitmap file to access it in the SolverActivity
                val fileOutputStream = baseContext.openFileOutput("tmpBitmap", Context.MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.close()

                // Step 5: Get the sudoku board (2d array) from the image using MLKit Text Recognition
                runBlocking {
                    val sudokuBoard = OCR.getSudokuFromImage(image)

                    // DEBUGGING
                    for (i in 0 until 9) {
                        val array = CharArray(9)
                        for (j in 0 until 9) {
                            array[j] = sudokuBoard[i][j].number.toString()[0]
                        }
                        Log.d(TAG, array.joinToString(" "))
                    }

                    // Step 6: Start the solving activity and finish this one
                    startActivity(Intent(baseContext, SolverActivity::class.java).apply {
                        putExtra("sudokuBoard", sudokuBoard)
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    })
                }
            }
            STATUS = STATUS_PREVIEWING
        }
        return null
    }
}