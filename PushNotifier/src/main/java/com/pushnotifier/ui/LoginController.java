package com.pushnotifier.ui;

import com.pushnotifier.service.DatabaseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private DatabaseService dbService;

    /**
     * Méthode appelée automatiquement après le chargement du FXML.
     */
    @FXML
    public void initialize() {
        try {
            dbService = new DatabaseService();
        } catch (Exception e) {
            lblMessage.setText("Erreur base de données.");
            e.printStackTrace();
        }
    }

    /**
     * Action sur le bouton “Connexion”.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();
        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Veuillez remplir tous les champs.");
            return;
        }
        boolean authenticated = dbService.authenticateAdmin(user, pass);
        if (authenticated) {
            // Ouvrir la fenêtre principale
            openMainWindow(event);
        } else {
            lblMessage.setText("Identifiants incorrects.");
        }
    }

    /**
     * Charge et affiche la fenêtre principale (MainView.fxml),
     * puis ferme la fenêtre de login.
     */
    private void openMainWindow(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("MainView.fxml")
            );
            Pane root = loader.load();

            // Récupérer le stage actuel à partir de n'importe quel noeud (ici txtUsername)
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Push Notification Manager");
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Impossible d’ouvrir la fenêtre principale.");
        }
    }
}
