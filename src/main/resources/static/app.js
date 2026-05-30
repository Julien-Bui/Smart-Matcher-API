document.addEventListener('DOMContentLoaded', () => {
    const cvUpload = document.getElementById('cv-upload');
    const dropZone = document.getElementById('drop-zone');
    const fileNameDisplay = document.getElementById('file-name');
    const jobDescription = document.getElementById('job-description');
    const analyzeBtn = document.getElementById('analyze-btn');

    const resultsContainer = document.getElementById('results-container');
    const scoreCircle = document.getElementById('score-circle');
    const scoreText = document.getElementById('score-text');
    const prosList = document.getElementById('pros-list');
    const consList = document.getElementById('cons-list');
    const summaryText = document.getElementById('summary-text');
    const resetBtn = document.getElementById('reset-btn');

    const btnText = analyzeBtn.querySelector('.btn-text');
    const loader = analyzeBtn.querySelector('.loader');

    let selectedFile = null;
    const USAGE_LIMIT = 3;
    const TIME_LIMIT_MS = 15 * 60 * 1000;

    let currentUsage = parseInt(localStorage.getItem('smart_matcher_usage')) || 0;
    let usageTimestamp = parseInt(localStorage.getItem('smart_matcher_usage_timestamp')) || 0;

    const usageLimitMsg = document.getElementById('usage-limit-msg');

    function checkInputs() {
        // Reset if we passed the time limit OR if state is invalid (usage > 0 but no timestamp)
        if (currentUsage > 0 && (usageTimestamp === 0 || Date.now() - usageTimestamp > TIME_LIMIT_MS)) {
            currentUsage = 0;
            usageTimestamp = 0;
            localStorage.setItem('smart_matcher_usage', 0);
            localStorage.setItem('smart_matcher_usage_timestamp', 0);
        }

        if (currentUsage >= USAGE_LIMIT) {
            const minutesLeft = Math.ceil((TIME_LIMIT_MS - (Date.now() - usageTimestamp)) / 60000);
            analyzeBtn.disabled = true;
            if (usageLimitMsg) {
                usageLimitMsg.classList.remove('hidden');
                // Ensure we don't display negative values just in case
                usageLimitMsg.textContent = `Limite atteinte. Veuillez patienter ${minutesLeft > 0 ? minutesLeft : 1} minute(s).`;
                usageLimitMsg.style.color = '#ef4444';
            }
            return;
        }

        if (usageLimitMsg) {
            usageLimitMsg.classList.remove('hidden');
            usageLimitMsg.textContent = `Analyses restantes : ${USAGE_LIMIT - currentUsage}/${USAGE_LIMIT} (se renouvelle toutes les 15min)`;
            usageLimitMsg.style.color = '#6b7280';
        }

        if (selectedFile && jobDescription.value.trim() !== '') {
            analyzeBtn.disabled = false;
        } else {
            analyzeBtn.disabled = true;
        }
    }

    cvUpload.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            const file = e.target.files[0];
            if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
                selectedFile = file;
                fileNameDisplay.textContent = `📝 ${selectedFile.name}`;
                fileNameDisplay.style.color = '#10b981';
            } else {
                selectedFile = null;
                fileNameDisplay.textContent = '❌ Fichier PDF uniquement';
                fileNameDisplay.style.color = '#ef4444';
                cvUpload.value = '';
            }
        } else {
            selectedFile = null;
            fileNameDisplay.textContent = '';
        }
        checkInputs();
    });

    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('dragover');
    });

    dropZone.addEventListener('dragleave', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');

        if (e.dataTransfer.files.length > 0) {
            const file = e.dataTransfer.files[0];
            if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
                selectedFile = file;
                cvUpload.files = e.dataTransfer.files;
                fileNameDisplay.textContent = `📝 ${selectedFile.name}`;
                fileNameDisplay.style.color = '#10b981';
            } else {
                fileNameDisplay.textContent = '❌ Fichier PDF uniquement';
                fileNameDisplay.style.color = '#ef4444';
                selectedFile = null;
            }
            checkInputs();
        }
    });

    jobDescription.addEventListener('input', checkInputs);

    analyzeBtn.addEventListener('click', async () => {
        checkInputs();
        if (currentUsage >= USAGE_LIMIT) return;
        if (!selectedFile || jobDescription.value.trim() === '') return;

        if (currentUsage === 0) {
            usageTimestamp = Date.now();
            localStorage.setItem('smart_matcher_usage_timestamp', usageTimestamp);
        }
        currentUsage++;
        localStorage.setItem('smart_matcher_usage', currentUsage);
        checkInputs();

        analyzeBtn.disabled = true;
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        resultsContainer.classList.add('hidden');

        const formData = new FormData();
        formData.append('cv', selectedFile);
        formData.append('description', jobDescription.value.trim());

        // Détermine l'URL de l'API selon qu'on est en local ou en production
        let apiUrl = '/api/match';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/match';
        }

        try {
            const response = await fetch(apiUrl, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }

            const data = await response.json();
            displayResults(data);
        } catch (error) {
            console.error('Error during analysis:', error);
            alert(error.message);
        } finally {
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
            checkInputs();
        }
    });

    function displayResults(data) {
        const score = data.score;
        let strokeColor = '#ef4444';
        if (score >= 75) strokeColor = '#10b981';
        else if (score >= 50) strokeColor = '#f59e0b';

        scoreCircle.style.stroke = strokeColor;
        scoreCircle.style.strokeDasharray = `${score}, 100`;
        scoreText.textContent = `${score}%`;

        prosList.innerHTML = '';
        if (data.matchedSkills && data.matchedSkills.trim().length > 0) {
            const skills = data.matchedSkills.split(/[\n,]/).filter(s => s.trim().length > 0);
            skills.forEach(pro => {
                const li = document.createElement('li');
                li.textContent = pro.trim().replace(/^[-*•]\s*/, '');
                prosList.appendChild(li);
            });
        } else {
            prosList.innerHTML = '<li>Aucun point fort spécifique trouvé.</li>';
        }

        consList.innerHTML = '';
        if (data.missingSkills && data.missingSkills.trim().length > 0) {
            const skills = data.missingSkills.split(/[\n,]/).filter(s => s.trim().length > 0);
            skills.forEach(con => {
                const li = document.createElement('li');
                li.textContent = con.trim().replace(/^[-*•]\s*/, '');
                consList.appendChild(li);
            });
        } else {
            consList.innerHTML = '<li>Aucune compétence manquante spécifique.</li>';
        }

        summaryText.textContent = data.summary || 'Aucune synthèse fournie.';

        resultsContainer.classList.remove('hidden');
        resultsContainer.scrollIntoView({ behavior: 'smooth' });
    }

    resetBtn.addEventListener('click', () => {
        selectedFile = null;
        cvUpload.value = '';
        fileNameDisplay.textContent = '';
        jobDescription.value = '';
        resultsContainer.classList.add('hidden');
        checkInputs();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    checkInputs();
});
