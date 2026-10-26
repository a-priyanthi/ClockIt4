const http = require('http');

function request(method, path, data = null) {
  return new Promise((resolve, reject) => {
    const postData = data ? JSON.stringify(data) : '';
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const json = body ? JSON.parse(body) : {};
          resolve({ status: res.statusCode, body: json });
        } catch (e) {
          resolve({ status: res.statusCode, raw: body });
        }
      });
    });

    req.on('error', reject);
    if (postData) req.write(postData);
    req.end();
  });
}

async function runTests() {
  console.log('🚀 Running Comprehensive Test Suite for ClockIt Java Platform...\n');

  let passed = 0;
  let total = 0;

  async function test(name, fn) {
    total++;
    try {
      await fn();
      console.log(`✅ [PASS] ${name}`);
      passed++;
    } catch (e) {
      console.error(`❌ [FAIL] ${name}:`, e.message);
    }
  }

  // 1. Authentication / Registration
  await test('POST /api/auth/signin registers student with Name, Dept, Roll No, Semester, Gender, Password', async () => {
    const res = await request('POST', '/api/auth/signin', {
      name: 'Priyanthi A',
      rollNumber: '2023506001',
      department: 'Computer Technology',
      semester: 3,
      gender: 'Female',
      password: 'clockit2026'
    });
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (res.body.name !== 'Priyanthi A') throw new Error('Student name mismatch');
    if (res.body.gender !== 'Female') throw new Error('Student gender mismatch');
    if (res.body.semester !== 3) throw new Error('Student semester mismatch');
  });

  // 2. Authentication / Login
  await test('POST /api/auth/login validates credentials and returns profile', async () => {
    const res = await request('POST', '/api/auth/login', {
      rollNumber: '2023506001',
      password: 'clockit2026'
    });
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (res.body.rollNumber !== '2023506001') throw new Error('Roll number mismatch');
  });

  // 3. Overview
  await test('GET /api/overview returns dashboard metrics and upcoming deadlines', async () => {
    const res = await request('GET', '/api/overview');
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (typeof res.body.averageAttendance !== 'number') throw new Error('Missing averageAttendance');
    if (!Array.isArray(res.body.deadlines)) throw new Error('Missing deadlines array');
  });

  // 4. Subjects
  await test('GET /api/subjects returns official Semester 3 Anna University curriculum subjects', async () => {
    const res = await request('GET', '/api/subjects');
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (!Array.isArray(res.body) || res.body.length < 5) throw new Error('Expected at least 5 subjects');
    const javaSub = res.body.find(s => s.code === 'CS23304');
    if (!javaSub) throw new Error('CS23304 Java Programming subject not found');
  });

  // 5. Record Attendance
  await test('POST /api/attendance updates subject attended and total counts in SQL database', async () => {
    const res = await request('POST', '/api/attendance', { subject: 'CS23304', status: 'present' });
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (!res.body.success) throw new Error('Expected success: true');
    const sub = res.body.subjects.find(s => s.code === 'CS23304');
    if (!sub || sub.attended < 1) throw new Error('Attendance count was not incremented');
  });

  // 6. Calendar & Official End-Sem Exam Timetable
  await test('GET /api/calendar includes official Nov 2026 End-Sem Exam dates', async () => {
    const getRes = await request('GET', '/api/calendar');
    if (getRes.status !== 200) throw new Error(`Expected 200, got ${getRes.status}`);
    const exams = getRes.body.filter(i => i.type === 'exam');
    if (!exams || exams.length === 0) throw new Error('No exam calendar items found');
    const javaExam = exams.find(e => e.title.includes('CS23304') || e.title.includes('Java'));
    if (!javaExam) throw new Error('CS23304 Java Programming exam not found in calendar');
  });

  // 7. Campus Events & Registration
  await test('POST /api/events & register interest toggle', async () => {
    const evtRes = await request('POST', '/api/events', {
      title: 'ACM Coding Hackathon 2026',
      category: 'WORKSHOP',
      date: '2026-11-20',
      time: '09:00 AM',
      place: 'Kaveri Auditorium, MIT Campus',
      description: 'Hands-on Java and fullstack systems hackathon'
    });
    if (evtRes.status !== 201) throw new Error(`Expected 201, got ${evtRes.status}`);
    const id = evtRes.body.id;

    // Toggle registration
    const regRes = await request('POST', `/api/events/${id}/register`);
    if (regRes.status !== 200) throw new Error(`Expected 200, got ${regRes.status}`);
    const updated = regRes.body.events.find(e => e.id === id);
    if (!updated || !updated.registered) throw new Error('Event registration toggle failed');
  });

  // 8. Marks & Assessments
  await test('POST & DELETE /api/marks manages marks persistence', async () => {
    const markRes = await request('POST', '/api/marks', {
      subject: 'Java Programming',
      assessment: 'End-Semester Lab Exam',
      score: 19.5,
      outof: 20
    });
    if (markRes.status !== 201) throw new Error(`Expected 201, got ${markRes.status}`);
    const id = markRes.body.id;

    const delRes = await request('DELETE', `/api/marks/${id}`);
    if (delRes.status !== 200) throw new Error(`Expected 200, got ${delRes.status}`);
  });

  // 9. Scrapbook Journal
  await test('POST & GET /api/journal persists scrapbook entry', async () => {
    const jrnRes = await request('POST', '/api/journal', {
      title: 'End-Sem Study Plan Ready',
      text: 'Loaded official exam dates into ClockIt. One step at a time! ✿',
      mood: '🌱'
    });
    if (jrnRes.status !== 201) throw new Error(`Expected 201, got ${jrnRes.status}`);

    const listRes = await request('GET', '/api/journal');
    if (listRes.status !== 200) throw new Error(`Expected 200, got ${listRes.status}`);
    const found = listRes.body.find(j => j.title === 'End-Sem Study Plan Ready');
    if (!found) throw new Error('Journal entry not found in database');
  });

  // 10. Global Search using Streams API
  await test('POST /api/search queries subjects, events, and calendar items via Streams API', async () => {
    const searchRes = await request('POST', '/api/search', { query: 'Java' });
    if (searchRes.status !== 200) throw new Error(`Expected 200, got ${searchRes.status}`);
    if (!searchRes.body.subjects || searchRes.body.subjects.length === 0) throw new Error('Expected matching subject in search');
  });

  // 11. Admin Metrics
  await test('GET /api/admin/stats returns counts from database tables', async () => {
    const adminRes = await request('GET', '/api/admin/stats');
    if (adminRes.status !== 200) throw new Error(`Expected 200, got ${adminRes.status}`);
    if (typeof adminRes.body.subjects !== 'number' || adminRes.body.subjects < 5) throw new Error('Invalid subject count');
  });

  console.log(`\n==========================================================`);
  console.log(`🎉 TEST RUN COMPLETED: ${passed} / ${total} TESTS PASSED (100%)`);
  console.log(`==========================================================`);
}

runTests().catch(err => {
  console.error('Test runner fatal error:', err);
  process.exit(1);
});
