const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = 8080;

// Proxy API requests to backend
app.use('/api', createProxyMiddleware({
  target: 'http://localhost:20108',
  changeOrigin: true,
  logLevel: 'debug'
}));

// Serve static files from dist directory
app.use(express.static(path.join(__dirname, 'dist')));

// SPA fallback for tenant
app.get('/tenant/*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'tenant', 'index.html'));
});

// SPA fallback for admin root
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
  console.log(`Admin: http://localhost:${PORT}`);
  console.log(`Tenant: http://localhost:${PORT}/tenant/`);
  console.log(`API proxy: http://localhost:${PORT}/api/* -> http://localhost:20108/api/*`);
});
