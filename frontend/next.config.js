/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Development-time proxy to avoid CORS while calling local backend
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:9090/api/:path*' // Proxy to backend
      }
    ]
  }
}

module.exports = nextConfig
