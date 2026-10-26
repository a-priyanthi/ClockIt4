# ClockIt

**Academic Calendar & Study Space**

ClockIt is a student-focused academic planning and study-support web application for Anna University’s MIT Campus. It brings semester-level context and day-to-day study tools into one workspace, including an annual academic clock, calendar, campus events, coursework tracking, and a personal journal.

## Live application

[Open ClockIt](https://clockit4.vercel.app/)

## Product overview

ClockIt is designed to help students keep academic dates visible, plan study time, and maintain a personal view of their coursework. The application combines calendar and campus information with tools for tracking attendance, assessments, and study habits.

## Features

| Area | Description |
| --- | --- |
| **Annual academic clock** | A year-at-a-glance wheel for navigating the academic calendar and opening a selected date. |
| **Academic calendar** | View calendar items by date and add or review items in the calendar. |
| **Campus events and study groups** | Browse campus event cards and manage event registration from the app. |
| **Syllabus shelf** | Browse subject and unit information, including the Semester 3 syllabus area. |
| **Attendance tracker** | Record subject attendance and review attendance summaries. |
| **Assessment tracker** | Keep marks and assessment information organized by subject. |
| **Daily planning** | Review the academic overview, upcoming items, deadlines, and daily plan. |
| **Scrapbook journal** | Save journal entries and mood check-ins as part of a personal study routine. |
| **Profile tools** | Manage the student profile and use the photo-to-doodle profile preview. |
| **Administrative data view** | Review database table contents and administrative summaries. |

## Getting started

1. Open the [live application](https://clockit4.vercel.app/) in a modern browser.
2. Use the sign-in or registration options displayed by the app.
3. Navigate to the calendar, events, syllabus, tracking, or journal areas using the in-app navigation.
4. Review the information shown for your selected date, subject, or activity.

Use an account and data you are authorized to access. The administrative data view should be used only by authorized maintainers.

## Academic information

ClockIt presents academic dates, syllabus material, and student tracking tools for planning purposes. Verify examination schedules, syllabus details, attendance rules, and other academic requirements against current official Anna University and MIT Campus communications before relying on them. The application should not be treated as the institution’s official academic record system.

## Technical overview

- **Web client:** HTML, CSS, and JavaScript.
- **Client assets:** `index.html`, `css/style.css`, `js/app.js`, and `js/api.js`.
- **API integration:** The client communicates with `/api` endpoints for sign-in, profiles, calendar items, events, attendance, subjects, marks, journal entries, and administrative summaries.
- **Deployment:** The live application is hosted on Vercel.

The public deployment does not identify the backend framework, database engine, or repository-specific build commands. Use the source repository and deployment configuration as the authority for those details.

## Local development and deployment

Local setup depends on the source repository’s configuration. Before running the project locally, review its package scripts, API configuration, and Vercel settings. Point development work at a non-production API and data store so test writes cannot affect live student records.

For deployment, configure the web client and its API service for the target environment, then verify sign-in, role-based access, and data operations before publishing. Store server-side secrets in the deployment platform’s environment settings; do not place them in browser-delivered JavaScript or HTML.

## Security and privacy

The application handles student profile and academic information. Treat this data as sensitive.

- Do not commit passwords, API keys, tokens, or real student records to Git.
- Keep demo accounts and seeded records synthetic, and remove any prefilled credentials from public client files.
- Review authentication and authorization for student and administrative API routes before using real records.
- Collect only information that is necessary, and follow the applicable consent, retention, and access policies for student data.

## Maintaining academic data

Keep curriculum content, event information, and examination dates current. Confirm changes against official university or campus notices, record the source and effective date where appropriate, and label sample data clearly so it cannot be mistaken for an official record.
