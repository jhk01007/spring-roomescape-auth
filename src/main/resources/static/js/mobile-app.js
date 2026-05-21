const state = {
  user: null,
  themes: [],
  popularThemes: [],
  times: [],
  availableTimes: [],
  reservations: [],
  editAvailableTimes: [],
  selectedThemeId: null,
  selectedTimeId: null,
  editingReservationId: null,
  editingThemeId: null
};

const colors = [
  ["#0e7372", "#163b3f"],
  ["#b84f2f", "#34231d"],
  ["#315d91", "#1c2b3d"],
  ["#6f6a35", "#252318"],
  ["#2f7a50", "#182b22"]
];

const $ = (selector) => document.querySelector(selector);

const elements = {
  authScreen: $("#authScreen"),
  appScreen: $("#appScreen"),
  loginTab: $("#loginTab"),
  signupTab: $("#signupTab"),
  loginForm: $("#loginForm"),
  signupForm: $("#signupForm"),
  loginId: $("#loginId"),
  loginPassword: $("#loginPassword"),
  loginButton: $("#loginButton"),
  signupLoginId: $("#signupLoginId"),
  signupPassword: $("#signupPassword"),
  signupNickname: $("#signupNickname"),
  signupButton: $("#signupButton"),
  authMessage: $("#authMessage"),
  logoutButton: $("#logoutButton"),
  userLabel: $("#userLabel"),
  tabButtons: document.querySelectorAll("[data-view]"),
  reserveView: $("#reserveView"),
  mineView: $("#mineView"),
  dateInput: $("#dateInput"),
  popularCount: $("#popularCount"),
  popularList: $("#popularList"),
  themeCount: $("#themeCount"),
  themeList: $("#themeList"),
  timeCount: $("#timeCount"),
  timeGrid: $("#timeGrid"),
  summaryText: $("#summaryText"),
  reserveButton: $("#reserveButton"),
  reserveMessage: $("#reserveMessage"),
  reservationCount: $("#reservationCount"),
  reservationMessage: $("#reservationMessage"),
  reservationList: $("#reservationList"),
  editForm: $("#editForm"),
  editTitle: $("#editTitle"),
  editDate: $("#editDate"),
  editTime: $("#editTime"),
  editButton: $("#editButton"),
  editCancelButton: $("#editCancelButton"),
  editMessage: $("#editMessage"),
  toast: $("#toast")
};

function todayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const date = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${date}`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function endpointMessageOr(error, fallback) {
  if (error instanceof Error && error.message && !/^HTTP \d+$/.test(error.message)) {
    return error.message;
  }
  return fallback;
}

function setMessage(element, text, type = "") {
  element.textContent = text;
  element.className = `message${type ? ` ${type}` : ""}`;
}

function normalizeTime(startAt) {
  return String(startAt || "").slice(0, 5);
}

function formatDate(dateText) {
  if (!dateText) {
    return "-";
  }
  const [year, month, date] = dateText.split("-");
  return `${year}.${month}.${date}`;
}

function posterFor(theme) {
  const [start, end] = colors[(Number(theme.id) - 1) % colors.length];
  const canvas = document.createElement("canvas");
  canvas.width = 420;
  canvas.height = 300;
  const context = canvas.getContext("2d");
  context.fillStyle = start;
  context.fillRect(0, 0, 420, 300);
  context.fillStyle = end;
  context.fillRect(0, 210, 420, 90);
  context.fillStyle = "rgba(255,255,255,0.14)";
  for (let index = 0; index < 7; index += 1) {
    context.fillRect(35 + index * 54, 42, 24, 186);
  }
  context.fillStyle = "#fffdf8";
  context.font = "800 34px system-ui, sans-serif";
  context.fillText(theme.name || "Theme", 28, 264);
  return canvas.toDataURL("image/png");
}

function themeImageSource(theme) {
  const thumbnail = String(theme.thumbnail || "").trim();
  if (!thumbnail || thumbnail.includes("example.com/")) {
    return posterFor(theme);
  }
  return thumbnail;
}

function showToast(title, detail = "") {
  elements.toast.innerHTML = `<strong>${escapeHtml(title)}</strong><span>${escapeHtml(detail)}</span>`;
  elements.toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    elements.toast.classList.remove("show");
  }, 3000);
}

function selectedTheme() {
  return state.themes.find((theme) => theme.id === state.selectedThemeId) || null;
}

function selectedTime() {
  return state.availableTimes.find((time) => time.id === state.selectedTimeId) || null;
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

function renderAuthMode(mode) {
  const loginMode = mode === "login";
  elements.loginTab.classList.toggle("active", loginMode);
  elements.signupTab.classList.toggle("active", !loginMode);
  elements.loginForm.hidden = !loginMode;
  elements.signupForm.hidden = loginMode;
  setMessage(elements.authMessage, "");
}

function renderShell() {
  state.user = window.MobileApi.getUser();
  const loggedIn = Boolean(window.MobileApi.getAccessToken() && state.user);
  elements.authScreen.hidden = loggedIn;
  elements.appScreen.hidden = !loggedIn;
  elements.logoutButton.hidden = !loggedIn;
  elements.userLabel.textContent = loggedIn ? `${state.user.nickname || state.user.loginId}님` : "-";
}

function activateView(view) {
  elements.tabButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.view === view);
  });
  elements.reserveView.hidden = view !== "reserve";
  elements.mineView.hidden = view !== "mine";

  if (view === "mine") {
    loadMyReservations();
  }
}

function renderPopularThemes() {
  const themes = state.popularThemes.slice(0, 10);
  elements.popularCount.textContent = `${themes.length}개`;
  elements.popularList.innerHTML = "";

  if (themes.length === 0) {
    elements.popularList.innerHTML = `<div class="empty-state">인기 테마가 없습니다.</div>`;
    return;
  }

  themes.forEach((theme, index) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "popular-card";
    button.innerHTML = `
      <span class="popular-rank">${index + 1}</span>
      <span>
        <span class="popular-name">${escapeHtml(theme.name)}</span>
        <span class="popular-desc">${escapeHtml(theme.description || "")}</span>
      </span>
    `;
    button.addEventListener("click", () => selectTheme(theme.id));
    elements.popularList.appendChild(button);
  });
}

function renderThemes() {
  elements.themeCount.textContent = `${state.themes.length}개`;
  elements.themeList.innerHTML = "";

  if (state.themes.length === 0) {
    elements.themeList.innerHTML = `<div class="empty-state">등록된 테마가 없습니다.</div>`;
    return;
  }

  state.themes.forEach((theme) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `theme-card${theme.id === state.selectedThemeId ? " selected" : ""}`;
    button.innerHTML = `
      <img src="${escapeHtml(themeImageSource(theme))}" alt="${escapeHtml(theme.name)} 썸네일">
      <span class="theme-copy">
        <span class="theme-name">${escapeHtml(theme.name)}</span>
        <span class="theme-desc">${escapeHtml(theme.description || "")}</span>
        <span class="theme-meta">60분 진행</span>
      </span>
    `;
    button.querySelector("img").addEventListener("error", (event) => {
      event.target.src = posterFor(theme);
    }, { once: true });
    button.addEventListener("click", () => selectTheme(theme.id));
    elements.themeList.appendChild(button);
  });
}

function renderTimes() {
  const availableCount = state.availableTimes.filter((time) => time.isAvailable).length;
  elements.timeCount.textContent = state.selectedThemeId ? `${availableCount}개 가능` : "테마 선택 필요";
  elements.timeGrid.innerHTML = "";

  if (!state.selectedThemeId) {
    elements.timeGrid.innerHTML = `<div class="empty-state">테마를 먼저 선택해주세요.</div>`;
    syncSummary();
    return;
  }

  if (state.availableTimes.length === 0) {
    elements.timeGrid.innerHTML = `<div class="empty-state">등록된 시간이 없습니다.</div>`;
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
  elements.summaryText.textContent = theme && time
    ? `${formatDate(elements.dateInput.value)} · ${theme.name} · ${normalizeTime(time.startAt)}`
    : "테마와 시간을 선택해주세요.";
  elements.reserveButton.disabled = !(theme && time);
}

async function selectTheme(themeId) {
  state.selectedThemeId = themeId;
  state.selectedTimeId = null;
  renderThemes();
  await loadAvailability();
}

async function loadAvailability() {
  if (!state.selectedThemeId) {
    state.availableTimes = [];
    renderTimes();
    return;
  }

  try {
    const date = elements.dateInput.value;
    const data = await window.MobileApi.get(`/times/availability?date=${date}&themeId=${state.selectedThemeId}`);
    state.availableTimes = data.availableTimes || [];
  } catch (error) {
    state.availableTimes = [];
    setMessage(elements.reserveMessage, endpointMessageOr(error, "예약 가능 시간 조회에 실패했습니다."), "error");
  }

  if (selectedTime() && !selectedTime().isAvailable) {
    state.selectedTimeId = null;
  }
  renderTimes();
}

async function loadAppData() {
  setMessage(elements.reserveMessage, "예약 정보를 불러오는 중입니다.");
  try {
    const [themeData, popularityData, timeData] = await Promise.all([
      window.MobileApi.get("/themes"),
      window.MobileApi.get("/themes/popularity?days=7&size=10"),
      window.MobileApi.get("/times")
    ]);
    state.themes = themeData.themes || [];
    state.popularThemes = popularityData.themes || popularityData.popularThemes || [];
    state.times = timeData.times || [];
    state.selectedThemeId = state.themes[0]?.id || null;
    state.selectedTimeId = null;
    renderPopularThemes();
    renderThemes();
    await loadAvailability();
    setMessage(elements.reserveMessage, "");
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    setMessage(elements.reserveMessage, endpointMessageOr(error, "데이터를 불러오지 못했습니다."), "error");
  }
}

async function reserve() {
  const theme = selectedTheme();
  const time = selectedTime();
  if (!theme || !time) {
    syncSummary();
    return;
  }

  elements.reserveButton.disabled = true;
  setMessage(elements.reserveMessage, "예약을 요청하는 중입니다.");

  try {
    const payload = {
      date: elements.dateInput.value,
      timeId: time.id,
      themeId: theme.id
    };
    const reservation = await window.MobileApi.post("/reservations", payload);
    state.reservations = [reservation, ...state.reservations];
    state.selectedTimeId = null;
    await loadAvailability();
    setMessage(elements.reserveMessage, "예약이 완료되었습니다.", "ok");
    showToast("예약이 완료되었습니다.", `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}`);
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    setMessage(elements.reserveMessage, endpointMessageOr(error, "예약에 실패했습니다."), "error");
  } finally {
    syncSummary();
  }
}

function renderReservations(reservations) {
  elements.reservationCount.textContent = `${reservations.length}건`;
  elements.reservationList.innerHTML = "";

  if (reservations.length === 0) {
    elements.reservationList.innerHTML = `<div class="empty-state">조회된 예약이 없습니다.</div>`;
    return;
  }

  [...reservations]
    .sort((a, b) => String(b.date).localeCompare(String(a.date)) || Number(b.id) - Number(a.id))
    .forEach((reservation) => {
      const theme = getReservationTheme(reservation);
      const time = getReservationTime(reservation);
      const card = document.createElement("article");
      card.className = "reservation-card";
      card.innerHTML = `
        <div>
          <span class="reservation-title">${escapeHtml(reservation.guestName || "예약자")}</span>
          <span class="reservation-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(theme?.name || "-")} · ${escapeHtml(normalizeTime(time?.startAt || "-"))}</span>
        </div>
        <div class="reservation-actions">
          <button class="secondary-button" type="button" data-edit-id="${reservation.id}">수정</button>
          <button class="danger-button" type="button" data-delete-id="${reservation.id}">취소</button>
        </div>
      `;
      elements.reservationList.appendChild(card);
    });
}

async function loadMyReservations() {
  setMessage(elements.reservationMessage, "예약을 조회하는 중입니다.");
  clearEditForm();
  try {
    const data = await window.MobileApi.get("/reservations/me");
    state.reservations = data.reservations || [];
    renderReservations(state.reservations);
    setMessage(
      elements.reservationMessage,
      state.reservations.length === 0 ? "조회된 예약이 없습니다." : "예약 조회가 완료되었습니다.",
      state.reservations.length === 0 ? "" : "ok"
    );
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    renderReservations([]);
    setMessage(elements.reservationMessage, endpointMessageOr(error, "예약 조회에 실패했습니다."), "error");
  }
}

function findReservation(id) {
  return state.reservations.find((reservation) => reservation.id === id) || null;
}

function clearEditForm() {
  state.editingReservationId = null;
  state.editingThemeId = null;
  state.editAvailableTimes = [];
  elements.editForm.hidden = true;
  elements.editForm.reset();
  elements.editTime.disabled = false;
  setMessage(elements.editMessage, "");
}

async function startEditReservation(id) {
  const reservation = findReservation(id);
  if (!reservation) {
    return;
  }
  const theme = getReservationTheme(reservation);
  const time = getReservationTime(reservation);
  state.editingReservationId = id;
  state.editingThemeId = getReservationThemeId(reservation);
  elements.editTitle.textContent = `예약 수정 #${id}`;
  elements.editDate.value = reservation.date;
  elements.editForm.hidden = false;
  setMessage(elements.editMessage, `${theme?.name || "-"} · ${normalizeTime(time?.startAt || "-")}`);
  await loadEditAvailability(getReservationTimeId(reservation));
}

