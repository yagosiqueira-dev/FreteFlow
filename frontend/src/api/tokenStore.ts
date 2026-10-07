const TOKEN_STORAGE_KEY = "freteflow_token";
export const AUTH_INVALIDATED_EVENT = "freteflow:auth-invalidated";

let currentToken: string | null = null;

export function getTokenExpiration(token: string): number | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "=")));
    return typeof decoded.exp === "number" ? decoded.exp * 1000 : null;
  } catch {
    return null;
  }
}

function clearStoredToken(): void {
  currentToken = null;
  try {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
  } catch {
    // Keep the in-memory session usable when browser storage is unavailable.
  }
}

export function invalidateToken(): void {
  clearStoredToken();
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_INVALIDATED_EVENT));
  }
}

export function getToken(): string | null {
  if (typeof window !== "undefined" && currentToken === null) {
    try {
      currentToken = window.localStorage.getItem(TOKEN_STORAGE_KEY);
    } catch {
      return null;
    }
  }

  if (currentToken) {
    const expiration = getTokenExpiration(currentToken);
    if (expiration === null || expiration <= Date.now()) {
      invalidateToken();
      return null;
    }
  }

  return currentToken;
}

export function setToken(token: string | null): void {
  currentToken = token;
  try {
    if (token) {
      window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
    } else {
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  } catch {
    // Keep the in-memory session usable when browser storage is unavailable.
  }
}
