window.MobileApi = (() => {
  const ACCESS_TOKEN_KEY = "roomescape.mobile.accessToken";
  const REFRESH_TOKEN_KEY = "roomescape.mobile.refreshToken";
  const LEGACY_TOKEN_KEY = "roomescape.mobile.token";
  const USER_KEY = "roomescape.mobile.user";
  let refreshPromise = null;

  function hasTokenValue(value) {
    return typeof value === "string" && value.trim() && value !== "undefined" && value !== "null";
  }

  function getAccessToken() {
    const token = window.localStorage.getItem(ACCESS_TOKEN_KEY);
    return hasTokenValue(token) ? token : null;
  }

  function getRefreshToken() {
    const token = window.localStorage.getItem(REFRESH_TOKEN_KEY);
    return hasTokenValue(token) ? token : null;
  }

  function getToken() {
    return getAccessToken();
  }

  function getUser() {
    try {
      const raw = window.localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  function saveTokens(tokens) {
    if (!tokens || !hasTokenValue(tokens.accessToken)) {
      throw new Error("토큰 발급 응답이 올바르지 않습니다.");
    }

    window.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    if (hasTokenValue(tokens.refreshToken)) {
      window.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    }
    window.localStorage.removeItem(LEGACY_TOKEN_KEY);
  }

  function saveSession(tokens, user) {
    saveTokens(tokens);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function clearSession() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(LEGACY_TOKEN_KEY);
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

  async function requestOnce(path, options = {}) {
    const token = getAccessToken();
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

  function shouldRefresh(error, options) {
    if (options.auth === false || options.skipRefresh) {
      return false;
    }
    if (!getRefreshToken()) {
      return false;
    }
    return error instanceof Error && (
      error.status === 401 ||
      error.code === "AUTHORIZATION_ERROR" ||
      error.code === "TOKEN_NOT_FOUND" ||
      error.code === "INVALID_TOKEN" ||
      error.code === "EXPIRED_TOKEN"
    );
  }

  async function refreshAccessToken() {
    if (!refreshPromise) {
      refreshPromise = requestOnce("/auth/mobile/refresh", {
        method: "POST",
        auth: false,
        body: { refreshToken: getRefreshToken() }
      })
        .then((response) => {
          saveTokens(response);
          return response.accessToken;
        })
        .catch((error) => {
          clearSession();
          throw error;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }
    return refreshPromise;
  }

  async function ensureAccessToken() {
    if (getAccessToken()) {
      return getAccessToken();
    }
    if (!getRefreshToken()) {
      return null;
    }
    return refreshAccessToken();
  }

  async function request(path, options = {}) {
    try {
      return await requestOnce(path, options);
    } catch (error) {
      if (!shouldRefresh(error, options)) {
        throw error;
      }
      await refreshAccessToken();
      return requestOnce(path, { ...options, skipRefresh: true });
    }
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
    getAccessToken,
    getRefreshToken,
    getToken,
    getUser,
    ensureAccessToken,
    saveSession,
    clearSession,
    isAuthError,
    get: (path, options = {}) => request(path, { ...options, method: "GET" }),
    post: (path, body, options = {}) => request(path, { ...options, method: "POST", body }),
    patch: (path, body, options = {}) => request(path, { ...options, method: "PATCH", body }),
    delete: (path, options = {}) => request(path, { ...options, method: "DELETE" })
  };
})();
