window.MobileAuth = (() => {
  async function login(loginId, password) {
    const response = await window.MobileApi.post(
      "/auth/mobile/login",
      { loginId, password },
      { auth: false }
    );
    window.MobileApi.saveSession(response, {
      loginId,
      nickname: loginId
    });
  }

  async function signUp(loginId, password, nickname) {
    return window.MobileApi.post(
      "/members",
      { loginId, password, nickname },
      { auth: false }
    );
  }

  async function logout() {
    const refreshToken = window.MobileApi.getRefreshToken();
    window.MobileApi.clearSession();
    if (!refreshToken) {
      return;
    }

    try {
      await window.MobileApi.post(
        "/auth/mobile/logout",
        { refreshToken },
        { auth: false, skipRefresh: true }
      );
    } catch (error) {
      // 서버 로그아웃 실패 여부와 무관하게 클라이언트 세션은 이미 정리한다.
    }
  }

  return {
    login,
    signUp,
    logout
  };
})();
