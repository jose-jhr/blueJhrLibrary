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
``` kotlin
lateinit var blue:BluJhr
 	
blue = BluJhr(this)
blue.onBluetooth()
```
  
  ![image](https://user-images.githubusercontent.com/66834393/175436397-7938ec4f-cf7c-4f5b-99d1-5c5846ee1144.png)

 
3) pedimos los permisos correspondientes, en android 12 la forma de pedir permisos cambia por esta razon se piden con ayuda de la libreria los siguiente permisos

esta seccion tambien pide al usuario encender el bluetooth, para iniciar los demas procesos

 ```kotlin
     /**
         * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes admin y scan
         * en android 12 o superior se requieren permisos adicionales
         */
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            if (blue.checkPermissions(requestCode,grantResults)){
                Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
                blue.initializeBluetooth()
            }else{
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                    blue.initializeBluetooth()
                }else{
                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
	
 ```
 
 4) verificar si el usuario encendio el bluetooth en caso contrario pedimos que se habilite el servicio de bluetooth con la funcion initializeBluetooth(),
 si el bluetooth se ha iniciado, capturamos los dispositivos vinculador con el codigo de respuesta 100.
 	 
 ```kotlin
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (!blue.stateBluetoooth() && requestCode == 100){
		    blue.initializeBluetooth()
		}else{
		    if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }
         
            }
		}
		super.onActivityResult(requestCode, resultCode, data)
	    }
 ```
 
5) en este ejemplo hago uso de un listView para mostrar la informacion de el nombre y la direccion mac del dispositivo.
 ![image](https://user-images.githubusercontent.com/66834393/175435990-6799faaa-c43e-42b2-be40-31abcf8a8f17.png)

6) ahora hacemos uso de la funcion listener setDataLoadFinishedListener de BluJhr con el fin de conocer el estado de conexion del dispositivo movil.
 
 ```kotlin
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
				 rxReceived()
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()
                                
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


```kotlin
private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(rx: String) {
               consola.text = consola.text.toString()+rx
            }
        })
    }
```

8) para enviar información hacemos uso de la funcion  blue.bluTx("a")
```kotlin
buttonSend.setOnClickListener {
            blue.bluTx(edtTx.text.toString())
}
```

9) en el siguiente ejemplo se diseño un aplicativo con el fin de enviar y recibir datos por medio de bluetooth, la información se recibe en un TextView y lo que se desea enviar se escribe en el editText y se envia al hacer click en el boton enviar, a continuación el ejemplo.


MainActivity.kt

```kotlin

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
				rxReceived()
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()
                                
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                    blue.initializeBluetooth()
                }else{
                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }
         
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```



activity_main.xml

```xml
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






Gracias por utilizar mi libreria, te agradeceria si te suscribes a mi canal https://www.youtube.com/c/INGENIER%C3%8DAJHR gracias.








2) Metodo con varias activitys


```kotlin

class MainActivity : AppCompatActivity() {

    lateinit var blue: BluJhr
    var devicesBluetooth = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blue = BluJhr(this)
        blue.onBluetooth()

        listDeviceBlu.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this,ConnBlue::class.java)
            intent.putExtra("addres",devicesBluetooth[i])
            startActivity(intent)
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBlu.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}

```

Aqui el layout de MainActivity

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <ListView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/listDeviceBlu"
        >
    </ListView>

</LinearLayout>
```


2.2) Creamos una nueva activity llamada en este caso ConnBlue.kt

```kotlin

class ConnBlue : AppCompatActivity() {

    var addres = ""

    lateinit var blu: BluJhr

    var estadoConexion = BluJhr.Connected.False

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conn_blue)

        addres = intent.getStringExtra("addres").toString()

        blu = BluJhr(this)

        blu.setDataLoadFinishedListener(object:BluJhr.ConnectedBluetooth{
            override fun onConnectState(state: BluJhr.Connected) {
                when (state) {
                    BluJhr.Connected.True -> {
                        Toast.makeText(applicationContext, "True", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                    }
                    BluJhr.Connected.Pending -> {
                        Toast.makeText(applicationContext, "Pending", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                        rxReceived()
                    }
                    BluJhr.Connected.False -> {
                        Toast.makeText(applicationContext, "False", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                    }
                    BluJhr.Connected.Disconnect -> {
                        Toast.makeText(applicationContext, "Disconnect", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                        startActivity(Intent(applicationContext,MainActivity::class.java))
                    }
                }
            }
        })

        btnSend.setOnClickListener {
            blu.bluTx(edtSend.text.toString())
        }



    }


    /**
     * Se llama al siguiente método cuando cambia el foco de la ventana.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Toast.makeText(applicationContext, "Entro", Toast.LENGTH_SHORT).show()
        if (estadoConexion != BluJhr.Connected.True){
            blu.connect(addres)
        }

    }

    private fun rxReceived() {
        blu.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(rx: String) {
                txtConsola.text = txtConsola.text.toString() +rx
            }
        })
    }


}

```

aqui el layout de ConnBlue

```xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".ConnBlue">

    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="10dp"
        android:layout_marginEnd="10dp"
        android:layout_marginTop="10dp"
        android:id="@+id/edtSend"
        >
    </EditText>

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="5dp"
        android:layout_gravity="center_horizontal"
        android:text="send"
        android:id="@+id/btnSend"
        >
    </Button>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="10dp"
        android:layout_marginEnd="10dp"
        android:layout_marginTop="10dp"
        android:text="Rx:  "
        android:id="@+id/txtConsola"
        >
    </TextView>


</LinearLayout>

```









