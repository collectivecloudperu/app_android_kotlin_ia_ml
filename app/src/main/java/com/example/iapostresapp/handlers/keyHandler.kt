package com.example.iapostresapp.handlers

import ai.api.util.IOUtils
import com.example.iapostresapp.MainActivity
import com.example.iapostresapp.R
import org.json.JSONObject

object KeyHandler {

    lateinit var DEVELOPER_ACCESS_TOKEN: String
    lateinit var CLIENT_ACCESS_TOKEN: String

    fun definirContexto(mainActivity: MainActivity) {
        val jsonString = IOUtils.readAll(mainActivity.resources.openRawResource(R.raw.tokens))
        val jsonObjeto = JSONObject(jsonString)
        DEVELOPER_ACCESS_TOKEN = jsonObjeto.getString("DEVELOPER_ACCESS_TOKEN")
        CLIENT_ACCESS_TOKEN = jsonObjeto.getString("CLIENT_ACCESS_TOKEN")
    }

}