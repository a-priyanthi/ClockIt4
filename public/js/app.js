/**
 * ClockIt — Application Controller
 * Synchronized with Java REST API & H2 SQL Database.
 * Anna University / MIT Campus Regulations 2023 (Semester 3 CSE/CT)
 */

const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

const titles = {
  dashboard: 'Your academic overview',
  calendar: 'Academic calendar & Exam timetable',
  events: 'Campus events & Study sprints',
  attendance: 'Attendance tracker',
  syllabus: 'Syllabus shelf',
  marks: 'Marks & assessments',
  journal: 'Your scrapbook journal',
  profile: 'My student profile',
  admin: 'Academic data desk'
};

// Application State (populated via live Java Database API)
let currentScreen = 'dashboard';
let calendarDate = new Date(2026, 10, 1); // November 2026 (End-Sem exam month)
let selectedDate = '2026-11-11'; // First End-Sem Exam Date
let selectedMood = '🌱';
let doodleOn = false;
let photoUrl = '';

let calendarItems = [];
let events = [];
let subjects = [];
let marks = [];
let journalEntries = [];
let currentStudent = null;

// Toast Notification
function toast(msg) {
  const t = $('#toast');
  if (!t) return;
  t.textContent = msg;
  t.style.display = 'block';
  clearTimeout(window.toastTimer);
  window.toastTimer = setTimeout(() => t.style.display = 'none', 2800);
}

