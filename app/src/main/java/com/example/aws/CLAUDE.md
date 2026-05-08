# AMS — Attendance Management System
## Claude.md — Project Context & Progress Tracker

---

## Project Overview

A mobile attendance system for **King Abdulaziz University (KAU)**, Faculty of Computing & Information Technology (FCAITR), Jeddah, Saudi Arabia.

- **Platform:** Android only (Kotlin, View-based UI)
- **Backend:** Oracle APEX REST (ORDS) on Oracle Cloud (me-jeddah-1)
- **Database:** Oracle DB
- **Package:** `com.example.aws`
- **Theme:** Dark green `#2E4F3D` throughout — matches KAU brand

---

## Tech Stack

| Layer | Technology |
|---|---|
| Mobile | Android (Kotlin), View-based UI |
| Networking | Retrofit 2 + Gson |
| UI Components | Material Components 1.12.0 |
| Backend | Oracle APEX REST (ORDS) |
| Database | Oracle DB (Cloud) |
| Auth | Server-side via `POST login/` |

---

## Core Flow

1. Instructor picks a timer duration (1/3/5/10/15 min)
2. Instructor taps Generate Code → `POST start/` → APEX generates 5-digit code, stores in `attendance_codes`
3. Code + countdown shown on instructor screen (persisted via `AttendanceTimerService` foreground service)
4. Student enters code → `POST attend/` → validates + records atomically
5. Timer ends → `AttendanceTimerService.onFinish()` calls `POST close_session/` → marks absent students automatically
6. Both roles can view history with P/L/A status

---

## Database Tables

| Table | Key Columns |
|---|---|
| `USERS` | `id`, `email`, `password`, `role`, `first_name`, `last_name`, `department`, `major`, `gpa`, `study_year` |
| `COURSES` | `course_code`, `name` |
| `SECTIONS` | `section_id`, `course_code`, `instructor_id` |
| `SECTION_ENROLLMENTS` | `enrollment_id`, `section_id`, `student_id` |
| `ATTENDANCE_CODES` | `section_id`, `code`, `start_time`, `end_time`, `expires_at` |
| `ATTENDANCE_RECORDS` | `id`, `student_id`, `section_id`, `code`, `status`, `"TIMESTAMP"` |
| `ATTENDANCE` | `attendance_id`, `enrollment_id`, `attendance_date`, `status`, `timestamp_marked` — NOT used by app |

> ⚠️ `TIMESTAMP` is a reserved word in Oracle — always quote it as `"TIMESTAMP"` in PL/SQL

---

## REST API Endpoints

Base URL: `https://tfjudkfbikoeqek-studentaiprojects.adb.me-jeddah-1.oraclecloudapps.com/ords/gp2user2/attendance_api/`

All PL/SQL endpoints wrap responses in `{"body": "..."}` — Android parses with `outer.optString("body", null)`.

| Method | Endpoint | Parameters | Notes |
|---|---|---|---|
| POST | `login/` | `email`, `id`, `password` (headers) | Server-side auth. Returns user JSON or `{"error":"..."}` |
| GET | `courses/` | `?student_id=` | Returns `CoursesResponse` → `List<StudentCourse>` |
| GET | `courseDetail/` | `?section_id=` | Returns instructor name |
| GET | `instructor/` | `?id=` | Returns `InstructorResponse` → `List<CourseItem>` |
| POST | `start/` | `section_id`, `minutes` (headers) | MERGE into attendance_codes, returns `{code, start_time, end_time}` |
| POST | `attend/` | `student_id`, `section_id`, `code` (headers) | Atomic validate + record. Returns `present/late/already_marked/invalid` |
| POST | `close_session/` | `section_id` (header) | Bulk inserts A for absent students |
| GET | `instructor_history` | `?section_id=` | Returns records with `student_id`, `student_name`, `status`, `timestamp` |
| GET | `student_history` | `?student_id=` | Returns records with `status`, `timestamp`, `section_id` |

