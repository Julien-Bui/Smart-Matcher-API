<div align="center">
  <h1>🎯 Smart Matcher API</h1>
  <p><strong>Un moteur de recrutement propulsé par l'IA (Mistral AI) et l'API Adzuna.</strong></p>

  [![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://java.com)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Security: Hardened](https://img.shields.io/badge/Security-Hardened-blue.svg)](#-sécurité--hardening)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
</div>

<br/>

**Smart Matcher** est une application web intelligente de recrutement conçue pour les candidats et les recruteurs. Elle évalue la compatibilité entre un CV (PDF) et une offre d'emploi, recherche des offres ciblées en temps réel, et génère des lettres de motivation personnalisées.

## ✨ Fonctionnalités Principales

- 🧠 **Analyse de CV (IA)** : Extraction intelligente des compétences clés via l'API **Mistral AI** (modèle *open-mixtral-8x7b*).
- 🎯 **Matching Intelligent** : Comparaison automatisée et scoring de pertinence entre votre profil et une offre d'emploi ciblée.
- 🔍 **Agrégation d'Offres d'Emploi** : Recherche d'offres en direct via l'**API Adzuna**, basée sur les compétences extraites du CV.
- 📝 **Génération de Lettre de Motivation** : Rédaction assistée par l'IA et exportation au format PDF.
- 🛡️ **Sécurité Renforcée** : Protection contre l'injection de prompt (LLM Zéro-Day), vérification des *Magic Bytes* pour les uploads PDF, limiteurs de débit (Rate Limiting) stricts.
- 🧹 **RGPD Compliance** : Les données temporaires sont purgées automatiquement (CRON toutes les 24h).

## 🛠️ Stack Technique

- **Backend** : Java 17, Spring Boot 3.2.4
- **Base de Données** : PostgreSQL (Production via Railway), H2 (Local/Dev)
- **IA & NLP** : Spring AI, Mistral AI API
- **Extraction de texte** : Apache Tika
- **Génération PDF** : OpenPDF
- **Frontend** : HTML5, CSS Vanilla (Design System Custom), JavaScript Vanilla
- **Déploiement** : Railway

---

## 🚀 Guide de Démarrage (Local)

### 1. Prérequis
- Java 17 ou supérieur
- Maven 3.6+
- Une clé API [Mistral AI](https://mistral.ai/)
- Des clés API [Adzuna](https://developer.adzuna.com/) (App ID & App Key)

### 2. Installation
Clonez le dépôt et naviguez dans le dossier du projet :
```bash
git clone https://github.com/votre-nom/Smart-Matcher-API.git
cd Smart-Matcher-API
```

### 3. Configuration des Variables d'Environnement
Copiez le fichier de propriétés d'exemple (qui ne contient pas de données sensibles) :
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
*Le fichier `application.properties` est ignoré par Git pour des raisons de sécurité.*

Ouvrez `src/main/resources/application.properties` et renseignez vos clés API :
```properties
spring.ai.mistralai.api-key=votre_cle_mistral_ici
adzuna.app.id=votre_app_id_adzuna
adzuna.app.key=votre_app_key_adzuna
```

### 4. Lancement
Démarrez l'application via le plugin Spring Boot Maven :
```bash
mvn clean compile spring-boot:run
```

L'application sera accessible sur : **http://localhost:8081/**

---

## 🔒 Sécurité & Hardening

Ce projet implémente des mesures strictes pour contrer les vulnérabilités du **OWASP Top 10** et **OWASP Top 10 LLM** :

- **Prévention Prompt Injection (LLM01)** : Les inputs utilisateurs (ex: texte du CV) envoyés au LLM sont encapsulés par des délimiteurs de type UUID générés aléatoirement, garantissant la sanitarisation du contexte IA.
- **Vérification d'Uploads Stricte** : Les fichiers PDF soumis sont validés au niveau binaire (*Magic Bytes* `0x25 0x50 0x44 0x46`) pour empêcher l'upload de scripts malveillants masqués.
- **Rate Limiting Granulaire** : Un système basé sur `Bucket4j` est implémenté pour limiter la fréquence de requêtes par IP et par type d'action afin de contrer le DDoS et l'abus de tokens IA.
- **Security Headers & CSP** : Implémentation d'un `SecurityFilter` qui injecte HSTS, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, et une Content-Security-Policy (CSP) stricte.

## 🤝 Contribution

Les contributions (Pull Requests, Issues) sont les bienvenues ! Pour toute contribution majeure, merci d'ouvrir une *issue* au préalable afin de discuter des changements proposés. Assurez-vous que vos modifications respectent les standards de sécurité et passent la compilation.

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
