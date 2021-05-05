package com.example.farfuture

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
import okhttp3.internal.http2.Huffman
import java.util.*
import kotlin.collections.ArrayList

var data_type : String? = null

class DataGraph : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datagraph)

        val backButton = findViewById<Button>(R.id.data_back_button)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.data_types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        val app : FarFutureApp = application as FarFutureApp
        //app.getSocket()?.emit("server", "server")

        val graph : GraphView = findViewById(R.id.graph)

        var labels : StaticLabelsFormatter = StaticLabelsFormatter(graph)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)

        class myListener : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    data_type = parent.getItemAtPosition(position) as String?
                }

                var newSeries: LineGraphSeries<DataPoint>? = null


                if (data_type != null) {
                    when {
                        data_type.equals("Humidity") -> {
                            Log.d("Graph", "Humidity selected")
                            //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Humidity))
                            newSeries = LineGraphSeries<DataPoint>(intArrToDatePointArr(app.Humidity, app.Time))
                            Log.d("Graph", "elements:" + app.Humidity)
                        }
                        data_type.equals("Temperature") -> {
                            Log.d("Graph", "Temperature selected")
                            //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Tempurature))
                            newSeries = LineGraphSeries<DataPoint>(intArrToDatePointArr(app.Tempurature, app.Time))
                            Log.d("Graph", "elements:" + app.Tempurature)
                        }
                        data_type.equals("Light Level") -> {
                            Log.d("Graph", "Light Level selected")
                            //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.LightLevel))
                            newSeries = LineGraphSeries<DataPoint>(intArrToDatePointArr(app.LightLevel, app.Time))
                        }
                        data_type.equals("Pressure") -> {
                            Log.d("Graph", "Pressure selected")
                            //newSeries = LineGraphSeries<DataPoint>(intArrToDataPointArr(app.Pressure))
                            newSeries = LineGraphSeries<DataPoint>(intArrToDatePointArr(app.Pressure, app.Time))
                        }
                    }

                    if (newSeries != null) {
                        Log.d("Graph", "New graph generated")
                        graph.removeAllSeries()
                        graph.gridLabelRenderer.numHorizontalLabels = app.Time.size
                        //graph.viewport.setMinX(app.Time[0].time.toDouble())
                        //graph.viewport.setMaxX(app.Time[app.Time.size - 1].time.toDouble())
                        graph.gridLabelRenderer.setHumanRounding(false)
                        graph.addSeries(newSeries)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                data_type = null
            }

        }
        spinner.onItemSelectedListener = myListener()
    }

    fun toDataPointArr (list : MutableList<Double>) : Array<DataPoint> {
        return Array(list.size) { it -> DataPoint(it.toDouble(), list[it])  }
    }

    fun intArrToDataPointArr (list : MutableList<Int>) : Array<DataPoint> {
        val doubleList = toDoubleList(list)
        return Array(list.size) { it -> DataPoint(it.toDouble(), doubleList[it])  }
    }

    fun intArrToDatePointArr (list : MutableList<Int>, dateList : MutableList<Date>) : Array<DataPoint> {
        val doubleList = toDoubleList(list)
        return Array(list.size) { it -> DataPoint(dateList[it], doubleList[it])  }
    }

    private fun toDoubleList (list : MutableList<Int>) : MutableList<Double> {
        return MutableList<Double>(list.size) {it -> list[it].toDouble()}
    }



}


