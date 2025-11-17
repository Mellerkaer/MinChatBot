const SERVER_URL = 'http://localhost:8080/api/v1/';

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

    // Nulstil UI
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

        result.textContent = data.answer || "Ingen tekst modtaget fra AI.";
        // Skjul den gamle result-tekst
        result.textContent = "";

        // Split AI-svaret i tre dele
        const text = data.answer || "";
        const part1 = text.split("2)")[0].replace("1)", "").trim();
        const part2 = text.split("2)")[1]?.split("3)")[0].trim() || "";
        const part3 = text.split("3)")[1]?.trim() || "";

        // Fyld boksene
        document.getElementById("ai-part-1").textContent = part1;
        document.getElementById("ai-part-2").textContent = part2;
        document.getElementById("ai-part-3").textContent = part3;

        // Vis boksene
        document.getElementById("ai-box-1").classList.remove("d-none");
        document.getElementById("ai-box-2").classList.remove("d-none");
        document.getElementById("ai-box-3").classList.remove("d-none");

        // tekst til statistik-boksen
        if (statsBox) {
            if (data.statsText) {
                statsBox.textContent = data.statsText;
                statsBox.classList.remove('d-none');
            } else {
                statsBox.classList.add('d-none');
                statsBox.textContent = "";
            }
        }

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