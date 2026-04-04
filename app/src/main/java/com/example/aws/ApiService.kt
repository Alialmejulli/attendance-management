package com.example.aws

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header

interface ApiService {

    @GET("users/")
    fun getUsers(): Call<UsersResponse>

    @GET("courses/")
    fun getCourses(@Query("student_id") studentId: String): Call<CoursesResponse>

    @GET("courseDetail/")
     fun getCourseDetail(
        @Query("section_id") sectionId: String
    ): Call <CourseDetailResponse>

    @GET("instructor/")
     fun getInstructorCourses(
        @Query("id") instructorId: String
    ): Call<InstructorResponse>

    @POST("generate_code/")
    fun sendActivationCode(
        @Header("section_id") sectionId: String
    ): Call<ResponseBody>

    @POST("validate_code/")
    fun validateCode(
        @Header("section_id") sectionId: String,
        @Header("code") code: String,
        @Header("student_id") studentId: String
    ): Call<ResponseBody>

    @POST("mark/")
    fun markAttendance(
        @Header("student_id") studentId: String,
        @Header("section_id") sectionId: String,
        @Header("code") code: String
    ): Call<ResponseBody>

    @GET("instructor_history")
    fun getInstructorHistory(
        @Query("section_id") sectionId: String
    ): Call<ResponseBody>

    @GET("student_history")
    fun getStudentHistory(
        @Query("student_id") studentId: String
    ): Call<ResponseBody>









}
