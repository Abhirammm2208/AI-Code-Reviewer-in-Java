'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';

export default function OAuth2CallbackPage() {
  const router = useRouter();
  const { login } = useAuth();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get token from URL parameters (if backend redirects with token)
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        
        if (token) {
          // Verify token with backend to get user info
          const response = await axios.get('/api/auth/me', {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });

          // Update auth context with token and user info
          login({ token, user: response.data });
          router.push('/');
        } else {
          // If no token in URL, something went wrong
          router.push('/login?error=oauth_failed');
        }
      } catch (error) {
        console.error('OAuth callback error:', error);
        router.push('/login?error=oauth_failed');
      }
    };

    handleCallback();
  }, [router]);

  return (
    <div className="loading-container">
      <div className="loading-spinner">
        <div className="spinner large"></div>
        <p>Completing sign in...</p>
      </div>
    </div>
  );
}