// Navigation Router
function go(id) {
  if (!titles[id] && id !== 'dashboard') return;
  currentScreen = id;
  $$('.screen').forEach(x => x.classList.toggle('active', x.id === id));
  $$('#nav button').forEach(b => b.classList.toggle('active', b.dataset.screen === id));
  $('#topTitle').textContent = titles[id] || 'Academic space';

  if (id === 'dashboard') loadDashboard();
  if (id === 'calendar') renderCalendar();
  if (id === 'events') renderEvents();
  if (id === 'attendance') renderAttendance();
  if (id === 'syllabus') renderSyllabus();
  if (id === 'marks') renderMarks();
  if (id === 'journal') renderJournal();
  if (id === 'profile') renderProfile();
  if (id === 'admin') renderAdmin();

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

$('#nav').addEventListener('click', e => {
  const b = e.target.closest('button[data-screen]');
  if (b) go(b.dataset.screen);
});

document.addEventListener('click', e => {
  const b = e.target.closest('[data-goto]');
  if (b) go(b.dataset.goto);
});

// Formatters
function iso(y, m, d) {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
}

function fmtDate(s, opts = { month: 'short', day: 'numeric' }) {
  if (!s) return '';
  const [y, m, d] = s.split('-').map(Number);
  return new Date(y, m - 1, d).toLocaleDateString('en-IN', opts);
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function typeColor(t) {
  return ({
    exam: '#7852FF',
    deadline: '#FF4D52',
    event: '#FFC043',
    completed: '#2EC4B6'
  })[t] || '#FF4D52';
}

// Modal Utility
function modal(title, fieldsHtml, onSave) {
  $('#modalTitle').textContent = title;
  $('#modalFields').innerHTML = fieldsHtml;
  $('#modal').classList.add('active', 'show');

  $('#modalForm').onsubmit = async (e) => {
    e.preventDefault();
    const data = {};
    new FormData(e.target).forEach((v, k) => data[k] = v);
    try {
      await onSave(data);
      $('#modal').classList.remove('active', 'show');
      e.target.reset();
    } catch (err) {
      toast('Error: ' + err.message);
    }
  };
}

$('#closeModal').onclick = $('#cancelModal').onclick = () => $('#modal').classList.remove('active', 'show');
$('#modal').onclick = e => { if (e.target.id === 'modal') $('#modal').classList.remove('active', 'show'); };

// ========================================================
// AUTHENTICATION & GATED ACCESS CONTROL
// Only after signing in can the student see the website!
// ========================================================
function checkAuthStatus() {
  const savedUser = localStorage.getItem('clockit_user');
  if (savedUser) {
    try {
      currentStudent = JSON.parse(savedUser);
      revealApp();
      return;
    } catch (e) {
      localStorage.removeItem('clockit_user');
    }
  }
  // Not logged in -> Show Auth Portal and hide main app
  lockApp();
}

function lockApp() {
  currentStudent = null;
  const portal = $('#authPortal');
  const app = $('#appContainer');
  if (portal) portal.classList.remove('hidden');
  if (app) app.style.display = 'none';
}

function revealApp() {
  const portal = $('#authPortal');
  const app = $('#appContainer');
  if (portal) portal.classList.add('hidden');
  if (app) app.style.display = 'grid';

  updateUserDisplay();
  initAppData();
}

function updateUserDisplay() {
  if (!currentStudent) return;
  const name = currentStudent.name || 'Student';
  const roll = currentStudent.rollNumber || currentStudent.roll_number || '2023506001';
  const dept = currentStudent.department || 'Computer Technology';
  const sem = currentStudent.semester || 3;
  const gender = currentStudent.gender || 'Female';

  // Update topbar badges
  const topName = $('#topStudentName');
  if (topName) topName.textContent = name;
  const topDeptSem = $('#topStudentDeptSem');
  if (topDeptSem) topDeptSem.textContent = `Sem ${sem} · ${dept}`;
  const topGender = $('#topGenderBadge');
  if (topGender) {
    topGender.textContent = gender.toUpperCase();
    topGender.className = 'gender-badge ' + (gender.toLowerCase().includes('male') ? 'male' : '');
  }
  const topAvatar = $('#topAvatar');
  if (topAvatar) {
    if (currentStudent.avatarUrl) {
      topAvatar.innerHTML = `<img src="${currentStudent.avatarUrl}" alt="Avatar">`;
    } else {
      topAvatar.textContent = name.trim()[0].toUpperCase();
    }
  }

  // Update Dashboard greeting
  const dashName = $('#dashStudentName');
  if (dashName) dashName.textContent = name.split(' ')[0];
}

// Auth Tab Switching
const tabRegister = $('#authTabRegister');
const tabLogin = $('#authTabLogin');
const fullForm = $('#authFullForm');
const quickForm = $('#authQuickForm');

if (tabRegister && tabLogin) {
  tabRegister.onclick = () => {
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    fullForm.style.display = 'block';
    quickForm.style.display = 'none';
  };

  tabLogin.onclick = () => {
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    fullForm.style.display = 'none';
    quickForm.style.display = 'block';
  };
}

const switchToReg = $('#switchToRegisterBtn');
if (switchToReg) {
  switchToReg.onclick = () => {
    tabRegister.click();
  };
}

// 1. Full Sign In / Registration Form
if (fullForm) {
  fullForm.onsubmit = async (e) => {
    e.preventDefault();
    const payload = {
      name: $('#authName').value.trim(),
      rollNumber: $('#authRoll').value.trim(),
      department: $('#authDept').value,
      semester: parseInt($('#authSemester').value, 10),
      gender: $('#authGender').value,
      password: $('#authPassword').value.trim()
    };

    try {
      const student = await API.registerOrSignIn(payload);
      currentStudent = student;
      localStorage.setItem('clockit_user', JSON.stringify(student));
      toast(`Welcome to ClockIt, ${student.name}! ✿`);
      revealApp();
    } catch (err) {
      toast('Registration Error: ' + err.message);
    }
  };
}

// 2. Quick Login Form
if (quickForm) {
  quickForm.onsubmit = async (e) => {
    e.preventDefault();
    const payload = {
      rollNumber: $('#quickRoll').value.trim(),
      password: $('#quickPassword').value.trim()
    };

    try {
      const student = await API.login(payload);
      currentStudent = student;
      localStorage.setItem('clockit_user', JSON.stringify(student));
      toast(`Welcome back, ${student.name}! ✿`);
      revealApp();
    } catch (err) {
      toast('Login failed: ' + err.message);
    }
  };
}

// 3. Quick Demo Login Button
const quickDemoBtn = $('#quickDemoBtn');
if (quickDemoBtn) {
  quickDemoBtn.onclick = async () => {
    const demoPayload = {
      name: 'Priyanthi A',
      rollNumber: '2023506001',
      department: 'Computer Technology',
      semester: 3,
      gender: 'Female',
      password: 'clockit2026'
    };

    try {
      const student = await API.registerOrSignIn(demoPayload);
      currentStudent = student;
      localStorage.setItem('clockit_user', JSON.stringify(student));
      toast(`Signed in as ${student.name} (Demo) ✿`);
      revealApp();
    } catch (err) {
      toast('Demo Login Error: ' + err.message);
    }
  };
}

// Sign Out Handler
function signOut() {
  localStorage.removeItem('clockit_user');
  currentStudent = null;
  lockApp();
  toast('Signed out from ClockIt. See you soon! ✿');
}

const topSignOutBtn = $('#topSignOutBtn');
if (topSignOutBtn) topSignOutBtn.onclick = signOut;

const navSignOutBtn = $('#navSignOutBtn');
if (navSignOutBtn) navSignOutBtn.onclick = signOut;

// ========================================================
// CORE APP FEATURES & DASHBOARD
// ========================================================

// 1. Dashboard Loading
async function loadDashboard() {
  try {
    const data = await API.getOverview();
    $('#dashAttendance').textContent = `${data.averageAttendance || 85}%`;
    $('#dashUpcoming').textContent = String(data.upcomingCount || 0).padStart(2, '0');
    $('#dashJournal').textContent = String(data.journalCount || 0).padStart(2, '0');

    // Deadlines & Exams list
    const deadlinesList = $('#dashboardDeadlines');
    if (data.deadlines && data.deadlines.length > 0) {
      deadlinesList.innerHTML = data.deadlines.map(x => `
        <div class="list-row">
          <i class="swatch" style="background:${typeColor(x.type)};margin-top:5px"></i>
          <div style="flex:1">
            <b>${esc(x.title)}</b>
            <small>${fmtDate(x.date, { day: 'numeric', month: 'short' })} · ${esc(x.detail || '')}</small>
          </div>
          <span class="tag ${x.type === 'exam' ? 'purple' : x.type === 'event' ? '' : 'blue'}" style="${x.type === 'exam' ? 'background:#d1c4e9;color:#311b92' : ''}">${esc(x.type.toUpperCase())}</span>
        </div>
      `).join('');
    } else {
      deadlinesList.innerHTML = '<div class="empty">Nothing queued right now.</div>';
    }

    $('#clockDate').textContent = new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }).toUpperCase();
    $('#clockYear').textContent = new Date().getFullYear() + ' · CLOCKIT';

    // Refresh annual ring markers
    renderAcademicClock();
  } catch (err) {
    console.warn('Dashboard fetch fallback:', err.message);
  }
}

