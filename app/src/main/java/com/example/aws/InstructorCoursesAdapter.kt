package com.example.aws

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InstructorCoursesAdapter(
    private val courses: List<CourseItem>,
    private val onCourseClick: (CourseItem) -> Unit
) : RecyclerView.Adapter<InstructorCoursesAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseText: TextView = itemView.findViewById(R.id.courseNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_item_instructor, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        holder.courseText.text = "${course.courseName} - ${course.courseCode}"

        holder.itemView.setOnClickListener {
            onCourseClick(course)
        }
    }

    override fun getItemCount(): Int = courses.size
}

