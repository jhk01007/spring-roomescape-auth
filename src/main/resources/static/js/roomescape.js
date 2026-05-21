const API_BASE = "";
    const GUEST_NAME_HEADER = "X-Guest-Name";
    const DEMO_DATE = "2026-05-06";
    const DEFAULT_DATE = todayDate();
    const PAGE = document.body.dataset.page || "user";

    const state = {
      currentView: "user",
      mode: "demo",
      themes: [],
      popularThemes: [],
      times: [],
      reservations: [],
      lookupReservations: [],
      availableTimes: [],
      demoReservations: [
        { id: 1, guestName: "guest-8", date: "2026-05-05", themeId: 1, timeId: 1 },
        { id: 2, guestName: "guest-9", date: "2026-05-05", themeId: 1, timeId: 2 },
        { id: 3, guestName: "guest-10", date: "2026-05-05", themeId: 1, timeId: 3 },
        { id: 4, guestName: "guest-18", date: "2027-05-05", themeId: 2, timeId: 1 },
        { id: 5, guestName: "guest-19", date: "2027-05-05", themeId: 2, timeId: 2 },
        { id: 6, guestName: "guest-56", date: "2027-05-06", themeId: 11, timeId: 1 },
        { id: 7, guestName: "guest-57", date: "2027-05-06", themeId: 11, timeId: 2 }
      ],
      selectedThemeId: null,
      selectedTimeId: null,
      editingReservationId: null,
      editingReservationThemeId: null,
      editAvailableTimes: [],
      adminSelectedThemeId: null,
      adminSelectedTimeId: null,
      adminAvailableTimes: [],
      adminReservationPage: 1,
      adminReservationSize: 20,
      adminReservationHasNext: false,
      currentUser: null,
      managerInfo: null,
      managerInfoStatus: "idle",
      managerInfoMessage: ""
    };

    const demoThemes = Array.from({ length: 12 }, (_, index) => {
      const id = index + 1;
      return {
        id,
        name: `Theme ${id}`,
        description: id === 11 ? "Out of range reservations only" : id === 12 ? "No reservations" : `Popular theme rank ${id}`,
        thumbnail: ""
      };
    });

    const demoTimes = [
      { id: 1, startAt: "10:00", isAvailable: true },
      { id: 2, startAt: "12:00", isAvailable: true },
      { id: 3, startAt: "14:00", isAvailable: true },
      { id: 4, startAt: "16:00", isAvailable: true },
      { id: 5, startAt: "18:00", isAvailable: true },
      { id: 6, startAt: "20:00", isAvailable: true }
    ];

    const colors = [
      ["#0e6f70", "#192f35"],
      ["#b84f2f", "#33241f"],
      ["#2f5c9a", "#1b2738"],
      ["#746a35", "#252318"],
      ["#7b3f68", "#2e1e2a"],
      ["#2f7a50", "#182b22"]
    ];

    const $ = (selector) => document.querySelector(selector);

    function todayDate() {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, "0");
      const date = String(today.getDate()).padStart(2, "0");
      return `${year}-${month}-${date}`;
    }

    function isAdminPage() {
      return PAGE === "admin";
    }

    function isUserPage() {
      return PAGE === "user";
    }

    function isLoginPage() {
      return PAGE === "login";
    }

    function isSignupPage() {
      return PAGE === "signup";
    }

    function isAuthPage() {
      return isLoginPage() || isSignupPage();
    }

    function restoreCurrentUser() {
      try {
        const raw = window.sessionStorage.getItem("roomescape.currentUser");
        state.currentUser = raw ? JSON.parse(raw) : null;
      } catch (error) {
        state.currentUser = null;
      }
    }

    function rememberCurrentUser(user) {
      state.currentUser = user;
      window.sessionStorage.setItem("roomescape.currentUser", JSON.stringify(user));
    }

    const elements = {
      sourceStatus: $("#sourceStatus"),
      authActionButton: $("#authActionButton"),
      authStatus: $("#authStatus"),
      authMessage: $("#authMessage"),
      loginForm: $("#loginForm"),
      loginIdInput: $("#loginIdInput"),
      loginPasswordInput: $("#loginPasswordInput"),
      loginButton: $("#loginButton"),
      signupForm: $("#signupForm"),
      signupLoginIdInput: $("#signupLoginIdInput"),
      signupPasswordInput: $("#signupPasswordInput"),
      signupNicknameInput: $("#signupNicknameInput"),
      signupButton: $("#signupButton"),
      popularList: $("#popularList"),
      dateInput: $("#dateInput"),
      dateNote: $("#dateNote"),
      themeGrid: $("#themeGrid"),
      themeCount: $("#themeCount"),
      timeGrid: $("#timeGrid"),
      timeCount: $("#timeCount"),
      nameInput: $("#nameInput"),
      summaryUser: $("#summaryUser"),
      summaryDate: $("#summaryDate"),
      summaryTheme: $("#summaryTheme"),
      summaryTime: $("#summaryTime"),
      reserveButton: $("#reserveButton"),
      formMessage: $("#formMessage"),
      lookupForm: $("#lookupForm"),
      lookupGuestName: $("#lookupGuestName"),
      lookupLoginButton: $("#lookupLoginButton"),
      lookupMessage: $("#lookupMessage"),
      lookupList: $("#lookupList"),
      lookupCount: $("#lookupCount"),
      managerPanel: $("#managerPanel"),
      managerMessage: $("#managerMessage"),
      managerStoreCard: $("#managerStoreCard"),
      managerStoreName: $("#managerStoreName"),
      managerStoreMeta: $("#managerStoreMeta"),
      managerLoginButton: $("#managerLoginButton"),
      managerAdminButton: $("#managerAdminButton"),
      editReservationForm: $("#editReservationForm"),
      editReservationTitle: $("#editReservationTitle"),
      editReservationMeta: $("#editReservationMeta"),
      editAuthorizationName: $("#editAuthorizationName"),
      editReservationDate: $("#editReservationDate"),
      editReservationTime: $("#editReservationTime"),
      editReservationButton: $("#editReservationButton"),
      editReservationMessage: $("#editReservationMessage"),
      editCancelButton: $("#editCancelButton"),
      themeForm: $("#themeForm"),
      timeForm: $("#timeForm"),
      adminThemeName: $("#adminThemeName"),
      adminThemeDescription: $("#adminThemeDescription"),
      adminThemeThumbnail: $("#adminThemeThumbnail"),
      adminReservationForm: $("#adminReservationForm"),
      adminReserveName: $("#adminReserveName"),
      adminReserveDate: $("#adminReserveDate"),
      adminReserveTheme: $("#adminReserveTheme"),
      adminReserveTimeGrid: $("#adminReserveTimeGrid"),
      adminReserveSummary: $("#adminReserveSummary"),
      adminReserveButton: $("#adminReserveButton"),
      adminReserveMessage: $("#adminReserveMessage"),
      adminTimeStartAt: $("#adminTimeStartAt"),
      adminMessage: $("#adminMessage"),
      adminReservationList: $("#adminReservationList"),
      adminThemeList: $("#adminThemeList"),
      adminTimeList: $("#adminTimeList"),
      reservationCount: $("#reservationCount"),
      adminReservationPageSize: $("#adminReservationPageSize"),
      adminReservationPrevPage: $("#adminReservationPrevPage"),
      adminReservationNextPage: $("#adminReservationNextPage"),
      adminReservationPageLabel: $("#adminReservationPageLabel"),
      adminThemeCount: $("#adminThemeCount"),
      adminTimeCount: $("#adminTimeCount"),
      toast: $("#toast")
    };

    function posterFor(theme) {
      const [start, end] = colors[(theme.id - 1) % colors.length];
      const canvas = document.createElement("canvas");
      canvas.width = 520;
      canvas.height = 320;
      const context = canvas.getContext("2d");
      const gradient = context.createLinearGradient(0, 0, 520, 320);
      gradient.addColorStop(0, start);
      gradient.addColorStop(1, end);
      context.fillStyle = gradient;
      context.fillRect(0, 0, 520, 320);
      context.fillStyle = "rgba(255,255,255,0.14)";
      for (let i = 0; i < 8; i += 1) {
        context.fillRect(42 + i * 58, 52, 26, 216);
      }
      context.fillStyle = "rgba(0,0,0,0.22)";
      context.fillRect(0, 232, 520, 88);
      context.fillStyle = "#fffdf8";
      context.font = "800 42px system-ui, sans-serif";
      context.fillText(theme.name, 34, 286);
      return canvas.toDataURL("image/png");
    }

    function themeImageSource(theme) {
      const thumbnail = String(theme.thumbnail || "").trim();
      if (!thumbnail || thumbnail.includes("example.com/")) {
        return posterFor(theme);
      }
      return thumbnail;
    }

    async function getJson(path, headers = {}) {
      const controller = new AbortController();
      const timer = window.setTimeout(() => controller.abort(), 5000);
      const response = await fetch(`${API_BASE}${path}`, {
        headers: {
          Accept: "application/json",
          ...headers
        },
        signal: controller.signal
      }).finally(() => window.clearTimeout(timer));
      if (!response.ok) {
        throw await apiErrorFrom(response);
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

    async function postJson(path, body) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });
      if (!response.ok) {
        throw await apiErrorFrom(response);
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

    async function patchJson(path, body, headers = {}) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "PATCH",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          ...headers
        },
        body: JSON.stringify(body)
      });
      if (!response.ok) {
        throw await apiErrorFrom(response);
      }
      return response.json();
    }

    async function deleteJson(path, headers = {}) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "DELETE",
        headers: {
          Accept: "application/json",
          ...headers
        }
      });
      if (!response.ok) {
        throw await apiErrorFrom(response);
      }
    }

    function guestNameHeaders(guestName) {
      return {
        [GUEST_NAME_HEADER]: encodeURIComponent(guestName)
      };
    }

    async function apiErrorFrom(response) {
      const fallback = `HTTP ${response.status}`;
      const contentType = response.headers.get("content-type") || "";
      if (!contentType.includes("application/json")) {
        return new Error(fallback);
      }

      try {
        const body = await response.json();
        const error = new Error(messageFromErrorBody(body, fallback));
        error.code = body.code;
        error.status = response.status;
        return error;
      } catch (error) {
        return new Error(fallback);
      }
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

    function redirectToLogin() {
      window.alert("로그인이 필요한 화면입니다.");
      window.location.href = "/login";
    }

    function isAuthorizationError(error) {
      return error instanceof Error && error.code === "AUTHORIZATION_ERROR";
    }

    function clearCurrentUser() {
      state.currentUser = null;
      clearManagerInfo();
      window.sessionStorage.removeItem("roomescape.currentUser");
    }

    function clearManagerInfo() {
      state.managerInfo = null;
      state.managerInfoStatus = "idle";
      state.managerInfoMessage = "";
    }

    function endpointMessageOr(error, fallback) {
      if (error instanceof Error && error.message && !/^HTTP \d+$/.test(error.message)) {
        return error.message;
      }
      return fallback;
    }

    function setAuthMessage(text, type = "") {
      if (!elements.authMessage) {
        return;
      }
      elements.authMessage.textContent = text;
      elements.authMessage.className = `message${type ? ` ${type}` : ""}`;
    }

    function fillLoginIdFromQuery() {
      if (!isLoginPage() || !elements.loginIdInput) {
        return;
      }

      const loginId = new URLSearchParams(window.location.search).get("loginId");
      if (loginId) {
        elements.loginIdInput.value = loginId;
        elements.loginPasswordInput.focus();
      }
    }

    function updateAuthUi() {
      if (elements.authActionButton) {
        const loggedIn = Boolean(state.currentUser);
        elements.authActionButton.textContent = loggedIn ? "로그아웃" : "로그인";
        elements.authActionButton.href = loggedIn ? "#" : "/login";
      }

      if (!isUserPage() || !elements.authStatus) {
        if (isUserPage() && elements.summaryUser) {
          const loggedIn = Boolean(state.currentUser);
          const nickname = state.currentUser?.nickname || state.currentUser?.loginId || "회원";
          elements.summaryUser.textContent = loggedIn ? nickname : "비로그인";
          syncSummary();
        }
        renderManagerInfo();
        return;
      }

      const loggedIn = Boolean(state.currentUser);
      const nickname = state.currentUser?.nickname || state.currentUser?.loginId || "회원";
      elements.authStatus.textContent = loggedIn ? `${nickname}님 로그인 중` : "로그인이 필요합니다.";
      elements.authStatus.classList.toggle("logged-in", loggedIn);
      elements.summaryUser.textContent = loggedIn ? nickname : "비로그인";
      syncSummary();
      renderManagerInfo();
    }

    function managedStore() {
      return state.managerInfo?.manager ? state.managerInfo.store : null;
    }

    function currentManagedStoreId() {
      const storeId = Number(managedStore()?.id);
      return Number.isFinite(storeId) ? storeId : null;
    }

    function storeDisplayName(store) {
      if (!store) {
        return "-";
      }
      return store.name || `매장 #${store.id}`;
    }

    function renderManagerInfo() {
      if (!elements.managerPanel) {
        return;
      }

      const loggedIn = Boolean(state.currentUser) || isAdminPage();
      elements.managerLoginButton.hidden = loggedIn;
      elements.managerAdminButton.hidden = true;
      elements.managerStoreCard.hidden = true;
      elements.managerMessage.className = "manager-message";

      if (!loggedIn) {
        elements.managerMessage.textContent = "로그인 후 매장 매니저 여부를 확인할 수 있습니다.";
        return;
      }

      if (state.managerInfoStatus === "loading") {
        elements.managerMessage.textContent = "관리 중인 매장 정보를 확인하는 중입니다.";
        return;
      }

      if (state.managerInfoStatus === "demo") {
        elements.managerMessage.textContent = "Spring 서버에 연결되면 관리 매장 정보를 확인할 수 있습니다.";
        return;
      }

      if (state.managerInfoStatus === "error") {
        elements.managerMessage.textContent = state.managerInfoMessage || "관리 매장 정보를 불러오지 못했습니다.";
        elements.managerMessage.classList.add("error");
        return;
      }

      const store = managedStore();
      if (store) {
        elements.managerMessage.textContent = "이 계정은 아래 매장을 관리하고 있습니다.";
        elements.managerStoreName.textContent = storeDisplayName(store);
        elements.managerStoreMeta.textContent = `storeId ${store.id}`;
        elements.managerStoreCard.hidden = false;
        elements.managerAdminButton.hidden = isAdminPage();
        return;
      }

      if (state.managerInfoStatus === "loaded") {
        elements.managerMessage.textContent = "현재 계정은 매장 매니저로 등록되어 있지 않습니다.";
        return;
      }

      elements.managerMessage.textContent = "로그인 상태를 확인한 뒤 관리 매장 정보를 보여드립니다.";
    }

    async function loadManagerInfo() {
      if (!elements.managerPanel) {
        return;
      }

      const canRequest = Boolean(state.currentUser) || isAdminPage();
      if (!canRequest) {
        clearManagerInfo();
        renderManagerInfo();
        return;
      }

      if (state.mode !== "live") {
        state.managerInfo = null;
        state.managerInfoStatus = "demo";
        state.managerInfoMessage = "";
        renderManagerInfo();
        return;
      }

      state.managerInfoStatus = "loading";
      state.managerInfoMessage = "";
      renderManagerInfo();

      try {
        state.managerInfo = await getJson("/store-managers/me");
        state.managerInfoStatus = "loaded";
      } catch (error) {
        if (isAuthorizationError(error)) {
          clearCurrentUser();
          updateAuthUi();
          if (isUserPage()) {
            renderLoggedOutLookup();
            clearEditReservation();
          }
          renderManagerInfo();
          return;
        }

        state.managerInfo = null;
        state.managerInfoStatus = "error";
        state.managerInfoMessage = endpointMessageOr(error, "관리 매장 정보를 불러오지 못했습니다.");
      }

      renderManagerInfo();
    }

    async function signUp(event) {
      event.preventDefault();
      const payload = {
        loginId: elements.signupLoginIdInput.value.trim(),
        password: elements.signupPasswordInput.value,
        nickname: elements.signupNicknameInput.value.trim()
      };

      if (!payload.loginId || !payload.password || !payload.nickname) {
        setAuthMessage("아이디, 비밀번호, 닉네임을 모두 입력해주세요.", "error");
        return;
      }

      elements.signupButton.disabled = true;
      setAuthMessage("회원가입 요청 중입니다.");

      try {
        const member = await postJson("/members", payload);
        elements.signupForm.reset();
        if (elements.loginIdInput && elements.loginPasswordInput) {
          elements.loginIdInput.value = payload.loginId;
          elements.loginPasswordInput.value = payload.password;
        }
        window.alert(`${member?.nickname || payload.nickname}님, 회원가입이 완료되었습니다.`);
        window.location.href = `/login?loginId=${encodeURIComponent(payload.loginId)}`;
      } catch (error) {
        setAuthMessage(endpointMessageOr(error, "회원가입에 실패했습니다."), "error");
      } finally {
        elements.signupButton.disabled = false;
      }
    }

    async function logout() {
      try {
        await postJson("/auth/web/logout", {});
      } catch (error) {
        if (!isAuthorizationError(error)) {
          showToast("로그아웃 요청에 실패했습니다.", endpointMessageOr(error, "다시 시도해주세요."));
          return;
        }
      }

      clearCurrentUser();
      updateAuthUi();
      renderManagerInfo();

      if (isUserPage()) {
        renderLoggedOutLookup();
        clearEditReservation();
        elements.formMessage.textContent = "로그아웃되었습니다.";
        elements.formMessage.className = "message";
      }

      showToast("로그아웃되었습니다.", "다시 예약하려면 로그인해주세요.");
    }

    async function login(event) {
      event.preventDefault();
      const payload = {
        loginId: elements.loginIdInput.value.trim(),
        password: elements.loginPasswordInput.value
      };

      if (!payload.loginId || !payload.password) {
        setAuthMessage("아이디와 비밀번호를 입력해주세요.", "error");
        return;
      }

      elements.loginButton.disabled = true;
      setAuthMessage("로그인 요청 중입니다.");

      try {
        await postJson("/auth/web/login", payload);
        rememberCurrentUser({ loginId: payload.loginId, nickname: payload.loginId });
        elements.loginForm.reset();
        setAuthMessage("로그인되었습니다. 이제 예약 생성과 내 예약 조회를 사용할 수 있습니다.", "ok");
        window.location.href = "/";
      } catch (error) {
        clearCurrentUser();
        updateAuthUi();
        setAuthMessage(endpointMessageOr(error, "아이디 또는 비밀번호를 확인해주세요."), "error");
      } finally {
        elements.loginButton.disabled = false;
      }
    }

    async function getReservationListData(page = state.adminReservationPage, size = state.adminReservationSize) {
      return getJson(`/admin/reservations?page=${page}&size=${size}`);
    }

    function escapeHtml(value) {
      return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
    }

    function setSourceStatus() {
      elements.sourceStatus.classList.toggle("live", state.mode === "live");
      const label = state.mode === "live"
        ? "Spring 서버 연결됨"
        : state.mode === "loading"
          ? "데이터 불러오는 중"
          : "데모 데이터";
      elements.sourceStatus.querySelector("span:last-child").textContent = label;
    }

    function selectedTheme() {
      return state.themes.find((theme) => theme.id === state.selectedThemeId) || null;
    }

    function selectedTime() {
      return state.availableTimes.find((time) => time.id === state.selectedTimeId) || null;
    }

    function selectedAdminTheme() {
      return state.themes.find((theme) => theme.id === state.adminSelectedThemeId) || null;
    }

    function selectedAdminTime() {
      return state.adminAvailableTimes.find((time) => time.id === state.adminSelectedTimeId) || null;
    }

    function normalizeTime(startAt) {
      return String(startAt).slice(0, 5);
    }

    function formatDate(dateText) {
      if (!dateText) {
        return "-";
      }
      const [year, month, date] = dateText.split("-");
      return `${year}.${month}.${date}`;
    }

    function renderPopularThemes() {
      const themes = state.popularThemes.slice(0, 10);
      elements.popularList.innerHTML = "";

      if (themes.length === 0) {
        elements.popularList.innerHTML = `<div class="empty">인기 테마가 없습니다.</div>`;
        return;
      }

      themes.forEach((theme, index) => {
        const item = document.createElement("button");
        item.type = "button";
        item.className = "popular-item";
        item.innerHTML = `
          <span class="rank">${index + 1}</span>
          <span>
            <span class="popular-name">${escapeHtml(theme.name)}</span>
            <span class="popular-desc">${escapeHtml(theme.description || "")}</span>
          </span>
        `;
        item.addEventListener("click", () => {
          state.selectedThemeId = theme.id;
          state.selectedTimeId = null;
          renderThemes();
          loadAvailability();
        });
        elements.popularList.appendChild(item);
      });
    }

    function renderThemes() {
      elements.themeGrid.innerHTML = "";
      elements.themeCount.textContent = `${state.themes.length}개 테마`;

      if (state.themes.length === 0) {
        elements.themeGrid.innerHTML = `<div class="empty">등록된 테마가 없습니다.</div>`;
        return;
      }

      state.themes.forEach((theme) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `theme-button${theme.id === state.selectedThemeId ? " selected" : ""}`;
        button.setAttribute("aria-pressed", theme.id === state.selectedThemeId ? "true" : "false");
        const imageSource = themeImageSource(theme);
        button.innerHTML = `
          <img class="theme-thumb" src="${escapeHtml(imageSource)}" alt="${escapeHtml(theme.name)} 썸네일">
          <span class="theme-body">
            <span class="theme-name">${escapeHtml(theme.name)}</span>
            <span class="theme-description">${escapeHtml(theme.description || "")}</span>
            <span class="theme-meta">60분 진행</span>
          </span>
        `;
        const img = button.querySelector("img");
        img.addEventListener("error", () => {
          img.src = posterFor(theme);
        }, { once: true });
        button.addEventListener("click", () => {
          state.selectedThemeId = theme.id;
          state.selectedTimeId = null;
          renderThemes();
          loadAvailability();
        });
        elements.themeGrid.appendChild(button);
      });
    }

    function renderTimes() {
      elements.timeGrid.innerHTML = "";
      const availableCount = state.availableTimes.filter((time) => time.isAvailable).length;
      elements.timeCount.textContent = state.selectedThemeId ? `${availableCount}개 가능` : "테마를 먼저 선택";

      if (!state.selectedThemeId) {
        elements.timeGrid.innerHTML = `<div class="empty">테마를 선택하면 시간 목록이 표시됩니다.</div>`;
        syncSummary();
        return;
      }

      if (state.availableTimes.length === 0) {
        elements.timeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncSummary();
        return;
      }

      state.availableTimes.forEach((time) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `time-button${time.id === state.selectedTimeId ? " selected" : ""}`;
        button.disabled = !time.isAvailable;
        button.textContent = normalizeTime(time.startAt);
        button.addEventListener("click", () => {
          state.selectedTimeId = time.id;
          renderTimes();
        });
        elements.timeGrid.appendChild(button);
      });

      syncSummary();
    }

    function syncSummary() {
      const theme = selectedTheme();
      const time = selectedTime();
      elements.dateNote.textContent = `${formatDate(elements.dateInput.value)} 기준으로 선택한 테마의 비어 있는 시간만 보여줍니다.`;
      elements.summaryDate.textContent = formatDate(elements.dateInput.value);
      elements.summaryTheme.textContent = theme ? theme.name : "-";
      elements.summaryTime.textContent = time ? normalizeTime(time.startAt) : "-";

      const canReserve = Boolean(state.currentUser && theme && time);
      elements.reserveButton.disabled = !canReserve;
      elements.formMessage.textContent = canReserve ? "" : "로그인 후 테마와 시간을 선택하면 예약할 수 있습니다.";
      elements.formMessage.className = "message";
    }

    function getDemoAvailabilityFor(date, themeId) {
      return state.times.map((time) => {
        const reserved = state.demoReservations.some((reservation) =>
          reservation.date === date &&
          reservation.themeId === themeId &&
          reservation.timeId === time.id
        );
        return { ...time, isAvailable: !reserved };
      });
    }

    function getDemoAvailability() {
      return getDemoAvailabilityFor(elements.dateInput.value, state.selectedThemeId);
    }

    async function loadAvailability() {
      if (!state.selectedThemeId) {
        state.availableTimes = [];
        renderTimes();
        return;
      }

      if (state.mode === "live") {
        try {
          const data = await getJson(`/times/availability?date=${elements.dateInput.value}&themeId=${state.selectedThemeId}`);
          state.availableTimes = data.availableTimes || [];
        } catch (error) {
          state.mode = "demo";
          setSourceStatus();
          state.availableTimes = getDemoAvailability();
        }
      } else {
        state.availableTimes = getDemoAvailability();
      }

      const selected = selectedTime();
      if (selected && !selected.isAvailable) {
        state.selectedTimeId = null;
      }
      renderTimes();
    }

    async function loadAdminAvailability() {
      const date = elements.adminReserveDate.value;
      const themeId = state.adminSelectedThemeId;
      if (!date || !themeId) {
        state.adminAvailableTimes = [];
        state.adminSelectedTimeId = null;
        renderAdminReserveTimes();
        return;
      }

      if (state.mode === "live") {
        try {
          const data = await getJson(`/times/availability?date=${date}&themeId=${themeId}`);
          state.adminAvailableTimes = data.availableTimes || [];
        } catch (error) {
          state.mode = "demo";
          setSourceStatus();
          state.adminAvailableTimes = getDemoAvailabilityFor(date, themeId);
        }
      } else {
        state.adminAvailableTimes = getDemoAvailabilityFor(date, themeId);
      }

      const selected = selectedAdminTime();
      if (selected && !selected.isAvailable) {
        state.adminSelectedTimeId = null;
      }
      renderAdminReserveTimes();
    }

    async function reserve() {
      const theme = selectedTheme();
      const time = selectedTime();
      if (!state.currentUser) {
        elements.formMessage.textContent = "로그인 후 예약할 수 있습니다.";
        elements.formMessage.className = "message error";
        return;
      }
      if (!theme || !time) {
        syncSummary();
        return;
      }

      const payload = {
        date: elements.dateInput.value,
        timeId: time.id,
        themeId: theme.id
      };

      try {
        let createdReservation = null;
        if (state.mode === "live") {
          createdReservation = await postJson("/reservations", payload);
        } else {
          createdReservation = {
            id: getNextId(state.demoReservations),
            guestName: state.currentUser.nickname || state.currentUser.loginId,
            date: payload.date,
            themeId: payload.themeId,
            timeId: payload.timeId
          };
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
        state.reservations = [...state.reservations, createdReservation];

        showToast("예약이 완료되었습니다.", `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}`);
        state.selectedTimeId = null;
        await loadAvailability();
        await loadMyReservations();
        elements.formMessage.textContent = "예약이 완료되었습니다.";
        elements.formMessage.className = "message ok";
      } catch (error) {
        elements.formMessage.textContent = endpointMessageOr(error, "예약 요청에 실패했습니다.");
        elements.formMessage.className = "message error";
      }
    }

    function showToast(title, detail) {
      elements.toast.innerHTML = `<strong>${title}</strong><span>${detail}</span>`;
      elements.toast.classList.add("show");
      window.clearTimeout(showToast.timer);
      showToast.timer = window.setTimeout(() => {
        elements.toast.classList.remove("show");
      }, 3600);
    }

    function getNextId(items) {
      return Math.max(0, ...items.map((item) => Number(item.id) || 0)) + 1;
    }

    function setAdminMessage(text, type = "") {
      elements.adminMessage.textContent = text;
      elements.adminMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function setAdminReserveMessage(text, type = "") {
      elements.adminReserveMessage.textContent = text;
      elements.adminReserveMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function getReservationTheme(reservation) {
      return reservation.theme || state.themes.find((theme) => theme.id === reservation.themeId) || null;
    }

    function getReservationTime(reservation) {
      return reservation.time || state.times.find((time) => time.id === reservation.timeId) || null;
    }

    function getReservationThemeId(reservation) {
      return Number(reservation.themeId || reservation.theme?.id);
    }

    function getReservationTimeId(reservation) {
      return Number(reservation.timeId || reservation.time?.id);
    }

    function setEditReservationMessage(text, type = "") {
      elements.editReservationMessage.textContent = text;
      elements.editReservationMessage.className = `message${type ? ` ${type}` : ""}`;
    }

    function syncEditReservationForm() {
      if (!elements.editReservationForm || elements.editReservationForm.hidden) {
        return;
      }

      const canEdit = Boolean(
        state.editingReservationId &&
        state.currentUser &&
        elements.editReservationDate.value &&
        elements.editReservationTime.value &&
        !elements.editReservationTime.disabled
      );
      elements.editReservationButton.disabled = !canEdit;
    }

    function renderEditTimeOptions(times, selectedTimeId = null) {
      elements.editReservationTime.innerHTML = "";
      const availableTimes = times.filter((time) => time.isAvailable);
      if (availableTimes.length === 0) {
        elements.editReservationTime.innerHTML = `<option value="">예약 가능한 시간 없음</option>`;
        elements.editReservationTime.disabled = true;
        syncEditReservationForm();
        return;
      }

      elements.editReservationTime.disabled = false;
      elements.editReservationTime.innerHTML = `<option value="">시간 선택</option>`;

      [...availableTimes]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const option = document.createElement("option");
          option.value = time.id;
          option.textContent = normalizeTime(time.startAt);
          elements.editReservationTime.appendChild(option);
        });

      if (availableTimes.some((time) => time.id === selectedTimeId)) {
        elements.editReservationTime.value = String(selectedTimeId);
      }
      syncEditReservationForm();
    }

    async function loadEditAvailability(selectedTimeId = null) {
      const date = elements.editReservationDate.value;
      const themeId = state.editingReservationThemeId;
      if (!state.editingReservationId || !date || !themeId) {
        state.editAvailableTimes = [];
        renderEditTimeOptions([]);
        return;
      }

      elements.editReservationTime.disabled = true;
      elements.editReservationTime.innerHTML = `<option value="">불러오는 중</option>`;
      setEditReservationMessage("예약 가능한 시간을 불러오는 중입니다.");
      syncEditReservationForm();

      try {
        const times = state.mode === "live"
          ? (await getJson(`/times/availability?date=${date}&themeId=${themeId}`)).availableTimes || []
          : getDemoAvailabilityFor(date, themeId);
        state.editAvailableTimes = times;
        renderEditTimeOptions(times, selectedTimeId);
        const hasAvailableTime = times.some((time) => time.isAvailable);
        setEditReservationMessage(hasAvailableTime ? "" : "예약 가능한 시간이 없습니다.", hasAvailableTime ? "" : "error");
      } catch (error) {
        state.editAvailableTimes = [];
        renderEditTimeOptions([]);
        setEditReservationMessage(endpointMessageOr(error, "예약 가능한 시간 조회에 실패했습니다."), "error");
      }
    }

    function findReservation(id) {
      return [...state.lookupReservations, ...state.reservations, ...state.demoReservations]
        .find((reservation) => reservation.id === id) || null;
    }

    function clearEditReservation() {
      state.editingReservationId = null;
      state.editingReservationThemeId = null;
      state.editAvailableTimes = [];
      elements.editReservationForm.hidden = true;
      elements.editReservationForm.reset();
      elements.editReservationMeta.textContent = "";
      elements.editReservationTime.disabled = false;
      setEditReservationMessage("");
    }

    async function startEditReservation(id) {
      const reservation = findReservation(id);
      if (!reservation) {
        return;
      }

      const theme = getReservationTheme(reservation);
      const time = getReservationTime(reservation);
      state.editingReservationId = id;
      state.editingReservationThemeId = getReservationThemeId(reservation);
      elements.editReservationForm.hidden = false;
      elements.editReservationTitle.textContent = `예약 수정 #${id}`;
      elements.editReservationMeta.textContent = `${theme?.name || "-"} · ${normalizeTime(time?.startAt || "-")}`;
      elements.editAuthorizationName.value = state.currentUser?.nickname || state.currentUser?.loginId || "";
      elements.editReservationDate.value = reservation.date;
      await loadEditAvailability(getReservationTimeId(reservation));
      syncEditReservationForm();
      elements.editReservationDate.focus();
    }

    function renderLookupReservations(reservations) {
      syncLookupLoginButton();
      state.lookupReservations = reservations;
      elements.lookupList.innerHTML = "";
      elements.lookupCount.textContent = `${reservations.length}건`;

      if (reservations.length === 0) {
        elements.lookupList.innerHTML = `<div class="empty">조회된 예약이 없습니다.</div>`;
        return;
      }

      [...reservations]
        .sort((a, b) => String(b.date).localeCompare(String(a.date)) || Number(b.id) - Number(a.id))
        .forEach((reservation) => {
          const theme = getReservationTheme(reservation);
          const time = getReservationTime(reservation);
          const row = document.createElement("div");
          row.className = "list-row";
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">${escapeHtml(reservation.guestName || "예약자")}</span>
              <span class="list-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(theme?.name || "-")} · ${escapeHtml(normalizeTime(time?.startAt || "-"))}</span>
            </div>
            <div class="row-actions">
              <button class="secondary-button compact-button" type="button" data-edit-reservation-id="${reservation.id}">수정</button>
              <button class="danger-button compact-button" type="button" data-cancel-reservation-id="${reservation.id}">취소</button>
            </div>
          `;
          elements.lookupList.appendChild(row);
        });
    }

    async function loadMyReservations() {
      if (!state.currentUser) {
        renderLoggedOutLookup();
        clearEditReservation();
        return;
      }

      elements.lookupMessage.textContent = "예약을 조회하는 중입니다.";
      elements.lookupMessage.className = "message";
      clearEditReservation();

      try {
        const reservations = state.mode === "live"
          ? (await getJson("/reservations/me")).reservations || []
          : state.demoReservations.filter((reservation) =>
              reservation.guestName === (state.currentUser.nickname || state.currentUser.loginId)
            );

        renderLookupReservations(reservations);
        elements.lookupMessage.textContent = reservations.length === 0 ? "조회된 예약이 없습니다." : "예약 조회가 완료되었습니다.";
        elements.lookupMessage.className = `message${reservations.length === 0 ? "" : " ok"}`;
      } catch (error) {
        if (isAuthorizationError(error)) {
          clearCurrentUser();
          updateAuthUi();
          renderLoggedOutLookup();
          return;
        }
        renderLookupReservations([]);
        elements.lookupMessage.textContent = endpointMessageOr(error, "예약 조회에 실패했습니다.");
        elements.lookupMessage.className = "message error";
      }
    }

    function renderLoggedOutLookup() {
      syncLookupLoginButton();
      elements.lookupCount.textContent = "로그인이 필요합니다.";
      elements.lookupMessage.textContent = "로그인 후 조회가 가능합니다.";
      elements.lookupMessage.className = "message";
      elements.lookupList.innerHTML = "";
    }

    function syncLookupLoginButton() {
      elements.lookupLoginButton.hidden = Boolean(state.currentUser);
    }

    function replaceReservation(reservations, editedReservation) {
      return reservations.map((reservation) =>
        reservation.id === editedReservation.id ? editedReservation : reservation
      );
    }

    function removeReservation(reservations, reservationId) {
      return reservations.filter((reservation) => reservation.id !== reservationId);
    }

    function editDemoReservation(id, payload, authorizationName) {
      const reservation = state.demoReservations.find((item) => item.id === id);
      if (!reservation) {
        throw new Error("존재하지 않는 예약입니다.");
      }
      if (reservation.guestName !== authorizationName) {
        throw new Error("본인의 예약만 수정할 수 있습니다.");
      }

      const themeId = getReservationThemeId(reservation);
      const duplicated = state.demoReservations.some((item) =>
        item.id !== id &&
        item.date === payload.date &&
        getReservationTimeId(item) === payload.timeId &&
        getReservationThemeId(item) === themeId
      );
      if (duplicated) {
        throw new Error("이미 존재하는 예약입니다.");
      }

      return {
        ...reservation,
        date: payload.date,
        timeId: payload.timeId
      };
    }

    function cancelDemoReservation(id, authorizationName) {
      const reservation = state.demoReservations.find((item) => item.id === id);
      if (!reservation) {
        throw new Error("존재하지 않는 예약입니다.");
      }
      if (reservation.guestName !== authorizationName) {
        throw new Error("본인의 예약만 취소할 수 있습니다.");
      }
    }

    async function cancelReservation(id) {
      if (!state.currentUser) {
        elements.lookupMessage.textContent = "로그인 후 예약을 취소할 수 있습니다.";
        elements.lookupMessage.className = "message error";
        return;
      }
      const authorizationName = state.currentUser.nickname || state.currentUser.loginId;

      elements.lookupMessage.textContent = "예약을 취소하는 중입니다.";
      elements.lookupMessage.className = "message";
      clearEditReservation();

      try {
        if (state.mode === "live") {
          await deleteJson(`/reservations/${id}`);
        } else {
          cancelDemoReservation(id, authorizationName);
          state.demoReservations = removeReservation(state.demoReservations, id);
        }

        state.reservations = removeReservation(state.reservations, id);
        state.lookupReservations = removeReservation(state.lookupReservations, id);
        renderLookupReservations(state.lookupReservations);
        showToast("예약이 취소되었습니다.", authorizationName);
        await loadAvailability();
        elements.lookupMessage.textContent = "예약 취소가 완료되었습니다.";
        elements.lookupMessage.className = "message ok";
      } catch (error) {
        elements.lookupMessage.textContent = endpointMessageOr(error, "예약 취소에 실패했습니다.");
        elements.lookupMessage.className = "message error";
      }
    }

    async function editReservation(event) {
      event.preventDefault();
      const reservationId = state.editingReservationId;
      const authorizationName = state.currentUser?.nickname || state.currentUser?.loginId || "";
      const payload = {
        date: elements.editReservationDate.value,
        timeId: Number(elements.editReservationTime.value)
      };

      if (!reservationId || !authorizationName || !payload.date || !payload.timeId) {
        setEditReservationMessage("로그인 후 날짜와 시간을 모두 입력해주세요.", "error");
        syncEditReservationForm();
        return;
      }

      elements.editReservationButton.disabled = true;
      setEditReservationMessage("예약을 수정하는 중입니다.");

      try {
        const editedReservation = state.mode === "live"
          ? await patchJson(`/reservations/${reservationId}`, payload)
          : editDemoReservation(reservationId, payload, authorizationName);

        if (state.mode === "demo") {
          state.demoReservations = replaceReservation(state.demoReservations, editedReservation);
        }
        state.reservations = replaceReservation(state.reservations, editedReservation);
        state.lookupReservations = replaceReservation(state.lookupReservations, editedReservation);

        renderLookupReservations(state.lookupReservations);
        clearEditReservation();
        showToast("예약이 수정되었습니다.", `${formatDate(editedReservation.date)} · ${normalizeTime(getReservationTime(editedReservation)?.startAt || "")}`);
        await loadAvailability();
        elements.lookupMessage.textContent = "예약 수정이 완료되었습니다.";
        elements.lookupMessage.className = "message ok";
      } catch (error) {
        setEditReservationMessage(endpointMessageOr(error, "예약 수정에 실패했습니다."), "error");
        syncEditReservationForm();
      }
    }

    function pagedDemoReservations() {
      const start = (state.adminReservationPage - 1) * state.adminReservationSize;
      const end = start + state.adminReservationSize;
      const reservations = [...state.demoReservations]
        .sort((a, b) => Number(a.id) - Number(b.id));
      state.adminReservationHasNext = reservations.length > end;
      return reservations.slice(start, end);
    }

    function renderDemoFirst() {
      state.mode = "demo";
      state.themes = demoThemes;
      state.popularThemes = demoThemes.slice(0, 10);
      state.times = demoTimes.map(({ id, startAt }) => ({ id, startAt }));
      state.adminReservationPage = 1;
      state.reservations = pagedDemoReservations();
      setSourceStatus();
      loadManagerInfo();

      if (isAdminPage()) {
        state.adminSelectedThemeId = state.themes[0]?.id || null;
        state.adminSelectedTimeId = null;
        elements.adminReserveDate.value = DEFAULT_DATE;
        state.adminAvailableTimes = getDemoAvailabilityFor(elements.adminReserveDate.value, state.adminSelectedThemeId);
        renderAdmin();
        return;
      }

      state.selectedThemeId = state.themes[0]?.id || null;
      state.selectedTimeId = null;
      elements.dateInput.value = DEFAULT_DATE;
      updateAuthUi();
      renderPopularThemes();
      renderThemes();
      state.availableTimes = getDemoAvailability();
      renderTimes();
      syncSummary();
      if (state.currentUser) {
        renderLookupReservations([]);
      } else {
        renderLoggedOutLookup();
      }
    }

    async function loadInitialData() {
      restoreCurrentUser();
      updateAuthUi();
      renderManagerInfo();
      if (isAuthPage()) {
        state.mode = "live";
        fillLoginIdFromQuery();
        return;
      }

      const isFilePreview = window.location.protocol === "file:";
      if (isFilePreview) {
        renderDemoFirst();
        return;
      } else {
        if (isAdminPage()) {
          state.adminReservationPage = 1;
          elements.adminReserveDate.value = DEFAULT_DATE;
        } else {
          elements.dateInput.value = DEFAULT_DATE;
        }
        state.mode = "loading";
        setSourceStatus();
      }

      try {
        const [themeData, popularityData, timeData, reservationData] = isAdminPage()
          ? await Promise.all([
              getJson("/themes"),
              Promise.resolve({ themes: [] }),
              getJson("/times"),
              getReservationListData(1, state.adminReservationSize)
            ])
          : await Promise.all([
              getJson("/themes"),
              getJson("/themes/popularity?days=7&size=10"),
              getJson("/times"),
              Promise.resolve({ reservations: [] })
            ]);
        state.mode = "live";
        state.themes = themeData.themes || [];
        state.popularThemes = popularityData.themes || popularityData.popularThemes || [];
        state.times = timeData.times || [];
        state.reservations = reservationData.reservations || [];
        state.adminReservationHasNext = state.reservations.length === state.adminReservationSize;
        setSourceStatus();

        if (isAdminPage()) {
          state.adminSelectedThemeId = state.themes[0]?.id || null;
          state.adminSelectedTimeId = null;
          await loadManagerInfo();
          await loadAdminAvailability();
          renderAdmin();
          return;
        }

        state.selectedThemeId = state.themes[0]?.id || null;
        state.selectedTimeId = null;
        updateAuthUi();
        renderPopularThemes();
        renderThemes();
        await loadManagerInfo();
        await loadAvailability();
        syncSummary();
        if (state.currentUser) {
          await loadMyReservations();
        } else {
          renderLoggedOutLookup();
        }
      } catch (error) {
        if (isAdminPage() && isAuthorizationError(error)) {
          redirectToLogin();
          return;
        }
        state.mode = "demo";
        renderDemoFirst();
        setSourceStatus();
      }
    }

    function initializeApp() {
    if (isUserPage()) {
      elements.authActionButton.addEventListener("click", (event) => {
        if (state.currentUser) {
          event.preventDefault();
          logout();
        }
      });
      elements.dateInput.addEventListener("change", () => {
        state.selectedTimeId = null;
        loadAvailability();
      });
      elements.reserveButton.addEventListener("click", reserve);
      elements.lookupList.addEventListener("click", (event) => {
        const editButton = event.target.closest("[data-edit-reservation-id]");
        if (editButton) {
          startEditReservation(Number(editButton.dataset.editReservationId));
          return;
        }

        const cancelButton = event.target.closest("[data-cancel-reservation-id]");
        if (cancelButton) {
          cancelReservation(Number(cancelButton.dataset.cancelReservationId));
        }
      });
      elements.editReservationForm.addEventListener("submit", editReservation);
      elements.editCancelButton.addEventListener("click", clearEditReservation);
      elements.editAuthorizationName.addEventListener("input", syncEditReservationForm);
      elements.editReservationDate.addEventListener("change", () => loadEditAvailability());
      elements.editReservationTime.addEventListener("change", syncEditReservationForm);
    }

    if (isLoginPage()) {
      elements.loginForm.addEventListener("submit", login);
    }

    if (isSignupPage()) {
      elements.signupForm.addEventListener("submit", signUp);
    }

    if (isAdminPage()) {
      elements.authActionButton.addEventListener("click", (event) => {
        if (state.currentUser) {
          event.preventDefault();
          logout();
        }
      });
      elements.adminReservationForm.addEventListener("submit", createAdminReservation);
      elements.adminReserveName.addEventListener("input", syncAdminReserveSummary);
      elements.adminReserveDate.addEventListener("change", () => {
        state.adminSelectedTimeId = null;
        loadAdminAvailability();
      });
      elements.adminReserveTheme.addEventListener("change", () => {
        state.adminSelectedThemeId = Number(elements.adminReserveTheme.value) || null;
        state.adminSelectedTimeId = null;
        loadAdminAvailability();
      });
      elements.themeForm.addEventListener("submit", createTheme);
      elements.timeForm.addEventListener("submit", createTime);
      elements.adminThemeList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-theme-id]");
        if (button) {
          deleteTheme(Number(button.dataset.deleteThemeId));
        }
      });
      elements.adminTimeList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-time-id]");
        if (button) {
          deleteTime(Number(button.dataset.deleteTimeId));
        }
      });
      elements.adminReservationList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-reservation-id]");
        if (button) {
          deleteReservation(Number(button.dataset.deleteReservationId));
        }
      });
      elements.adminReservationPageSize.addEventListener("change", async () => {
        state.adminReservationSize = Number(elements.adminReservationPageSize.value);
        state.adminReservationPage = 1;
        await loadAdminReservations();
        renderAdmin();
      });
      elements.adminReservationPrevPage.addEventListener("click", async () => {
        if (state.adminReservationPage <= 1) {
          return;
        }
        state.adminReservationPage -= 1;
        await loadAdminReservations();
        renderAdmin();
      });
      elements.adminReservationNextPage.addEventListener("click", async () => {
        if (!state.adminReservationHasNext) {
          return;
        }
        state.adminReservationPage += 1;
        await loadAdminReservations();
        renderAdmin();
      });
    }

    loadInitialData();
    }