// 2. Academic Clock Wheel (Annual 12-Month Ring)
function renderAcademicClock() {
  const clock = $('#academicClock');
  if (!clock) return;

  clock.querySelectorAll('.month, .clock-tick, .clock-day-number, .activity-marker').forEach(n => n.remove());

  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const year = 2026;
  const leap = (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
  const yearDays = leap ? 366 : 365;

  const polar = (day, radius) => {
    const a = (-90 + (day / yearDays) * 360) * Math.PI / 180;
    return {
      x: 50 + Math.cos(a) * radius,
      y: 50 + Math.sin(a) * radius,
      a: a * 180 / Math.PI
    };
  };

  // Month names
  let elapsed = 0;
  months.forEach((m, i) => {
    const days = new Date(year, i + 1, 0).getDate();
    const mid = elapsed + (days - 1) / 2;
    const pos = polar(mid, 42);
    const el = document.createElement('div');
    el.className = 'month';
    el.textContent = m;
    el.style.setProperty('--mx', pos.x + '%');
    el.style.setProperty('--my', pos.y + '%');
    clock.append(el);
    elapsed += days;
  });

  // Daily ticks & date labels
  elapsed = 0;
  for (let day = 0; day < yearDays; day++) {
    const date = new Date(year, 0, 1 + day);
    const isMonthStart = date.getDate() === 1;
    const isWeek = date.getDay() === 0;
    const pos = polar(day, isMonthStart ? 38.5 : 37.5);

    const tick = document.createElement('span');
    tick.className = 'clock-tick' + (isMonthStart ? ' month-start' : isWeek ? ' week' : '');
    tick.style.left = pos.x + '%';
    tick.style.top = pos.y + '%';
    tick.style.transform = `translate(-50%, 0) rotate(${pos.a + 90}deg)`;
    tick.style.setProperty('--tick-h', isMonthStart ? '10px' : isWeek ? '6px' : '3px');
    clock.append(tick);

    if ([1, 10, 20].includes(date.getDate())) {
      const label = document.createElement('span');
      const lp = polar(day, 31.5);
      label.className = 'clock-day-number';
      label.textContent = date.getDate();
      label.style.setProperty('--dx', lp.x + '%');
      label.style.setProperty('--dy', lp.y + '%');
      clock.append(label);
    }
  }

  // Activity markers from database calendar items
  const grouped = {};
  calendarItems.forEach(item => {
    if (!item.date) return;
    (grouped[item.date] ||= []).push(item);
  });

  Object.entries(grouped).forEach(([dateStr, items]) => {
    items.forEach((item, index) => {
      const [y, m, d] = dateStr.split('-').map(Number);
      const dt = new Date(y, m - 1, d);
      if (y !== year) return;

      const dayOfYear = Math.floor((dt - new Date(y, 0, 1)) / 86400000);
      const pos = polar(dayOfYear, index ? 28.5 : 34);

      const marker = document.createElement('button');
      marker.type = 'button';
      marker.className = 'activity-marker ' + (item.type || 'deadline');
      marker.style.setProperty('--ax', pos.x + '%');
      marker.style.setProperty('--ay', pos.y + '%');
      marker.title = `${item.title} · ${fmtDate(dateStr, { day: 'numeric', month: 'long', year: 'numeric' })} · ${item.type.toUpperCase()}`;
      marker.setAttribute('aria-label', marker.title);

      marker.onclick = () => {
        selectedDate = dateStr;
        calendarDate = new Date(y, m - 1, 1);
        go('calendar');
        toast(`Viewing: ${item.title}`);
      };

      clock.append(marker);
    });
  });

  // Current real-time clock hands
  const now = new Date();
  const hourDeg = (now.getHours() % 12) * 30 + now.getMinutes() / 2;
  const minDeg = now.getMinutes() * 6;
  $('#hourHand').style.transform = `rotate(${hourDeg}deg)`;
  $('#minuteHand').style.transform = `rotate(${minDeg}deg)`;
}

// 3. Academic Calendar & Exam Timetable
async function renderCalendar() {
  renderAcademicClock();

  const y = calendarDate.getFullYear();
  const m = calendarDate.getMonth();
  $('#monthTitle').textContent = calendarDate.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });

  const grid = $('#calendarGrid');
  grid.innerHTML = '';

  ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'].forEach(w => {
    const el = document.createElement('div');
    el.className = 'weekday';
    el.textContent = w;
    grid.append(el);
  });

  const firstDay = new Date(y, m, 1).getDay();
  const totalDays = new Date(y, m + 1, 0).getDate();
  const prevMonthDays = new Date(y, m, 0).getDate();

  for (let i = 0; i < 42; i++) {
    const d = i - firstDay + 1;
    let dt;
    let muted = false;

    if (d < 1) {
      dt = iso(y, m - 1, prevMonthDays + d);
      muted = true;
    } else if (d > totalDays) {
      dt = iso(y, m + 1, d - totalDays);
      muted = true;
    } else {
      dt = iso(y, m, d);
    }

    const b = document.createElement('button');
    b.className = 'day' + (muted ? ' muted' : '') + (dt === '2026-10-20' ? ' today' : '') + (dt === selectedDate ? ' selected' : '');

    const dayItems = calendarItems.filter(x => x.date === dt);
    const hasExam = dayItems.some(x => x.type === 'exam');
    if (hasExam) b.style.borderColor = '#9575cd';

    b.innerHTML = `
      ${Number(dt.slice(-2))}
      <div class="day-markers">
        ${dayItems.slice(0, 3).map(x => `<i style="background:${typeColor(x.type)}"></i>`).join('')}
      </div>
    `;

    b.onclick = () => {
      selectedDate = dt;
      renderCalendar();
    };

    grid.append(b);
  }

  $('#selectedDateTitle').textContent = fmtDate(selectedDate, {
    weekday: 'short', month: 'long', day: 'numeric', year: 'numeric'
  });

  const list = calendarItems.filter(x => x.date === selectedDate);
  $('#selectedDateCount').textContent = `${list.length} ITEM${list.length === 1 ? '' : 'S'}`;

  const itemsContainer = $('#selectedDateItems');
  if (list.length > 0) {
    itemsContainer.innerHTML = list.map(x => `
      <div class="list-row" style="${x.type === 'exam' ? 'border-left: 4px solid #7d65b5; background:#faf7ff;' : ''}">
        <i class="swatch" style="background:${typeColor(x.type)};margin-top:5px"></i>
        <div style="flex:1">
          <b>${esc(x.title)}</b>
          <small>${esc(x.detail || '')}</small>
        </div>
        <span class="tag ${x.type === 'exam' ? 'purple' : x.type === 'event' ? '' : 'blue'}" style="${x.type === 'exam' ? 'background:#d1c4e9;color:#311b92' : ''}">${esc(x.type.toUpperCase())}</span>
      </div>
    `).join('');
  } else {
    itemsContainer.innerHTML = '<div class="empty">A peaceful open day. No official exams or submissions scheduled. ✿</div>';
  }

  $('#adminCalendarCount').textContent = calendarItems.length;
}

