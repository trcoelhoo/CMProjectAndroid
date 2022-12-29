package com.example.savenight

class Message {
    var isSent: Boolean? = null
    var message: String? = null
    var sender: String? = null

    constructor() {}

    constructor(message: String?, sender: String?, isSent: Boolean) {
        this.message = message
        this.sender = sender
        this.isSent = isSent
    }


}