package com.example.aws

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

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



}
