import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../lib/api';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Something went wrong signing in.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-charcoal px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <h1 className="font-display text-2xl font-semibold text-paper">Order Rail</h1>
          <p className="mt-1 text-sm text-paper/50">Restaurant operations dashboard</p>
        </div>

        <form onSubmit={handleSubmit} className="rounded-md bg-paper p-6 shadow-xl">
          <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate">
            Email
          </label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mb-4 w-full rounded-sm border border-paper-dim bg-white px-3 py-2 text-sm text-ink outline-none focus:border-teal"
            placeholder="owner@spiceroute.test"
          />

          <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate">
            Password
          </label>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mb-5 w-full rounded-sm border border-paper-dim bg-white px-3 py-2 text-sm text-ink outline-none focus:border-teal"
          />

          {error && (
            <p className="mb-4 rounded-sm border border-brick/30 bg-brick-soft px-3 py-2 text-sm text-brick">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-sm bg-marigold px-4 py-2 text-sm font-semibold text-charcoal transition-opacity hover:opacity-90 disabled:opacity-50"
          >
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>

          <p className="mt-4 text-center text-xs text-slate">
            First run? Check the backend startup logs for the seeded owner password.
          </p>
        </form>
      </div>
    </div>
  );
}
