package com.example.savenight

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import java.util.stream.DoubleStream.builder
import java.util.stream.IntStream.builder

class DrunkTest : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var beer: ImageView
    private lateinit var greencircle: ImageView
    private lateinit var redcircle: ImageView
    private lateinit var startButton: Button
    //Timer
    private var startTimeCenter: Long = 0
    private var endTimeCenter: Long = 0
    private var timeTakenCenter: Long = 0
    private var inCenter= false
    //Timer
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var timeTaken: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drunk_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        beer = view.findViewById(R.id.beerIcon)
        greencircle = view.findViewById(R.id.greenCircle)
        redcircle = view.findViewById(R.id.redCircle)
        startButton = view.findViewById(R.id.startTestButton)

        setupSensor()

        startButton.setOnClickListener {
            if(startButton.text == "Start Test"){
                //reset timer
                startTime = 0
                endTime = 0
                timeTaken = 0
                //reset timer
                startTimeCenter = 0
                endTimeCenter = 0
                timeTakenCenter = 0
                inCenter = false

                startTime= System.currentTimeMillis()
                startButton.text = "Stop Test"
                greencircle.visibility = View.INVISIBLE
                redcircle.visibility = View.VISIBLE
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
            }else{
                startButton.text = "Start Test"
                greencircle.visibility = View.INVISIBLE
                redcircle.visibility = View.INVISIBLE
                sensorManager.unregisterListener(this)
            }
        }


    }

    private fun setupSensor() {
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val x = event.values[0]
            val y = event.values[1]


            beer.apply {

                translationX = x * -40
                translationY = y * 65
            }
            endTime = System.currentTimeMillis()
            timeTaken = endTime - startTime

            if (timeTaken > 40000) {
                greencircle.visibility = View.INVISIBLE
                redcircle.visibility = View.INVISIBLE
                sensorManager.unregisterListener(this)
                startButton.text = "Start Test"
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Test Complete")
                builder.setMessage("You are 100% drunk! Please call a taxi or a friend to pick you up.")
                builder.setPositiveButton("OK"){dialog, which ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.setCancelable(false)
                dialog.show()
            }
            if (x.toInt()>-0.5 && x.toInt()<0.5 && y.toInt()>-0.5 && y.toInt()<0.5) {
                if(!inCenter){
                    startTimeCenter = System.currentTimeMillis()
                    inCenter = true
                }
                //see if passed 5 seconds
                endTimeCenter = System.currentTimeMillis()
                timeTakenCenter = endTimeCenter - startTimeCenter
                Log.d("TimeCenter", timeTakenCenter.toString())
                if(timeTakenCenter > 5000){
                    endTime = System.currentTimeMillis()
                    timeTaken = endTime - startTime
                    val time = timeTaken.toInt()
                    Log.d("Time", time.toString())
                    val percent = (time/400).toDouble()

                    Log.d("Time", timeTaken.toString())
                    Log.d("Percent", percent.toString())
                    //passed test
                    greencircle.visibility = View.INVISIBLE
                    redcircle.visibility = View.INVISIBLE
                    sensorManager.unregisterListener(this)
                    startButton.text = "Start Test"
                    //alert user his percentage
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Test Complete")
                    builder.setMessage("You are $percent% drunk")
                    builder.setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.setCancelable(false)
                    dialog.show()


                }
                else{
                    greencircle.visibility = View.VISIBLE
                    redcircle.visibility = View.INVISIBLE
                }

            } else {
                if (inCenter){

                    inCenter = false
                }
                greencircle.visibility = View.INVISIBLE
                redcircle.visibility = View.VISIBLE
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}