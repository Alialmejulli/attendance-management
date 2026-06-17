# AMS — Attendance Management System
## CLAUDE.md — Project Context & Progress Tracker
### Last updated: 2026-06-17

---

## Project Overview

Mobile attendance system for **King Abdulaziz University (KAU)**, Faculty of Computing & Information Technology (FCAITR), Jeddah, Saudi Arabia.

- **Platform:** Android only (Kotlin, View-based UI)
- **Backend:** Oracle APEX REST (ORDS) on Oracle Cloud (me-jeddah-1)
- **Database:** Oracle DB
- **Package:** `com.example.aws`
- **Theme:** Dark green `#2E4F3D` — matches KAU brand
- **Repo:** https://github.com/Alialmejulli/attendance-management

---

## Tech Stack

| Layer | Technology |
|---|---|
| Mobile | Android (Kotlin), View-based UI |
| Networking | Retrofit 2 + Gson |
| UI Components | Material Components 1.12.0 |
| Backend | Oracle APEX REST (ORDS) |
| Database | Oracle DB (Cloud) |

---

## Core Flow (WORKING ✅)

1. Instructor selects timer duration (1/3/5/10/15 min)
2. Instructor taps Generate Code → `POST start/` → APEX generates 5-digit code
3. Code stored with `end_time` = 80% of duration (present cutoff), `expires_at` = 100% (actual expiry)
4. Code + countdown shown via `AttendanceTimerService` foreground service
5. Student gets local notification within 5 seconds via polling `GET active_session/`
6. Student enters code → `POST attend/` → P if before end_time, L if after, invalid if expired
7. Timer ends → `close_session/` → marks all non-attendees A automatically
8. Instructor can slide to cancel session → `POST cancel_session/` → deletes records + expires code
9. Both roles view history filtered by section

---

## Database Tables

| Table | Key Columns | Notes |
|---|---|---|
| `USERS` | `id`, `email`, `password`, `role`, `first_name`, `last_name`, `department`, `major`, `gpa`, `study_year` | Login: `WHERE (email=v_email OR id=v_input_id)` |
| `COURSES` | `course_code`, `name` | |
| `SECTIONS` | `section_id`, `course_code`, `instructor_id` | |
| `SECTION_ENROLLMENTS` | `enrollment_id`, `section_id`, `student_id` | |
| `ATTENDANCE_CODES` | `section_id`, `code`, `start_time`, `end_time`, `expires_at` | MERGE on section_id. `end_time`=80%, `expires_at`=100% |
| `ATTENDANCE_RECORDS` | `id`, `student_id`, `section_id`, `code`, `status`, `"TIMESTAMP"` | `"TIMESTAMP"` quoted — Oracle reserved word |

> ⚠️ All timestamps: `CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Riyadh' AS TIMESTAMP)`
> ⚠️ `TIMESTAMP` always quoted as `"TIMESTAMP"` in PL/SQL

---

## REST API Endpoints

Base URL: `https://tfjudkfbikoeqek-studentaiprojects.adb.me-jeddah-1.oraclecloudapps.com/ords/gp2user2/attendance_api/`

> All PL/SQL endpoints wrap responses: `{"body":"...","status_code":200}` — always parse outer first

| Method | Endpoint | Parameters | Status |
|---|---|---|---|
| POST | `login/` | `email`, `id`, `password` (headers) | ✅ |
| GET | `courses/` | `?student_id=` | ✅ |
| GET | `courseDetail/` | `?section_id=` | ✅ |
| GET | `instructor/` | `?id=` | ✅ |
| POST | `start/` | `section_id`, `minutes` (headers) | ✅ MERGE, 80/100 split |
| POST | `attend/` | `student_id`, `section_id`, `code` (headers) | ✅ atomic P/L/invalid |
| POST | `close_session/` | `section_id` (header) | ✅ bulk inserts A |
| POST | `cancel_session/` | `section_id` (header) | ✅ deletes records + expires code |
| GET | `active_session/` | `?student_id=` | ✅ returns active session or false |
| GET | `instructor_history` | `?section_id=` + `session_id` (header, optional) | ✅ filters by session when session_id provided |
| GET | `student_history` | `?student_id=` `?section_id=` | ✅ filtered by section |
| GET | `session_list/` | `?section_id=` | ✅ returns all sessions with date, time, P/L/A counts |

---

