package com.example.savenight

import android.widget.Button

class Host {
    var deviceName : String? = null
    var endpointId : String? = null


    constructor(deviceName: String?, endpointId: String?) {
        this.deviceName = deviceName
        this.endpointId = endpointId

    }

    constructor(){}

}