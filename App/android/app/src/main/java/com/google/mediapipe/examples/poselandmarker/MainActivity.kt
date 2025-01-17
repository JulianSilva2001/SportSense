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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper.Companion.TAG
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import android.media.MediaPlayer
import android.view.View
import android.widget.Button
import com.google.mediapipe.examples.poselandmarker.techniques.ActivitySprint


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var button1: Button
    private lateinit var button2: Button







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)





        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "No message"
        val technique = intent.getStringExtra("EXTRA_TECHNIQUE") ?: "No message"
        val userName = intent.getStringExtra("USER_NAME") ?: "No message"

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)

        button1.setOnClickListener {
            Intent(this, MainActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, ActivitySprint::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("EXTRA_MESSAGE", message)
            startActivity(intent)
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.sound)


        // Update the OverlayView with the message
        OverlayView.updateMessage(message)
        OverlayView.updateTechnique(technique)
        OverlayView.updateUsername(userName)







        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection

        }


    }



    override fun onBackPressed() {
        finish()
    }

    private fun playSound() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }






}