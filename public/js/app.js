/**
 * MIT Academic Clock — Application Controller
 * Synchronized with Java REST API & H2 SQL Database.
 */

const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

const titles = {
  dashboard: 'Your academic overview',
  calendar: 'Academic calendar',
  events: 'Campus events',
  attendance: 'Attendance tracker',
  syllabus: 'Syllabus shelf',
  marks: 'Marks & assessments',
  journal: 'Your scrapbook journal',
  profile: 'My student profile',
  admin: 'Academic data desk',
  login: 'Student & admin access'
};

// Application State (populated via live Java Database API)
let currentScreen = 'dashboard';
let calendarDate = new Date(2026, 9, 1); // October 2026
let selectedDate = '2026-10-20';
let selectedMood = '🙂';
let doodleOn = false;
let loginMode = 'student';
let photoUrl = '';

let calendarItems = [];
let events = [];
let subjects = [];
let marks = [];
let journalEntries = [];
let studentProfile = {};

// Toast Notification
function toast(msg) {
  const t = $('#toast');
  if (!t) return;
  t.textContent = msg;
  t.style.display = 'block';
  clearTimeout(window.toastTimer);
  window.toastTimer = setTimeout(() => t.style.display = 'none', 2600);
}

// Navigation Router
function go(id) {
  if (!titles[id] && id !== 'login') return;
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
    deadline: '#d99588',
    event: '#e6c96e',
    exam: '#b2a5ce',
    completed: '#9ebd8e'
  })[t] || '#8fb6c2';
}

// Modal Utility
function modal(title, fieldsHtml, onSave) {
  $('#modalTitle').textContent = title;
  $('#modalFields').innerHTML = fieldsHtml;
  $('#modal').classList.add('show');

  $('#modalForm').onsubmit = async (e) => {
    e.preventDefault();
    const data = {};
    new FormData(e.target).forEach((v, k) => data[k] = v);
    try {
      await onSave(data);
      $('#modal').classList.remove('show');
      e.target.reset();
    } catch (err) {
      toast('Error: ' + err.message);
    }
  };
}

$('#closeModal').onclick = $('#cancelModal').onclick = () => $('#modal').classList.remove('show');
$('#modal').onclick = e => { if (e.target.id === 'modal') $('#modal').classList.remove('show'); };

// 1. Dashboard Loading
async function loadDashboard() {
  try {
    const data = await API.getOverview();
    $('#dashAttendance').textContent = `${data.averageAttendance || 85}%`;
    $('#dashUpcoming').textContent = String(data.upcomingCount || 0).padStart(2, '0');
    $('#dashJournal').textContent = String(data.journalCount || 0).padStart(2, '0');

    // Deadlines list
    const deadlinesList = $('#dashboardDeadlines');
    if (data.deadlines && data.deadlines.length > 0) {
      deadlinesList.innerHTML = data.deadlines.map(x => `
        <div class="list-row">
          <i class="swatch" style="background:${typeColor(x.type)};margin-top:5px"></i>
          <div style="flex:1">
            <b>${esc(x.title)}</b>
            <small>${fmtDate(x.date, { day: 'numeric', month: 'short' })} · ${esc(x.detail || '')}</small>
          </div>
          <span class="tag ${x.type === 'event' ? '' : 'blue'}">${esc(x.type.toUpperCase())}</span>
        </div>
      `).join('');
    } else {
      deadlinesList.innerHTML = '<div class="empty">Nothing queued right now.</div>';
    }

    $('#clockDate').textContent = new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }).toUpperCase();
    $('#clockYear').textContent = new Date().getFullYear() + ' · LIVE DB';

    // Refresh annual ring markers
    renderAcademicClock();
  } catch (err) {
    console.warn('Dashboard fetch fallback:', err.message);
  }
}

