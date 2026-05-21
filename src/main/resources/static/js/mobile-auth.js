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

  function logout() {
    window.MobileApi.clearSession();
  }

  return {
    login,
    signUp,
    logout
  };
})();
