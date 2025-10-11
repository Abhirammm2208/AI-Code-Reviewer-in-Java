# Frontend - AI Code Reviewer (Next.js)

This folder contains a Next.js frontend that interacts with the backend running at `http://localhost:9090`.

## Quick start

1. Install dependencies

```bash
cd frontend
npm install
```

2. Run the development server

```bash
npm run dev
```

3. Open http://localhost:3000 in your browser

## Notes

- The frontend uses a development-time proxy so calls to `/api/*` are forwarded to `http://localhost:9090/api/*` (see `next.config.js`).
- Ensure the backend is running on port 9090 and start the frontend with `npm run dev` from the `frontend/` folder.
- Feel free to improve the UI. The current UI stores review history in `localStorage`.
