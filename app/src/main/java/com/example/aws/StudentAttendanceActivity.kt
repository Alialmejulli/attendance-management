package com.example.aws

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentAttendanceActivity : BaseActivity() {

    private fun getLoggedInStudentId(): String {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        return prefs.getString("student_id", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mark_attendance_page)

        val courseCode = intent.getStringExtra("course_code")
        findViewById<TextView>(R.id.courseCode).text = courseCode

        val sectionId = intent.getStringExtra("section_id")

        val codeInput = findViewById<EditText>(R.id.codeInput)
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        submitBtn.setOnClickListener {
            val code = codeInput.text.toString().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyCode(sectionId, code)
        }
    }

    private fun verifyCode(sectionId: String?, code: String) {

        val studentId = getLoggedInStudentId()

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Error: No student ID found", Toast.LENGTH_SHORT).show()
            return
        }


        RetrofitClient.instance.validateCode(
            sectionId = sectionId!!,
            code = code,
            studentId = studentId
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    val raw = response.body()?.string()
                    val obj = JSONObject(raw)

                    // Extract ORDS "body" JSON
                    val bodyString = obj.getString("body")
                    val bodyJson = JSONObject(bodyString)

                    val status = bodyJson.getString("status")

                    if (status == "valid") {
                        markAttendance(studentId, sectionId, code)
                    } else {
                        Toast.makeText(
                            this@StudentAttendanceActivity,
                            "Invalid or expired code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        "Server error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Network error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun markAttendance(studentId: String, sectionId: String, code: String) {

        RetrofitClient.instance.markAttendance(
            studentId = studentId,
            sectionId = sectionId,
            code = code
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    val raw = response.body()?.string()
                    val obj = JSONObject(raw)

                    val bodyString = obj.getString("body")
                    val bodyJson = JSONObject(bodyString)

                    val status = bodyJson.getString("status")

                    if (status == "marked") {
                        Toast.makeText(
                            this@StudentAttendanceActivity,
                            "Attendance recorded!",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this@StudentAttendanceActivity,
                            "Failed to record attendance",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        "Failed to record attendance",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Network error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
