const express = require('express');
const path = require('path');
const apiHandler = require('./api/index.js');

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Forward all /api requests to apiHandler
app.use('/api', (req, res) => {
  apiHandler(req, res);
});

// Serve static frontend files from /public
app.use(express.static(path.join(__dirname, 'public')));

// Catch-all fallback to index.html for single-page app routes
app.use((req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

const PORT = process.env.PORT || 3000;
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`ClockIt server running at http://localhost:${PORT}`);
  });
}

module.exports = app;