## Status Values

| Value | Meaning | Written by |
|---|---|---|
| `P` | Present — submitted before `end_time` (80%) | `attend/` |
| `L` | Late — submitted after `end_time` but before `expires_at` | `attend/` |
| `A` | Absent | `close_session/` |
| `-` | Pending — session still active | `instructor_history` fallback |

---

## Android Activity Map

```
LoginActivity  (launcher)
├── StudentHomeActivity        ← inline courses, polls active_session every 5s
│   ├── StudentAttendanceActivity   (mark attendance, auto-fill from notification)
│   ├── StudentHistoryActivity      (P/L/A history filtered by section)
│   └── SettingsActivity
│
└── InstructorHomeActivity     ← inline classes, action buttons
    ├── InstructorAttendanceActivity  (generate code + timer + slide to cancel)
    ├── InstructorSessionsActivity    (session list with date, P/L/A counts per session)
    │   └── InstructorHistoryActivity     (Total/P/L/A chips + student ID, filtered by session_id)
    └── SettingsActivity
```

---

## Key Files

### Kotlin
| File | Purpose |
|---|---|
| `LoginActivity.kt` | Email or ID login, server-side auth |
| `StudentHomeActivity.kt` | Inline courses, 5s polling for active sessions |
| `InstructorHomeActivity.kt` | Inline classes, selection-based buttons |
| `StudentAttendanceActivity.kt` | Single `attend/` call, auto-fill from notification |
| `InstructorAttendanceActivity.kt` | Binds to `AttendanceTimerService`, slide to cancel |
| `AttendanceTimerService.kt` | Foreground service — survives screen changes, calls `close_session/` on finish |
| `StudentHistoryActivity.kt` | RecyclerView, empty state, P/L/A chips, section-filtered |
| `InstructorSessionsActivity.kt` | Session list — cards with date, P/L/A counts, tapping navigates to history |
| `InstructorHistoryActivity.kt` | RecyclerView, empty state, Total/P/L/A chips, student ID in rows, filtered by session_id |
| `SettingsActivity.kt` | Profile card, language toggle, About dialog, logout |
| `NotificationHelper.kt` | Two channels: session alert (HIGH) + confirmation (LOW) |
| `SlideToCancelView.kt` | Custom view — drag right to cancel session |
| `ApiService.kt` | All Retrofit endpoints |
| `ActiveSessionResponse.kt` | Data class for active_session response |
| `StudentCoursesAdapter.kt` | Selection highlight |
| `InstructorCoursesAdapter.kt` | Selection highlight |

### Drawables
`avatar_circle`, `stat_chip`, `circle_btn`, `card_top_rounded`, `course_row_bg`, `course_row_selected`, `code_box`, `step_circle_inactive`, `chip_present`, `chip_late`, `chip_absent`, `chip_pending`, `language_selected`, `language_unselected`

---

## Layout Status

| Screen | File | Status |
|---|---|---|
| Login | `login.xml` | ✅ |
| Student Home | `student_home_page.xml` | ✅ |
| Instructor Home | `instructor_home_page.xml` | ✅ |
| Settings | `settings_page.xml` | ✅ |
| Take Attendance | `take_attendance_page.xml` | ✅ |
| Mark Attendance | `mark_attendance_page.xml` | ✅ |
| Course Detail | `course_detail_page.xml` | ✅ |
| Student History | `view_attendance_page.xml` | ✅ |
| Instructor History | `instructor_history_page.xml` | ✅ |
| Student row | `student_row_template.xml` | ✅ |
| Instructor history row | `item_instructor_history.xml` | ✅ |
| Course item student | `course_item_student.xml` | ✅ |
| Course item instructor | `course_item_instructor.xml` | ✅ |

---

## Notifications

| # | Trigger | Channel | Priority | Notes |
|---|---|---|---|---|
| 1 | Active session detected | `ams_session_channel` | HIGH — drops from top | Fires once per unique code via `lastNotifiedCode` tracker. Tapping opens `StudentAttendanceActivity` with auto-filled code |
| 2 | Attendance submitted successfully | `ams_confirm_channel` | LOW — silent receipt | Fires after P or L recorded. Also cancels notification 1 |

- Polling interval: **5 seconds** in `StudentHomeActivity.onResume()`
- Stopped in `onPause()` to save battery
- Both notifications respect the notifications toggle in Settings

