# java-application-push-sytem-project


Manuel Utilisateur – Push System Notifier G20
1. Installation et Première Configuration
Ce guide vous explique comment installer et configurer l'application " Push System Notifier G20" pour votre première utilisation.
1.1 Prérequis
    • Java Runtime Environment (JRE) : Assurez-vous que Java (version 17 ou supérieure) est installé sur votre machine.
    • Fichiers de l'application : Vous devez disposer des trois fichiers suivants dans le même dossier :
        1. PushNotificationApp-1.0-SNAPSHOT-jar-with-dependencies.jar (le programme)
        2. database.db (la base de données vide)
        3. config.properties (le fichier de configuration)
1.2 Configuration Initiale (Étape Cruciale)
Avant de lancer l'application, vous devez configurer le fichier config.properties avec vos propres informations. Ouvrez ce fichier avec un éditeur de texte (Bloc-notes, VS Code, etc.).
Exemple de configuration :
# Fichier de configuration de l'application

# --- Configuration de l'API Orange SMS ---
# Remplacez par votre Header d'autorisation Base64 (ex: Basic ABC...XYZ)
orange.auth.header=Basic VOTRE_VRAI_HEADER_API_ICI

# Remplacez par votre NOM D'EXPÉDITEUR fourni par Orange
orange.sender.name=SMS 574007

# --- Configuration du service Email (via Gmail) ---
# Votre adresse email Gmail
email.username=mon.adresse@gmail.com

# Votre "Mot de passe d'application" généré depuis votre compte Google
email.password=abcdefghijklmnop
Important pour Gmail : Vous ne devez pas utiliser votre mot de passe Gmail habituel. Vous devez générer un "Mot de passe d'application" depuis les paramètres de sécurité de votre compte Google.
1.3 Lancement de l'Application
Double-cliquez sur le fichier .jar ou exécutez la commande java -jar PushNotificationApp-1.0-SNAPSHOT-jar-with-dependencies.jar dans un terminal ouvert dans le dossier de l'application.
2. Utilisation de l'Application
2.1 Connexion
    • Une fenêtre de connexion apparaît.
    • Pour la première connexion, utilisez les identifiants par défaut :
        ◦ Utilisateur : admin
        ◦ Mot de passe : password123
    • Cliquez sur "Connexion".
2.2 Interface Principale
L'interface est divisée en deux : un menu de navigation à gauche et la zone de travail à droite.
2.3 Gérer les Contacts et les Groupes
C'est la première chose à faire pour peupler votre application.
    1. Cliquez sur "Gérer les Contacts" dans le menu de gauche.
    2. Ajouter un contact : Cliquez sur Ajouter Contact, remplissez les champs et validez.
    3. Importer des contacts : Cliquez sur Importer CSV et sélectionnez un fichier CSV. Le fichier doit avoir 3 colonnes : Nom, Téléphone, Email.
    4. Créer un groupe : Cliquez sur Créer Groupe et donnez-lui un nom.
    5. Ajouter des contacts à un groupe :
        ◦ Dans la table, cochez la case "Sélection" pour tous les contacts que vous voulez ajouter.
        ◦ Cliquez sur Ajouter Sélection au Groupe.
        ◦ Choisissez le groupe de destination dans la liste déroulante et validez.
2.4 Envoyer des Notifications
    1. Cliquez sur "Envoyer SMS" ou "Envoyer Email".
    2. Choisir les destinataires :
        ◦ Cliquez sur Choisir des Contacts Individuels... pour utiliser les contacts que vous avez cochés dans l'onglet "Gérer les Contacts".
        ◦ OU cliquez sur Choisir un Groupe... pour sélectionner un groupe prédéfini.
        ◦ Le label vous confirmera votre sélection.
    3. Rédiger le message :
        ◦ Pour un Email : Remplissez le champ "Sujet" et le corps du message. Vous pouvez cliquer sur Joindre un fichier pour ajouter une pièce jointe.
        ◦ Pour un SMS : Remplissez le corps du message.
    4. Envoyer : Cliquez sur Envoyer Maintenant et confirmez l'envoi.
2.5 Consulter l'Historique
    • Cliquez sur "Historique". Le tableau affiche tous les envois tentés.
    • La colonne Statut vous indique si l'envoi a réussi ou échoué.
    • La colonne Détails vous donne la réponse brute de l'API (très utile en cas d'échec).
    • Cliquez sur Rafraîchir pour être sûr d'avoir les données les plus récentes.
2.6 Planifier un Envoi
    1. Cliquez sur "Planificateur".
    2. Cliquez sur Programmer un nouvel envoi. Une fenêtre s'ouvre.
    3. Configurez l'envoi :
        ◦ Destinataires : Choisissez si vous visez les contacts cochés ou un groupe entier.
        ◦ Type : Sélectionnez "SMS" ou "Email". Les champs s'adapteront.
        ◦ Contenu : Remplissez le sujet (si email), le message et ajoutez une pièce jointe (si email).
        ◦ Date d'envoi : Choisissez la date et l'heure exactes.
    4. Validez. Votre envoi programmé apparaît dans la liste. Le service en arrière-plan le déclenchera automatiquement à l'heure dite, même si vous naviguez sur d'autres panneaux (l'application doit rester ouverte).
    5. Annuler : Vous pouvez sélectionner une tâche "En attente" dans la liste et cliquer sur Annuler la sélection.
