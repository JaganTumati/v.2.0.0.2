/**
 * Resolves the backend's base URL.
 *
 * In GitHub Codespaces, the frontend is loaded from a forwarded URL like
 * https://<name>-5173.app.github.dev - but the backend forwards separately as
 * https://<name>-8080.app.github.dev. "localhost:8080" would NOT work here:
 * inside browser JS, "localhost" always means the viewer's own machine, not
 * the Codespace, once you're on the forwarded public URL. Since the forwarded
 * subdomain is random per Codespace session, it can't be hardcoded either -
 * so this detects the pattern at runtime and swaps the port segment.
 */
function resolveApiBaseUrl(): string {
  const host = window.location.hostname;
  const codespacesMatch = host.match(/^(.*)-\d+\.(app\.github\.dev|githubpreview\.dev)$/);
  if (codespacesMatch) {
    const [, namePrefix, domain] = codespacesMatch;
    return `${window.location.protocol}//${namePrefix}-8080.${domain}`;
  }
  return import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
}

const API_BASE_URL = resolveApiBaseUrl();

const ACCESS_TOKEN_KEY = 'orderrail_access_token';
const REFRESH_TOKEN_KEY = 'orderrail_refresh_token';

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getAccessToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

  if (response.status === 401) {
    clearTokens();
    window.location.assign('/login');
    throw new ApiError(401, 'Session expired');
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message ?? message;
    } catch {
      // response had no JSON body - keep the generic message
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};
