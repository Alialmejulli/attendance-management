package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.Button
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_detail_page)

        val sectionId = intent.getStringExtra("section_id")!!


        val markAttendanceBtn = findViewById<Button>(R.id.markAttendanceBtn)
        markAttendanceBtn.setOnClickListener {
            val intent = Intent(this, MarkAttendanceActivity::class.java)
            intent.putExtra("section_id", sectionId) // sectionId from your details screen
            startActivity(intent)
        }


        val courseCode = intent.getStringExtra("course_code")
        val courseName = intent.getStringExtra("course_name")
        findViewById<TextView>(R.id.courseCode).text = "$courseCode - $courseName"

        // Back button
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        // Load full details from backend
        loadCourseDetails(sectionId)
    }

    private fun loadCourseDetails(sectionId: String) {
        RetrofitClient.instance.getCourseDetail(sectionId)
            .enqueue(object : Callback<CourseDetailResponse> {

                override fun onResponse(
                    call: Call<CourseDetailResponse>,
                    response: Response<CourseDetailResponse>
                ) {
                    val detail = response.body()?.items?.firstOrNull() ?: return

                    findViewById<TextView>(R.id.instructor).text =
                        "Instructor: ${detail.instructor_name}"
                }

                override fun onFailure(call: Call<CourseDetailResponse>, t: Throwable) {
                    Toast.makeText(this@CourseDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }
}
