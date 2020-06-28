package com.example.iapostresapp

import ai.api.AIConfiguration
import ai.api.AIListener
import ai.api.AIServiceContextBuilder
import ai.api.android.AIDataService
import ai.api.android.AIService
import ai.api.model.AIError
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import ai.api.model.Result
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
//import android.support.v4.app.ActivityCompat
import androidx.core.app.ActivityCompat
//import android.support.v4.content.ContextCompat
import androidx.core.content.ContextCompat
//import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.iapostresapp.asynctasks.tareaSolicitudIA
import com.example.iapostresapp.handlers.*
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AIListener {

    private val TAG = "MainActivity"
    private val MENSAJE_BIENVENIDA = "Escribe: Hola"
    private val REQUEST = 200

    private lateinit var mensajesAdapter: MensajesAdapter

    private lateinit var mensajesView: ListView
    private lateinit var campoTexto: EditText
    private lateinit var mensajesBoton: ImageButton
    private lateinit var enviarMensajeBoton: ImageButton

    private var modoVoz:  Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Defino los elementos de la interface de la aplicación
        mensajesBoton = findViewById(R.id.microfono_img)
        enviarMensajeBoton = findViewById(R.id.enviar_img)
        campoTexto = findViewById(R.id.caja_chat)
        mensajesView = findViewById(R.id.contenedor_chat)

        // Inicio el handler del Bot que responderá a la consulta del usuario
        vozHandler.definirContexto(this)

        // Asigno el Adapter a la vista mensajesView a una instancia de la clase 'MensajesAdapter'
        mensajesAdapter = MensajesAdapter(this)
        mensajesView.adapter = mensajesAdapter

        // Ejecuto el handler 'KeyHandler'
        KeyHandler.definirContexto(this)

        // Llamo a estos 2 métodos para verificar los permisos y configurar el asistente
        // Estos métodos los crearé mas abajo
        verificarPermisos()
        configurarAsistente()

    }

    // Verifico si el usuario tiene los permisos correctos, si no los tiene se lo solicitamos
    private fun verificarPermisos() {
        // Las versiones superiores a Android 5 (Lollipop) requieren permiso
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            solicitarPermiso()
        }
    }

    // Solicito los permisos al usuario para usar la grabación de audio en su dispositivo
    private fun solicitarPermiso() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST)
        }
    }

    private fun configurarAsistente() {
        Log.d("MainActivity", "Configurando...")

        // Creo una instancia de Configuration de la Inteligencia Artificial
        // y especifico el token de acceso, la localización y el motor de reconocimiento.
        val config = ai.api.android.AIConfiguration(KeyHandler.CLIENT_ACCESS_TOKEN,
            AIConfiguration.SupportedLanguages.Spanish,
            ai.api.android.AIConfiguration.RecognitionEngine.System)

        // Configuración de DialogFlow para usar peticiones de voz
        // Usamos el objeto de Configuration de la Inteligencia Artificial para obtener una referencia
        // al Servicio de Inteligencia Artificial, que realizará las solicitudes de las consultas.
        val servicioIA = AIService.getService(applicationContext, config)

        // Establezco la instancia de escucha del servicio de Inteligencia Artificial
        servicioIA.setListener(this)

        // Establezco el botón 'mensajesBoton' como oyente (Listener)
        mensajesBoton.setOnClickListener {
            vozHandler.detener()
            servicioIA.startListening()
        }

        // Configuración de DialogFlow para usar peticiones de texto
        val servicioDatosIA = AIDataService(this, config)
        val contextoServicioPersonalizadoIA = AIServiceContextBuilder.buildFromSessionId(REQUEST.toString())
        val peticionIA = AIRequest()

        // Asigno al botón 'enviarMensajeBoton' el cual cada vez que se hace clic en el, coge el texto
        // del campoTexto y hace una nueva consulta asincrónicamente
        enviarMensajeBoton.setOnClickListener {
            val query = campoTexto.text.toString()
            campoTexto.text.clear()
            if (modoVoz) {
                modoVoz = false
            }

            // Método para enviar mensajes, lo defino más abajo
            enviarMensaje(query, false)

            peticionIA.setQuery(query)
            tareaSolicitudIA(this, servicioDatosIA, contextoServicioPersonalizadoIA).execute(peticionIA)
        }

        // Envio el mensaje de bienvenida al iniciar el asistente
        campoTexto.hint = MENSAJE_BIENVENIDA

    }

    // Método que se invoca cuando el asistente escucha algo
    override fun onResult(response: AIResponse?) {
        val resultado = response?.result

        // Assign query and query response to the cardviews in the layout.
        val query = resultado?.resolvedQuery
        val queryResponse = resultado?.fulfillment?.speech

        if (query != null && queryResponse != null) {
            if (!modoVoz) {
                modoVoz = true
            }

            // Método para enviar mensajes, lo defino más abajo
            enviarMensaje(query, false)
            enviarMensaje(queryResponse, true)
        }

        // Método para manejar el Intent, lo defino más abajo
        manejarIntent(resultado)
    }

    // Método 'requestCallback' que se llama cuando una peticionIA tiene un aiResponse.
    // Agrega el mensaje a la ListView como entrante.
    fun requestCallback(aiResponse: AIResponse?) {
        val respuestaQuery = aiResponse?.result?.fulfillment?.speech

        if (respuestaQuery != null) {
            enviarMensaje(respuestaQuery, true) // Método para enviar mensajes, lo defino más abajo
            manejarIntent(aiResponse?.result)  // Método para manejar el Intent, lo defino más abajo
        }
    }

    // Manejo la intención con el objeto 'IntentHandler'.
    private fun manejarIntent(result: Result?) {
        // Obtengo el nombre y los parámetros del Intent y dejo que IntentHandler lo maneje.
        val intentName = result?.metadata?.intentName
        val intentParameters = if (result != null) result.parameters else HashMap<String, JsonElement>()
        //IntentHandler.handleIntent(this, intentName, intentParameters)
    }

    // Método que agrega un nuevo mensaje al adapter de mensajes
    // por ende a la vista de la lista de mensajes
    fun enviarMensaje(text: String, incomingMessage: Boolean, numberOfMessagesBefore: Int = 0) {
        val mensaje = Mensaje(text, incomingMessage)

        if (campoTexto.hint == MENSAJE_BIENVENIDA) {
            campoTexto.hint = resources.getString(R.string.iam)
        }

        if (modoVoz && incomingMessage) {
            vozHandler.reproducir(text)
        }

        // Agrego un mensaje y hago que se desplace el ListView al último elemento agregado
        runOnUiThread {
            mensajesAdapter.agregarMensaje(mensaje)
            mensajesView.setSelection(mensajesView.count - 1 - numberOfMessagesBefore)
        }
    }

    override fun onStop() {
        super.onStop()
        vozHandler.detener()
    }

    // Método no implementado de la interfaz AI Listener 
    override fun onError(error: AIError?) {
        Log.e(TAG, error.toString())
    }

    /**
     * Method not implemented of interface AIListener
     */
    override fun onListeningStarted() {}

    /**
     * Method not implemented of interface AIListener
     */
    override fun onAudioLevel(level: Float) {}

    /**
     * Method not implemented of interface AIListener
     */
    override fun onListeningCanceled() {}

    /**
     * Method not implemented of interface AIListener
     */
    override fun onListeningFinished() {}

}
