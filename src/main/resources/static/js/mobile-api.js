window.MobileApi = (() => {
  const TOKEN_KEY = "roomescape.mobile.token";
  const USER_KEY = "roomescape.mobile.user";

  function getToken() {
    return window.localStorage.getItem(TOKEN_KEY);
  }

  function getUser() {
    try {
      const raw = window.localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  function saveSession(token, user) {
    window.localStorage.setItem(TOKEN_KEY, token);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function clearSession() {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
  }

  function messageFromErrorBody(body, fallback) {
    if (Array.isArray(body.messages) && body.messages.length > 0) {
      return body.messages.filter(Boolean).join("\n");
    }
    if (typeof body.message === "string" && body.message.trim()) {
      return body.message;
    }
    return fallback;
  }

  async function parseError(response) {
    const fallback = `HTTP ${response.status}`;
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
      const error = new Error(fallback);
      error.status = response.status;
      return error;
    }

    try {
      const body = await response.json();
      const error = new Error(messageFromErrorBody(body, fallback));
      error.code = body.code;
      error.status = response.status;
      return error;
    } catch (error) {
      const parsedError = new Error(fallback);
      parsedError.status = response.status;
      return parsedError;
    }
  }

  async function request(path, options = {}) {
    const token = getToken();
    const headers = {
      Accept: "application/json",
      ...options.headers
    };

    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
    }
    if (options.auth !== false && token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(path, {
      method: options.method || "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    if (!response.ok) {
      throw await parseError(response);
    }
    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
      return null;
    }
    return response.json();
  }

  function isAuthError(error) {
    return error instanceof Error && (
      error.status === 401 ||
      error.code === "AUTHORIZATION_ERROR" ||
      error.code === "TOKEN_NOT_FOUND" ||
      error.code === "INVALID_TOKEN" ||
      error.code === "EXPIRED_TOKEN"
    );
  }

  return {
    getToken,
    getUser,
    saveSession,
    clearSession,
    isAuthError,
    get: (path, options = {}) => request(path, { ...options, method: "GET" }),
    post: (path, body, options = {}) => request(path, { ...options, method: "POST", body }),
    patch: (path, body, options = {}) => request(path, { ...options, method: "PATCH", body }),
    delete: (path, options = {}) => request(path, { ...options, method: "DELETE" })
  };
})();