// 2. Academic Clock (Wheel / Ring)
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

  // Daily graduations & week ticks
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
    tick.style.transform = `translate(-50%,0) rotate(${pos.a + 90}deg)`;
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

  // Activity pins from database
  const grouped = {};
  calendarItems.forEach(item => {
    if (!item.date) return;
    (grouped[item.date] ||= []).push(item);
  });

  Object.entries(grouped).forEach(([date, items]) => items.forEach((item, index) => {
    const [y, m, d] = date.split('-').map(Number);
    const dt = new Date(y, m - 1, d);
    if (y !== year) return;
    const dayOfYear = Math.floor((dt - new Date(y, 0, 1)) / 86400000);
    const pos = polar(dayOfYear, index ? 28.5 : 34);

    const marker = document.createElement('button');
    marker.type = 'button';
    marker.className = 'activity-marker ' + (item.type || 'deadline');
    marker.style.setProperty('--ax', pos.x + '%');
    marker.style.setProperty('--ay', pos.y + '%');
    marker.title = `${item.title} · ${fmtDate(date, { day: 'numeric', month: 'long', year: 'numeric' })} · ${item.type}`;
    marker.setAttribute('aria-label', marker.title);
    marker.onclick = () => {
      selectedDate = date;
      calendarDate = new Date(y, m - 1, 1);
      go('calendar');
    };
    clock.append(marker);
  }));

  // Hands
  const now = new Date();
  $('#hourHand').style.transform = `rotate(${(now.getHours() % 12) * 30 + now.getMinutes() / 2}deg)`;
  $('#minuteHand').style.transform = `rotate(${now.getMinutes() * 6}deg)`;
}

// 3. Calendar Screen
async function renderCalendar() {
  renderAcademicClock();
  const y = calendarDate.getFullYear(), m = calendarDate.getMonth();
  $('#monthTitle').textContent = calendarDate.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });

  const grid = $('#calendarGrid');
  grid.innerHTML = '';
  ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'].forEach(w => {
    const el = document.createElement('div');
    el.className = 'weekday';
    el.textContent = w;
    grid.append(el);
  });

  const first = new Date(y, m, 1).getDay();
  const days = new Date(y, m + 1, 0).getDate();
  const prevDays = new Date(y, m, 0).getDate();

  for (let i = 0; i < 42; i++) {
    const d = i - first + 1;
    let dt, muted = false;
    if (d < 1) {
      dt = iso(y, m - 1, prevDays + d);
      muted = true;
    } else if (d > days) {
      dt = iso(y, m + 1, d - days);
      muted = true;
    } else {
      dt = iso(y, m, d);
    }

    const b = document.createElement('button');
    b.className = 'day' + (muted ? ' muted' : '') + (dt === '2026-10-20' ? ' today' : '') + (dt === selectedDate ? ' selected' : '');
    const dayMarkers = calendarItems.filter(x => x.date === dt).slice(0, 3).map(x => `<i style="background:${typeColor(x.type)}"></i>`).join('');
    b.innerHTML = `${Number(dt.slice(-2))}<div class="day-markers">${dayMarkers}</div>`;
    b.onclick = () => {
      selectedDate = dt;
      renderCalendar();
    };
    grid.append(b);
  }

  $('#selectedDateTitle').textContent = fmtDate(selectedDate, { weekday: 'short', month: 'long', day: 'numeric', year: 'numeric' });
  const list = calendarItems.filter(x => x.date === selectedDate);
  $('#selectedDateCount').textContent = `${list.length} ITEM${list.length === 1 ? '' : 'S'}`;

  $('#selectedDateItems').innerHTML = list.length ? list.map(x => `
    <div class="list-row">
      <i class="swatch" style="background:${typeColor(x.type)};margin-top:5px"></i>
      <div style="flex:1">
        <b>${esc(x.title)}</b>
        <small>${esc(x.detail || '')}</small>
      </div>
      <span class="tag ${x.type === 'event' ? '' : 'blue'}">${esc(x.type.toUpperCase())}</span>
    </div>
  `).join('') : '<div class="empty">A little blank space. Add a deadline, event or reminder for this day. ✿</div>';

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

