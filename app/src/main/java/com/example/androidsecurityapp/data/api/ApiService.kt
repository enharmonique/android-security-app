package com.example.androidsecurityapp.data.api

import com.example.androidsecurityapp.data.requests.LoginRequest
import com.example.androidsecurityapp.data.requests.RegisterRequest
import com.example.androidsecurityapp.data.responses.ApiResponse
import com.example.androidsecurityapp.data.responses.AuthResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/auth/register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): Response<ApiResponse>

    @POST("/api/auth/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<AuthResponse>

    @GET("/api/home")
    suspend fun getHome(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponse>
}