> ❌ Deleted: `validate_code/`, `mark/`, `generate_code/` (replaced by `attend/` and `start/`)
> ❌ `GET users/` removed — replaced by `POST login/`

---

## Android Activity Map

```
LoginActivity
├── StudentHomeActivity          ← courses + action buttons inline
│   ├── StudentAttendanceActivity   (mark attendance — single attend/ call)
│   ├── StudentHistoryActivity      (P/L/A history with summary chips)
│   └── SettingsActivity
│
└── InstructorHomeActivity       ← classes + action buttons inline
    ├── InstructorAttendanceActivity  (generate code + foreground timer)
    ├── InstructorHistoryActivity     (session history with 4 chips + student ID)
    └── SettingsActivity
```

> `CoursesActivity` and `MyClassesDetailActivity` are no longer needed — courses load directly on home screens

---

## SharedPreferences

| File | Key | Value | Set by |
|---|---|---|---|
| `user_session` | `student_id` | Logged-in student ID | `LoginActivity` |
| `user_session` | `first_name` | First name | `LoginActivity` |
| `user_session` | `last_name` | Last name | `LoginActivity` |
| `user_session` | `role` | `student` or `instructor` | `LoginActivity` |
| `app_settings` | `app_language` | `en` or `ar` | `SettingsActivity` |
| `app_settings` | `notifications_enabled` | Boolean | `SettingsActivity` |

> ⚠️ Both prefs files must be cleared on logout — `SettingsActivity` does this

---

## Key Files

### Kotlin
| File | Purpose |
|---|---|
| `LoginActivity.kt` | Server-side login, sends email+id+password as headers |
| `StudentHomeActivity.kt` | Inline course list, selection-based action buttons |
| `InstructorHomeActivity.kt` | Inline class list, selection-based action buttons |
| `StudentAttendanceActivity.kt` | Single `attend/` call — no more two-step flow |
| `InstructorAttendanceActivity.kt` | Binds to `AttendanceTimerService` |
| `AttendanceTimerService.kt` | Foreground service — timer survives screen changes, calls `close_session/` on finish |
| `StudentHistoryActivity.kt` | RecyclerView with empty state + P/L/A summary chips |
| `InstructorHistoryActivity.kt` | RecyclerView with empty state + Total/P/L/A chips + student ID in rows |
| `SettingsActivity.kt` | Profile card, language toggle, About dialog, logout clears both prefs |
| `ApiService.kt` | Retrofit interface — all endpoints defined here |
| `RetrofitClient.kt` | Singleton Retrofit instance |
| `BaseActivity.kt` | Locale + notification settings applied on every activity |
| `StudentCoursesAdapter.kt` | Selection highlight with `course_row_selected` drawable |
| `InstructorCoursesAdapter.kt` | Selection highlight with `course_row_selected` drawable |

### Drawables
| File | Purpose |
|---|---|
| `avatar_circle.xml` | White circle for avatar initial |
| `stat_chip.xml` | `#1AFFFFFF` frosted chip background |
| `circle_btn.xml` | `#26FFFFFF` circle for back/settings icons |
| `card_top_rounded.xml` | White sheet with rounded top corners (28dp) |
| `course_row_bg.xml` | `#1AFFFFFF` transparent row background |
| `course_row_selected.xml` | `#33FFFFFF` + white border for selected row |
| `chip_present.xml` | Green status chip |
| `chip_late.xml` | Amber status chip |
| `chip_absent.xml` | Red status chip |
| `chip_pending.xml` | Gray status chip |
| `language_selected.xml` | White fill for active language button |
| `language_unselected.xml` | Transparent for inactive language button |

---

## Layout Status

