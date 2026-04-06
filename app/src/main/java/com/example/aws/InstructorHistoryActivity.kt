package com.example.aws

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstructorHistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructor_history_page)

        val sectionId = intent.getStringExtra("section_id")

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        if (sectionId == null) {
            Toast.makeText(this, "Missing section ID", Toast.LENGTH_SHORT).show()
            return
        }

        loadHistory(sectionId)
    }

    private fun loadHistory(sectionId: String) {

        RetrofitClient.instance.getInstructorHistory(sectionId)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@InstructorHistoryActivity, "Server error", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val raw = response.body()?.string()
                    val obj = JSONObject(raw)
                    val bodyString = obj.getString("body")
                    val bodyJson = JSONObject(bodyString)
                    val records = bodyJson.getJSONArray("records")

                    // IMPORTANT: use the correct ID from XML
                    val container = findViewById<LinearLayout>(R.id.instructorHistoryList)

                    // Clear the card
                    container.removeAllViews()

                    // EMPTY STATE HANDLING
                    if (records.length() == 0) {
                        val emptyText = TextView(this@InstructorHistoryActivity).apply {
                            text = "No attendance history yet"
                            textSize = 16f
                            setTextColor(android.graphics.Color.GRAY)
                            gravity = Gravity.CENTER
                            setPadding(0, 32, 0, 32)
                        }
                        container.addView(emptyText)
                        return
                    }

                    // Inflate rows
                    val inflater = LayoutInflater.from(this@InstructorHistoryActivity)

                    for (i in 0 until records.length()) {
                        val rec = records.getJSONObject(i)

                        val row = inflater.inflate(R.layout.instructor_row_template, container, false)

                        row.findViewById<TextView>(R.id.studentIdText).text = rec.getString("student_id")
                        row.findViewById<TextView>(R.id.studentNameText).text = rec.getString("student_name")
                        row.findViewById<TextView>(R.id.statusText).text = rec.getString("status")
                        row.findViewById<TextView>(R.id.dateText).text =
                            rec.getString("timestamp").replace("T", " ")

                        container.addView(row)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@InstructorHistoryActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
