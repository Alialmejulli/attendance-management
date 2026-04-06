package com.example.aws

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentHistoryActivity : BaseActivity() {

    private fun getLoggedInStudentId(): String {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        return prefs.getString("student_id", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_attendance_page)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val studentId = getLoggedInStudentId()
        if (studentId.isEmpty()) {
            Toast.makeText(this, "No student ID found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadHistory(studentId)
    }

    private fun loadHistory(studentId: String) {

        RetrofitClient.instance.getStudentHistory(studentId)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    if (!response.isSuccessful) {
                        Toast.makeText(this@StudentHistoryActivity, "Server error", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val raw = response.body()?.string() ?: return
                    val obj = JSONObject(raw)
                    val bodyString = obj.getString("body")
                    val bodyJson = JSONObject(bodyString)
                    val items = bodyJson.getJSONArray("items")



                    val container = findViewById<LinearLayout>(R.id.attendanceList)
                    container.removeAllViews()

                    if (items.length() == 0) {
                        val emptyText = TextView(this@StudentHistoryActivity).apply {
                            text = "No attendance history yet"
                            textSize = 16f
                            setTextColor(android.graphics.Color.GRAY)
                        }
                        container.addView(emptyText)
                        return
                    }

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)

                        val row = LayoutInflater.from(this@StudentHistoryActivity)
                            .inflate(R.layout.student_row_template, container, false)

                        row.findViewById<TextView>(R.id.dateText).text =
                            item.getString("date")

                        row.findViewById<TextView>(R.id.statusText).text =
                            item.getString("status")

                        container.addView(row)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@StudentHistoryActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
