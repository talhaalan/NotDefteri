package com.example.notebook.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {

        const val BASE_URL = "https://notes-f3388-default-rtdb.europe-west1.firebasedatabase.app"


        private var apiInterface : ApiInterface? = null

        fun getInstance() : ApiInterface {
            if (apiInterface == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                apiInterface = retrofit.create(ApiInterface::class.java)
            }
            return apiInterface!!
        }



    }


}