$('#prevMonth').onclick = () => {
  calendarDate.setMonth(calendarDate.getMonth() - 1);
  renderCalendar();
};

$('#nextMonth').onclick = () => {
  calendarDate.setMonth(calendarDate.getMonth() + 1);
  renderCalendar();
};

$('#addCalendarEvent').onclick = () => modal('Add Calendar Milestone', `
  <div class="field"><label>Title</label><input name="title" required placeholder="e.g. End Sem Preparation or Lab Record"></div>
  <div class="field"><label>Date</label><input name="date" type="date" required value="${selectedDate}"></div>
  <div class="field"><label>Category</label><select name="type">
    <option value="exam">Official End-Sem Exam / Lab Test</option>
    <option value="deadline" selected>Assignment / Record Deadline</option>
    <option value="event">Campus Event / Study Sprint</option>
    <option value="completed">Completed Milestone</option>
  </select></div>
  <div class="field"><label>Notes / Timings / Venue</label><input name="detail" placeholder="e.g. FN (10:00 AM - 1:00 PM) · MIT Chromepet"></div>
`, async (d) => {
  const saved = await API.addCalendarItem(d);
  calendarItems.push(saved);
  selectedDate = d.date;
  const [sy, sm] = d.date.split('-').map(Number);
  calendarDate = new Date(sy, sm - 1, 1);
  renderCalendar();
  loadDashboard();
  toast('Item saved to SQL database ✿');
});

