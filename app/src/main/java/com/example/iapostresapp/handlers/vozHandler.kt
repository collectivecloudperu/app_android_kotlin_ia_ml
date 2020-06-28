package com.example.iapostresapp.handlers

import android.speech.tts.TextToSpeech
import com.example.iapostresapp.MainActivity

object vozHandler : TextToSpeech.OnInitListener {

    private var textoaVoz: TextToSpeech? = null

    fun definirContexto(context: MainActivity) {
        textoaVoz = TextToSpeech(context, this)
    }

    fun reproducir(textToSpeak: String?) {
        textoaVoz?.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun detener() {
        textoaVoz?.stop()
    }

    override fun onInit(status: Int) {}

}