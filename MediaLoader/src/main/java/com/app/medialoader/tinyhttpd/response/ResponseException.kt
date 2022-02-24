package com.app.medialoader.tinyhttpd.response

class ResponseException : Exception {
    val status: HttpStatus

    constructor(status: HttpStatus) : super(status.desc) {
        this.status = status
    }

    constructor(status: HttpStatus, message: String?) : super(message) {
        this.status = status
    }

    constructor(status: HttpStatus, message: String?, e: Exception?) : super(message, e) {
        this.status = status
    }


}