// 4. Campus Events Screen
async function renderEvents() {
  const root = $('#eventCards');
  if (!events.length) {
    root.innerHTML = '<div class="empty" style="grid-column:1/-1">No campus events currently scheduled.</div>';
    return;
  }

  root.innerHTML = events.map((e, i) => `
    <article class="panel">
      <div class="event-poster">${['✧ meet & share', '⌘ code & sprint', '✿ find your calm'][i % 3]}</div>
      <span class="tag ${i === 1 ? 'blue' : i === 2 ? 'green' : ''}">${esc(e.category)}</span>
      <h3 style="margin-top:12px">${esc(e.title)}</h3>
      <p style="font-size:12px;line-height:1.6;color:var(--muted)">${esc(e.description || '')}</p>
      <div class="list-row">
        <span>▦</span>
        <div>
          <b>${fmtDate(e.date, { day: 'numeric', month: 'long', year: 'numeric' })} · ${esc(e.time || '')}</b>
          <small>${esc(e.place || 'MIT Campus')}</small>
        </div>
      </div>
      <button class="btn ${e.registered ? 'ghost' : 'yellow'}" style="width:100%;margin-top:12px" data-register="${esc(e.id)}">
        ${e.registered ? '✓ Registered in Database' : 'Register interest →'}
      </button>
    </article>
  `).join('');

  root.querySelectorAll('[data-register]').forEach(b => {
    b.onclick = async () => {
      const id = b.dataset.register;
      const res = await API.toggleEventRegistration(id);
      events = res.events;
      renderEvents();
      toast('Registration updated in database');
    };
  });

  $('#adminEventCount').textContent = events.length;
}

$('#createEventBtn').onclick = () => modal('Add Campus Event', `
  <div class="field"><label>Event title</label><input name="title" required placeholder="e.g. End-Sem Peer Discussion"></div>
  <div class="field"><label>Category</label><select name="category">
    <option>WORKSHOP</option>
    <option>TECH TALK</option>
    <option>STUDY SPRINT</option>
    <option>CAMPUS LIFE</option>
  </select></div>
  <div class="field"><label>Date</label><input name="date" type="date" required value="2026-11-06"></div>
  <div class="field"><label>Time</label><input name="time" value="2:00 PM"></div>
  <div class="field"><label>Venue</label><input name="place" placeholder="Rajam Hall, MIT Campus"></div>
  <div class="field"><label>Description</label><textarea name="description" placeholder="Details about this campus activity..."></textarea></div>
`, async (d) => {
  const saved = await API.addEvent(d);
  events.push(saved);
  calendarItems.push({
    id: 'cal-gen-' + Date.now(),
    date: d.date,
    type: 'event',
    title: d.title,
    detail: `Event · ${d.place || 'MIT Campus'}`
  });
  renderEvents();
  renderCalendar();
  loadDashboard();
  toast('Event created and saved to database');
});

