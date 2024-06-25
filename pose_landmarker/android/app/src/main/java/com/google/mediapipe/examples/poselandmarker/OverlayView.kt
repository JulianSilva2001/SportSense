/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper.Companion.TAG
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var circlePaint = Paint()

    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var redLinePaint = Paint()
    private var range = 30
    private var joint1 =0
    private var joint2 =0
    private var joint3 =0
    private var expectedAngle=0.0
    private var angle=0.0
    private var radius=15f





    private val tickPaint = Paint().apply {
        color = Color.GREEN  // Color of the tick mark
        strokeWidth = 50f    // Stroke width of the tick mark
        style = Paint.Style.STROKE  // Draw only the outline
        strokeCap = Paint.Cap.ROUND  // Rounded stroke ends for a nicer tick appearance
        isAntiAlias = true   // Smooth edges
        // Shadow layer for a modern look
        setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }








    private var textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }

    private val allJoints = mutableListOf<Map<String, Any>>()









    init {

        sport= getSport()
        technique= getTechnique()

        initPaints()
        fetchAndStoreData(sport, technique, "joints")
        addNewTechnique()

    }



    private fun fetchAndStoreData(sportName: String, techniqueName: String, subcollectionName: String) {
        FirebaseManager.fetchJointsAndAngles(sportName, techniqueName, subcollectionName) { joints ->
            allJoints.clear()
            allJoints.addAll(joints.map { joint ->
                val joint1 = (joint["joint1"] as Long).toInt()
                val joint2 = (joint["joint2"] as Long).toInt()
                val expectedAngle = joint["expectedAngle"] as Long

                val jointMap = mutableMapOf<String, Any>(
                    "joint1" to joint1,
                    "joint2" to joint2,
                    "expectedAngle" to expectedAngle
                )

                if (joint.containsKey("joint3")) {
                    val joint3 = (joint["joint3"] as Long).toInt()
                    jointMap["joint3"] = joint3
                }

                jointMap
            })
            invalidate() // Call this to redraw the view with the new data
            Log.d(TAG, "Updated joints data: $allJoints")
        }
    }





    private fun addNewTechnique() {
        val sportName = "Sprint"
        val techniqueName = "Technique1"
        val jointsData = listOf(
            mapOf("joint1" to 24, "joint2" to 26, "joint3" to 28, "expectedAngle" to 90),
            mapOf("joint1" to 12, "joint2" to 14,  "expectedAngle" to 90),
            mapOf("joint1" to 23, "joint2" to 25, "joint3" to 27, "expectedAngle" to 120)
            // Add more joint sets as needed
        )
        FirebaseManager.addTechnique(sportName, techniqueName, jointsData)
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        circlePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.GREEN
        //ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        redLinePaint.color = Color.RED
        redLinePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        redLinePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.WHITE
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        circlePaint.color = Color.WHITE
        circlePaint.strokeWidth = LANDMARK_STROKE_WIDTH-5f
        circlePaint.style = Paint.Style.STROKE

        textPaint.apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) // Example: Bold text
            isAntiAlias = true // Smooth edges
            // Optionally, add shadow
            setShadowLayer(5f, 0f, 0f, Color.BLACK) // Example: Adds a black shadow
        }
    }



    private fun getLineColor(angle: Double, expectedAngle: Double, range: Int): Int {
        val deviation = kotlin.math.abs(angle - expectedAngle).toFloat()
        val maxDeviation = range.toFloat()
        val fraction = deviation / maxDeviation
        val green = Color.GREEN
        val red = Color.RED

        return ArgbEvaluator().evaluate(fraction, green, red) as Int
    }





    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            val linesToDraw = mutableListOf<LineData>()
            var allAnglesValid = false
            var set1isInExpectedRange = false
            var set2isInExpectedRange = false

            // Process landmarks and angles
            for (landmark in poseLandmarkerResult.landmarks()) {
                Log.d(TAG, "brooo: $allJoints")
                // Draw points
//                for (normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        normalizedLandmark.x() * imageWidth * scaleFactor,
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )
//                }

                // Calculate angles and determine line colors
                for ((index, angleSet) in allJoints.withIndex()) {
                    val aSize= angleSet.size

                    Log.d(TAG, "index: $index")
                    Log.d(TAG, "brooo: $aSize")
                    Log.d(TAG, "index: $sport")
                    Log.d(TAG, "index: $technique")

                    if (angleSet.size ==4) {
                        joint1 = angleSet["joint1"] as Int
                        joint2 = angleSet["joint2"] as Int
                        joint3 = angleSet["joint3"] as Int
                        expectedAngle = (angleSet["expectedAngle"] as Long).toDouble()
                        angle = getAngle(landmark, joint1, joint2, joint3)
                    }
                    else {
                        joint1 = angleSet["joint1"] as Int
                        joint2 = angleSet["joint2"] as Int
                        expectedAngle = (angleSet["expectedAngle"] as Long).toDouble()
                        angle = getAngle(landmark, joint1, joint2)

                    }

                    if (index == 0) { set1isInExpectedRange = isAngleInExpectedRange(angle, expectedAngle, 15)
                    } else if ((index == 1)) { set2isInExpectedRange = isAngleInExpectedRange(angle, expectedAngle, 15)
                    } else if ((index == 2)) { set2isInExpectedRange = isAngleInExpectedRange(angle, expectedAngle, 15)
                    } else if ((index == 3)) { set2isInExpectedRange = isAngleInExpectedRange(angle, expectedAngle, 15)
                    }







                    val useRedPaint = (angle < expectedAngle - range || angle > expectedAngle + range)



                    if (set1isInExpectedRange && set2isInExpectedRange ) {
                        allAnglesValid = true
                    }

                    Log.d(TAG, "Angle: $angle")

                    // Store line data
                    PoseLandmarker.POSE_LANDMARKS.forEach { poseLandmark ->
                        val startX = poseLandmarkerResult.landmarks()[0][poseLandmark!!.start()].x() * imageWidth * scaleFactor
                        val startY = poseLandmarkerResult.landmarks()[0][poseLandmark.start()].y() * imageHeight * scaleFactor
                        val endX = poseLandmarkerResult.landmarks()[0][poseLandmark.end()].x() * imageWidth * scaleFactor
                        val endY = poseLandmarkerResult.landmarks()[0][poseLandmark.end()].y() * imageHeight * scaleFactor





                        if (angleSet.size ==4){

                            if ((poseLandmark.start() == joint2 && poseLandmark.end() == joint3) ||
                                (poseLandmark.start() == joint1 && poseLandmark.end() == joint2) ||
                                (poseLandmark.start() == joint3 && poseLandmark.end() == joint1)) {
                                linesToDraw.add(LineData(startX, startY, endX, endY, angle, expectedAngle, useRedPaint))
                                if ((poseLandmark.start() == joint2 && poseLandmark.end() == joint3)  ) {
                                    canvas.drawText(String.format("%.1f", angle), startX-30f, startY, textPaint)}


                            }
                        }
                        else if (angleSet.size ==3){
                            if ((poseLandmark.start() == joint2 && poseLandmark.end() == joint1) ||
                                (poseLandmark.start() == joint1 && poseLandmark.end() == joint2) ) {
                                linesToDraw.add(LineData(startX, startY, endX, endY, angle, expectedAngle, useRedPaint))
                                canvas.drawText(String.format("%.1f", angle), (startX+endX)/2-30f, (startY+endY)/2, textPaint)

                                // Draw a tick mark (a simple check mark shape)
//                                if (isInExpectedRange) {
//                                    // Draw tick mark if angle is within range
//                                    drawTickMark(canvas, startX, startY, endX, endY)
 //                               }

                            }
                        }

                    }
                }
            }

            // Draw all lines
            for (lineData in linesToDraw) {

                if (lineData.angle != null) {
                    // Draw angle text

                    // Determine line color
                    linePaint.color = getLineColor(lineData.expectedAngle!!, lineData.angle, range)
                    canvas.drawLine(lineData.startX, lineData.startY, lineData.endX, lineData.endY, if (lineData.useRedPaint) redLinePaint else linePaint)
                    canvas.drawCircle(
                        lineData.startX ,
                        lineData.startY ,
                        radius+10f,
                        circlePaint)

                    canvas.drawCircle(
                        lineData.startX ,
                        lineData.startY ,
                        radius,
                        pointPaint)

                    canvas.drawCircle(
                        lineData.endX ,
                        lineData.endY ,
                        radius+10f,
                        circlePaint)

                    canvas.drawCircle(
                        lineData.endX ,
                        lineData.endY ,
                        radius,
                        pointPaint)


                } else {
                    linePaint.color = Color.GREEN
                    canvas.drawLine(lineData.startX, lineData.startY, lineData.endX, lineData.endY, linePaint)
                }
            }
            if (allAnglesValid) {
                drawTickMark(canvas)
            }
        }
    }

    // Helper data class to store line information
    data class LineData(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val angle: Double?,
        val expectedAngle: Double?,
        val useRedPaint: Boolean
    )

    private fun isAngleInExpectedRange(angle: Double, expectedAngle: Double, range: Int): Boolean {
        return angle >= (expectedAngle - range) && angle <= (expectedAngle + range)
    }

    private fun drawTickMark(canvas: Canvas) {
        // Example implementation of drawing a tick mark
        val width = width.toFloat()
        val height = height.toFloat()

        val tStartX = 0.25f * width
        val tStartY = 0.6f * height
        val tMidX = 0.45f * width
        val tMidY = 0.8f * height
        val tEndX = 0.75f * width
        val tEndY = 0.4f * height

        canvas.drawLine(tStartX, tStartY, tMidX, tMidY, tickPaint)
        canvas.drawLine(tMidX, tMidY, tEndX, tEndY, tickPaint)
    }






    private fun drawTickMark(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        // Draw a tick mark (a simple check mark shape)
        val width = width.toFloat()
        val height = height.toFloat()

        // Adjust tick mark positions and sizes as needed
        val tStartX = 0.25f * width
        val tStartY = 0.6f * height
        val tMidX = 0.45f * width
        val tMidY = 0.8f * height
        val tEndX = 0.75f * width
        val tEndY = 0.4f * height

        // Draw the tick mark
        canvas.drawLine(tStartX, tStartY, tMidX, tMidY, tickPaint)
        canvas.drawLine(tMidX, tMidY, tEndX, tEndY, tickPaint)
    }


    // Helper data class to store line information


    // Function to calculate the angle between three points
    private fun getAngle(landmarks: MutableList<NormalizedLandmark>, index1: Int, index2: Int, index3: Int): Double {
        if (landmarks.size > index1 && landmarks.size > index2 && landmarks.size > index3) {
            return calculateAngle(
                landmarks[index1].x(), landmarks[index2].x(), landmarks[index3].x(),
                landmarks[index1].y(), landmarks[index2].y(), landmarks[index3].y()
            )
        } else {
            Log.e(PoseLandmarkerHelper.TAG, "Invalid landmark indices provided.")
            return -1.0 // Return -1.0 to indicate an error
        }
    }

    private fun getAngle(landmarks: MutableList<NormalizedLandmark>, index1: Int, index2: Int): Double {
        if (landmarks.size > index1 && landmarks.size > index2 ) {
            return calculateAngle(
                landmarks[index1].x(), landmarks[index2].x(),
                landmarks[index1].y(), landmarks[index2].y()
            )
        } else {
            Log.e(PoseLandmarkerHelper.TAG, "Invalid landmark indices provided.")
            return -1.0 // Return -1.0 to indicate an error
        }
    }

    // Function to calculate the angle
    private fun calculateAngle(x1: Float, x2: Float, x3: Float, y1: Float, y2: Float, y3: Float): Double {
        val angle = Math.toDegrees(
            (atan2(y3 - y2, x3 - x2) -
                    atan2(y1 - y2, x1 - x2)).toDouble()
        )

        val absAngle = kotlin.math.abs(angle) % 360
        return if (absAngle > 180) 360 - absAngle else absAngle
    }

    private fun calculateAngle(x1: Float, x2: Float,  y1: Float, y2: Float): Double {
        val angle = Math.toDegrees(
            (atan2(y1 - y2, x1 - x2)).toDouble()
        )

        val absAngle = kotlin.math.abs(angle) % 360
        return if (absAngle > 180) 360 - absAngle else absAngle
    }






    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {

        private var sport: String = ""
        private var technique: String = ""

        fun updateMessage(s: String) {
            sport = s

        }

        fun getSport(): String {
            return sport
        }

        fun updateTechnique(t: String) {
            technique = t

        }

        fun getTechnique(): String {
            return technique
        }

        private const val LANDMARK_STROKE_WIDTH = 12F
    }




}