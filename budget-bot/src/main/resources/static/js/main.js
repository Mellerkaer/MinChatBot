// javascript
const SERVER_URL = '/api/v1/';

document.getElementById('form-budget').addEventListener('submit', getBudget);
document.getElementById('resetBtn').addEventListener('click', resetForm);

const statsBox = document.getElementById('stats-box');

async function getBudget(event) {
    event.preventDefault();

    const income = parseFloat(document.getElementById('income').value || "0");
    const rent = parseFloat(document.getElementById('rent').value || "0");
    const fixed = parseFloat(document.getElementById('fixed').value || "0");
    const savingsVal = document.getElementById('savings').value;
    const savings = savingsVal ? parseFloat(savingsVal) : null;
    const studyLine = document.getElementById('studyLine').value;
    const comment = document.getElementById('comment').value;
    const ageInput = document.getElementById('age');
    const genderSelect = document.getElementById('gender');
    const age = ageInput && ageInput.value ? parseInt(ageInput.value, 10) : null;
    const gender = genderSelect && genderSelect.value ? genderSelect.value : null;

    if (!income || !rent || !fixed) {
        alert("Udfyld mindst indkomst, husleje og øvrige faste udgifter 🙏");
        return;
    }

    const spinner = document.getElementById('spinner');
    const btn = document.getElementById('submitBtn');
    const result = document.getElementById('result');

    spinner.classList.remove('d-none');
    btn.disabled = true;

    // Reset UI
    result.textContent = "AI’en tænker over dit budget...";
    if (statsBox) {
        statsBox.classList.add('d-none');
        statsBox.textContent = "";
    }

    try {
        const res = await fetch(SERVER_URL + 'budget', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                monthlyIncome: income,
                rent: rent,
                fixedCosts: fixed,
                savingsGoal: savings,
                studyLine: studyLine,
                comment: comment,
                age: age,
                gender: gender
            })
        });

        const data = await handleHttpErrors(res);

        const fullText = data.answer || "";
        // Robust split using markers "1)", "2)", "3)"
        let part1 = fullText, part2 = "", part3 = "";
        const idx2 = fullText.indexOf('2)');
        if (idx2 >= 0) {
            part1 = fullText.slice(0, idx2);
            const idx3 = fullText.indexOf('3)', idx2);
            if (idx3 >= 0) {
                part2 = fullText.slice(idx2, idx3);
                part3 = fullText.slice(idx3);
            } else {
                part2 = fullText.slice(idx2);
            }
        }

        // Remove leading numeric markers if present
        const clean = (s) => s.replace(/^(\s*\d+\)|\s*1\)|\s*2\)|\s*3\))/g, '').trim();

        document.getElementById("ai-part-1").textContent = clean(part1 || "");
        document.getElementById("ai-part-2").textContent = clean(part2 || "");
        document.getElementById("ai-part-3").textContent = clean(part3 || "");

        document.getElementById("ai-box-1").classList.remove("d-none");
        document.getElementById("ai-box-2").classList.remove("d-none");
        document.getElementById("ai-box-3").classList.remove("d-none");

        if (statsBox) {
            if (data.statsText) {
                statsBox.textContent = data.statsText;
                statsBox.classList.remove('d-none');
            } else {
                statsBox.classList.add('d-none');
                statsBox.textContent = "";
            }
        }

        if (data.stats) {
            drawIncomeGraph(data.stats);
        }

        // Show short result note
        result.textContent = "AI har lavet et budgetforslag — se bokse ovenfor for detaljer.";

    } catch (err) {
        console.error(err);
        result.textContent = "Der skete en fejl: " + err.message;
        if (statsBox) {
            statsBox.classList.add('d-none');
            statsBox.textContent = "";
        }
    } finally {
        spinner.classList.add('d-none');
        btn.disabled = false;
    }
}

function resetForm() {
    document.getElementById('income').value = "";
    document.getElementById('rent').value = "";
    document.getElementById('fixed').value = "";
    document.getElementById('savings').value = "";
    document.getElementById('studyLine').value = "";
    document.getElementById('comment').value = "";

    const ageInput = document.getElementById('age');
    const genderSelect = document.getElementById('gender');
    if (ageInput) ageInput.value = "";
    if (genderSelect) genderSelect.value = "";

    document.getElementById('result').textContent =
        'Ingen svar endnu. Udfyld felterne og tryk "Få budget-tips".';

    if (statsBox) {
        statsBox.classList.add('d-none');
        statsBox.textContent = "";
    }

    document.getElementById("ai-box-1").classList.add("d-none");
    document.getElementById("ai-box-2").classList.add("d-none");
    document.getElementById("ai-box-3").classList.add("d-none");
}

async function handleHttpErrors(res) {
    if (!res.ok) {
        let msg = "Ukendt fejl";
        try {
            const errorResponse = await res.json();
            msg = errorResponse.message ? errorResponse.message : msg;
        } catch (_) {
            // ignore
        }
        throw new Error(msg);
    }
    return res.json();
}

function drawIncomeGraph(stats) {
    const wrapper = document.getElementById('income-graph-wrapper');
    const marker = document.getElementById('income-marker');
    const medianLabel = document.getElementById('median-label');
    const userLabel = document.getElementById('user-label');

    if (!wrapper || !marker || !medianLabel || !userLabel) return;

    const user = stats.userYearlyIncome;
    const median = stats.medianYearlyIncome;

    if (!user || !median) {
        wrapper.classList.add('d-none');
        return;
    }

    const max = median * 2;
    let pos = user / max;
    if (pos < 0) pos = 0;
    if (pos > 1) pos = 1;

    marker.style.left = (pos * 100) + "%";

    medianLabel.textContent = "Median: " +
        Math.round(median).toLocaleString('da-DK') + " kr/år";
    userLabel.textContent = "Dig: " +
        Math.round(user).toLocaleString('da-DK') + " kr/år";

    wrapper.classList.remove('d-none');
}