// 5. Attendance Tracker Screen
async function renderAttendance() {
  const tb = $('#attendanceRows');
  if (!subjects.length) {
    tb.innerHTML = '<tr><td colspan="5" class="empty">No subjects in database.</td></tr>';
    return;
  }

  tb.innerHTML = subjects.map(s => {
    const total = s.total || 0;
    const attended = s.attended || 0;
    const pct = total > 0 ? Math.round((attended / total) * 100) : 100;
    const isShortage = pct < 75;

    return `
      <tr>
        <td>
          <b>${esc(s.name)}</b><br>
          <small style="color:var(--muted)">${esc(s.code)} · ${s.units} units</small>
        </td>
        <td>${attended}</td>
        <td>${total}</td>
        <td>
          <b style="color:${isShortage ? '#c62828' : 'inherit'}">${pct}%</b>
        </td>
        <td>
          <div style="display:flex;align-items:center;gap:10px">
            <div class="progress" style="flex:1">
              <span style="width:${pct}%;background:${isShortage ? '#e57373' : '#81c784'}"></span>
            </div>
            <span class="tag ${isShortage ? 'pink' : 'green'}" style="font-size:9px">
              ${isShortage ? '⚠ SHORTAGE (<75%)' : '✓ ELIGIBLE'}
            </span>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  const valid = subjects.filter(s => s.total > 0);
  const avg = valid.length ? Math.round(valid.reduce((a, s) => a + (s.attended / s.total) * 100, 0) / valid.length) : 85;
  $('#dashAttendance').textContent = avg + '%';
}

$('#recordAttendance').onclick = () => modal('Record Attendance for Today', `
  <div class="field"><label>Subject</label><select name="subject">
    ${subjects.map(s => `<option value="${esc(s.code)}">${esc(s.name)} (${esc(s.code)})</option>`).join('')}
  </select></div>
  <div class="field"><label>Today's Status</label><select name="status">
    <option value="present">Present (Attended)</option>
    <option value="absent">Absent (Missed)</option>
  </select></div>
  <div class="note blue">Updates live subject totals in H2 database and recalculates exam eligibility.</div>
`, async (d) => {
  await API.recordAttendance(d.subject, d.status);
  subjects = await API.getSubjects();
  renderAttendance();
  loadDashboard();
  toast('Attendance saved to database');
});

// 6. Syllabus Shelf Screen
async function renderSyllabus() {
  const container = $('#syllabusCards');
  if (!subjects.length) {
    container.innerHTML = '<div class="empty" style="grid-column:1/-1">No subjects found.</div>';
    return;
  }

  container.innerHTML = subjects.map(s => `
    <div class="panel">
      <div class="panel-head">
        <h3>${esc(s.name)}</h3>
        <span class="pill">${esc(s.code)}</span>
      </div>
      <div class="eyebrow">UNIT &amp; SYLLABUS PROGRESS</div>
      <div class="subject-row">
        <span>Curriculum Progress</span>
        <div class="progress"><span style="width:${s.progress}%"></span></div>
        <b>${s.progress}%</b>
      </div>
      <p style="font-size:12px;color:var(--muted);line-height:1.6">
        ${s.units} syllabus units · Anna University Regulation 2023 · B.E. CSE / CT
      </p>
      <button class="btn ghost" data-edit-subject="${esc(s.code)}">Update progress</button>
    </div>
  `).join('');

  $$('[data-edit-subject]').forEach(b => {
    b.onclick = () => {
      const s = subjects.find(x => x.code === b.dataset.editSubject);
      if (!s) return;
      modal('Update Subject Progress', `
        <div class="field"><label>Subject Name</label><input name="name" required value="${esc(s.name)}"></div>
        <div class="field"><label>Number of Units</label><input name="units" type="number" min="1" max="10" value="${s.units}"></div>
        <div class="field"><label>Completed Progress (%)</label><input name="progress" type="number" min="0" max="100" value="${s.progress}"></div>
      `, async (d) => {
        await API.updateSubject(s.code, d);
        subjects = await API.getSubjects();
        renderSyllabus();
        renderAttendance();
        toast('Syllabus progress updated in database');
      });
    };
  });

  $('#adminSubjectCount').textContent = subjects.length;
}

$('#addSubject').onclick = () => modal('Add Curriculum Subject', `
  <div class="field"><label>Subject Name</label><input name="name" required placeholder="e.g. Machine Learning"></div>
  <div class="field"><label>Subject Code</label><input name="code" required placeholder="CS23603"></div>
  <div class="field"><label>Total Units</label><input name="units" type="number" min="1" value="5"></div>
  <div class="field"><label>Initial progress %</label><input name="progress" type="number" min="0" max="100" value="0"></div>
`, async (d) => {
  const saved = await API.addSubject(d);
  subjects.push(saved);
  renderSyllabus();
  renderAttendance();
  toast('New subject added to SQL database');
});

// 7. Marks & Assessments Screen
async function renderMarks() {
  $('#marksRows').innerHTML = marks.map(m => `
    <tr>
      <td>${esc(m.subject)}</td>
      <td>${esc(m.assessment)}</td>
      <td>${m.score}</td>
      <td>${m.outof}</td>
      <td><button class="btn ghost" data-delete-mark="${esc(m.id)}">Remove</button></td>
    </tr>
  `).join('');

  $$('[data-delete-mark]').forEach(b => {
    b.onclick = async () => {
      const id = b.dataset.deleteMark;
      const res = await API.deleteMark(id);
      marks = res.marks;
      renderMarks();
      toast('Assessment mark removed from database');
    };
  });
}

$('#addMark').onclick = () => modal('Add Assessment Score', `
  <div class="field"><label>Subject</label><select name="subject">
    ${subjects.map(s => `<option>${esc(s.name)}</option>`).join('')}
  </select></div>
  <div class="field"><label>Assessment Name</label><input name="assessment" required placeholder="e.g. Assessment 1 / Lab Practical"></div>
  <div class="field"><label>Score Obtained</label><input name="score" type="number" min="0" step="0.5" required value="0"></div>
  <div class="field"><label>Total Marks (Out of)</label><input name="outof" type="number" min="1" step="0.5" required value="20"></div>
`, async (d) => {
  const saved = await API.addMark(d);
  marks.push(saved);
  renderMarks();
  toast('Mark saved to SQL database');
});

// 8. Scrapbook Journal Screen
async function renderJournal() {
  const todayDate = new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
  $('#journalDate').textContent = todayDate;

  const entriesList = $('#journalEntries');
  if (!journalEntries.length) {
    entriesList.innerHTML = '<div class="empty">Your saved pages will appear here. Write the first little memory. ♡</div>';
  } else {
    entriesList.innerHTML = journalEntries.map((j, i) => `
      <div class="list-row">
        <span style="font-size:20px">${j.mood || '🙂'}</span>
        <div style="flex:1">
          <b>${esc(j.title)}</b>
          <small>${esc(j.date)} · ${esc((j.text || '').slice(0, 90))}${(j.text || '').length > 90 ? '…' : ''}</small>
        </div>
        <button class="tiny-link" data-open-journal="${i}">Open</button>
      </div>
    `).join('');
  }

  $('#journalCount').textContent = `${journalEntries.length} ENTR${journalEntries.length === 1 ? 'Y' : 'IES'}`;
  $('#dashJournal').textContent = String(journalEntries.length).padStart(2, '0');

  $$('[data-open-journal]').forEach(b => {
    b.onclick = () => {
      const j = journalEntries[+b.dataset.openJournal];
      if (!j) return;
      $('#journalTitle').value = j.title;
      $('#journalText').value = j.text;
      selectedMood = j.mood || '🙂';
      $$('.mood').forEach(x => x.classList.toggle('active', x.dataset.mood === selectedMood));
    };
  });
}

$('#moodRow').onclick = e => {
  const b = e.target.closest('[data-mood]');
  if (!b) return;
  selectedMood = b.dataset.mood;
  $$('.mood').forEach(x => x.classList.toggle('active', x === b));
};

$('#saveJournal').onclick = async () => {
  const title = $('#journalTitle').value.trim() || 'A small memory';
  const text = $('#journalText').value.trim();

  try {
    const saved = await API.addJournal({ title, text, mood: selectedMood });
    journalEntries.unshift(saved);
    renderJournal();
    loadDashboard();
    $('#journalSaveState').textContent = 'Saved to H2 SQL database! Persisted across all sessions. ♡';
    toast('Journal entry saved to SQL database');
  } catch (err) {
    toast('Error: ' + err.message);
  }
};

// 9. Student Profile Screen
async function renderProfile() {
  if (currentStudent) {
    $('#profileName').value = currentStudent.name || 'Priyanthi A';
    $('#profileRoll').value = currentStudent.rollNumber || currentStudent.roll_number || '2023506001';
    $('#profileDept').value = currentStudent.department || 'Computer Technology';
    $('#profileProgramme').value = currentStudent.programme || 'B.E. Computer Science and Engineering';
    $('#profileSemester').value = currentStudent.semester || '3';
    $('#profileGender').value = currentStudent.gender || 'Female';

    if (currentStudent.avatarUrl) {
      $('#avatarPreview').innerHTML = `<img src="${currentStudent.avatarUrl}" alt="Profile avatar">`;
    }
    if (currentStudent.doodleApplied) {
      $('#avatarPreview').classList.add('doodle-preview');
      $('#doodleToggle').textContent = 'Remove doodle-inspired filter';
      doodleOn = true;
    }
  }
}

$('#avatarUpload').onchange = e => {
  const f = e.target.files[0];
  if (!f) return;
  const reader = new FileReader();
  reader.onload = () => {
    photoUrl = reader.result;
    $('#avatarPreview').innerHTML = `<img src="${photoUrl}" alt="Selected profile preview">`;
    doodleOn = false;
    $('#avatarPreview').classList.remove('doodle-preview');
  };
  reader.readAsDataURL(f);
};

$('#doodleToggle').onclick = () => {
  if (!$('#avatarPreview img')) {
    toast('Choose a photo first');
    return;
  }
  doodleOn = !doodleOn;
  $('#avatarPreview').classList.toggle('doodle-preview', doodleOn);
  $('#doodleToggle').textContent = doodleOn ? 'Remove doodle-inspired filter' : 'Apply doodle-inspired filter';
  toast(doodleOn ? 'Doodle illustration filter applied' : 'Filter removed');
};

$('#saveProfile').onclick = async () => {
  const payload = {
    name: $('#profileName').value.trim() || 'Priyanthi A',
    rollNumber: $('#profileRoll').value.trim() || '2023506001',
    department: $('#profileDept').value,
    programme: $('#profileProgramme').value.trim(),
    semester: parseInt($('#profileSemester').value, 10),
    gender: $('#profileGender').value,
    avatarUrl: photoUrl || (currentStudent && currentStudent.avatarUrl) || '',
    doodleApplied: doodleOn
  };

  const newPass = $('#profilePassword').value.trim();
  if (newPass) payload.password = newPass;

  try {
    const updated = await API.updateProfile(payload);
    currentStudent = updated;
    localStorage.setItem('clockit_user', JSON.stringify(updated));
    updateUserDisplay();
    toast('Profile updated in H2 SQL database ✿');
  } catch (err) {
    toast('Error: ' + err.message);
  }
};

// 10. Admin Metrics & SQL Database Explorer
let currentDbTable = 'students';

async function renderDbTable(table) {
  currentDbTable = table || 'students';
  const meta = $('#dbTableMeta');
  const container = $('#dbTableContainer');
  if (!container) return;

  $$('.db-tab-btn').forEach(btn => {
    btn.classList.toggle('yellow', btn.dataset.table === currentDbTable);
    btn.classList.toggle('ghost', btn.dataset.table !== currentDbTable);
  });

  try {
    let rows = [];
    let cols = [];

    if (currentDbTable === 'students') {
      const s = await API.getProfile();
      rows = s ? [s] : [];
      cols = ['rollNumber', 'name', 'department', 'semester', 'gender', 'programme', 'password'];
    } else if (currentDbTable === 'subjects') {
      rows = await API.getSubjects();
      cols = ['code', 'name', 'units', 'progress', 'attended', 'total'];
    } else if (currentDbTable === 'calendar_items') {
      rows = await API.getCalendar();
      cols = ['date', 'title', 'type', 'detail'];
    } else if (currentDbTable === 'campus_events') {
      rows = await API.getEvents();
      cols = ['date', 'title', 'category', 'place', 'time', 'registered'];
    } else if (currentDbTable === 'assessment_marks') {
      rows = await API.getMarks();
      cols = ['subject', 'assessment', 'score', 'outof'];
    } else if (currentDbTable === 'journal_entries') {
      rows = await API.getJournal();
      cols = ['date', 'title', 'mood', 'text'];
    }

    if (meta) {
      meta.innerHTML = `Showing records for table: <b style="color:var(--text);font-family:monospace">${currentDbTable}</b> (${rows.length} rows)`;
    }

    if (!rows || rows.length === 0) {
      container.innerHTML = '<div style="padding:20px;text-align:center;color:var(--muted)">No records in table.</div>';
      return;
    }

    let html = '<table style="width:100%;border-collapse:collapse;font-size:12px;text-align:left">';
    html += '<thead style="background:var(--bg);border-bottom:1px solid var(--border)"><tr>';
    cols.forEach(c => {
      html += `<th style="padding:10px 14px;font-weight:600;color:var(--muted);text-transform:uppercase;letter-spacing:0.5px">${c}</th>`;
    });
    html += '</tr></thead><tbody>';

    rows.forEach((r, idx) => {
      const bg = idx % 2 === 0 ? 'transparent' : 'rgba(0,0,0,0.02)';
      html += `<tr style="border-bottom:1px solid var(--border);background:${bg}">`;
      cols.forEach(c => {
        let val = r[c] !== undefined && r[c] !== null ? r[c] : '';
        if (typeof val === 'boolean') {
          val = val ? '<span class="tag green">TRUE</span>' : '<span class="tag">FALSE</span>';
        } else if (c === 'progress') {
          val = `<span class="pill" style="background:#eaf0ea;color:#2d5032;font-weight:600">${val}%</span>`;
        } else if (c === 'type' || c === 'category') {
          val = `<span class="pill" style="font-size:11px">${esc(String(val))}</span>`;
        } else if (c === 'text' && String(val).length > 80) {
          val = esc(String(val).substring(0, 80)) + '...';
        } else {
          val = esc(String(val));
        }
        html += `<td style="padding:10px 14px;vertical-align:top">${val}</td>`;
      });
      html += '</tr>';
    });

    html += '</tbody></table>';
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div style="padding:20px;color:#a63d33">Failed to load table rows: ${esc(err.message)}</div>`;
  }
}

$$('.db-tab-btn').forEach(b => {
  b.onclick = () => renderDbTable(b.dataset.table);
});

async function renderAdmin() {
  try {
    const stats = await API.getAdminStats();
    $('#adminCalendarCount').textContent = stats.calendarItems || 0;
    $('#adminEventCount').textContent = stats.events || 0;
    $('#adminSubjectCount').textContent = stats.subjects || 0;
    renderDbTable(currentDbTable);
  } catch (err) {
    console.warn('Admin stats error:', err);
  }
}

// 11. Global Search Handler
$('#globalSearch').addEventListener('keydown', async (e) => {
  if (e.key !== 'Enter') return;
  const q = e.target.value.trim();
  if (!q) return;

  try {
    const res = await API.search(q);
    if (res.subjects && res.subjects.length > 0) {
      go('syllabus');
      toast(`Found ${res.subjects.length} matching subject(s) in database`);
    } else if (res.events && res.events.length > 0) {
      go('events');
      toast(`Found ${res.events.length} matching event(s) in database`);
    } else if (res.calendarItems && res.calendarItems.length > 0) {
      go('calendar');
      toast(`Found ${res.calendarItems.length} matching calendar item(s)`);
    } else {
      toast('No matching item found in database');
    }
  } catch (err) {
    toast('Search error: ' + err.message);
  }
});

// Fetch all initial data once student is authenticated
async function initAppData() {
  try {
    const [overviewData, subjectsData, calendarData, eventsData, marksData, journalData] = await Promise.all([
      API.getOverview().catch(() => ({})),
      API.getSubjects().catch(() => []),
      API.getCalendar().catch(() => []),
      API.getEvents().catch(() => []),
      API.getMarks().catch(() => []),
      API.getJournal().catch(() => [])
    ]);

    subjects = subjectsData;
    calendarItems = calendarData;
    events = eventsData;
    marks = marksData;
    journalEntries = journalData;

    renderAcademicClock();
    loadDashboard();
    renderCalendar();
    renderEvents();
    renderAttendance();
    renderSyllabus();
    renderMarks();
    renderJournal();
    renderProfile();
    renderAdmin();

    console.log('✨ ClockIt platform initialized with H2 SQL Database.');
  } catch (err) {
    console.error('Data initialization error:', err);
  }
}

// Bootstrap on window load
window.addEventListener('DOMContentLoaded', () => {
  checkAuthStatus();
});
