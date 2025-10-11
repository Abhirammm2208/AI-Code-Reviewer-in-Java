import './globals.css'

export const metadata = {
  title: 'AI Code Reviewer',
  description: 'Interactive UI for AI Code Reviewer backend'
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <main className="container">{children}</main>
      </body>
    </html>
  )
}