package com.example.notebook.api

import java.io.IOException

sealed class NetworkResponse<out T : Any, out U : Any> {

    data class Success<T: Any>(val body : T) : NetworkResponse<T,Nothing>() {
        // success
    }
    data class ApiError<U : Any>(val body: U, val code: Int) : NetworkResponse<Nothing,U>() {
        // error
    }

    data class NetworkError(val error : IOException) : NetworkResponse<Nothing,Nothing>() {
        // json error
    }
    data class UnknownError(val error: Throwable?)  : NetworkResponse<Nothing,Nothing>() {

    }
}