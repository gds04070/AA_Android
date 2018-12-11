package com.gds.aa_android

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.gds.aa_android.adapter.ChartDataRecyclerViewAdapter
import com.gds.aa_android.db.ChartData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rv_item_chart_data.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.lang.IndexOutOfBoundsException

class MainActivity : AppCompatActivity() {

    var n : Int = 1

    var realValue : Double = 0.0
    var iexpextValue : Double = 0.0

    //블루투스
    private val bt : BluetoothSPP by lazy{
        BluetoothSPP(this)
    }

    //리사이클러뷰
    lateinit var ChartDataRecyclerViewAdapter : ChartDataRecyclerViewAdapter
    val dataList : ArrayList<ChartData> by lazy {
        ArrayList<ChartData>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setRecyclerView()

        setOnBtnClickListener()

        if(!bt.isBluetoothAvailable){
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
        }

        onBluetooth()
    }

    //블루투스
    private fun onBluetooth(){

        bt.setOnDataReceivedListener(object : BluetoothSPP.OnDataReceivedListener{
            override fun onDataReceived(data: ByteArray?, message: String?) {
                //Toast.makeText(this@MainActivity,message,Toast.LENGTH_SHORT).show()
                tv_main_receiveText.setText(message)
                if(!message.isNullOrEmpty()){
                    addItem(n, message)
                    realValue = message.toDouble()/1000.0
                    n = n+1
                }
            }
        })

        bt.setBluetoothConnectionListener(object:BluetoothSPP.BluetoothConnectionListener{
            override fun onDeviceConnected(name: String?, address: String?) { //연결되었을떄
                applicationContext.toast("Connected to$name\n$address")
            }

            override fun onDeviceDisconnected() { //연결해제
                applicationContext.toast("Connection lost")
            }

            override fun onDeviceConnectionFailed() { //연결실패
                applicationContext.toast("Unable to connect")
            }
        })

        //연결시도
        val btnConnect = findViewById<Button>(R.id.btn_main_btConnect)
        btnConnect.setOnClickListener {
            if(bt.serviceState == BluetoothState.STATE_CONNECTED){
                bt.disconnect()
            }else{
                val intent : Intent = Intent(applicationContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
    }

    override fun onStart() {
        super.onStart()
        if(!bt.isBluetoothEnabled){
            val intent : Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
        }else{
            if(!bt.isServiceAvailable){
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
            }
        }
    }

    private fun CheckExpectValue(){
        val btnSend = findViewById<Button>(R.id.btn_main_btSend)
        btnSend.setOnClickListener {
            CompareExpValue(iexpextValue, realValue)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data)
        }else if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
            }else{
                Toast.makeText(applicationContext, "Bluetooth was not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //리사이클러뷰
    private fun setRecyclerView(){
        //Test용
        dataList.add(ChartData(1,"0"))
        dataList.add(ChartData(2,"0"))

        ChartDataRecyclerViewAdapter = ChartDataRecyclerViewAdapter(this, dataList)
        rv_main_chartDataList.adapter = ChartDataRecyclerViewAdapter
        rv_main_chartDataList.layoutManager = LinearLayoutManager(this)
    }

    private fun addItem(n:Int, average: String){
        val position = ChartDataRecyclerViewAdapter.itemCount
        ChartDataRecyclerViewAdapter.dataList.add(ChartData(n, average))
        ChartDataRecyclerViewAdapter.notifyItemInserted(position)
    }


    private fun setOnBtnClickListener(){
        CheckExpectValue()
        removeItem()
        CalculateExpValue()
        bt_drawing_chart.setOnClickListener {
            startActivity<ChartActivity>()
        }
    }

    private fun removeItem(){
        bt_main_reset.setOnClickListener {
            try{
                dataList.removeAll(dataList)
                n = 1
                ChartDataRecyclerViewAdapter.notifyDataSetChanged()
            }catch (e: IndexOutOfBoundsException){
                Log.e("Index error", e.toString())
            }
        }

    }

    private fun CalculateExpValue() {
        bt_main_Calculate.setOnClickListener {
            var Length : String = et_main_lineLength.text.toString()
            var g : Double = 1000.0
            if(Length.isNotEmpty()){
                if(Length.toDouble() > 0.0){

                    var expectValue = 2*Math.PI*Math.sqrt(Length.toDouble()/g)
                    iexpextValue = expectValue
                    tv_main_expectValue.setText(expectValue.toString())

                }else{
                    tv_main_receiveText.setText("유효한 줄의 길이가 아닙니다")
                }
            }
        }
    }


    private fun CompareExpValue(expextValue: Double, realValue: Double){
        if((expextValue-realValue) != 0.0){
            var error : Double = (expextValue-realValue)*100.0/expextValue
            tv_main_error.setText(error.toString())
            if(error > 5.0 || error < -5.0){
                bt.send("F", true)
            }
        }

    }


}

