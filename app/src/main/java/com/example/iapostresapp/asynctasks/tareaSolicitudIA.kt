package com.example.iapostresapp.asynctasks

import ai.api.AIServiceContext
import ai.api.android.AIDataService
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import android.app.Activity
import android.os.AsyncTask
import com.example.iapostresapp.MainActivity

/**
 * Clase que manejará la comunicación con el Bot de forma asincrónica.
 */
class tareaSolicitudIA(private val activity: Activity, private val aiDataService: AIDataService, private val customAIServiceContext: AIServiceContext) :
    AsyncTask<AIRequest, Void, AIResponse>() {

    /**
     * Utilizamos un mienbro del Servicio de Inteligencia Artificial (Lo veremos más adelante) para realizar una solicitud.
     */
    override fun doInBackground(vararg aiRequests: AIRequest?): AIResponse {
        val solicitud = aiRequests[0]
        return aiDataService.request(solicitud, customAIServiceContext)
    }

    /**
     * Una vez que se hace la solicitud, la respuesta se envía a la actividad principal (Main Activity), que la maneja.
     */
    override fun onPostExecute(aiResponse: AIResponse?) {
        (activity as MainActivity).requestCallback(aiResponse)
    }

}

