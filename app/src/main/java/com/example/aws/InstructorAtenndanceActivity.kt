package com.example.aws

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aws.databinding.TakeAttendancePageBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject

class InstructorAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: TakeAttendancePageBinding
    private lateinit var sectionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TakeAttendancePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get section ID from CourseDetailActivity
        sectionId = intent.getStringExtra("section_id")!!

        // Back button
        binding.backButton.setOnClickListener { finish() }

        // Generate Code Button
        binding.btnGenerateCode.setOnClickListener {
            sendActivationCodeToServer()
        }
    }

    private fun sendActivationCodeToServer() {
        RetrofitClient.instance.sendActivationCode(sectionId)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val raw = response.body()?.string()
                    android.util.Log.d("GEN_CODE_RESPONSE", "Raw response: $raw")

                    if (response.isSuccessful && raw != null) {
                        val obj = JSONObject(raw)

                        // ORDS REST modules return: { "status_code":200, "body":"{\"code\":\"12345\"}" }
                        if (obj.has("body")) {
                            val bodyString = obj.getString("body")       // string containing JSON
                            val bodyJson = JSONObject(bodyString)        // convert to JSON object
                            val code = bodyJson.getString("code")        // extract code

                            binding.generatedCode.text = code
                            Toast.makeText(
                                this@InstructorAttendanceActivity,
                                "Code generated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@InstructorAttendanceActivity,
                                "Server did not return a code",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@InstructorAttendanceActivity,
                            "Failed to generate code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@InstructorAttendanceActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
