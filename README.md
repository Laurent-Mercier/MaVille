# MaVille

MaVille est une application visant à transformer la gestion et la communication des travaux publics et privés à Montréal. Face à la multiplication des chantiers et aux perturbations fréquentes pour les résidents, il est crucial de centraliser les informations pour améliorer la mobilité urbaine. Actuellement, le manque de coordination engendre des frustrations liées aux embouteillages et aux détours prolongés. MaVille répond à ces défis en offrant une plateforme en temps réel qui informe les citoyens sur les chantiers en cours et à venir, et propose des alertes personnalisées. En facilitant la communication entre la Ville, les promoteurs et les citoyens, MaVille permet une meilleure planification des déplacements, réduit les désagréments, et contribue à une circulation plus fluide pour tous.

## Fonctionnalités

	• Créer un compte (résident et intervenant)
	• Se connecter/déconnecter (résident et intervenant)
	• Modifier ses préférences horaires (résident)
	• Voir ses notifications (résident)
	• Consulter les travaux en cours (résident)
	• Consulter les travaux à venir (3 prochains mois) (résident)
	• Chercher ou filtrer les travaux par quartier (résident)
	• Consulter les entraves engendrées par un travail (résident)
	• Chercher ou filtrer les entraves par rue (résident)
	• Soumettre une requête de travail (résident)
	• Faire le suivi d’une requête de travail (résident)
	• Consulter les requêtes de travail (intervenant)
	• Soumettre/Soustraire sa candidature (intervenant)
	• Faire le suivi de sa candidature (intervenant)
	• Soumettre un projet (intervenant)
	• Modifier le statut d’un projet (intervenant)
 
## Fonctionnement de l'application
- Pour lancer l'application, il suffit d'entrer la commande ```java -jar MaVille.jar``` dans un terminal travaillant dans le répertoire ```application```.
- Pour lancer l'application avec Docker il faut: ```docker build -t maville .``` et ensuite ```docker run -it -p 3000:3000 maville``` dans un terminal.
- Il est possible de créer un compte et s'y connecter ensuite.
- Pour se connecter comme intervenant, il suffit d'entrer ```regis.labeaume@quebec.qc.ca``` quand le courriel est demandé. Vous pouvez entrer ```QuebecL0VE``` pour le mot de passe.
- Similairement, vous pouvez entrer ```dromadaire.chameau@umontreal.ca``` comme courriel pour vous connecter en tant que résident avec ```FFFF9999``` comme mot de passe.
- Il y a d'autres utilisateurs déjà définis dans les fichiers ```application/src/main/resources/residents.csv``` et ```application/src/main/resources/intervenants.csv```, vous pouvez également les utiliser.
- La version de java utilisé est 17 et la version de Maven 4.0.0
  
## Tests

- Pour lancer les tests, il suffit d'aller dans le répertoire ```application/maville``` et lancer la commande ```mvn test```.
- le rapport se trouve [ici](application/maville/target/site/jacoco).
- les tests sont éffectués à chaque push et chaque pull request par une github action. Il n'est pas possible de merge dans le main si les tests ne passent pas.
  
## Documentation

- La documentation javadoc se retrouve [ici](application/maville/javadoc)

## Organisation des fichiers du répéertoire
```
.
├── main
│   ├── java
│   │   └── ca
│   │       └── umontreal
│   │           ├── Main.java
│   │           ├── menu
│   │           │   ├── Menu.java
│   │           │   ├── MenuConnexion.java
│   │           │   ├── MenuConsulterEntraves.java
│   │           │   ├── MenuConsulterRequetes.java
│   │           │   ├── MenuConsulterTravaux.java
│   │           │   ├── MenuCreerUnCompte.java
│   │           │   ├── MenuExit.java
│   │           │   ├── MenuNotification.java
│   │           │   ├── MenuPreferencesTravaux.java
│   │           │   ├── MenuPrincipal.java
│   │           │   ├── MenuSeConnecter.java
│   │           │   ├── MenuSoumettreProjet.java
│   │           │   └── MenuSoumettreRequete.java
│   │           ├── restApi
│   │           │   ├── RestApi.java
│   │           │   ├── controllers
│   │           │   │   ├── CityController.java
│   │           │   │   ├── RequestController.java
│   │           │   │   └── UserController.java
│   │           │   └── csvManager
│   │           │       └── CsvManager.java
│   │           └── user
│   │               └── User.java
│   └── resources
│       ├── candidatures.csv
│       ├── codesPostaux.csv
│       ├── intervenants.csv
│       ├── notifications.csv
│       ├── projets.csv
│       ├── requetes.csv
│       └── residents.csv
├── test
│   └── java
│       └── ca
│           └── umontreal
│               ├── TestClass.java
│               └── TestCsvManager.java
```

