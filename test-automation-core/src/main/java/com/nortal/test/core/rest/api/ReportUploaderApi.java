package com.nortal.test.core.rest.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ReportUploaderApi {

	@Multipart
	@Headers("Content-Encoding: gzip")
	@POST("/v2/testsupport/automation/reports/{buildName}/{buildNumber}")
	Call<Void> upload(@Path("buildName") String buildName, @Path("buildNumber") String buildNumber, @Part MultipartBody.Part filePart);

}
