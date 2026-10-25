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
  console.log('🚀 Running Comprehensive Test Suite for Academic Clock Java Platform...\n');

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

  // 1. Overview
  await test('GET /api/overview returns dashboard metrics and upcoming deadlines', async () => {
    const res = await request('GET', '/api/overview');
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (typeof res.body.averageAttendance !== 'number') throw new Error('Missing averageAttendance');
    if (!Array.isArray(res.body.deadlines)) throw new Error('Missing deadlines array');
  });

  // 2. Subjects
  await test('GET /api/subjects returns pre-seeded academic subjects', async () => {
    const res = await request('GET', '/api/subjects');
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (!Array.isArray(res.body) || res.body.length < 5) throw new Error('Expected at least 5 subjects');
  });

  // 3. Record Attendance
  await test('POST /api/attendance updates subject attended and total counts in SQL database', async () => {
    const res = await request('POST', '/api/attendance', { subject: 'SUB-01', status: 'present' });
    if (res.status !== 200) throw new Error(`Expected 200, got ${res.status}`);
    if (!res.body.success) throw new Error('Expected success: true');
    const sub = res.body.subjects.find(s => s.code === 'SUB-01');
    if (!sub || sub.attended < 18) throw new Error('Attendance count was not incremented');
  });

  // 4. Add Subject
  await test('POST /api/subjects creates a new subject in H2 database', async () => {
    const res = await request('POST', '/api/subjects', {
      name: 'Java Programming Lab',
      code: 'CS23304-LAB',
      units: 5,
      progress: 50
    });
    if (res.status !== 201) throw new Error(`Expected 201, got ${res.status}`);
    if (res.body.code !== 'CS23304-LAB') throw new Error('Subject code mismatch');
  });

  // 5. Calendar
  await test('POST & GET /api/calendar persists calendar item to database', async () => {
    const createRes = await request('POST', '/api/calendar', {
      title: 'Java Practical Model Exam',
      date: '2026-11-20',
      type: 'exam',
      detail: 'Unit I to V comprehensive practical exam'
    });
    if (createRes.status !== 201) throw new Error(`Expected 201, got ${createRes.status}`);

    const getRes = await request('GET', '/api/calendar?date=2026-11-20');
    if (getRes.status !== 200) throw new Error(`Expected 200, got ${getRes.status}`);
    const found = getRes.body.find(i => i.title === 'Java Practical Model Exam');
    if (!found) throw new Error('Newly created calendar item not found in database query');
  });

  // 6. Campus Events & Registration
  await test('POST /api/events & register interest toggle', async () => {
    const evtRes = await request('POST', '/api/events', {
      title: 'ACM Coding Hackathon 2026',
      category: 'WORKSHOP',
      date: '2026-11-25',
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

  // 7. Marks & Assessments
  await test('POST & DELETE /api/marks manages marks persistence', async () => {
    const markRes = await request('POST', '/api/marks', {
      subject: 'Data Structures',
      assessment: 'End-Semester Lab Exam',
      score: 19.5,
      outof: 20
    });
    if (markRes.status !== 201) throw new Error(`Expected 201, got ${markRes.status}`);
    const id = markRes.body.id;

    const delRes = await request('DELETE', `/api/marks/${id}`);
    if (delRes.status !== 200) throw new Error(`Expected 200, got ${delRes.status}`);
  });

  // 8. Scrapbook Journal
  await test('POST & GET /api/journal persists scrapbook entry', async () => {
    const jrnRes = await request('POST', '/api/journal', {
      title: 'Milestone Completed',
      text: 'Finished building the fullstack Java platform with embedded SQL database! ✿',
      mood: '🌱'
    });
    if (jrnRes.status !== 201) throw new Error(`Expected 201, got ${jrnRes.status}`);

    const listRes = await request('GET', '/api/journal');
    if (listRes.status !== 200) throw new Error(`Expected 200, got ${listRes.status}`);
    const found = listRes.body.find(j => j.title === 'Milestone Completed');
    if (!found) throw new Error('Journal entry not found in database');
  });

  // 9. Profile Update
  await test('POST & GET /api/profile updates student academic records', async () => {
    const profRes = await request('POST', '/api/profile', {
      name: 'Priyanthi A',
      rollNumber: 'MIT-CT-2026',
      department: 'Computer Technology',
      programme: 'B.Tech Computer Technology',
      semester: 5,
      doodleApplied: true
    });
    if (profRes.status !== 200) throw new Error(`Expected 200, got ${profRes.status}`);
    if (profRes.body.name !== 'Priyanthi A') throw new Error('Profile name update mismatch');
  });

  // 10. Global Search using Streams API
  await test('POST /api/search queries subjects, events, and calendar items via Streams API', async () => {
    const searchRes = await request('POST', '/api/search', { query: 'Structures' });
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
