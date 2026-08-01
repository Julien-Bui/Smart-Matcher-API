document.addEventListener('DOMContentLoaded', () => {
    const cvUpload = document.getElementById('cv-upload');
    const dropZone = document.getElementById('drop-zone');
    const fileNameDisplay = document.getElementById('file-name');
    const extractCvBtn = document.getElementById('extract-cv-btn');
    
    const step1Section = document.getElementById('step-1-cv');
    const step2Section = document.getElementById('step-2-choices');
    
    const jobDescription = document.getElementById('job-description');
    const analyzeBtn = document.getElementById('analyze-btn');
    const searchApplyBtn = document.getElementById('search-apply-btn');

    const resultsContainer = document.getElementById('results-container');
    const scoreCircle = document.getElementById('score-circle');
    const scoreText = document.getElementById('score-text');
    const prosList = document.getElementById('pros-list');
    const consList = document.getElementById('cons-list');
    const summaryText = document.getElementById('summary-text');
    const resetBtn = document.getElementById('reset-btn');

    const usageLimitMsg = document.getElementById('usage-limit-msg');

    const offersContainer = document.getElementById('offers-container');
    const offersList = document.getElementById('offers-list');
    const backToChoicesBtn = document.getElementById('back-to-choices-btn');
    const refreshOffersBtn = document.getElementById('refresh-offers-btn');

    const coverLetterModal = document.getElementById('cover-letter-modal');
    const coverLetterText = document.getElementById('cover-letter-text');
    const closeModalBtn = document.getElementById('close-modal-btn');
    const downloadPdfBtn = document.getElementById('download-pdf-btn');

    const jobLocationInput = document.getElementById('job-location');
    const jobKeywordsInput = document.getElementById('job-keywords');
    const contractTypeSelect = document.getElementById('contract-type');

    let selectedFile = null;
    let cvExtracted = false;
    let currentCompanyName = "Entreprise";
    let currentPage = 1;
    
    // Limite remise en place pour la production
    const USAGE_LIMIT = 3;
    const TIME_LIMIT_MS = 5 * 60 * 1000;

    let currentUsage = parseInt(localStorage.getItem('smart_matcher_usage')) || 0;
    let usageTimestamp = parseInt(localStorage.getItem('smart_matcher_usage_timestamp')) || 0;

    function checkInputs() {
        if (currentUsage > 0 && (usageTimestamp === 0 || Date.now() - usageTimestamp > TIME_LIMIT_MS)) {
            currentUsage = 0;
            usageTimestamp = 0;
            localStorage.setItem('smart_matcher_usage', 0);
            localStorage.setItem('smart_matcher_usage_timestamp', 0);
        }

        if (currentUsage >= USAGE_LIMIT) {
            const minutesLeft = Math.ceil((TIME_LIMIT_MS - (Date.now() - usageTimestamp)) / 60000);
            analyzeBtn.disabled = true;
            searchApplyBtn.disabled = true;
            if (usageLimitMsg) {
                usageLimitMsg.classList.remove('hidden');
                usageLimitMsg.textContent = `Limite atteinte. Veuillez patienter ${minutesLeft > 0 ? minutesLeft : 1} minute(s).`;
                usageLimitMsg.style.color = 'var(--danger)';
            }
            return;
        }

        if (usageLimitMsg) {
            usageLimitMsg.classList.remove('hidden');
            usageLimitMsg.textContent = `Analyses restantes : ${USAGE_LIMIT - currentUsage}/${USAGE_LIMIT} (se renouvelle toutes les 5min)`;
            usageLimitMsg.style.color = 'var(--text-muted)';
        }

        if (cvExtracted) {
            searchApplyBtn.disabled = false;
            if (jobDescription.value.trim() !== '') {
                analyzeBtn.disabled = false;
            } else {
                analyzeBtn.disabled = true;
            }
        }
    }

    function handleFileSelection() {
        const extractionLoader = document.getElementById('extraction-loader');
        
        // Cacher les autres éléments pour mettre en avant le chargement
        document.querySelector('.upload-title.desktop-only').classList.add('hidden');
        document.querySelector('.or-text.desktop-only').classList.add('hidden');
        document.querySelector('.upload-btn').classList.add('hidden');
        
        extractionLoader.classList.remove('hidden');
        
        setTimeout(() => {
            extractionLoader.classList.add('hidden');
            
            // Remettre les classes pour le prochain reset
            document.querySelector('.upload-title.desktop-only').classList.remove('hidden');
            document.querySelector('.or-text.desktop-only').classList.remove('hidden');
            document.querySelector('.upload-btn').classList.remove('hidden');
            
            cvExtracted = true;
            step1Section.classList.add('hidden');
            step2Section.classList.remove('hidden');
            checkInputs();
        }, 1200);
    }

    cvUpload.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            const file = e.target.files[0];
            if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
                selectedFile = file;
                fileNameDisplay.textContent = `📝 ${selectedFile.name}`;
                fileNameDisplay.className = 'file-name success';
                handleFileSelection();
            } else {
                selectedFile = null;
                fileNameDisplay.textContent = '❌ Fichier PDF uniquement';
                fileNameDisplay.className = 'file-name error';
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
                fileNameDisplay.className = 'file-name success';
                handleFileSelection();
            } else {
                fileNameDisplay.textContent = '❌ Fichier PDF uniquement';
                fileNameDisplay.className = 'file-name error';
                selectedFile = null;
            }
            checkInputs();
        }
    });

    jobDescription.addEventListener('input', checkInputs);

    function incrementUsage() {
        if (currentUsage === 0) {
            usageTimestamp = Date.now();
            localStorage.setItem('smart_matcher_usage_timestamp', usageTimestamp);
        }
        currentUsage++;
        localStorage.setItem('smart_matcher_usage', currentUsage);
    }

    analyzeBtn.addEventListener('click', async () => {
        checkInputs();
        if (currentUsage >= USAGE_LIMIT) return;
        if (!selectedFile || jobDescription.value.trim() === '') return;

        incrementUsage();
        checkInputs();

        analyzeBtn.disabled = true;
        searchApplyBtn.disabled = true;
        const btnText = analyzeBtn.querySelector('.btn-text');
        const loader = analyzeBtn.querySelector('.loader');
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        resultsContainer.classList.add('hidden');

        const formData = new FormData();
        formData.append('cv', selectedFile);
        formData.append('description', jobDescription.value.trim());

        let apiUrl = '/api/match';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/match';
        }

        try {
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
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

    searchApplyBtn.addEventListener('click', async () => {
        checkInputs();
        if (currentUsage >= USAGE_LIMIT) return;
        if (!selectedFile) return;

        incrementUsage();
        checkInputs();

        analyzeBtn.disabled = true;
        searchApplyBtn.disabled = true;
        const saBtnText = searchApplyBtn.querySelector('.btn-text');
        const saLoader = searchApplyBtn.querySelector('.loader');
        saBtnText.classList.add('hidden');
        saLoader.classList.remove('hidden');

        const formData = new FormData();
        formData.append('cv', selectedFile);
        
        const keywordsVal = jobKeywordsInput ? jobKeywordsInput.value.trim() : '';
        const locationVal = jobLocationInput ? jobLocationInput.value.trim() : '';
        const contractVal = contractTypeSelect ? contractTypeSelect.value : 'Tous';
        
        if (keywordsVal !== '') {
            formData.append('keywords', keywordsVal);
        }
        if (locationVal !== '') {
            formData.append('location', locationVal);
        }
        if (contractVal !== 'Tous') {
            formData.append('contractType', contractVal);
        }
        formData.append('page', currentPage);

        let apiUrl = '/api/extension/search-jobs';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/extension/search-jobs';
        }

        try {
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }

            const offers = await response.json();
            displayOffers(offers);
            
            step2Section.classList.add('hidden');
            offersContainer.classList.remove('hidden');

        } catch (error) {
            console.error('Error during job search:', error);
            alert("Erreur lors de la recherche d'offres : " + error.message);
        } finally {
            saBtnText.classList.remove('hidden');
            saLoader.classList.add('hidden');
            checkInputs();
        }
    });

    function displayOffers(offers) {
        offersList.innerHTML = '';
        offers.forEach(offer => {
            const card = document.createElement('div');
            card.className = 'offer-card';

            const title = document.createElement('h3');
            title.textContent = offer.title;
            
            const company = document.createElement('p');
            company.textContent = offer.company || 'Entreprise inconnue';
            company.className = 'offer-company';
            
            const sourceUrl = document.createElement('a');
            sourceUrl.href = offer.url;
            sourceUrl.target = '_blank';
            sourceUrl.textContent = "Voir l'annonce originale (" + offer.source + ")";
            sourceUrl.className = 'offer-source';
            
            const desc = document.createElement('p');
            desc.textContent = (offer.description.length > 200) ? offer.description.substring(0, 200) + '...' : offer.description;
            desc.className = 'offer-desc';

            const generateBtn = document.createElement('button');
            generateBtn.className = 'btn btn-primary btn-sm mt-4';
            generateBtn.style.alignSelf = 'flex-start';
            generateBtn.innerHTML = '<span class="btn-text">Générer Lettre de Motivation</span><span class="loader hidden"></span>';
            
            generateBtn.addEventListener('click', () => generateCoverLetter(offer, generateBtn));

            card.appendChild(title);
            card.appendChild(company);
            card.appendChild(sourceUrl);
            card.appendChild(desc);
            card.appendChild(generateBtn);
            
            offersList.appendChild(card);
        });
    }

    async function generateCoverLetter(offer, btnElement) {
        const btnText = btnElement.querySelector('.btn-text');
        const loader = btnElement.querySelector('.loader');
        
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        btnElement.disabled = true;

        const formData = new FormData();
        formData.append('cv', selectedFile);
        formData.append('jobDescription', offer.description);

        let apiUrl = '/api/extension/generate-cover-letter';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/extension/generate-cover-letter';
        }

        try {
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            const data = await response.json();
            
            coverLetterText.value = data.coverLetter;
            currentCompanyName = offer.company || 'Entreprise';
            coverLetterModal.classList.remove('hidden');
            
        } catch (error) {
            console.error('Error generating letter:', error);
            alert("Erreur lors de la génération de la lettre : " + error.message);
        } finally {
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
            btnElement.disabled = false;
        }
    }

    closeModalBtn.addEventListener('click', () => {
        coverLetterModal.classList.add('hidden');
    });

    downloadPdfBtn.addEventListener('click', async () => {
        const btnText = downloadPdfBtn.querySelector('.btn-text');
        const loader = downloadPdfBtn.querySelector('.loader');
        
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        downloadPdfBtn.disabled = true;

        const formData = new FormData();
        formData.append('coverLetterText', coverLetterText.value);
        formData.append('companyName', currentCompanyName);

        let apiUrl = '/api/extension/download-pdf';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/extension/download-pdf';
        }

        try {
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            const safeCompany = currentCompanyName.replace(/\s+/g, '_');
            a.download = `Lettre_Motivation_${safeCompany}.pdf`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
            
            // Optionally close the modal after download
            // coverLetterModal.classList.add('hidden');
            
        } catch (error) {
            console.error('Error downloading PDF:', error);
            alert("Erreur lors du téléchargement : " + error.message);
        } finally {
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
            downloadPdfBtn.disabled = false;
        }
    });

    backToChoicesBtn.addEventListener('click', () => {
        offersContainer.classList.add('hidden');
        step2Section.classList.remove('hidden');
        currentPage = 1;
    });

    refreshOffersBtn.addEventListener('click', async () => {
        currentPage++;
        const btnText = refreshOffersBtn.querySelector('.btn-text');
        const loader = refreshOffersBtn.querySelector('.loader');
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        refreshOffersBtn.disabled = true;

        const formData = new FormData();
        formData.append('cv', selectedFile);
        const keywordsVal = jobKeywordsInput ? jobKeywordsInput.value.trim() : '';
        const locationVal = jobLocationInput ? jobLocationInput.value.trim() : '';
        const contractVal = contractTypeSelect ? contractTypeSelect.value : 'Tous';
        if (keywordsVal !== '') formData.append('keywords', keywordsVal);
        if (locationVal !== '') formData.append('location', locationVal);
        if (contractVal !== 'Tous') formData.append('contractType', contractVal);
        formData.append('page', currentPage);

        let apiUrl = '/api/extension/search-jobs';
        if (window.location.hostname === 'julienbui.dev' || window.location.hostname === 'www.julienbui.dev') {
            apiUrl = 'https://smart-matcher-api-production.up.railway.app/api/extension/search-jobs';
        }

        try {
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || response.statusText);
            }
            const offers = await response.json();
            displayOffers(offers);
            offersList.scrollIntoView({ behavior: 'smooth' });
        } catch (error) {
            alert("Plus d'offres disponibles pour cette recherche.");
            currentPage--;
        } finally {
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
            refreshOffersBtn.disabled = false;
        }
    });

    function displayResults(data) {
        const score = data.score;
        let strokeColor = 'var(--danger-text)';
        if (score >= 75) strokeColor = 'var(--success-text)';
        else if (score >= 50) strokeColor = '#fbbf24';

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
        cvExtracted = false;
        cvUpload.value = '';
        fileNameDisplay.textContent = '';
        jobDescription.value = '';
        resultsContainer.classList.add('hidden');
        offersContainer.classList.add('hidden');
        step1Section.classList.remove('hidden');
        step2Section.classList.add('hidden');
        checkInputs();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    const legalModal = document.getElementById('legal-modal');
    const openLegalBtn = document.getElementById('open-legal-btn');
    const closeLegalBtn = document.getElementById('close-legal-btn');

    if (openLegalBtn && legalModal && closeLegalBtn) {
        openLegalBtn.addEventListener('click', () => {
            legalModal.classList.remove('hidden');
        });
        closeLegalBtn.addEventListener('click', () => {
            legalModal.classList.add('hidden');
        });
    }

    checkInputs();
});