$('#addCalendarEvent').onclick = () => modal('Add a calendar item', `
  <div class="field"><label>Title</label><input name="title" required placeholder="e.g. Assignment submission"></div>
  <div class="field"><label>Date</label><input name="date" type="date" required value="${selectedDate}"></div>
  <div class="field"><label>Category</label><select name="type">
    <option value="deadline">Assignment / registration deadline</option>
    <option value="event">Campus event</option>
    <option value="exam">Exam / lab reminder</option>
    <option value="completed">Completed work</option>
  </select></div>
  <div class="field"><label>Notes</label><input name="detail" placeholder="Time, location or reminder"></div>
  <div class="note blue">Persisted in H2 SQL database via JDBC connection.</div>
`, async (d) => {
  const saved = await API.addCalendarItem({ ...d, detail: d.detail || 'Personal reminder' });
  calendarItems.push(saved);
  selectedDate = d.date;
  const [sy, sm] = d.date.split('-').map(Number);
  calendarDate = new Date(sy, sm - 1, 1);
  renderCalendar();
  loadDashboard();
  toast('Calendar item saved to database ✿');
});

// 4. Campus Events Screen
async function renderEvents() {
  const root = $('#eventCards');
  root.innerHTML = events.map((e, i) => `
    <article class="panel">
      <div class="event-poster">${['✧ meet & share', '⌘ make & learn', '✿ find your people'][i % 3]}</div>
      <span class="tag ${i === 1 ? 'blue' : i === 2 ? 'green' : ''}">${esc(e.category)}</span>
      <h3 style="margin-top:12px">${esc(e.title)}</h3>
      <p style="font-size:12px;line-height:1.6;color:var(--muted)">${esc(e.description || '')}</p>
      <div class="list-row">
        <span>▦</span>
        <div>
          <b>${fmtDate(e.date, { day: 'numeric', month: 'long', year: 'numeric' })} · ${esc(e.time || '')}</b>
          <small>${esc(e.place || 'Venue TBC')}</small>
        </div>
      </div>
      <button class="btn ${e.registered ? 'ghost' : 'yellow'}" style="width:100%;margin-top:12px" data-event-id="${esc(e.id)}">
        ${e.registered ? '✓ Registered' : 'Register interest →'}
      </button>
    </article>
  `).join('');

  root.querySelectorAll('[data-event-id]').forEach(b => {
    b.onclick = async () => {
      const id = b.dataset.eventId;
      try {
        const res = await API.toggleEventRegistration(id);
        events = res.events;
        renderEvents();
        toast('Event registration updated in database');
      } catch (err) {
        toast('Error: ' + err.message);
      }
    };
  });

  $('#adminEventCount').textContent = events.length;
}

$('#createEventBtn').onclick = () => modal('Add a campus event', `
  <div class="field"><label>Event title</label><input name="title" required></div>
  <div class="field"><label>Category</label><select name="category">
    <option>TECH TALK</option>
    <option>WORKSHOP</option>
    <option>CAMPUS LIFE</option>
    <option>CLUB EVENT</option>
  </select></div>
  <div class="field"><label>Date</label><input name="date" type="date" required value="2026-11-15"></div>
  <div class="field"><label>Time</label><input name="time" value="2:00 PM"></div>
  <div class="field"><label>Venue</label><input name="place" placeholder="To be confirmed"></div>
  <div class="field"><label>Description</label><textarea name="description" placeholder="What should students know?"></textarea></div>
`, async (d) => {
  const saved = await API.addEvent(d);
  events.push(saved);
  calendarItems.push({ id: 'cal-' + saved.id, title: saved.title, date: saved.date, type: 'event', detail: saved.place });
  renderEvents();
  renderCalendar();
  loadDashboard();
  toast('Campus event saved to SQL database');
});