function renderEditTimeOptions(times, selectedTimeId = null) {
  const availableTimes = times.filter((time) => time.isAvailable);
  elements.editTime.innerHTML = "";

  if (availableTimes.length === 0) {
    elements.editTime.innerHTML = `<option value="">예약 가능한 시간 없음</option>`;
    elements.editTime.disabled = true;
    syncEditForm();
    return;
  }

  elements.editTime.disabled = false;
  elements.editTime.innerHTML = `<option value="">시간 선택</option>`;
  availableTimes
    .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
    .forEach((time) => {
      const option = document.createElement("option");
      option.value = time.id;
      option.textContent = normalizeTime(time.startAt);
      elements.editTime.appendChild(option);
    });

  if (availableTimes.some((time) => time.id === selectedTimeId)) {
    elements.editTime.value = String(selectedTimeId);
  }
  syncEditForm();
}

async function loadEditAvailability(selectedTimeId = null) {
  if (!state.editingReservationId || !elements.editDate.value || !state.editingThemeId) {
    renderEditTimeOptions([]);
    return;
  }

  elements.editTime.disabled = true;
  elements.editTime.innerHTML = `<option value="">불러오는 중</option>`;
  try {
    const data = await window.MobileApi.get(`/times/availability?date=${elements.editDate.value}&themeId=${state.editingThemeId}`);
    state.editAvailableTimes = data.availableTimes || [];
    renderEditTimeOptions(state.editAvailableTimes, selectedTimeId);
    const hasAvailable = state.editAvailableTimes.some((time) => time.isAvailable);
    setMessage(elements.editMessage, hasAvailable ? "" : "예약 가능한 시간이 없습니다.", hasAvailable ? "" : "error");
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    renderEditTimeOptions([]);
    setMessage(elements.editMessage, endpointMessageOr(error, "시간 조회에 실패했습니다."), "error");
  }
}

