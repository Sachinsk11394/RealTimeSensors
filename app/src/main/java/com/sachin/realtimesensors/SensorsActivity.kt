package com.sachin.realtimesensors

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.anychart.*
import kotlinx.android.synthetic.main.activity_sensors.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class SensorsActivity : AppCompatActivity() {

    @Inject
    lateinit var mViewModelFactory: SensorsViewModelFactory
    private val mViewModel: SensorsListViewModel by lazy {
        ViewModelProvider(
            this@SensorsActivity,
            mViewModelFactory
        ).get(SensorsListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        supportActionBar?.hide()

        DaggerCoreComponent.builder().coreModule(CoreModule(this@SensorsActivity)).build()
            .injectSensorsListActivity(this@SensorsActivity)

        setSensors()
    }

    private fun setSensors() {
        val sdf = SimpleDateFormat("mm:ss")

        val entries = arrayListOf<DataEntry>()
        val cartesian = AnyChart.line()
        cartesian.setAnimation(true)
        val minMarker = cartesian.getLineMarker(0)
        minMarker.axis = cartesian.yAxis
        minMarker.setStroke("green")
        val maxMarker = cartesian.getLineMarker(1)
        maxMarker.axis = cartesian.yAxis
        maxMarker.setStroke("red")
        cartesian.getYAxis(0).setTitle("Temperature")
        cartesian.setTitle("Sensor graph")
        cartesian.setData(entries)
        anyChartView.setChart(cartesian)

        mViewModel.mSensorsData.observe(this@SensorsActivity, { data ->
            if (mViewModel.mSensorsList.value?.size ?: 0 > 0) {
                entries.clear()
                val tempArray = arrayListOf<Pair<Date, Int>>()
                if (isRecent.isChecked) {
                    data[mViewModel.mSensorName.value]?.mRecentValues?.let { tempArray.addAll(it) }
                } else {
                    data[mViewModel.mSensorName.value]?.mMinuteValues?.let { tempArray.addAll(it) }
                }
                tempArray.forEach { keyValue ->
                    entries.add(ValueDataEntry(sdf.format(keyValue.first), keyValue.second))
                }
                cartesian.setData(entries)
            }
        })

        mViewModel.mSensorName.observe(this@SensorsActivity, { name ->
            minMarker.setValue(mViewModel.mSensorsConfig[name]?.first ?: 0)
            maxMarker.setValue(mViewModel.mSensorsConfig[name]?.second ?: 150)
        })

        mViewModel.getSensorsList().observe(this@SensorsActivity, { sensorsList ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sensorsList)
            chooseSensor.adapter = adapter
            chooseSensor.visibility = View.VISIBLE
            chooseSensor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mViewModel.subscribeSensor(sensorsList.get(position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Intentionally left blank
                }
            }
        })

        isRecent.setOnCheckedChangeListener { _, _ ->
            mViewModel.mSensorsData.notifyObserver()
        }
    }
}