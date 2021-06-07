package com.example.farfuture

import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

// user input from spinner
var data_type : String? = null

class DataGraph : AppCompatActivity() {

    var timeArr : MutableList<Date>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datagraph)

        // Setup button to return to main menu
        val backButton = findViewById<Button>(R.id.data_back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set spinner contents
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.data_types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        updateGraphInternal(spinner.selectedItem.toString())

        // Enable date axis labels
        val graph : GraphView = findViewById(R.id.graph)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)

        // Spinner user input management
        class myListener : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    data_type = parent.getItemAtPosition(position) as String?
                }
                data_type?.let { updateGraphInternal(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                data_type = null
            }
        }
        spinner.onItemSelectedListener = myListener()


        // Update graph in real time
        val updateHandler : Handler = Handler(Looper.getMainLooper())
        class updateRunner() : Runnable {
            override fun run() {
                val dataType = spinner.selectedItem.toString()
                updateGraphInternal(dataType)
                updateHandler.postDelayed(this, 2000)
            }

        }
        updateHandler.post(updateRunner())

    }

    private fun updateGraphInternal (dataType : String) {
        Log.d("Graph", "\nUpdating Graph")
        val app : FarFutureApp = application as FarFutureApp

        // Copy all data arrays so that inserts don't break
        // the graphing logic
        timeArr = MutableList(app.Time.size) { it -> Date.from((app.Time[it] as LocalDateTime).atZone(ZoneId.systemDefault()).toInstant())  }

        val humidity = ArrayList<Double>()
        humidity.addAll(app.Humidity)

        val temperature = ArrayList<Double>()
        temperature.addAll(app.Tempurature)

        val lightLevel = ArrayList<Double>()
        lightLevel.addAll(app.LightLevel)

        val pressure = ArrayList<Double>()
        pressure.addAll(app.Pressure)

        val soilMoisture = ArrayList<Double>()
        soilMoisture.addAll(app.soilMoisture)

        val graph : GraphView = findViewById(R.id.graph)

        var newSeries: LineGraphSeries<DataPoint>? = null

        // Select data array based on input from spinner
        if (data_type != null) {
            when {
                data_type.equals("Humidity") -> {
                    Log.d("Graph", "Humidity selected")
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(humidity, timeArr))
                    app.Humidity.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Humidity (%)"
                }
                data_type.equals("Temperature") -> {
                    Log.d("Graph", "Temperature selected")
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(temperature, timeArr))
                    app.Tempurature.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Temperature (C°)"
                }
                data_type.equals("Light Level") -> {
                    Log.d("Graph", "Light Level selected")
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(lightLevel, timeArr))
                    app.LightLevel.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Light Level"
                }
                data_type.equals("Pressure") -> {
                    Log.d("Graph", "Pressure selected")
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(pressure, timeArr))
                    app.Pressure.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Humidity (hPa)"
                }
                data_type.equals("Soil Moisture") -> {
                    Log.d("Graph", "Soil Moisture selected")
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(soilMoisture, timeArr))
                    app.soilMoisture.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Humidity (hPa)"
                }
            }

            if (newSeries != null) {
                Log.d("Graph", "New graph generated")
                Log.d("Graph", "Elements:" + timeArr!!.size.toString())
                Log.d("Graph", timeArr.toString())

                // Remove previous graph
                graph.removeAllSeries()

                // Set xy bounds
                timeArr!!.min().let {
                    if (it != null) {
                        graph.viewport.setMinX(it.time.toDouble())
                    }
                }
                timeArr!!.max().let {
                    if (it != null) {
                        graph.viewport.setMaxX(it.time.toDouble())
                    }
                }
                graph.viewport.setMinY(0.0)

                // Set graph display settings
                graph.gridLabelRenderer.setHumanRounding(true)
                graph.gridLabelRenderer.horizontalAxisTitle = "Time"
                graph.viewport.isYAxisBoundsManual = true
                graph.viewport.isXAxisBoundsManual = true

                // Add new series
                graph.addSeries(newSeries)
            }
        }
    }

    private fun arrToDatePointArr (list : MutableList<Double>, dateList : MutableList<Date>?) : Array<DataPoint> {
        Log.d("Graph", "sizes  doublelist:${list.size} datelist:${dateList?.size}")
        return Array(list.size) { i -> DataPoint(dateList?.get(i), list[i])  }
    }
}