function syncEditForm() {
  elements.editButton.disabled = !(
    state.editingReservationId &&
    elements.editDate.value &&
    elements.editTime.value &&
    !elements.editTime.disabled
  );
}

async function editReservation(event) {
  event.preventDefault();
  const id = state.editingReservationId;
  const payload = {
    date: elements.editDate.value,
    timeId: Number(elements.editTime.value)
  };

  if (!id || !payload.date || !payload.timeId) {
    setMessage(elements.editMessage, "날짜와 시간을 모두 선택해주세요.", "error");
    return;
  }

  elements.editButton.disabled = true;
  setMessage(elements.editMessage, "예약을 수정하는 중입니다.");
  try {
    const edited = await window.MobileApi.patch(`/reservations/${id}`, payload);
    state.reservations = state.reservations.map((reservation) =>
      reservation.id === edited.id ? edited : reservation
    );
    renderReservations(state.reservations);
    clearEditForm();
    await loadAvailability();
    setMessage(elements.reservationMessage, "예약 수정이 완료되었습니다.", "ok");
    showToast("예약이 수정되었습니다.", `${formatDate(edited.date)} · ${normalizeTime(getReservationTime(edited)?.startAt || "")}`);
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    setMessage(elements.editMessage, endpointMessageOr(error, "예약 수정에 실패했습니다."), "error");
    syncEditForm();
  }
}

async function cancelReservation(id) {
  setMessage(elements.reservationMessage, "예약을 취소하는 중입니다.");
  clearEditForm();
  try {
    await window.MobileApi.delete(`/reservations/${id}`);
    state.reservations = state.reservations.filter((reservation) => reservation.id !== id);
    renderReservations(state.reservations);
    await loadAvailability();
    setMessage(elements.reservationMessage, "예약 취소가 완료되었습니다.", "ok");
    showToast("예약이 취소되었습니다.");
  } catch (error) {
    if (handleAuthError(error)) {
      return;
    }
    setMessage(elements.reservationMessage, endpointMessageOr(error, "예약 취소에 실패했습니다."), "error");
  }
}

