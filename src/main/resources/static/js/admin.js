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
            <button class="danger-button" type="button" data-delete-reservation-id="${reservation.id}">삭제</button>
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

    async function createTheme(event) {
      event.preventDefault();
      const payload = {
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
          ? await postJson("/admin/times", { startAt })
          : { id: getNextId(state.times), startAt };
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
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.id !== id);
        setAdminMessage("예약이 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "예약 삭제에 실패했습니다."), "error");
      }
    }

