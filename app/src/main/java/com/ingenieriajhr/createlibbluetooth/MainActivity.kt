
package com.ingenieriajhr.createlibbluetooth

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.ingenieriajhr.blujhr.BluJhr
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    
    var permisosOnBluetooth = false
    var requiredPermissions = listOf<String>()
    var devicesBluetooth = ArrayList<String>()

    lateinit var blue:BluJhr

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blue = BluJhr(this)
        blue.onBluetooth()

        listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()){

                blue.connect(devicesBluetooth[i].subSequence(devicesBluetooth[i].length-17,devicesBluetooth[i].length).toString())

                blue.setDataLoadFinishedListener(object:BluJhr.ConnectedBluetooth{
                    override fun onConnectState(state: BluJhr.Connected) {
                        when(state){

                            BluJhr.Connected.True->{
                                Toast.makeText(applicationContext,"True",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.GONE
                                viewConn.visibility = View.VISIBLE
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()
                                rxReceived()
                            }

                            BluJhr.Connected.False->{
                                Toast.makeText(applicationContext,"False",Toast.LENGTH_SHORT).show()
                            }

                            BluJhr.Connected.Disconnect->{
                                Toast.makeText(applicationContext,"Disconnect",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.VISIBLE
                                viewConn.visibility = View.GONE
                            }

                        }
                    }
                })
            }
        }



        buttonSend.setOnClickListener {
            blue.bluTx(edtTx.text.toString())
        }

        buttonSend.setOnLongClickListener {
            blue.closeConnection()
            true
        }



    }

    private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(rx: String) {
               consola.text = consola.text.toString()+rx
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                listDeviceBluetooth.adapter = adapter
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }




}