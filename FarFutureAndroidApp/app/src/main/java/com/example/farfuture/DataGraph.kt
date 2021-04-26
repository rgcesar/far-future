package com.example.farfuture

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

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


        val arr_h = arrayOf( DataPoint(0.0,  1.0)
                ,DataPoint(1.0,  3.0)
                ,DataPoint(2.0,  1.0)
        )
        val arr_t = arrayOf( DataPoint(0.0,  -1.0)
                ,DataPoint(1.0,  -3.0)
                ,DataPoint(2.0,  1.0)
                ,DataPoint(3.0,  6.0)
                ,DataPoint(4.0,  2.0)
        )
        val arr_l = arrayOf( DataPoint(-1.0,  14.0)
                ,DataPoint(1.0,  3.0)

        )
        val graph : GraphView = findViewById(R.id.graph)

        class myListener : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    data_type = parent.getItemAtPosition(position) as String?
                }

                var newSeries: LineGraphSeries<DataPoint>? = null
                if (data_type != null) {
                    if (data_type.equals("Humidity")) {
                        newSeries = LineGraphSeries<DataPoint>(arr_h)
                    } else if (data_type.equals("Temperature")) {
                        newSeries = LineGraphSeries<DataPoint>(arr_t)
                    } else if (data_type.equals("Light Level")) {
                        newSeries = LineGraphSeries<DataPoint>(arr_l)
                    }

                    if (newSeries != null) {
                        graph.removeAllSeries()
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
}