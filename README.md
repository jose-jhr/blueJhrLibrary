# blueJhrLibrary
Para usar la libreria BlueJhr utilizada para microcontroladores sigue los siguientes pasos

1) Importar la libreria BlueJhr 
 ```
 allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 ```
 
  ```
  	dependencies {
	        implementation 'com.github.jose-jhr:blueJhrLibrary:0.1.0'
	}
 ```
![image](https://user-images.githubusercontent.com/66834393/173492533-d3c0f3e5-85bf-4b57-9890-2fd7709891af.png)
![image](https://user-images.githubusercontent.com/66834393/173492579-5f19d094-3cc4-48a6-902a-793ab19e6899.png)

 
 2) Inicializamos el objeto BlueJhr 
```
lateinit var blue:BluJhr
 	
blue = BluJhr(this)
blue.onBluetooth()
```
  
  ![image](https://user-images.githubusercontent.com/66834393/175436397-7938ec4f-cf7c-4f5b-99d1-5c5846ee1144.png)

 
3) pedimos los permisos correspondientes, en android 12 la forma de pedir permisos cambia por esta razon se piden con ayuda de la libreria los siguiente permisos

esta seccion tambien pide al usuario encender el bluetooth, para iniciar los demas procesos

 ```
     /**
         * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes admin y scan
         * en android 12 o superior se requieren permisos diferentes
         */
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            if (blue.checkPermissions(requestCode,grantResults)){
                Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
	
 ```
 
 4) verificar si el usuario encendio el bluetooth en caso contrario pedimos que se habilite el servicio de bluetooth con la funcion initializeBluetooth(),
 si el bluetooth se ha iniciado, capturamos los dispositivos vinculador con el codigo de respuesta 100.
 	 
 ```
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
 ```
 
5) en este ejemplo hago uso de un listView para mostrar la informacion de el nombre y la direccion mac del dispositivo.
 ![image](https://user-images.githubusercontent.com/66834393/175435990-6799faaa-c43e-42b2-be40-31abcf8a8f17.png)

6) ahora hacemos uso de la funcion listener setDataLoadFinishedListener de BluJhr con el fin de conocer el estado de conexion del dispositivo movil.
 
 ```
 listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()){
                blue.connect(devicesBluetooth[i])
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
  ```
 
 True -> Conexion exitosa.
 False -> Error en conexión.
 Pending -> en progreso.
 Disconnect -> Se desconecto la conexión.

7) una vez la conexión fue exitosa hacemos uso de la funcion rxReceived(), funcion que se encargar de recibir información del microcontralador o dispositivo por medio de bluetooth, cada vez que exista un dato nuevo la variable rx:String tomara el valor en cadena de texto de la informacion recibida.


```
private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(rx: String) {
               consola.text = consola.text.toString()+rx
            }
        })
    }
```

8) para enviar información hacemos uso de la funcion  blue.bluTx("a")
```
buttonSend.setOnClickListener {
            blue.bluTx(edtTx.text.toString())
        }
```

9) en el siguiente ejemplo se diseño un aplicativo con el fin de enviar y recibir datos por medio de bluetooth, la información se recibe en un TextView y lo que se desea enviar se escribe en el editText y se envia al hacer click en el boton enviar, a continuación el ejemplo.


MainActivity.kt

```

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
                blue.connect(devicesBluetooth[i])
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

        /**
         * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes admin y scan
         * en android 12 o superior se requieren permisos diferentes
         */
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
```



activity_main.xml

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:orientation="vertical"
    >

    <ListView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/listDeviceBluetooth"
        android:visibility="visible"
        >
    </ListView>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:id="@+id/viewConn"
        android:visibility="gone"
        >

        <EditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:id="@+id/edtTx"
            android:layout_marginStart="10dp"
            android:layout_marginEnd="10dp"
            >
        </EditText>

        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/buttonSend"
            android:text="Send A"
            android:layout_gravity="center"
            android:layout_marginTop="10dp"
            >
        </Button>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp"
            android:id="@+id/consola"
            >
        </TextView>


    </LinearLayout>







</LinearLayout>

```

imagen del programa de ejemplo.
![image](https://user-images.githubusercontent.com/66834393/175436980-5535a3fb-32bf-4c36-a06a-e1bb09e46af9.png)