---

## AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<service
    android:name=".AttendanceTimerService"
    android:foregroundServiceType="shortService"/>
```

> Runtime `POST_NOTIFICATIONS` permission requested in `StudentHomeActivity` and `InstructorAttendanceActivity`

---

## SharedPreferences

| File | Key | Value |
|---|---|---|
| `user_session` | `student_id` | Logged-in student ID |
| `user_session` | `first_name` | First name |
| `user_session` | `last_name` | Last name |
| `user_session` | `role` | `student` or `instructor` |
| `app_settings` | `app_language` | `en` or `ar` |
| `app_settings` | `notifications_enabled` | Boolean |

> ⚠️ Both prefs files cleared on logout

---

## All Issues Fixed

| # | Issue | Fix |
|---|---|---|
| 1 | Client-side auth — passwords exposed | `POST login/` server-side |
| 2 | Race condition validate + mark | Single atomic `POST attend/` |
| 3 | `generate_code/` dead endpoint | Deleted |
| 4 | `MainActivity` empty | Redirects to `LoginActivity` |
| 5 | Logout didn't clear `user_session` | Both prefs cleared |
| 6 | `ATTENDANCE_CODES` unique constraint | MERGE in `start/` |
| 7 | `"TIMESTAMP"` reserved word | Quoted everywhere |
| 8 | Status written as `present/late` | Fixed to `P`/`L` |
| 9 | Absent date not showing | Falls back to `ac.expires_at` |
| 10 | Everyone marked late | Split `end_time` 80% vs `expires_at` 100% |
| 11 | Wrong date (yesterday) | `CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Riyadh' AS TIMESTAMP)` |
| 12 | `close_session/` not firing | Endpoint wasn't created in APEX |
| 13 | "Unknown error" popup on submit | `when` updated to handle `"P"`/`"L"` |
| 14 | Null bytes in HTTP headers | `.replace("\u0000", "")` |
| 15 | Notification firing every 15s | `lastNotifiedCode` tracker — fires once per unique code |
| 16 | Session showing active after expiry | `active_session/` now uses Riyadh time comparison |
| 17 | Student history showing all sections | Added `section_id` filter to `student_history` endpoint |
| 18 | `student_history` server error | `v_section_id` missing from DECLARE block in PL/SQL |

---

## Pending Tasks

### Features
- [ ] **Excel export** — needs thorough planning before implementing
    - Apache POI library
    - Scope: current session vs full history — TBD
    - Button on `InstructorHistoryActivity`
- [x] **Instructor history — all sessions** — DONE. `InstructorSessionsActivity` shows all past sessions with date and P/L/A counts. Tapping a session opens `InstructorHistoryActivity` filtered by `session_id`. `attendance_codes` PK changed from `section_id` to `code`. `start/` changed from MERGE to INSERT.
- [ ] **Notification #3** — absent warning when student opens app and missed a session (deferred)

### Cleanup
- [ ] Remove unused activities: `CoursesActivity`, `MyClassesDetailActivity`, `InstructorClassesActivity`
- [ ] Arabic RTL full pass

### Known Limitations
- Foreground service (`AttendanceTimerService`) may be killed by OS on some devices
- No semester/schedule system — prototype level only
- Single section per `attendance_codes` row (by design for simplicity)

---

## Critical Gotchas

- `TIMESTAMP` reserved Oracle keyword — always `"TIMESTAMP"` in PL/SQL
- ORDS wraps PL/SQL: `{"body":"...","status_code":200}` — parse outer first
- `attend/` duplicate check by `code` not date — one submission per code
- `close_session/` auto-called by `AttendanceTimerService.onFinish()`
- `cancel_session/` deletes ALL records for that session code — clean slate
- Login sends same input as both `email` and `id` headers
- Null bytes `\u0000` in `TextInputEditText` — strip before HTTP headers
- All timestamps stored in Riyadh time via `CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Riyadh' AS TIMESTAMP)`
- `attendance_codes` PK is now `code` not `section_id` — each session creates a new row via INSERT
- `session_list/` returns all sessions for a section ordered by `start_time DESC`
- `instructor_history` accepts optional `session_id` header — empty string treated as null, falls back to latest session
- **DO NOT TOUCH** attendance P/L/A logic — working correctly as of 2026-05-07
- `CLAUDE.md` is in `.gitignore` — never committed to repo