| Screen | Status | Notes |
|---|---|---|
| `login.xml` | ✅ Done | TextInputLayout, email or ID login |
| `student_home_page.xml` | ✅ Done | Inline courses, stat chips, action buttons |
| `instructor_home_page.xml` | ✅ Done | Inline classes, stat chips, action buttons |
| `settings_page.xml` | ✅ Done | Profile card, prefs, about dialog, red logout |
| `view_attendance_page.xml` | ✅ Done | Empty state + RecyclerView + P/L/A chips |
| `instructor_history_page.xml` | ✅ Done | Empty state + RecyclerView + Total/P/L/A chips |
| `student_row_template.xml` | ✅ Done | Date + section + status chip |
| `item_instructor_history.xml` | ✅ Done | Name + ID + date + status chip |
| `take_attendance_page.xml` | ⏳ Pending | Instructor generate code + timer |
| `mark_attendance_page.xml` | ⏳ Pending | Student enter code + submit |
| `course_detail_page.xml` | ⏳ Pending | Student course detail |
| `myclasses_detail_page.xml` | ⏳ Pending | May no longer be needed |
| `courses_page.xml` | ⏳ Pending | May no longer be needed |

---

## Issues Fixed

| # | Severity | Issue | Fix |
|---|---|---|---|
| 1 | HIGH | Client-side auth — all passwords exposed | `POST login/` endpoint, server-side comparison |
| 2 | MEDIUM | Race condition validate + mark | Single atomic `POST attend/` endpoint |
| 3 | LOW | Student history ignored section_id | Queries by student_id (intentional — shows all courses) |
| 4 | LOW | `generate_code/` dead endpoint | Deleted from APEX |
| 5 | LOW | `MainActivity` empty | Redirects to `LoginActivity` |
| 6 | LOW | Logout didn't clear `user_session` | Both prefs cleared on logout |
| 7 | BACKEND | `ATTENDANCE_CODES` unique constraint on `SECTION_ID` | Changed INSERT to MERGE in `start/` |
| 8 | BACKEND | `"TIMESTAMP"` reserved word causing silent insert fail | Quoted as `"TIMESTAMP"` everywhere |
| 9 | BACKEND | Status written as `present/late` not `P/L` | Fixed in `attend/` — writes `P`/`L` directly |
| 10 | BACKEND | Absent date not showing in instructor history | Fall back to `ac.expires_at` when `ar."TIMESTAMP"` is null |

---

## Pending Tasks

### Layouts (in progress)
- [ ] `take_attendance_page.xml` — instructor take attendance screen
- [ ] `mark_attendance_page.xml` — student mark attendance screen
- [ ] `course_detail_page.xml` — student course detail

### Features
- [ ] Notification system — `AttendanceTimerService` already has the channel set up
- [ ] Arabic RTL fixes — `paddingStart/End` done, activity stack recreation needs work
- [ ] Excel export — instructor can export section attendance as `.xlsx`
  - Apache POI library needed in `build.gradle`
  - Export button on instructor history screen
  - Option A: current session only
  - Option B: full semester history
  - Save to Downloads, share via intent

### Nice to have
- [ ] Loading spinners on API calls
- [ ] `inputType="number"` on code input field in mark attendance
- [ ] Remove `CoursesActivity` and `MyClassesDetailActivity` if confirmed unused

---

## AndroidManifest Additions Needed

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<service
    android:name=".AttendanceTimerService"
    android:foregroundServiceType="shortService" />
```

---

## Notes & Gotchas

- `TIMESTAMP` is a reserved Oracle keyword — always use `"TIMESTAMP"` in PL/SQL
- ORDS wraps PL/SQL responses in `{"body": "..."}` — always parse with `outer.optString("body", null)`  
- `attend/` duplicate check is by `code` not by date — allows multiple sessions per day
- `close_session/` is called automatically by `AttendanceTimerService` when timer ends
- `AttendanceTimerService` must be started with `EXTRA_SECTION` so it knows which section to close
- Login sends the same user input as both `email` and `id` headers — APEX checks `WHERE (email = v_email OR id = v_input_id)`
- Null bytes `\u0000` can appear in `TextInputEditText` — strip with `.replace("\u0000", "")` before sending as HTTP header
- Status chips map: `P` → `chip_present` (green), `L` → `chip_late` (amber), `A` → `chip_absent` (red), else → `chip_pending` (gray)
