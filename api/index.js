/**
 * ClockIt — Vercel Serverless API Handler
 * Mirrors the Java Backend REST endpoints for seamless cloud deployment on Vercel.
 * Provides identical functionality with Anna University Semester 3 syllabus & End-Sem exam schedule.
 */

// In-Memory Database store for Vercel Serverless instances
let db = {
  student: {
    id: 'std-priyanthi-01',
    rollNumber: '2023506001',
    name: 'Priyanthi A',
    department: 'Computer Technology',
    programme: 'B.E. Computer Science and Engineering',
    semester: 3,
    gender: 'Female',
    password: 'clockit2026',
    avatarUrl: '',
    doodleApplied: false
  },
  subjects: [
    { id: 'sub-ma23c05', code: 'MA23C05', name: 'Probability and Statistics', units: 5, progress: 65, attended: 18, total: 20 },
    { id: 'sub-cs23301', code: 'CS23301', name: 'Software Engineering', units: 5, progress: 50, attended: 19, total: 20 },
    { id: 'sub-cs23302', code: 'CS23302', name: 'Data Structures', units: 5, progress: 70, attended: 17, total: 20 },
    { id: 'sub-cs23303', code: 'CS23303', name: 'Digital System Design', units: 5, progress: 55, attended: 16, total: 20 },
    { id: 'sub-cs23304', code: 'CS23304', name: 'Java Programming', units: 5, progress: 80, attended: 19, total: 20 },
    { id: 'sub-cs23u01', code: 'CS23U01', name: 'Standards - Computer Science & Engg.', units: 2, progress: 85, attended: 9, total: 10 },
    { id: 'sub-uc23u01', code: 'UC23U01', name: 'Universal Human Values', units: 5, progress: 90, attended: 10, total: 10 }
  ],
  calendar: [
    { id: 'cal-01', title: 'Examination Timetable Published', date: '2026-10-22', type: 'event', detail: 'Anna University Office of Additional Controller of Examinations' },
    { id: 'cal-02', title: 'Internal Assessment 2 & Attendance Freeze', date: '2026-10-28', type: 'deadline', detail: 'Final eligibility list publication · min 75% attendance' },
    { id: 'cal-03', title: 'Hall Ticket Distribution & Model Lab Reviews', date: '2026-11-05', type: 'deadline', detail: 'Collect signed hall tickets from Department Advisor' },
    
    // Official End-Semester Examination Timetable:
    { id: 'cal-exam-01', title: 'End Sem: MA23C05 Probability & Statistics', date: '2026-11-11', type: 'exam', detail: 'FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023' },
    { id: 'cal-exam-02', title: 'End Sem: CS23304 Java Programming', date: '2026-11-14', type: 'exam', detail: 'FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023' },
    { id: 'cal-exam-03', title: 'End Sem: CS23303 Digital System Design', date: '2026-11-18', type: 'exam', detail: 'AN (02:00 PM – 05:00 PM) · MIT Chromepet Campus · Reg 2023' },
    { id: 'cal-exam-04', title: 'End Sem: CS23302 Data Structures', date: '2026-11-25', type: 'exam', detail: 'AN (02:00 PM – 05:00 PM) · MIT Chromepet Campus · Reg 2023' },
    { id: 'cal-exam-05', title: 'End Sem: CS23301 Software Engineering', date: '2026-11-30', type: 'exam', detail: 'FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023' },
    
    { id: 'cal-04', title: 'End Sem Practical Lab Examinations', date: '2026-12-04', type: 'exam', detail: 'CS23304 Java Programming & CS23302 Data Structures Lab' },
    { id: 'cal-05', title: 'Winter Break & Semester 4 Reopening Prep', date: '2026-12-15', type: 'event', detail: 'Campus reopening notice for academic year 2026–2027' }
  ],
  events: [
    { id: 'evt-01', title: 'Student Tech Meetup', category: 'TECH TALK', date: '2026-10-25', time: '4:00 PM', venue: 'Rajam Hall, MIT Campus', description: 'Talks, coding demos and peer tech discussions on full-stack Java development.', registered: false },
    { id: 'evt-02', title: 'End-Sem Java & DSA Peer Study Sprint', category: 'WORKSHOP', date: '2026-11-06', time: '10:00 AM', venue: 'Computer Tech Lab 2, MIT', description: 'Focused peer revision on Unit I–V concepts, multithreading, and trees.', registered: true },
    { id: 'evt-03', title: 'Creative Club Afternoon', category: 'CAMPUS LIFE', date: '2026-11-12', time: '3:00 PM', venue: 'Hostel Quadrangle / Open Air', description: 'A calm study-break corner with music, sketching, and chai before exams.', registered: false }
  ],
  marks: [
    { id: 'mrk-01', subject: 'Probability and Statistics', assessment: 'Assessment 1 (Written)', score: 18.5, outof: 20 },
    { id: 'mrk-02', subject: 'Java Programming', assessment: 'Lab Assessment (JDBC & Concurrency)', score: 19.0, outof: 20 },
    { id: 'mrk-03', subject: 'Data Structures', assessment: 'Quiz 1 (Binary Trees)', score: 17.5, outof: 20 },
    { id: 'mrk-04', subject: 'Digital System Design', assessment: 'Class Test (Sequential Logic)', score: 16.0, outof: 20 },
    { id: 'mrk-05', subject: 'Software Engineering', assessment: 'SRS & Agile Sprint Review', score: 18.0, outof: 20 }
  ],
  journal: [
    { id: 'jrn-01', title: 'End-Sem Preparation & Small Steps', text: 'The exam schedule is out for November 2026. Starting revision with Probability & Statistics and Java Programming. One day at a time. ✿', mood: '🌱', date: '22 Oct 2026' }
  ]
};

