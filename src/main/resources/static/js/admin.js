    function renderAdmin() {
      elements.reservationCount.textContent =
        `GET /admin/reservations?page=${state.adminReservationPage}&size=${state.adminReservationSize} · ${state.reservations.length}건`;
      elements.adminThemeCount.textContent = `${state.themes.length}개`;
      elements.adminTimeCount.textContent = `${state.times.length}개`;
      renderAdminReservationForm();
      renderAdminReservations();
      renderAdminReservationPagination();
      renderAdminThemes();
      renderAdminTimes();
    }

    function adminStoreId() {
      if (typeof currentManagedStoreId !== "function") {
        return 1;
      }
      return currentManagedStoreId() || 1;
    }

    function renderAdminReservationForm() {
      const previousThemeId = Number(elements.adminReserveTheme.value) || state.adminSelectedThemeId;
      elements.adminReserveTheme.innerHTML = "";

      if (state.themes.length === 0) {
        elements.adminReserveTheme.innerHTML = `<option value="">테마 없음</option>`;
        state.adminSelectedThemeId = null;
      } else {
        state.themes.forEach((theme) => {
          const option = document.createElement("option");
          option.value = theme.id;
          option.textContent = theme.name;
          elements.adminReserveTheme.appendChild(option);
        });
        const nextThemeId = state.themes.some((theme) => theme.id === previousThemeId)
          ? previousThemeId
          : state.themes[0].id;
        state.adminSelectedThemeId = nextThemeId;
        elements.adminReserveTheme.value = String(nextThemeId);
      }

      renderAdminReserveTimes();
    }

    function renderAdminReserveTimes() {
      elements.adminReserveTimeGrid.innerHTML = "";

      if (!state.adminSelectedThemeId) {
        elements.adminReserveTimeGrid.innerHTML = `<div class="empty">테마를 선택하면 시간이 표시됩니다.</div>`;
        syncAdminReserveSummary();
        return;
      }

      if (state.adminAvailableTimes.length === 0) {
        elements.adminReserveTimeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncAdminReserveSummary();
        return;
      }

      state.adminAvailableTimes.forEach((time) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `time-button${time.id === state.adminSelectedTimeId ? " selected" : ""}`;
        button.disabled = !time.isAvailable;
        button.textContent = normalizeTime(time.startAt);
        button.addEventListener("click", () => {
          state.adminSelectedTimeId = time.id;
          renderAdminReserveTimes();
        });
        elements.adminReserveTimeGrid.appendChild(button);
      });

      syncAdminReserveSummary();
    }

    function syncAdminReserveSummary() {
      const theme = selectedAdminTheme();
      const time = selectedAdminTime();
      const name = elements.adminReserveName.value.trim();
      elements.adminReserveSummary.innerHTML = `
        <span>날짜 <strong>${escapeHtml(formatDate(elements.adminReserveDate.value))}</strong></span>
        <span>테마 <strong>${escapeHtml(theme?.name || "-")}</strong></span>
        <span>시간 <strong>${escapeHtml(time ? normalizeTime(time.startAt) : "-")}</strong></span>
      `;
      elements.adminReserveButton.disabled = !(name && theme && time);
    }

    function renderAdminReservations() {
      elements.adminReservationList.innerHTML = "";
      if (state.reservations.length === 0) {
        elements.adminReservationList.innerHTML = `<div class="empty">등록된 예약이 없습니다.</div>`;
        return;
      }

      state.reservations.forEach((reservation) => {
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
              <button class="secondary-button compact-button" type="button" data-edit-admin-reservation-id="${reservation.id}">수정</button>
              <button class="danger-button compact-button" type="button" data-delete-reservation-id="${reservation.id}">삭제</button>
            </div>
          `;
          elements.adminReservationList.appendChild(row);
        });
    }

    function renderAdminReservationPagination() {
      elements.adminReservationPageSize.value = String(state.adminReservationSize);
      elements.adminReservationPageLabel.textContent = `${state.adminReservationPage} 페이지`;
      elements.adminReservationPrevPage.disabled = state.adminReservationPage <= 1;
      elements.adminReservationNextPage.disabled = !state.adminReservationHasNext;
    }

    async function loadAdminReservations() {
      if (!isAdminPage()) {
        return;
      }

      if (state.mode === "live") {
        const data = await getReservationListData();
        state.reservations = data.reservations || [];
        state.adminReservationHasNext = state.reservations.length === state.adminReservationSize;
        return;
      }

      state.reservations = pagedDemoReservations();
    }

    function renderAdminThemes() {
      elements.adminThemeList.innerHTML = "";
      if (state.themes.length === 0) {
        elements.adminThemeList.innerHTML = `<div class="empty">등록된 테마가 없습니다.</div>`;
        return;
      }

      state.themes.forEach((theme) => {
        const row = document.createElement("div");
        row.className = "list-row theme-list-row";
        const imageSource = themeImageSource(theme);
        row.innerHTML = `
          <img class="admin-thumb" src="${escapeHtml(imageSource)}" alt="${escapeHtml(theme.name)} 썸네일">
          <div class="list-main">
            <span class="list-title">${escapeHtml(theme.name)}</span>
            <span class="list-meta">${escapeHtml(theme.description || "")}</span>
          </div>
          <button class="danger-button" type="button" data-delete-theme-id="${theme.id}">삭제</button>
        `;
        const img = row.querySelector("img");
        img.addEventListener("error", () => {
          img.src = posterFor(theme);
        }, { once: true });
        elements.adminThemeList.appendChild(row);
      });
    }

    function renderAdminTimes() {
      elements.adminTimeList.innerHTML = "";
      if (state.times.length === 0) {
        elements.adminTimeList.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        return;
      }

      [...state.times]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const row = document.createElement("div");
          row.className = "list-row";
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">${escapeHtml(normalizeTime(time.startAt))}</span>
              <span class="list-meta">모든 테마 공통 시작 시간</span>
            </div>
            <button class="danger-button" type="button" data-delete-time-id="${time.id}">삭제</button>
          `;
          elements.adminTimeList.appendChild(row);
        });
    }

    async function syncAfterAdminChange() {
      await loadAdminReservations();
      await loadAdminAvailability();
      renderAdmin();
    }

    function setAdminEditReservationMessage(text, type = "") {
      elements.adminEditReservationMessage.textContent = text;
      elements.adminEditReservationMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function syncAdminEditReservationForm() {
      if (!elements.adminEditReservationForm || elements.adminEditReservationForm.hidden) {
        return;
      }

      elements.adminEditReservationButton.disabled = !(
        state.adminEditingReservationId &&
        elements.adminEditReservationDate.value &&
        elements.adminEditReservationTime.value &&
        !elements.adminEditReservationTime.disabled
      );
    }

    function renderAdminEditTimeOptions(times, selectedTimeId = null) {
      elements.adminEditReservationTime.innerHTML = "";
      const availableTimes = times.filter((time) => time.isAvailable);
      if (availableTimes.length === 0) {
        elements.adminEditReservationTime.innerHTML = `<option value="">예약 가능한 시간 없음</option>`;
        elements.adminEditReservationTime.disabled = true;
        syncAdminEditReservationForm();
        return;
      }

      elements.adminEditReservationTime.disabled = false;
      elements.adminEditReservationTime.innerHTML = `<option value="">시간 선택</option>`;

      [...availableTimes]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const option = document.createElement("option");
          option.value = time.id;
          option.textContent = normalizeTime(time.startAt);
          elements.adminEditReservationTime.appendChild(option);
        });

      if (availableTimes.some((time) => time.id === selectedTimeId)) {
        elements.adminEditReservationTime.value = String(selectedTimeId);
      }
      syncAdminEditReservationForm();
    }

    async function loadAdminEditAvailability(selectedTimeId = null) {
      const date = elements.adminEditReservationDate.value;
      const themeId = state.adminEditingReservationThemeId;
      if (!state.adminEditingReservationId || !date || !themeId) {
        state.adminEditAvailableTimes = [];
        renderAdminEditTimeOptions([]);
        return;
      }

      elements.adminEditReservationTime.disabled = true;
      elements.adminEditReservationTime.innerHTML = `<option value="">불러오는 중</option>`;
      setAdminEditReservationMessage("예약 가능한 시간을 불러오는 중입니다.");
      syncAdminEditReservationForm();

      try {
        let times = state.mode === "live"
          ? (await getJson(`/times/availability?date=${date}&themeId=${themeId}`)).availableTimes || []
          : getDemoAvailabilityFor(date, themeId);

        const reservation = findReservation(state.adminEditingReservationId);
        const currentTime = getReservationTime(reservation);
        if (
          reservation &&
          reservation.date === date &&
          selectedTimeId &&
          currentTime &&
          !times.some((time) => time.id === selectedTimeId)
        ) {
          times = [{ ...currentTime, isAvailable: true }, ...times];
        }

        state.adminEditAvailableTimes = times;
        renderAdminEditTimeOptions(times, selectedTimeId);
        const hasAvailableTime = times.some((time) => time.isAvailable);
        setAdminEditReservationMessage(hasAvailableTime ? "" : "예약 가능한 시간이 없습니다.", hasAvailableTime ? "" : "error");
      } catch (error) {
        state.adminEditAvailableTimes = [];
        renderAdminEditTimeOptions([]);
        setAdminEditReservationMessage(endpointMessageOr(error, "예약 가능한 시간 조회에 실패했습니다."), "error");
      }
    }

    async function startAdminEditReservation(id) {
      const reservation = findReservation(id);
      if (!reservation) {
        return;
      }

      const theme = getReservationTheme(reservation);
      const time = getReservationTime(reservation);
      state.adminEditingReservationId = id;
      state.adminEditingReservationThemeId = getReservationThemeId(reservation);
      elements.adminEditReservationForm.hidden = false;
      elements.adminEditReservationTitle.textContent = `예약 수정 #${id}`;
      elements.adminEditReservationMeta.textContent = `${reservation.guestName || "예약자"} · ${theme?.name || "-"} · ${normalizeTime(time?.startAt || "-")}`;
      elements.adminEditReservationDate.value = reservation.date;
      await loadAdminEditAvailability(getReservationTimeId(reservation));
      syncAdminEditReservationForm();
      elements.adminEditReservationDate.focus();
    }

    function clearAdminEditReservation() {
      state.adminEditingReservationId = null;
      state.adminEditingReservationThemeId = null;
      state.adminEditAvailableTimes = [];
      elements.adminEditReservationForm.hidden = true;
      elements.adminEditReservationForm.reset();
      elements.adminEditReservationMeta.textContent = "";
      elements.adminEditReservationTime.disabled = false;
      setAdminEditReservationMessage("");
    }

    function editAdminDemoReservation(id, payload) {
      const reservation = findReservation(id);
      if (!reservation) {
        throw new Error("존재하지 않는 예약입니다.");
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
        timeId: payload.timeId,
        time: state.times.find((time) => time.id === payload.timeId) || reservation.time
      };
    }

    async function editAdminReservation(event) {
      event.preventDefault();
      const reservationId = state.adminEditingReservationId;
      const payload = {
        date: elements.adminEditReservationDate.value,
        timeId: Number(elements.adminEditReservationTime.value)
      };

      if (!reservationId || !payload.date || !payload.timeId) {
        setAdminEditReservationMessage("날짜와 시간을 모두 선택해주세요.", "error");
        syncAdminEditReservationForm();
        return;
      }

      elements.adminEditReservationButton.disabled = true;
      setAdminEditReservationMessage("예약을 수정하는 중입니다.");

      try {
        const editedReservation = state.mode === "live"
          ? await patchJson(`/admin/reservations/${reservationId}`, payload)
          : editAdminDemoReservation(reservationId, payload);

        if (state.mode === "demo") {
          state.demoReservations = replaceReservation(state.demoReservations, editedReservation);
        }
        state.reservations = replaceReservation(state.reservations, editedReservation);

        clearAdminEditReservation();
        showToast("예약이 수정되었습니다.", `${formatDate(editedReservation.date)} · ${normalizeTime(getReservationTime(editedReservation)?.startAt || "")}`);
        setAdminMessage("예약이 수정되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminEditReservationMessage(endpointMessageOr(error, "예약 수정에 실패했습니다."), "error");
        syncAdminEditReservationForm();
      }
    }

    async function createTheme(event) {
      event.preventDefault();
      const payload = {
        storeId: adminStoreId(),
        name: elements.adminThemeName.value.trim(),
        description: elements.adminThemeDescription.value.trim(),
        thumbnail: elements.adminThemeThumbnail.value.trim()
      };
      if (!payload.name) {
        setAdminMessage("테마 이름을 입력해주세요.", "error");
        return;
      }

      try {
        const theme = state.mode === "live"
          ? await postJson("/admin/themes", payload)
          : { id: getNextId(state.themes), ...payload };
        state.themes = [...state.themes, theme];
        if (state.popularThemes.length < 10) {
          state.popularThemes = [...state.popularThemes, theme];
        }
        state.selectedThemeId = state.selectedThemeId || theme.id;
        elements.themeForm.reset();
        setAdminMessage("테마가 추가되었습니다.", "ok");
        showToast("테마가 추가되었습니다.", theme.name);
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "테마 추가에 실패했습니다."), "error");
      }
    }

    async function createTime(event) {
      event.preventDefault();
      const startAt = elements.adminTimeStartAt.value;
      if (!startAt) {
        setAdminMessage("시작 시간을 선택해주세요.", "error");
        return;
      }

      try {
        const time = state.mode === "live"
          ? await postJson("/admin/times", { storeId: adminStoreId(), startAt })
          : { id: getNextId(state.times), storeId: adminStoreId(), startAt };
        state.times = [...state.times, time];
        setAdminMessage("예약 시간이 추가되었습니다.", "ok");
        showToast("예약 시간이 추가되었습니다.", normalizeTime(startAt));
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "시간 추가에 실패했습니다."), "error");
      }
    }

    async function createAdminReservation(event) {
      event.preventDefault();
      const theme = selectedAdminTheme();
      const time = selectedAdminTime();
      const name = elements.adminReserveName.value.trim();
      if (!name || !theme || !time) {
        setAdminReserveMessage("이름, 테마, 시간을 모두 선택해주세요.", "error");
        syncAdminReserveSummary();
        return;
      }

      const payload = {
        guestName: name,
        date: elements.adminReserveDate.value,
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
            guestName: payload.guestName,
            date: payload.date,
            themeId: payload.themeId,
            timeId: payload.timeId
          };
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
        elements.adminReserveName.value = "";
        state.adminSelectedTimeId = null;
        setAdminReserveMessage("예약이 추가되었습니다.", "ok");
        showToast("관리자 예약이 추가되었습니다.", `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}`);
        await syncAfterAdminChange();
      } catch (error) {
        setAdminReserveMessage(endpointMessageOr(error, "예약 추가에 실패했습니다."), "error");
      }
    }

    async function deleteTheme(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/themes/${id}`);
        }
        state.themes = state.themes.filter((theme) => theme.id !== id);
        state.popularThemes = state.popularThemes.filter((theme) => theme.id !== id);
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.themeId !== id);
        state.reservations = state.reservations.filter((reservation) => {
          const themeId = reservation.themeId || reservation.theme?.id;
          return themeId !== id;
        });
        if (state.selectedThemeId === id) {
          state.selectedThemeId = state.themes[0]?.id || null;
          state.selectedTimeId = null;
        }
        setAdminMessage("테마가 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "테마 삭제에 실패했습니다."), "error");
      }
    }

    async function deleteTime(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/times/${id}`);
        }
        state.times = state.times.filter((time) => time.id !== id);
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.timeId !== id);
        state.reservations = state.reservations.filter((reservation) => {
          const timeId = reservation.timeId || reservation.time?.id;
          return timeId !== id;
        });
        if (state.selectedTimeId === id) {
          state.selectedTimeId = null;
        }
        setAdminMessage("예약 시간이 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "시간 삭제에 실패했습니다."), "error");
      }
    }

    async function deleteReservation(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/reservations/${id}`);
        }
        if (state.adminEditingReservationId === id) {
          clearAdminEditReservation();
        }
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.id !== id);
        setAdminMessage("예약이 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "예약 삭제에 실패했습니다."), "error");
      }
    }
