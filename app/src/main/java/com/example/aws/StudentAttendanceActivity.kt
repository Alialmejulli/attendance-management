package com.example.aws

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
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

        val courseCode = intent.getStringExtra("course_code") ?: ""
        val courseName = intent.getStringExtra("course_name") ?: ""
        val sectionId  = intent.getStringExtra("section_id")  ?: ""

        // Header subtitle
        findViewById<TextView>(R.id.courseCode).text =
            if (courseCode.isNotEmpty()) "$courseCode — $courseName" else ""

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val codeInput = findViewById<EditText>(R.id.codeInput)
        val submitBtn = findViewById<MaterialButton>(R.id.submitBtn)

        val autoCode = intent.getStringExtra("auto_code") ?: ""
        if (autoCode.isNotEmpty()) {
            codeInput.setText(autoCode)
        }

        submitBtn.setOnClickListener {
            val code = codeInput.text.toString().trim().replace("\u0000", "")

            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val studentId = getLoggedInStudentId()
            if (studentId.isEmpty()) {
                Toast.makeText(this, "Error: No student ID found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sectionId.isEmpty()) {
                Toast.makeText(this, "Error: Missing section ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitBtn.isEnabled = false
            submitBtn.text      = "Submitting…"

            attend(studentId, sectionId, code, courseCode, courseName, submitBtn)
        }
    }

    private fun attend(
        studentId: String,
        sectionId: String,
        code: String,
        courseCode: String,
        courseName: String,
        submitBtn: MaterialButton
    ) {
        RetrofitClient.instance.attend(
            studentId = studentId,
            sectionId = sectionId,
            code      = code
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                submitBtn.isEnabled = true
                submitBtn.text      = "Submit Attendance"

                val raw = response.body()?.string() ?: run {
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        "Server error",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                Log.d("ATTEND", "raw = $raw")

                try {
                    val outer      = JSONObject(raw)
                    val bodyString = outer.optString("body", null) ?: raw
                    val body       = JSONObject(bodyString)
                    val status     = body.optString("status", "")

                    when (status) {
                        "P", "present" -> {
                            Toast.makeText(
                                this@StudentAttendanceActivity,
                                "✓ Marked Present",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Fire confirmation notification
                            fireConfirmationNotification(courseCode, courseName, "P")
                        }

                        "L", "late" -> {
                            Toast.makeText(
                                this@StudentAttendanceActivity,
                                "✓ Marked Late",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Fire confirmation notification
                            fireConfirmationNotification(courseCode, courseName, "L")
                        }

                        "already_marked" -> Toast.makeText(
                            this@StudentAttendanceActivity,
                            "Already marked for this session",
                            Toast.LENGTH_SHORT
                        ).show()

                        "invalid" -> Toast.makeText(
                            this@StudentAttendanceActivity,
                            "Invalid or expired code",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> {
                            val err = body.optString("error", "Unknown response")
                            Log.e("ATTEND", "Unhandled status: $status — $err")
                            Toast.makeText(
                                this@StudentAttendanceActivity,
                                err,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("ATTEND", "Parse error: ${e.message}")
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        "Unexpected server response",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                submitBtn.isEnabled = true
                submitBtn.text      = "Submit Attendance"
                Log.e("ATTEND", "Network failure: ${t.message}")
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fireConfirmationNotification(
        courseCode: String,
        courseName: String,
        status: String
    ) {
        // Respect notifications toggle
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return

        NotificationHelper.showAttendanceConfirmedNotification(
            context    = this,
            courseName = courseName,
            courseCode = courseCode,
            status     = status
        )

        // Also cancel the active session notification since student just attended
        NotificationHelper.cancel(this, NotificationHelper.NOTIF_ACTIVE_SESSION)
    }
}