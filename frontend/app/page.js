"use client"
import { useState, useEffect } from 'react'
import axios from 'axios'

function IconRocket() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2c.552 0 1 .448 1 1v3c0 .552-.448 1-1 1s-1-.448-1-1V3c0-.552.448-1 1-1z" fill="#fff" opacity=".9"/>
      <path d="M5 20c-1 0-3 1-3 1s1.2-2.1 1-3c-.2-.9 1.5-3 3-3 1.5 0 3 1.5 3 3 0 1.5-2 3-4 2z" fill="#7c3aed" opacity=".95"/>
    </svg>
  )
}

export default function Home() {
  const [language, setLanguage] = useState('javascript')
  const [author, setAuthor] = useState('')
  const [code, setCode] = useState(`// Paste your code here for intelligent analysis\nfunction fibonacci(n) {\n  if (n <= 1) return n;\n  return fibonacci(n - 1) + fibonacci(n - 2);\n}`)
  const [loading, setLoading] = useState(false)
  const [review, setReview] = useState(null)
  const [history, setHistory] = useState([])
  const [stats, setStats] = useState({ reviewsToday: 0, linesAnalyzed: 0, issuesFound: 0 })
  const [error, setError] = useState(null)
  const [theme, setTheme] = useState('light')

  useEffect(() => {
    const saved = localStorage.getItem('reviews')
    if (saved) setHistory(JSON.parse(saved))
    // load theme preference
    try {
      const t = localStorage.getItem('theme') || (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
      setTheme(t)
      if (t === 'dark') document.documentElement.classList.add('dark')
      else document.documentElement.classList.remove('dark')
    } catch (e) {
      // ignore
    }
  }, [])

  useEffect(() => {
    localStorage.setItem('reviews', JSON.stringify(history))
  }, [history])

  const resultsRef = null
  let resultsEl = null

  async function submit() {
    setLoading(true)
    setError(null)
    setReview(null)
    try {
      const resp = await axios.post('/api/reviews', { code, language, author })
      // expected resp.data.review or entire response
      const payload = resp.data || { review: 'No response body' }
      setReview(payload)
      const summary = (payload.review && payload.review.slice ? payload.review.slice(0, 240) : JSON.stringify(payload))
      setHistory(prev => [{ id: Date.now(), code: code.slice(0, 240), language, review: summary, author }, ...prev])
      setStats(s => ({ ...s, reviewsToday: s.reviewsToday + 1, linesAnalyzed: s.linesAnalyzed + code.split('\n').length }))
      // auto-scroll to results after a short delay to allow render
      setTimeout(() => {
        const el = document.querySelector('.results-card')
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }, 200)
    } catch (e) {
      console.error('submit', e)
      setError(e.message || 'Network Error')
    } finally {
      setLoading(false)
    }
  }

  function clearHistory() {
    setHistory([])
    localStorage.removeItem('reviews')
  }

  function toggleTheme() {
    const next = theme === 'light' ? 'dark' : 'light'
    setTheme(next)
    try {
      localStorage.setItem('theme', next)
    } catch (e) {}
    if (next === 'dark') document.documentElement.classList.add('dark')
    else document.documentElement.classList.remove('dark')
  }

  return (
    <div className="dashboard">
      <header className="hero">
        <div className="hero-inner">
          <div>
            <h1>Elevate Your Code Quality</h1>
            <p className="subtitle">Get instant, intelligent feedback on your code with our advanced AI reviewer. Improve performance, security, and maintainability with every analysis.</p>
          </div>
          <div className="hero-status">Ready to analyze</div>
        </div>
      </header>

      {/* fixed theme toggle in the top-right corner */}
      <button className="theme-toggle fixed" onClick={toggleTheme} aria-label="Toggle theme">{theme === 'light' ? '🌙 Dark' : '☀️ Light'}</button>

      <div className="grid three-col">
        <div className="left-col">
          <div className="card config-card">
            <div className="card-row">
              <div className="config-item">
                <label className="label-dot">Author</label>
                <input value={author} onChange={e => setAuthor(e.target.value)} placeholder="Your name" />
              </div>
              <div className="config-item">
                <label className="label-dot">Language</label>
                <select value={language} onChange={e => setLanguage(e.target.value)}>
                  <option>javascript</option>
                  <option>typescript</option>
                  <option>java</option>
                  <option>kotlin</option>
                  <option>python</option>
                  <option>go</option>
                  <option>ruby</option>
                  <option>php</option>
                  <option>c</option>
                  <option>c++</option>
                  <option>c#</option>
                  <option>rust</option>
                  <option>swift</option>
                  <option>objective-c</option>
                  <option>scala</option>
                  <option>groovy</option>
                  <option>perl</option>
                  <option>r</option>
                  <option>matlab</option>
                  <option>dart</option>
                  <option>elixir</option>
                  <option>lua</option>
                  <option>haskell</option>
                  <option>clojure</option>
                  <option>bash</option>
                  <option>powershell</option>
                  <option>sql</option>
                  <option>yaml</option>
                  <option>json</option>
                </select>
              </div>
            </div>
          </div>

          <div className="card stats-card left-stats">
            <h4>Session Stats</h4>
            <div className="stats-grid">
              <div className="stat-box blue">
                <div className="stat-num">{stats.reviewsToday}</div>
                <div className="stat-label">Reviews Today</div>
              </div>
              <div className="stat-box green">
                <div className="stat-num">{stats.linesAnalyzed}</div>
                <div className="stat-label">Lines Analyzed</div>
              </div>
              <div className="stat-box orange">
                <div className="stat-num">{stats.issuesFound}</div>
                <div className="stat-label">Issues Found</div>
              </div>
            </div>
          </div>
        </div>

        <div className="center-col">
          <div className="card code-card">
            <div className="card-header">
              <h3>Code Editor</h3>
              <div className="char-count">{code.length} characters</div>
            </div>
            <textarea className="editor" value={code} onChange={e => setCode(e.target.value)} />
            <div className="card-actions">
              <button className="primary" onClick={submit} disabled={loading}>{loading ? 'Analyzing...' : <><IconRocket/> Analyze Code</>}</button>
              <button className="ghost" onClick={() => setCode('')}>Clear</button>
            </div>
            <div className="status-pill">
              <div className="left">● Ready to analyze</div>
              <div className="right">AI-powered code review</div>
            </div>
          </div>

          <div className="card results-card">
            <h3>Analysis Results</h3>
            {!review && <div className="placeholder">Ready for Analysis<br/><small>Paste your code above and click "Analyze Code" to get detailed insights</small></div>}
            {review && (
              <div className="review-output">
                <pre>{review.review || JSON.stringify(review, null, 2)}</pre>
              </div>
            )}
          </div>
        </div>

        <div className="right-col">
          <div className="card history-card">
            <div className="history-header">
              <h4>Review History</h4>
              <button className="link" onClick={clearHistory}>Clear</button>
            </div>
            <div className="history-list">
              {history.length === 0 && <div className="empty">No reviews yet</div>}
              {history.map(h => (
                <div key={h.id} className="history-item">
                    <div className="meta">{h.language} • {h.author ? h.author + ' • ' : ''}{new Date(h.id).toLocaleString()}</div>
                  <div className="snippet">{h.code}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {error && <div className="toast">Error: {error}</div>}
    </div>
  )
}