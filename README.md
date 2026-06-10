# 🎯 Smart Matcher

**Smart Matcher** est une application web intelligente de recrutement. Elle permet d'évaluer la compatibilité entre le profil d'un candidat (CV au format PDF) et une description d'offre d'emploi/d'alternance. Le tout est propulsé par l'intelligence artificielle **Mistral AI**.

## 🚀 Comment lancer le projet

Le projet est propulsé par une base de données PostgreSQL en production et utilise automatiquement H2 (base en mémoire) pour le développement local.
Les données sont automatiquement supprimées après 24h pour des raisons de conformité RGPD.

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+

### Lancement en 1 ligne de commande
Ouvrez votre terminal à la racine du projet et tapez :
```bash
mvn clean compile spring-boot:run
```

Une fois que l'application a démarré dans le terminal, ouvrez votre navigateur à cette adresse :
👉 **http://localhost:8081/**

---