// 5. Attendance Tracker Screen
async function renderAttendance() {
  const tb = $('#attendanceRows');
  tb.innerHTML = subjects.map(s => {
    const pct = s.attendancePercentage ?? (s.total > 0 ? Math.round(s.attended / s.total * 100) : 100);
    return `
      <tr>
        <td><b>${esc(s.name)}</b><br><small style="color:var(--muted)">${esc(s.code)}</small></td>
        <td>${s.attended}</td>
        <td>${s.total}</td>
        <td><b>${pct}%</b></td>
        <td>
          <div class="progress">
            <span style="width:${pct}%;background:${pct < 75 ? '#d99588' : '#8fb6c2'}"></span>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  if (subjects.length > 0) {
    const avg = Math.round(subjects.reduce((a, s) => a + (s.attendancePercentage || 0), 0) / subjects.length);
    $('#dashAttendance').textContent = `${avg}%`;
  }
}

$('#recordAttendance').onclick = () => modal('Record attendance', `
  <div class="field"><label>Subject</label><select name="subject">
    ${subjects.map(s => `<option value="${esc(s.code)}">${esc(s.name)} (${esc(s.code)})</option>`).join('')}
  </select></div>
  <div class="field"><label>Today's status</label><select name="status">
    <option value="present">Present</option>
    <option value="absent">Absent</option>
  </select></div>
  <div class="note blue">Updates total classes and attended classes directly in the H2 SQL database.</div>
`, async (d) => {
  const res = await API.recordAttendance(d.subject, d.status);
  subjects = res.subjects;
  renderAttendance();
  loadDashboard();
  toast('Attendance saved to database');
});

// 6. Syllabus Shelf Screen
async function renderSyllabus() {
  $('#syllabusCards').innerHTML = subjects.map(s => `
    <div class="panel">
      <div class="panel-head">
        <h3>${esc(s.name)}</h3>
        <span class="pill">${esc(s.code)}</span>
      </div>
      <div class="eyebrow">UNIT PROGRESS</div>
      <div class="subject-row">
        <span>Study progress</span>
        <div class="progress"><span style="width:${s.progress}%"></span></div>
        <b>${s.progress}%</b>
      </div>
      <p style="font-size:12px;color:var(--muted)">${s.units} syllabus units · CS23304 Core Curriculum</p>
      <button class="btn ghost" data-edit-code="${esc(s.code)}">Edit subject</button>
    </div>
  `).join('');

  $$('[data-edit-code]').forEach(b => {
    b.onclick = () => {
      const s = subjects.find(x => x.code === b.dataset.editCode);
      if (!s) return;
      modal('Edit subject', `
        <div class="field"><label>Subject name</label><input name="name" required value="${esc(s.name)}" readonly></div>
        <div class="field"><label>Units</label><input name="units" type="number" min="1" max="20" value="${s.units}"></div>
        <div class="field"><label>Progress %</label><input name="progress" type="number" min="0" max="100" value="${s.progress}"></div>
      `, async (d) => {
        const res = await API.updateSubject(s.code, { units: d.units, progress: d.progress });
        subjects = res.subjects;
        renderSyllabus();
        toast('Subject progress saved to SQL database');
      });
    };
  });

  $('#adminSubjectCount').textContent = subjects.length;
}

$('#addSubject').onclick = () => modal('Add a curriculum subject', `
  <div class="field"><label>Subject name</label><input name="name" required placeholder="e.g. Operating Systems"></div>
  <div class="field"><label>Subject code</label><input name="code" required placeholder="e.g. CS23305"></div>
  <div class="field"><label>Number of units</label><input name="units" type="number" min="1" value="5"></div>
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

$('#addMark').onclick = () => modal('Add an assessment mark', `
  <div class="field"><label>Subject</label><select name="subject">
    ${subjects.map(s => `<option>${esc(s.name)}</option>`).join('')}
  </select></div>
  <div class="field"><label>Assessment name</label><input name="assessment" required placeholder="Assignment / test / lab"></div>
  <div class="field"><label>Score</label><input name="score" type="number" min="0" step="0.5" required value="0"></div>
  <div class="field"><label>Out of</label><input name="outof" type="number" min="1" step="0.5" required value="20"></div>
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
    $('#journalSaveState').textContent = 'Saved to the database! Persisted across sessions. ♡';
    toast('Journal entry saved to SQL database');
  } catch (err) {
    toast('Error: ' + err.message);
  }
};

// 9. Student Profile Screen
async function renderProfile() {
  if (studentProfile.name) {
    $('#profileName').value = studentProfile.name || 'Student';
    $('#profileRoll').value = studentProfile.rollNumber || 'DEMO-ROLL-001';
    $('#profileDept').value = studentProfile.department || 'Computer Technology';
    $('#profileProgramme').value = studentProfile.programme || '';
    $('#profileSemester').value = studentProfile.semester || '4';
    $('.profile-dot').textContent = (studentProfile.name || 'S')[0].toUpperCase();

    if (studentProfile.avatarUrl) {
      $('#avatarPreview').innerHTML = `<img src="${studentProfile.avatarUrl}" alt="Profile avatar">`;
    }
    if (studentProfile.doodleApplied) {
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
    name: $('#profileName').value.trim() || 'Student',
    rollNumber: $('#profileRoll').value.trim() || 'DEMO-ROLL-001',
    department: $('#profileDept').value,
    programme: $('#profileProgramme').value.trim(),
    semester: $('#profileSemester').value,
    avatarUrl: photoUrl || studentProfile.avatarUrl || '',
    doodleApplied: doodleOn
  };

  try {
    const updated = await API.updateProfile(payload);
    studentProfile = updated;
    $('.profile-dot').textContent = updated.name[0].toUpperCase();
    toast('Profile details updated in SQL database');
  } catch (err) {
    toast('Error: ' + err.message);
  }
};

// 10. Admin Metrics Screen
async function renderAdmin() {
  try {
    const stats = await API.getAdminStats();
    $('#adminCalendarCount').textContent = stats.calendarItems || 0;
    $('#adminEventCount').textContent = stats.events || 0;
    $('#adminSubjectCount').textContent = stats.subjects || 0;
  } catch (err) {
    console.warn('Admin stats error:', err);
  }
}

// 11. Login Flow Demo
$('#studentTab').onclick = () => setLogin('student');
$('#adminTab').onclick = () => setLogin('admin');

function setLogin(m) {
  loginMode = m;
  $('#studentTab').classList.toggle('ghost', m !== 'student');
  $('#adminTab').classList.toggle('ghost', m !== 'admin');
  $('#loginHeading').textContent = m === 'student' ? 'Welcome back ♡' : 'Admin demo access';
  $('#loginDesc').textContent = m === 'student' ? 'Use sample details to preview the student journey.' : 'Demo admin desk connected to live Java backend.';
  $('#loginIdLabel').textContent = m === 'student' ? 'Roll number' : 'Admin ID';
  $('#loginId').value = m === 'student' ? (studentProfile.rollNumber || 'DEMO-ROLL-001') : 'admin.mit';
  $('#loginPassword').value = 'cs23304';
}

$('#loginSubmit').onclick = () => {
  toast('Signed in successfully');
  go(loginMode === 'admin' ? 'admin' : 'dashboard');
};

// 12. Global Search Handler
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

// Initial Bootstrap on Page Load
async function initApp() {
  try {
    // 1. Fetch initial dataset from Java REST API
    const [overviewData, subjectsData, calendarData, eventsData, marksData, journalData, profileData] = await Promise.all([
      API.getOverview().catch(() => ({})),
      API.getSubjects().catch(() => []),
      API.getCalendar().catch(() => []),
      API.getEvents().catch(() => []),
      API.getMarks().catch(() => []),
      API.getJournal().catch(() => []),
      API.getProfile().catch(() => ({}))
    ]);

    subjects = subjectsData;
    calendarItems = calendarData;
    events = eventsData;
    marks = marksData;
    journalEntries = journalData;
    studentProfile = profileData;

    // Render initial views
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

    console.log('✨ MIT Academic Clock connected to Java Backend & H2 SQL Database.');
  } catch (err) {
    console.error('Initialization error:', err);
  }
}

initApp();
