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




