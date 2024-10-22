package com.example.api7

import android.telecom.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class Ordinateur(
    val id : Int,
    val name : String,
    val price : Double,
    val haveFP : Boolean,
    val image : String
)

data class AddResponse(
    val code : Int,
    val message: String
)

interface ApiService {
    @GET("/OrdiAPI/read/")
    fun getPC(): retrofit2.Call<List<Ordinateur>>

    @POST("/OrdiAPI/add/")
    fun addPC(@Body PC: Ordinateur): retrofit2.Call<AddResponse>

    @POST("/OrdiAPI/update/")
    fun upPC(@Body PC: Ordinateur): retrofit2.Call<AddResponse>

    @POST("/OrdiAPI/delete.php")
    fun deletePC(@Body PC: Ordinateur): retrofit2.Call<AddResponse>
}