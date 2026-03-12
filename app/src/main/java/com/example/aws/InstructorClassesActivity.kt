package com.example.aws.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aws.BaseActivity
import com.example.aws.InstructorCoursesAdapter
import com.example.aws.InstructorResponse
import com.example.aws.R
import com.example.aws.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstructorClassesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.courses_page)

        val recycler = findViewById<RecyclerView>(R.id.coursesRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        loadInstructorClasses(recycler)
    }

    private fun loadInstructorClasses(recycler: RecyclerView) {
        val instructorId = intent.getStringExtra("instructor_id") ?: ""

        RetrofitClient.instance.getInstructorCourses(instructorId)
            .enqueue(object : Callback<InstructorResponse> {

                override fun onResponse(
                    call: Call<InstructorResponse>,
                    response: Response<InstructorResponse>
                ) {
                    val courses = response.body()?.items ?: emptyList()

                    recycler.adapter = InstructorCoursesAdapter(courses) { selectedCourse ->
                        Toast.makeText(
                            this@InstructorClassesActivity,
                            "Clicked: ${selectedCourse.courseName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<InstructorResponse>, t: Throwable) {
                    Toast.makeText(
                        this@InstructorClassesActivity,
                        "Error loading classes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
