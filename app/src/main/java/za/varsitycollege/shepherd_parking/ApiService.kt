package za.varsitycollege.shepherd_parking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Define the API interface
interface ApiService {

    @POST("/api/convert")
    fun postData(@Body request: ApiRequest): Call<ApiResponse>
}

