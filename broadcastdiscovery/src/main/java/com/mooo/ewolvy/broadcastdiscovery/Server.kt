package com.mooo.ewolvy.broadcastdiscovery

import org.json.JSONObject

data class Server (val description: String, val JSONResponse: JSONObject?){
    override fun toString(): String {
        return description
    }
}