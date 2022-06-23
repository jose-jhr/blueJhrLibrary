package com.ingenieriajhr.blujhr

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class BluJhr(val context: Context) {

    enum class Connected {
        False, Pending, True,Disconnect
    }

    private var meInStream: InputStream? = null
    private var msOuStream: OutputStream? = null

    val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    var btSocket: BluetoothSocket? = null
    val btAdapter = (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    var requiredPermissions = listOf<String>()
    private var permisosAdmitidos = false

    private var connected = Connected.False

    fun onBluetooth(){
        if (!permisosAdmitidos){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                verifyPermission()
            }else{
                initializeBluetooth()
            }
        }else{
            initializeBluetooth()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun verifyPermission() {

        requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            initializeBluetooth()
        } else {
            (context as Activity).requestPermissions(missingPermissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun initializeBluetooth() {
        (context as Activity).startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),100)
    }

    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999

    }


    fun stateBluetoooth() = btAdapter.isEnabled



    fun checkPermissions(requestCode: Int, grantResults: IntArray):Boolean {
        return when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    permisosAdmitidos = true
                    // all permissions are granted
                    initializeBluetooth()
                    return true
                } else {
                    permisosAdmitidos = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        (context as Activity).requestPermissions(
                            requiredPermissions.toTypedArray(),
                            BLUETOOTH_PERMISSION_REQUEST_CODE
                        )
                    }
                    return false
                }
            }
            // else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            else -> false
        }

    }

    fun deviceBluetooth():ArrayList<String>{
        var pairedDevices = btAdapter.bondedDevices
        val arrayListDevice = ArrayList<String>()
        for (i in pairedDevices){
            arrayListDevice.add(i.name+"\n"+i.address)
        }
        return arrayListDevice
    }

    interface ConnectedBluetooth{
        fun onConnectState(state: Connected)
    }

    private var mConnectedFinish: ConnectedBluetooth? = null

    fun setDataLoadFinishedListener(date: ConnectedBluetooth) {
        this.mConnectedFinish = date
    }

    fun updateStateConnectBluetooth(state:Connected) {
        mConnectedFinish!!.onConnectState(state)
    }


    /**
     * Conectar dispositivo bluetooth
     */
    fun connect(address:String){
            val dirAddres = address.subSequence(address.length-17,address.length).toString()
            val device = btAdapter.getRemoteDevice(dirAddres)
            btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
            //cancela el proceso de deteccion de dispositvos actual
            btAdapter.cancelDiscovery()
            thread(start = true){
                updateStateListen(Connected.Pending)
                Connected.Pending
                try {
                    btSocket!!.connect()
                    Connected.True
                    connectThread()
                    updateStateListen(Connected.True)
                }catch(e:Exception){
                    updateStateListen(Connected.False)
                }
            }
    }



    interface ReceivedData{
        fun rxDate(rx: String)
    }

    private var mreceivedDate: ReceivedData? = null

    fun loadDateRx(date: ReceivedData) {
        this.mreceivedDate = date
    }

    fun updateRxConnectBluetooth(message:String) {
        (context as Activity).runOnUiThread {
            mreceivedDate!!.rxDate(message)
        }
    }


    private fun connectThread() {
        var DatosIn: InputStream? = null
        var DatosOut: OutputStream? = null
        try {
            DatosIn = btSocket!!.inputStream
            DatosOut = btSocket!!.outputStream
        } catch (var6: IOException) {
        }
        meInStream = DatosIn
        msOuStream = DatosOut
        bluRx()
    }

    private fun bluRx() {
        thread(start = true){
            while (true){
                try{
                    var input = BufferedReader(InputStreamReader(meInStream))
                    var rx = input.readLine()
                    updateRxConnectBluetooth(rx)
                }catch (e:IOException){
                    meInStream!!.close()
                    msOuStream!!.close()
                    updateStateListen(Connected.Disconnect)
                    break
                }
            }
        }
    }

    fun bluTx(message:String): Boolean {
        return try{
            btSocket!!.outputStream.write(message.toByteArray())
            true
        }catch(e:Exception){
            btSocket!!.close()
            updateStateListen(Connected.Disconnect)
            false
        }
    }

    private fun updateStateListen(conn:Connected){
        (context as Activity).runOnUiThread {
            updateStateConnectBluetooth(conn)
        }

    }


    fun closeConnection(){
        if (btSocket!=null){
            try {
                btSocket!!.close()
                updateStateListen(Connected.Disconnect)
            }catch(e:Exception){
            }
            btSocket = null
        }
    }










/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    // all permissions are granted
                    initializeBluetooth()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(requiredPermissions.toTypedArray(),
                            BLUETOOTH_PERMISSION_REQUEST_CODE
                        )
                    }
                    Toast.makeText(context, "no tienes permisos", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

 */



}