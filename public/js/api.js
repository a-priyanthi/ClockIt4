/**
 * ClockIt - REST API Client
 * Connects frontend interactions directly to the Java Backend and H2 SQL Database.
 */
const API = {
  baseUrl: '/api',

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    try {
      const response = await fetch(url, { ...options, headers });
      if (!response.ok) {
        let errMessage = 'Request failed (' + response.status + ')';
        try {
          const errData = await response.json();
          if (errData.error) errMessage = errData.error;
        } catch (_) {}
        throw new Error(errMessage);
      }
      return await response.json();
    } catch (err) {
      console.error(`[API Error] ${options.method || 'GET'} ${url}:`, err.message);
      throw err;
    }
  },

  // Authentication & Registration
  registerOrSignIn(studentData) {
    return this.request('/auth/signin', {
      method: 'POST',
      body: JSON.stringify(studentData)
    });
  },

  login(credentials) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  },

  getAuthMe() {
    return this.request('/auth/me');
  },

  // Overview
  getOverview() {
    return this.request('/overview');
  },

  // Subjects & Attendance
  getSubjects() {
    return this.request('/subjects');
  },

  addSubject(data) {
    return this.request('/subjects', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateSubject(code, data) {
    return this.request(`/subjects/${encodeURIComponent(code)}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  recordAttendance(subject, status) {
    return this.request('/attendance', {
      method: 'POST',
      body: JSON.stringify({ subject, status })
    });
  },

  // Calendar Items
  getCalendar(date = null) {
    const q = date ? `?date=${encodeURIComponent(date)}` : '';
    return this.request(`/calendar${q}`);
  },

  addCalendarItem(data) {
    return this.request('/calendar', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Campus Events
  getEvents() {
    return this.request('/events');
  },

  addEvent(data) {
    return this.request('/events', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  toggleEventRegistration(eventId) {
    return this.request(`/events/${encodeURIComponent(eventId)}/register`, {
      method: 'POST'
    });
  },

  // Assessment Marks
  getMarks() {
    return this.request('/marks');
  },

  addMark(data) {
    return this.request('/marks', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  deleteMark(id) {
    return this.request(`/marks/${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  },

  // Scrapbook Journal
  getJournal() {
    return this.request('/journal');
  },

  addJournal(data) {
    return this.request('/journal', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Student Profile
  getProfile() {
    return this.request('/profile');
  },

  updateProfile(data) {
    return this.request('/profile', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Admin Metrics
  getAdminStats() {
    return this.request('/admin/stats');
  },

  // Global Search
  search(query) {
    return this.request('/search', {
      method: 'POST',
      body: JSON.stringify({ query })
    });
  }
};
