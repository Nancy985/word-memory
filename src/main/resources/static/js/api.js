const API_BASE = '';

const api = {
  getToken() {
    return localStorage.getItem('token');
  },

  setToken(token) {
    if (token) localStorage.setItem('token', token);
    else localStorage.removeItem('token');
  },

  getUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  },

  setUser(user) {
    if (user) localStorage.setItem('user', JSON.stringify(user));
    else localStorage.removeItem('user');
  },

  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    const token = this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
    const json = await res.json();

    if (json.code === 401 && !path.includes('/auth/login') && !path.includes('/auth/register')) {
      this.setToken(null);
      this.setUser(null);
      location.hash = '#/login';
      throw new Error(json.message || '请重新登录');
    }

    if (json.code !== 200) {
      const err = new Error(json.message || '请求失败');
      err.code = json.code;
      throw err;
    }
    return json.data;
  },

  login(username, password) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },

  register(username, password, nickname) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, nickname }),
    });
  },

  logout() {
    return this.request('/api/auth/logout', { method: 'POST' }).catch(() => {});
  },

  getMe() {
    return this.request('/api/auth/me');
  },

  getWordBooks() {
    return this.request('/api/wordbooks');
  },

  createWordBook(data) {
    return this.request('/api/wordbooks', { method: 'POST', body: JSON.stringify(data) });
  },

  updateWordBook(id, data) {
    return this.request(`/api/wordbooks/${id}`, { method: 'PUT', body: JSON.stringify(data) });
  },

  deleteWordBook(id) {
    return this.request(`/api/wordbooks/${id}`, { method: 'DELETE' });
  },

  getWords(bookId) {
    return this.request(`/api/wordbooks/${bookId}/words`);
  },

  addWord(bookId, data) {
    return this.request(`/api/wordbooks/${bookId}/words`, { method: 'POST', body: JSON.stringify(data) });
  },

  deleteWord(bookId, wordId) {
    return this.request(`/api/wordbooks/${bookId}/words/${wordId}`, { method: 'DELETE' });
  },

  getTodayWords(bookId, limit = 20) {
    return this.request(`/api/memorize/today?wordBookId=${bookId}&limit=${limit}`);
  },

  submitReview(wordId, correct) {
    return this.request('/api/memorize/review', {
      method: 'POST',
      body: JSON.stringify({ wordId, correct }),
    });
  },

  getProgress(bookId) {
    return this.request(`/api/memorize/progress?wordBookId=${bookId}`);
  },

  generateQuiz(bookId, count) {
    return this.request('/api/quiz/generate', {
      method: 'POST',
      body: JSON.stringify({ wordBookId: bookId, count }),
    });
  },

  submitQuiz(bookId, answers, durationSeconds) {
    return this.request('/api/quiz/submit', {
      method: 'POST',
      body: JSON.stringify({ wordBookId: bookId, answers, durationSeconds }),
    });
  },

  getWrongWords(bookId) {
    const q = bookId ? `?wordBookId=${bookId}` : '';
    return this.request(`/api/wrong-words${q}`);
  },

  getWrongStats() {
    return this.request('/api/wrong-words/stats');
  },

  importWords(bookId, csvContent) {
    return this.request(`/api/wordbooks/${bookId}/words/import`, {
      method: 'POST',
      body: JSON.stringify({ csvContent }),
    });
  },

  getEbbinghausCurve(bookId) {
    return this.request(`/api/memorize/curve?wordBookId=${bookId}`);
  },

  getExams() {
    return this.request('/api/exams');
  },

  getExam(id) {
    return this.request(`/api/exams/${id}`);
  },
};
