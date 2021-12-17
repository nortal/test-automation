package com.nortal.test.core.rest.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import java.lang.Void

interface ReportUploaderApi {
    @Multipart
    @Headers("Content-Encoding: gzip")
    @POST("/v2/testsupport/automation/reports/{buildName}/{buildNumber}")
    fun upload(@Path("buildName") buildName: String?, @Path("buildNumber") buildNumber: String?, @Part filePart: MultipartBody.Part?): Call<Void?>?
}