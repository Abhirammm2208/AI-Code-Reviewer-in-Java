import './globals.css'
import { AuthProvider } from './context/AuthContext'

export const metadata = {
  title: 'AI Code Reviewer',
  description: 'Interactive UI for AI Code Reviewer backend'
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <main className="container">{children}</main>
        </AuthProvider>
      </body>
    </html>
  )
}