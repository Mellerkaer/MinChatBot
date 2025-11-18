// javascript
const loginForm      = document.getElementById("admin-login-form");
const loginCard      = document.getElementById("login-card");
const loginError     = document.getElementById("login-error");
const adminContent   = document.getElementById("admin-content");
const refreshBtn     = document.getElementById("refreshBtn");
const logoutBtn      = document.getElementById("logoutBtn");
const adminSpinner   = document.getElementById("admin-spinner");
const adminTableBody = document.getElementById("admin-table-body");

const ADMIN_TOKEN_KEY = "budget_admin_token";

// Use sessionStorage so token is not persisted across browser sessions
function storeToken(token) {
    sessionStorage.setItem(ADMIN_TOKEN_KEY, token);
}
function getStoredToken() {
    return sessionStorage.getItem(ADMIN_TOKEN_KEY);
}
function clearToken() {
    sessionStorage.removeItem(ADMIN_TOKEN_KEY);
}

// Call backend with token
async function tryFetchWithToken(token) {
    const response = await fetch("/api/v1/admin/budget", {
        headers: { "X-Admin-Token": token }
    });

    if (response.status === 401) throw new Error("Unauthorized");
    return await response.json();
}

// Escape AI-text to avoid injecting HTML
function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

// Load admin data if token present
async function loadAdminData() {
    const token = getStoredToken();
    if (!token) return;

    if (adminSpinner) adminSpinner.classList.remove("d-none");

    try {
        const data = await tryFetchWithToken(token);
        renderTable(data);
    } catch (err) {
        console.error(err);
        if (adminTableBody) {
            adminTableBody.innerHTML = `
      <tr><td colspan="8" class="text-center text-danger">
      Kunne ikke hente data. Prøv igen.
      </td></tr>`;
        }
    } finally {
        if (adminSpinner) adminSpinner.classList.add("d-none");
    }
}

// Login handling
if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (loginError) loginError.classList.add("d-none");

        const passwordEl = document.getElementById("adminPassword");
        const password = passwordEl ? passwordEl.value.trim() : "";

        if (!password) {
            if (loginError) {
                loginError.textContent = "Indtast en adgangskode.";
                loginError.classList.remove("d-none");
            }
            return;
        }

        const loginResponse = await fetch("/api/v1/admin/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ password })
        });

        if (!loginResponse.ok) {
            if (loginError) {
                loginError.textContent = "Forkert kode. Prøv igen.";
                loginError.classList.remove("d-none");
            }
            return;
        }

        storeToken(password);
        if (loginCard) loginCard.classList.add("d-none");
        if (adminContent) adminContent.classList.remove("d-none");

        await loadAdminData();
    });
}

// Refresh button
if (refreshBtn) {
    refreshBtn.addEventListener("click", loadAdminData);
}

// Logout
if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
        clearToken();
        if (adminContent) adminContent.classList.add("d-none");
        if (loginCard) loginCard.classList.remove("d-none");
        if (adminTableBody) {
            adminTableBody.innerHTML = `
          <tr><td colspan="8" class="text-center text-muted">
            Ingen data endnu. Log ind og tryk “Opdater”.
          </td></tr>`;
        }
    });
}

// Render table with escaped AI text and formatted numbers
function renderTable(data) {
    if (!adminTableBody) return;
    adminTableBody.innerHTML = "";

    if (!data || data.length === 0) {
        adminTableBody.innerHTML = `
      <tr><td colspan="8" class="text-center text-muted">
      Ingen data endnu...
      </td></tr>`;
        return;
    }

    data.forEach(item => {
        const row = document.createElement("tr");

        const created = item.createdAt
            ? new Date(item.createdAt).toLocaleString("da-DK")
            : "-";

        const fmt = (val) => (val === null || val === undefined) ? "-" :
            Number(val).toLocaleString("da-DK");

        const adviceText = escapeHtml(item.adviceText || "Intet AI-svar gemt.");

        row.innerHTML = `
      <td>${created}</td>
      <td>${fmt(item.monthlyIncome)}</td>
      <td>${fmt(item.rent)}</td>
      <td>${fmt(item.fixedCosts)}</td>
      <td>${item.savingsGoal ?? "-"}</td>
      <td>${item.studyLine ?? "-"}</td>
      <td>${item.comment ?? "-"}</td>
      <td>
        <details>
          <summary class="text-primary" style="cursor: pointer;">Se AI-svar</summary>
          <pre style="white-space: pre-wrap; margin-top: .5rem; max-width: 500px;">${adviceText}</pre>
        </details>
      </td>
    `;
        adminTableBody.appendChild(row);
    });
}
