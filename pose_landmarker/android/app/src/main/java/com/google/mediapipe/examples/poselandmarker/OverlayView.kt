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
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper.Companion.TAG
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.abs
import kotlin.math.atan2

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var redLinePaint = Paint()
    private var range = 30
    private var expAngle1: Double= 90.0
    private var expAngle2: Double= 120.0

    private var textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }


    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
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

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun getLineColor(angle: Double, expectedAngle: Double, range: Int): Int {
        val deviation = abs(angle - expectedAngle).toFloat()
        val maxDeviation = range
        return ArgbEvaluator().evaluate(
            (deviation / maxDeviation).toFloat(),
            Color.RED,
            Color.GREEN
        ) as Int
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                // Calculate angle between landmarks 28, 26, 24


                val angle1 = getAngle(landmark, 28, 26, 24)
                val useRedPaint1 = (angle1 < expAngle1- range || angle1 > expAngle1 + range)

                val angle2 = getAngle(landmark, 27, 25, 23)
                val useRedPaint2 = (angle2 < expAngle2 - range || angle2 >expAngle2 + range)

                Log.d(TAG, "Angle: $angle1")
                Log.d(TAG, "Angle: $angle2")

                PoseLandmarker.POSE_LANDMARKS.forEach { poseLandmark ->
                    val startX = poseLandmarkerResult.landmarks()[0][poseLandmark!!.start()].x() * imageWidth * scaleFactor
                    val startY = poseLandmarkerResult.landmarks()[0][poseLandmark.start()].y() * imageHeight * scaleFactor
                    val endX = poseLandmarkerResult.landmarks()[0][poseLandmark.end()].x() * imageWidth * scaleFactor
                    val endY = poseLandmarkerResult.landmarks()[0][poseLandmark.end()].y() * imageHeight * scaleFactor

                    Log.d(TAG, "Angle: $poseLandmark.start()")

                    if ((poseLandmark.start() == 26 && poseLandmark.end() == 28) ||
                        (poseLandmark.start() == 24 && poseLandmark.end() == 26) ||
                        (poseLandmark.start() == 28 && poseLandmark.end() == 24)
                    ) {
                        canvas.drawText(String.format("%.1f", angle1), startX, startY, textPaint)
                        linePaint.color= getLineColor(expAngle1, angle1, range)
                        canvas.drawLine(startX, startY, endX, endY, if (useRedPaint1) redLinePaint else linePaint)
                    }

                    else if ((poseLandmark.start() == 25 && poseLandmark.end() == 27) ||
                        (poseLandmark.start() == 23 && poseLandmark.end() == 25) ||
                        (poseLandmark.start() == 27 && poseLandmark.end() == 23)
                    ) {
                        canvas.drawText(String.format("%.1f", angle2), startX, startY, textPaint)
                        linePaint.color= getLineColor(expAngle2, angle2, range)
                        canvas.drawLine(startX, startY, endX, endY, if (useRedPaint2) redLinePaint else linePaint)
                    } else {
                        linePaint.color=Color.GREEN
                        canvas.drawLine(startX, startY, endX, endY, linePaint)
                    }
                }
            }
        }
    }

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

    // Function to calculate the angle
    private fun calculateAngle(x1: Float, x2: Float, x3: Float, y1: Float, y2: Float, y3: Float): Double {
        val angle = Math.toDegrees(
            (atan2(y3 - y2, x3 - x2) -
                    atan2(y1 - y2, x1 - x2)).toDouble()
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
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}