function handleAuthError(error) {
  if (!window.MobileApi.isAuthError(error)) {
    return false;
  }
  window.MobileAuth.logout();
  state.user = null;
  renderShell();
  renderAuthMode("login");
  setMessage(elements.authMessage, endpointMessageOr(error, "다시 로그인해주세요."), "error");
  return true;
}

async function submitLogin(event) {
  event.preventDefault();
  const loginId = elements.loginId.value.trim();
  const password = elements.loginPassword.value;

  if (!loginId || !password) {
    setMessage(elements.authMessage, "아이디와 비밀번호를 입력해주세요.", "error");
    return;
  }

  elements.loginButton.disabled = true;
  setMessage(elements.authMessage, "토큰을 발급받는 중입니다.");
  try {
    await window.MobileAuth.login(loginId, password);
    elements.loginForm.reset();
    renderShell();
    await loadAppData();
    activateView("reserve");
    showToast("로그인되었습니다.", "이후 요청은 JWT 토큰으로 인증됩니다.");
  } catch (error) {
    window.MobileAuth.logout();
    setMessage(elements.authMessage, endpointMessageOr(error, "로그인에 실패했습니다."), "error");
  } finally {
    elements.loginButton.disabled = false;
  }
}

async function submitSignup(event) {
  event.preventDefault();
  const loginId = elements.signupLoginId.value.trim();
  const password = elements.signupPassword.value;
  const nickname = elements.signupNickname.value.trim();

  if (!loginId || !password || !nickname) {
    setMessage(elements.authMessage, "아이디, 비밀번호, 닉네임을 모두 입력해주세요.", "error");
    return;
  }

  elements.signupButton.disabled = true;
  setMessage(elements.authMessage, "회원가입 요청 중입니다.");
  try {
    await window.MobileAuth.signUp(loginId, password, nickname);
    elements.signupForm.reset();
    elements.loginId.value = loginId;
    elements.loginPassword.value = password;
    renderAuthMode("login");
    window.alert(`${nickname}님, 회원가입이 완료되었습니다.`);
  } catch (error) {
    setMessage(elements.authMessage, endpointMessageOr(error, "회원가입에 실패했습니다."), "error");
  } finally {
    elements.signupButton.disabled = false;
  }
}

function logout() {
  window.MobileAuth.logout();
  state.user = null;
  state.reservations = [];
  clearEditForm();
  renderShell();
  renderAuthMode("login");
  setMessage(elements.authMessage, "로그아웃되었습니다.", "ok");
}

function bindEvents() {
  elements.loginTab.addEventListener("click", () => renderAuthMode("login"));
  elements.signupTab.addEventListener("click", () => renderAuthMode("signup"));
  elements.loginForm.addEventListener("submit", submitLogin);
  elements.signupForm.addEventListener("submit", submitSignup);
  elements.logoutButton.addEventListener("click", logout);
  elements.dateInput.addEventListener("change", () => {
    state.selectedTimeId = null;
    loadAvailability();
  });
  elements.reserveButton.addEventListener("click", reserve);
  elements.tabButtons.forEach((button) => {
    button.addEventListener("click", () => activateView(button.dataset.view));
  });
  elements.reservationList.addEventListener("click", (event) => {
    const editButton = event.target.closest("[data-edit-id]");
    if (editButton) {
      startEditReservation(Number(editButton.dataset.editId));
      return;
    }

    const deleteButton = event.target.closest("[data-delete-id]");
    if (deleteButton) {
      cancelReservation(Number(deleteButton.dataset.deleteId));
    }
  });
  elements.editCancelButton.addEventListener("click", clearEditForm);
  elements.editDate.addEventListener("change", () => loadEditAvailability());
  elements.editTime.addEventListener("change", syncEditForm);
  elements.editForm.addEventListener("submit", editReservation);
}

async function initialize() {
  elements.dateInput.value = todayDate();
  bindEvents();
  renderAuthMode("login");

  try {
    await window.MobileApi.ensureAccessToken();
  } catch (error) {
    window.MobileAuth.logout();
    setMessage(elements.authMessage, endpointMessageOr(error, "다시 로그인해주세요."), "error");
  }

  renderShell();

  if (window.MobileApi.getAccessToken()) {
    await loadAppData();
    activateView("reserve");
  }
}

initialize();
