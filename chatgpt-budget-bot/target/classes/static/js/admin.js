const loginForm      = document.getElementById("admin-login-form");
const loginCard      = document.getElementById("login-card");
const loginError     = document.getElementById("login-error");
const adminContent   = document.getElementById("admin-content");
const refreshBtn     = document.getElementById("refreshBtn");
const adminSpinner   = document.getElementById("admin-spinner");
const adminTableBody = document.getElementById("admin-table-body");

const ADMIN_TOKEN_KEY = "budget_admin_token";

// Gem / hent token
function storeToken(token) {
    localStorage.setItem(ADMIN_TOKEN_KEY, token);
}
function getStoredToken() {
    return localStorage.getItem(ADMIN_TOKEN_KEY);
}

// Kald backend med token
async function tryFetchWithToken(token) {
    const response = await fetch("/api/v1/admin/budget", {
        headers: { "X-Admin-Token": token }
    });

    if (response.status === 401) throw new Error("Unauthorized");
    return await response.json();
}

// Escape AI-tekst
function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

// Hent data
async function loadAdminData() {
    const token = getStoredToken();
    if (!token) return;

    adminSpinner.classList.remove("d-none");

    try {
        const data = await tryFetchWithToken(token);
        renderTable(data);
    } catch (err) {
        console.error(err);
        adminTableBody.innerHTML = `
      <tr><td colspan="8" class="text-center text-danger">
      Kunne ikke hente data. Prøv igen.
      </td></tr>`;
    } finally {
        adminSpinner.classList.add("d-none");
    }
}

// Login handling
loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    loginError.style.display = "none";

    const password = document.getElementById("adminPassword").value.trim();
    if (!password) {
        loginError.textContent = "Indtast en adgangskode.";
        loginError.style.display = "block";
        return;
    }

    const loginResponse = await fetch("/api/v1/admin/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password })
    });

    if (loginResponse.status === 401) {
        loginError.textContent = "Forkert kode. Prøv igen.";
        loginError.style.display = "block";
        return;
    }

    storeToken(password);
    loginCard.classList.add("d-none");
    adminContent.classList.remove("d-none");

    await loadAdminData();
});

// Opdater knap
refreshBtn.addEventListener("click", loadAdminData);

// Tabel med fold-ud AI-svar
function renderTable(data) {
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

        const adviceText = escapeHtml(item.adviceText || "Intet AI-svar gemt.");

        row.innerHTML = `
      <td>${created}</td>
      <td>${item.monthlyIncome}</td>
      <td>${item.rent}</td>
      <td>${item.fixedCosts}</td>
      <td>${item.savingsGoal ?? "-"}</td>
      <td>${item.studyLine ?? "-"}</td>
      <td>${item.comment ?? "-"}</td>
      <td>
        <details>
          <summary class="text-primary" style="cursor: pointer;">Se AI-svar</summary>
          <pre style="white-space: pre-wrap; margin-top: .5rem; max-width: 500px;">
${adviceText}
          </pre>
        </details>
      </td>
    `;

        adminTableBody.appendChild(row);
    });
}