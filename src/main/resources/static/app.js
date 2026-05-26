document.addEventListener('DOMContentLoaded', () => {
    const cvUpload = document.getElementById('cv-upload');
    const dropZone = document.getElementById('drop-zone');
    const fileNameDisplay = document.getElementById('file-name');
    const jobDescription = document.getElementById('job-description');
    const analyzeBtn = document.getElementById('analyze-btn');
    
    // Results elements
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

    // Check Button State
    function checkInputs() {
        if (selectedFile && jobDescription.value.trim() !== '') {
            analyzeBtn.disabled = false;
        } else {
            analyzeBtn.disabled = true;
        }
    }

    // File Input Handle
    cvUpload.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            const file = e.target.files[0];
            if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
                selectedFile = file;
                fileNameDisplay.textContent = `📝 ${selectedFile.name}`;
                fileNameDisplay.style.color = '#10b981'; // Success color
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

    // Drag and Drop
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
                cvUpload.files = e.dataTransfer.files; // Sync to input
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

    // Call API
    analyzeBtn.addEventListener('click', async () => {
        if (!selectedFile || jobDescription.value.trim() === '') return;

        // UI Loading state
        analyzeBtn.disabled = true;
        btnText.classList.add('hidden');
        loader.classList.remove('hidden');
        resultsContainer.classList.add('hidden');

        // Form Data
        const formData = new FormData();
        formData.append('cv', selectedFile);
        formData.append('description', jobDescription.value.trim());

        try {
            const response = await fetch('/api/match', {
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
            // Restore button
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
            analyzeBtn.disabled = false;
        }
    });

    function displayResults(data) {
        // Calculate stroke color based on score
        const score = data.score;
        let strokeColor = '#ef4444'; // Red
        if (score >= 75) strokeColor = '#10b981'; // Green
        else if (score >= 50) strokeColor = '#f59e0b'; // Yellow

        // Animate score
        scoreCircle.style.stroke = strokeColor;
        scoreCircle.style.strokeDasharray = `${score}, 100`;
        scoreText.textContent = `${score}%`;

        // Populate Lists (Splitting comma or newline strings)
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

        // Summary
        summaryText.textContent = data.summary || 'Aucune synthèse fournie.';

        // Show results
        resultsContainer.classList.remove('hidden');
        // Scroll to results
        resultsContainer.scrollIntoView({ behavior: 'smooth' });
    }

    // Reset 
    resetBtn.addEventListener('click', () => {
        selectedFile = null;
        cvUpload.value = '';
        fileNameDisplay.textContent = '';
        jobDescription.value = '';
        resultsContainer.classList.add('hidden');
        checkInputs();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
});