module.exports = async (req, res) => {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  // Normalize path
  let path = req.url.split('?')[0];
  if (path.startsWith('/api')) {
    path = path.substring(4);
  }
  if (!path.startsWith('/')) {
    path = '/' + path;
  }

  const method = req.method.toUpperCase();
  let body = req.body || {};
  if (typeof body === 'string') {
    try {
      body = JSON.parse(body);
    } catch (e) {
      body = {};
    }
  } else if (!req.body && method !== 'GET' && method !== 'HEAD' && typeof req.on === 'function') {
    body = await new Promise((resolve) => {
      let data = '';
      req.on('data', chunk => { data += chunk; });
      req.on('end', () => {
        try { resolve(JSON.parse(data)); } catch (e) { resolve({}); }
      });
      req.on('error', () => resolve({}));
    });
  }

  try {
    // 1. Auth: Sign In / Register
    if ((path === '/auth/signin' || path === '/auth/register') && method === 'POST') {
      const { name, rollNumber, roll_number, department, semester, gender, password } = body;
      const roll = (rollNumber || roll_number || '2023506001').trim();
      db.student = {
        ...db.student,
        name: (name || 'Priyanthi A').trim(),
        rollNumber: roll,
        department: department || 'Computer Technology',
        semester: parseInt(semester, 10) || 3,
        gender: gender || 'Female',
        password: password || 'clockit2026'
      };
      return res.status(200).json(db.student);
    }

    // 2. Auth: Login
    if (path === '/auth/login' && method === 'POST') {
      const { rollNumber, roll_number, password } = body;
      const roll = (rollNumber || roll_number || '').trim();
      if (roll && (roll === db.student.rollNumber || roll === 'DEMO-ROLL-001' || roll === '2023506001')) {
        return res.status(200).json(db.student);
      }
      return res.status(401).json({ error: 'Invalid Roll Number or Password' });
    }

    // 3. Auth: Current user
    if (path === '/auth/me' && method === 'GET') {
      return res.status(200).json(db.student);
    }

    // 4. Overview
    if (path === '/overview' && method === 'GET') {
      const validSubjects = db.subjects.filter(s => s.total > 0);
      const avgAttendance = validSubjects.length
        ? Math.round(validSubjects.reduce((acc, s) => acc + (s.attended / s.total) * 100, 0) / validSubjects.length)
        : 85;

      const baseline = '2026-10-20';
      const upcomingDeadlines = db.calendar
        .filter(c => c.date >= baseline)
        .sort((a, b) => a.date.localeCompare(b.date))
        .slice(0, 5);

      return res.status(200).json({
        averageAttendance: avgAttendance,
        upcomingCount: upcomingDeadlines.length,
        journalCount: db.journal.length,
        deadlines: upcomingDeadlines,
        totalSubjects: db.subjects.length,
        totalEvents: db.events.length
      });
    }

    // 5. Subjects
    if (path === '/subjects' && method === 'GET') {
      return res.status(200).json(db.subjects);
    }
    if (path === '/subjects' && method === 'POST') {
      const newSub = {
        id: 'sub-' + Date.now(),
        code: body.code || 'SUB-' + (db.subjects.length + 1),
        name: body.name || 'New Subject',
        units: parseInt(body.units, 10) || 5,
        progress: parseInt(body.progress, 10) || 0,
        attended: 0,
        total: 0
      };
      db.subjects.push(newSub);
      return res.status(201).json(newSub);
    }
    if (path.startsWith('/subjects/') && (method === 'PUT' || method === 'POST')) {
      const code = decodeURIComponent(path.substring('/subjects/'.length));
      const sub = db.subjects.find(s => s.code.toUpperCase() === code.toUpperCase());
      if (sub) {
        if (body.name) sub.name = body.name;
        if (body.units !== undefined) sub.units = parseInt(body.units, 10);
        if (body.progress !== undefined) sub.progress = parseInt(body.progress, 10);
        return res.status(200).json(sub);
      }
      return res.status(404).json({ error: 'Subject not found' });
    }

    // 6. Attendance
    if (path === '/attendance' && method === 'POST') {
      const { subject, status } = body;
      const sub = db.subjects.find(s => s.code.toUpperCase() === (subject || '').toUpperCase());
      if (sub) {
        sub.total++;
        if (status === 'present') sub.attended++;
        return res.status(200).json({ success: true, subjects: db.subjects });
      }
      return res.status(404).json({ error: 'Subject not found' });
    }

    // 7. Calendar
    if (path === '/calendar' && method === 'GET') {
      return res.status(200).json(db.calendar);
    }
    if (path === '/calendar' && method === 'POST') {
      const newItem = {
        id: 'cal-' + Date.now(),
        title: body.title,
        date: body.date,
        type: body.type || 'deadline',
        detail: body.detail || ''
      };
      db.calendar.push(newItem);
      return res.status(201).json(newItem);
    }
    if (path.startsWith('/calendar/') && method === 'DELETE') {
      const id = decodeURIComponent(path.substring('/calendar/'.length));
      db.calendar = db.calendar.filter(c => c.id !== id);
      return res.status(200).json({ success: true, calendar: db.calendar });
    }

    // 8. Events
    if (path === '/events' && method === 'GET') {
      return res.status(200).json(db.events);
    }
    if (path === '/events' && method === 'POST') {
      const newEvt = {
        id: 'evt-' + Date.now(),
        title: body.title,
        category: body.category || 'WORKSHOP',
        date: body.date,
        time: body.time || '10:00 AM',
        venue: body.place || 'MIT Campus',
        description: body.description || '',
        registered: false
      };
      db.events.push(newEvt);
      return res.status(201).json(newEvt);
    }
    if (path.match(/\/events\/[^\/]+\/register/) && method === 'POST') {
      const id = path.split('/')[2];
      const evt = db.events.find(e => e.id === id);
      if (evt) {
        evt.registered = !evt.registered;
        return res.status(200).json({ success: true, events: db.events });
      }
      return res.status(404).json({ error: 'Event not found' });
    }

    // 9. Marks
    if (path === '/marks' && method === 'GET') {
      return res.status(200).json(db.marks);
    }
    if (path === '/marks' && method === 'POST') {
      const newMark = {
        id: 'mrk-' + Date.now(),
        subject: body.subject,
        assessment: body.assessment,
        score: parseFloat(body.score) || 0,
        outof: parseFloat(body.outof) || 20
      };
      db.marks.push(newMark);
      return res.status(201).json(newMark);
    }
    if (path.startsWith('/marks/') && method === 'DELETE') {
      const id = decodeURIComponent(path.substring('/marks/'.length));
      db.marks = db.marks.filter(m => m.id !== id);
      return res.status(200).json({ success: true, marks: db.marks });
    }

    // 10. Journal
    if (path === '/journal' && method === 'GET') {
      return res.status(200).json(db.journal);
    }
    if (path === '/journal' && method === 'POST') {
      const newJrn = {
        id: 'jrn-' + Date.now(),
        title: body.title || 'A small memory',
        text: body.text || '',
        mood: body.mood || '🌱',
        date: new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
      };
      db.journal.unshift(newJrn);
      return res.status(201).json(newJrn);
    }

    // 11. Profile
    if (path === '/profile' && method === 'GET') {
      return res.status(200).json(db.student);
    }
    if (path === '/profile' && method === 'POST') {
      db.student = {
        ...db.student,
        name: body.name || db.student.name,
        rollNumber: body.rollNumber || db.student.rollNumber,
        department: body.department || db.student.department,
        programme: body.programme || db.student.programme,
        semester: parseInt(body.semester, 10) || db.student.semester,
        gender: body.gender || db.student.gender,
        avatarUrl: body.avatarUrl !== undefined ? body.avatarUrl : db.student.avatarUrl,
        doodleApplied: body.doodleApplied !== undefined ? body.doodleApplied : db.student.doodleApplied
      };
      if (body.password) db.student.password = body.password;
      return res.status(200).json(db.student);
    }

    // 12. Admin Stats
    if (path === '/admin/stats' && method === 'GET') {
      return res.status(200).json({
        calendarItems: db.calendar.length,
        events: db.events.length,
        subjects: db.subjects.length,
        marks: db.marks.length,
        journals: db.journal.length
      });
    }

    // 13. Search
    if (path === '/search') {
      const q = (body.query || '').trim().toLowerCase();
      const matchedSubjects = db.subjects.filter(s => s.name.toLowerCase().includes(q) || s.code.toLowerCase().includes(q));
      const matchedEvents = db.events.filter(e => e.title.toLowerCase().includes(q) || e.description.toLowerCase().includes(q));
      const matchedCalendar = db.calendar.filter(c => c.title.toLowerCase().includes(q) || c.detail.toLowerCase().includes(q));
      return res.status(200).json({
        query: body.query,
        subjects: matchedSubjects,
        events: matchedEvents,
        calendarItems: matchedCalendar
      });
    }

    return res.status(404).json({ error: 'Endpoint not found: ' + path });

  } catch (err) {
    return res.status(500).json({ error: 'Serverless execution error: ' + err.message });
  }
};
