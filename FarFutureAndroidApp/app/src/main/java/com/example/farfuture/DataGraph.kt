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

var data_type : String? = null

class DataGraph : AppCompatActivity() {

    var timeArr : MutableList<Date>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datagraph)

        val backButton = findViewById<Button>(R.id.data_back_button)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val app : FarFutureApp = application as FarFutureApp
        Log.d("Graph", "TIMESIZE: ${app.Time.size}")
        //timeArr = MutableList(app.Time.size) { it -> Date.from((app.Time[it] as LocalDateTime).atZone(ZoneId.systemDefault()).toInstant())  }
        Log.d("Graph", "DATESIZE: ${timeArr?.size}")
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.data_types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter



        //app.getSocket()?.emit("server", "server")

        val graph : GraphView = findViewById(R.id.graph)

        var labels : StaticLabelsFormatter = StaticLabelsFormatter(graph)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)

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
        if (data_type != null) {
            when {
                data_type.equals("Humidity") -> {
                    Log.d("Graph", "Humidity selected")
                    //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Humidity))
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(humidity, timeArr))
                    //Log.d("Graph", "elements:" + app.Humidity)
                    app.Humidity.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Humidity (%)"//resources.getStringArray(R.array.data_types_array)[0]
                }
                data_type.equals("Temperature") -> {
                    Log.d("Graph", "Temperature selected")
                    //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Tempurature))
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(temperature, timeArr))
                    //Log.d("Graph", "elements:" + app.Tempurature)
                    app.Tempurature.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Temperature (C°)"//resources.getStringArray(R.array.data_types_array)[0]
                }
                data_type.equals("Light Level") -> {
                    Log.d("Graph", "Light Level selected")
                    //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.LightLevel))
                    newSeries = LineGraphSeries<DataPoint>(arrToDatePointArr(lightLevel, timeArr))
                    app.LightLevel.max()?.let { graph.viewport.setMaxY(it.toDouble() * 1.5) }
                    graph.gridLabelRenderer.verticalAxisTitle = "Light Level"
                }
                data_type.equals("Pressure") -> {
                    Log.d("Graph", "Pressure selected")
                    //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Pressure))
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
                graph.removeAllSeries()
                //graph.gridLabelRenderer.numHorizontalLabels = 7
                //graph.gridLabelRenderer.numVerticalLabels = 10
                //graph.viewport.setMinX(app.Time[0].time.toDouble())
                //graph.viewport.setMaxX(app.Time[app.Time.size - 1].time.toDouble())

                Log.d("Graph", "Elements:" + app.Time.size.toString())
                graph.gridLabelRenderer.setHumanRounding(true)
                graph.addSeries(newSeries)
                graph.viewport.setMinY(0.0)
                graph.gridLabelRenderer.horizontalAxisTitle = "Time"
                graph.viewport.isYAxisBoundsManual = true
            }
        }
    }


    private fun toDataPointArr (list : MutableList<Double>) : Array<DataPoint> {
        return Array(list.size) { it -> DataPoint(it.toDouble(), list[it])  }
    }

    private fun intArrToDataPointArr (list : MutableList<Int>) : Array<DataPoint> {
        val doubleList = toDoubleList(list)
        return Array(list.size) { it -> DataPoint(it.toDouble(), doubleList[it])  }
    }

    private fun arrToDatePointArr (list : MutableList<Double>, dateList : MutableList<Date>?) : Array<DataPoint> {
        //val doubleList = toDoubleList(list)
        //Log.d("??", doubleList.toString())
        //Log.d("??", dateList.toString())
        Log.d("Graph", "sizes  doublelist:${list.size} datelist:${dateList?.size}")

        val temp = Array(list.size) { i -> DataPoint(dateList?.get(i), list[i])  }
        //val temp = Array(doubleList.size) { i -> DataPoint(i.toDouble(), i.toDouble()) }


        return temp
    }

    private fun toDoubleList (list : MutableList<Int>) : MutableList<Double> {
        return MutableList<Double>(list.size) {i -> list[i].toDouble()}
    }

    override fun onDestroy() {
        super.onDestroy()
        val app : FarFutureApp = application as FarFutureApp
        app.disconnectSocket()
    }
}



