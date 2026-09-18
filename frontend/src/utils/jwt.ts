export function decodeJwtRole(token: string): string | null {
  try {
    const payload = token.split(".")[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return decoded.role ?? null;
  } catch {
    return null;
  }
}