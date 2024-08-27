package com.google.mediapipe.examples.poselandmarker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.mediapipe.examples.poselandmarker.techniques.ActivitySprint

private var sport= "Sprint"
private var activity= "no"

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val buttonS = findViewById<Button>(R.id.button5)
        buttonS.setOnClickListener {
            sport= "Sprint"
            //activity = "ActivitySprint"
            callActivity()
        }
        val buttonW = findViewById<Button>(R.id.button7)
        buttonW.setOnClickListener {
            sport= "Workout"
            //activity = "ActivityWorkout"

            callActivity()
        }
    }

    private fun callActivity() {


        Intent(this, ActivitySprint::class.java).also {
            it.putExtra("EXTRA_MESSAGE", sport
            )


            startActivity(it)
        }
    }

//    private fun callActivity() {
//        val targetActivity = when (activity) {
//            "ActivityWorkout" -> ActivitySprint::class.java
//            "ActivitySprint" -> ActivitySprint::class.java
//            else -> null
//        }
//
//        targetActivity?.let {
//            val intent = Intent(this, it).apply {
//                putExtra("EXTRA_MESSAGE", sport)
//            }
//            startActivity(intent)
//        } ?: run {
//            // Handle the case where the activity is not found
//            Log.e("MainActivity2", "Activity not found")
//        }
//    }
}