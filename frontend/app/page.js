"use client"
import { useState, useEffect } from 'react'
import axios from 'axios'
import ProtectedRoute from './components/ProtectedRoute'
import UserMenu from './components/UserMenu'

function IconRocket() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2c.552 0 1 .448 1 1v3c0 .552-.448 1-1 1s-1-.448-1-1V3c0-.552.448-1 1-1z" fill="#fff" opacity=".9"/>
      <path d="M5 20c-1 0-3 1-3 1s1.2-2.1 1-3c-.2-.9 1.5-3 3-3 1.5 0 3 1.5 3 3 0 1.5-2 3-4 2z" fill="#7c3aed" opacity=".95"/>
    </svg>
  )
}

// Format AI response to look like ChatGPT/Gemini
function formatAIResponse(response) {
  if (!response) return null
  
  // Try to parse as JSON if it's a string
  let data = response
  if (typeof response === 'string') {
    try {
      data = JSON.parse(response)
    } catch (e) {
      // If not JSON, treat as plain text
      return <div className="formatted-content"><p>{response}</p></div>
    }
  }
  
  // If we have structured data from backend
  if (data.summary || data.comments) {
    return (
      <div className="formatted-content">
        {/* Summary Section */}
        {(data.summary || data.comments) && (
          <div className="response-section">
            <h4 className="section-title">🔍 Analysis Summary</h4>
            <p className="section-content">{data.summary || data.comments}</p>
          </div>
        )}
        
        {/* Score if available */}
        {data.score !== undefined && (
          <div className="score-badge">
            <span className="score-label">Code Quality Score:</span>
            <span className="score-value">{data.score}/100</span>
          </div>
        )}
        
        {/* Issues Section */}
        {data.issues && data.issues.length > 0 && (
          <div className="response-section">
            <h4 className="section-title">⚠️ Main Issues</h4>
            <div className="issue-list">
              {data.issues.map((issue, idx) => (
                <div key={`issue-${idx}`} className="ai-bullet">
                  <span className="bullet-point error">•</span>
                  <span>{issue}</span>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {/* Suggestions Section */}
        {data.suggestions && data.suggestions.length > 0 && (
          <div className="response-section">
            <h4 className="section-title">💡 Suggestions for Improvement</h4>
            <div className="suggestion-list">
              {data.suggestions.map((suggestion, idx) => (
                <div key={`suggestion-${idx}`} className="ai-bullet">
                  <span className="bullet-point suggest">•</span>
                  <span>{suggestion}</span>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {/* Best Practices Section */}
        {data.bestPractices && data.bestPractices.length > 0 && (
          <div className="response-section">
            <h4 className="section-title">✅ Best Practices</h4>
            <div className="practice-list">
              {data.bestPractices.map((practice, idx) => (
                <div key={`practice-${idx}`} className="ai-bullet">
                  <span className="bullet-point success">•</span>
                  <span>{practice}</span>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {/* Fixed Code Section */}
        {data.fixCode && (
          <div className="response-section">
            <h4 className="section-title">🧠 Improved Version of the Code</h4>
            <pre className="code-block">
              <code>{data.fixCode}</code>
            </pre>
          </div>
        )}
      </div>
    )
  }
  
  // Fallback for plain text
  return <div className="formatted-content"><p>{JSON.stringify(data, null, 2)}</p></div>
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
    <ProtectedRoute>
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

        {/* Theme toggle and user menu in the top-left corner */}
        <div style={{ position: 'fixed', top: '20px', left: '20px', display: 'flex', gap: '12px', zIndex: 1000 }}>
          <button className="theme-toggle" onClick={toggleTheme} aria-label="Toggle theme">{theme === 'light' ? '🌙 Dark' : '☀️ Light'}</button>
          <UserMenu />
        </div>

      <div className="grid three-col">
        <div className="left-col">
          <div className="card config-card">
            {/* Empty card for future use */}
            <div className="card-placeholder">
              <p className="placeholder-text">Configuration options will appear here</p>
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
              <div className="header-controls">
                <div className="language-selector">
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
                <div className="char-count">{code.length} characters</div>
              </div>
            </div>
            <textarea className="editor" value={code} onChange={e => setCode(e.target.value)} />
            <div className="card-actions">
              <button className="primary" onClick={submit} disabled={loading}>
                {loading ? 'Analyzing...' : (
                  <>
                    <span>Analyze Code</span>
                  </>
                )}
              </button>
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
              <div className="review-output ai-response">
                <div className="ai-message">
                  {formatAIResponse(review.review || JSON.stringify(review, null, 2))}
                </div>
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
    </ProtectedRoute>
  )
}