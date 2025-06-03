package com.pushnotifier.ui;

import com.pushnotifier.model.HistoryEntry;
import com.pushnotifier.model.Recipient;
import com.pushnotifier.service.DatabaseService;
import com.pushnotifier.service.EmailSender;
import com.pushnotifier.service.OrangeSmsService;
import com.pushnotifier.service.SmsProvider;
import com.pushnotifier.util.CsvLoader;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    // === Composants UI Onglet “Envoyer” ===
    @FXML private TableView<Recipient> tableRecipients;
    @FXML private TableColumn<Recipient, Boolean> colSelect;
    @FXML private TableColumn<Recipient, String> colName;
    @FXML private TableColumn<Recipient, String> colEmail;
    @FXML private TableColumn<Recipient, String> colPhone;
    @FXML private TextArea txtMessage;
    @FXML private ComboBox<String> comboChannel;
    @FXML private ComboBox<String> comboSmsProvider;

    // === Composants UI Onglet “Historique” ===
    @FXML private TableView<HistoryEntry> tableHistory;
    @FXML private TableColumn<HistoryEntry, LocalDateTime> colTime;
    @FXML private TableColumn<HistoryEntry, String> colChan;
    @FXML private TableColumn<HistoryEntry, String> colRecip;
    @FXML private TableColumn<HistoryEntry, Boolean> colSuccess;
    @FXML private TableColumn<HistoryEntry, String> colError;

    // === Données en mémoire ===
    private ObservableList<Recipient> recipients = FXCollections.observableArrayList();
    private ObservableList<HistoryEntry> historyData = FXCollections.observableArrayList();

    // === Services ===
    private DatabaseService dbService;
    private EmailSender emailSender;
    private SmsProvider orangeSmsService;
    // Pour MTN, vous devriez un jour implémenter MtnSmsService de façon similaire.

    // === Paramètres à configurer (modifier ici) ===
    // 1) Chemin vers la base SQLite, réglé dans DatabaseService.
    // 2) Paramètres SMTP (pour EmailSender).
    // 3) Paramètres API Orange (pour OrangeSmsService).

    @FXML
    public void initialize() {
        // 1) Initialiser DatabaseService
        try {
            dbService = new DatabaseService();
        } catch (Exception e) {
            showAlert("Erreur BD", "Impossible de se connecter à SQLite.");
            e.printStackTrace();
            return;
        }

        // 2) Configurer TableView destinataires
        colSelect.setCellValueFactory(new PropertyValueFactory<>("selected"));
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setEditable(true);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        tableRecipients.setItems(recipients);
        tableRecipients.setEditable(true);

        // 3) Configurer TableView historique
        colTime.setCellValueFactory(new PropertyValueFactory<>("sendTime"));
        colChan.setCellValueFactory(new PropertyValueFactory<>("channel"));
        colRecip.setCellValueFactory(new PropertyValueFactory<>("recipient"));
        colSuccess.setCellValueFactory(new PropertyValueFactory<>("success"));
        colError.setCellValueFactory(new PropertyValueFactory<>("errorMsg"));
        tableHistory.setItems(historyData);

        // 4) Configurer comboChannel
        comboChannel.getItems().addAll("EMAIL", "SMS", "EMAIL+SMS");
        comboChannel.getSelectionModel().select("EMAIL");
        comboSmsProvider.getItems().addAll("OrangeCameroon", "MTNCameroon");
        comboSmsProvider.setDisable(true);

        comboChannel.setOnAction(e -> {
            String sel = comboChannel.getValue();
            // Si SMS ou EMAIL+SMS, on active le choix SMS
            boolean needSms = sel.contains("SMS");
            comboSmsProvider.setDisable(!needSms);
        });

        // 5) Initialiser EmailSender
        String smtpHost = "smtp.gmail.com";
        int smtpPort = 587;
        String smtpUser = "votre.adresse@gmail.com";       // <-- remplacez par votre e-mail
        String smtpPass = "votreAppPassword";              // <-- mot de passe d’appli ou équivalent
        emailSender = new EmailSender(smtpHost, smtpPort, smtpUser, smtpPass, smtpUser);

        // 6) Initialiser OrangeSmsService (API “Messagerie Pro”)
        //    Vous avez reçu (endpointURL, username, password) de la part d’Orange.
        String orangeEndpoint = "https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B237XXXXXXXX/requests";
        String orangeUser = "VotreOrangeUsername";         // <-- remplacez
        String orangePass = "VotreOrangePassword";         // <-- remplacez
        orangeSmsService = new OrangeSmsService(orangeEndpoint, orangeUser, orangePass);

        // 7) Charger immédiatement l’historique existant
        loadHistory();
    }

    /** Ouvre un FileChooser pour charger un CSV de destinataires. */
    @FXML
    private void handleLoadCsv(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner un fichier CSV de destinataires");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fc.showOpenDialog(tableRecipients.getScene().getWindow());
        if (file != null) {
            List<Recipient> list = CsvLoader.loadRecipients(file.getAbsolutePath());
            recipients.clear();
            recipients.addAll(list);
        }
    }

    /** Envoie les notifications selon le canal choisi. */
    @FXML
    private void handleSendNotifications(ActionEvent event) {
        String channel = comboChannel.getValue();
        String provider = comboSmsProvider.getValue(); // “OrangeCameroon” ou “MTNCameroon”
        String messageTemplate = txtMessage.getText().trim();

        if (messageTemplate.isEmpty()) {
            showAlert("Erreur", "Le message ne peut pas être vide.");
            return;
        }

        // Récupérer la liste des destinataires cochés
        List<Recipient> toSend = recipients.stream()
                .filter(Recipient::isSelected)
                .collect(Collectors.toList());
        if (toSend.isEmpty()) {
            showAlert("Erreur", "Aucun destinataire sélectionné.");
            return;
        }

        // Si on doit envoyer en SMS, vérifier qu’un fournisseur est sélectionné
        if ((channel.contains("SMS")) && (provider == null || provider.isEmpty())) {
            showAlert("Erreur", "Veuillez choisir un fournisseur SMS.");
            return;
        }

        // Exécuter l’envoi dans un thread distinct pour ne pas bloquer l’UI
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // 1) EMAIL uniquement
                if (channel.equals("EMAIL")) {
                    sendEmails(toSend, messageTemplate);
                }
                // 2) SMS uniquement
                else if (channel.equals("SMS")) {
                    sendSmss(toSend, messageTemplate, provider);
                }
                // 3) EMAIL+SMS
                else {
                    sendEmails(toSend, messageTemplate);
                    sendSmss(toSend, messageTemplate, provider);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    /** Envoie un e-mail personnalisé à chaque destinataire (en injectant {name}). */
    private void sendEmails(List<Recipient> list, String template) {
        for (Recipient r : list) {
            try {
                // On remplace {name} par le nom du destinataire
                String personalized = template.replace("{name}", r.getName());

                emailSender.sendEmail(
                        java.util.Collections.singletonList(r.getEmail()),
                        "Notification",
                        personalized
                );
                // Log succès
                LocalDateTime now = LocalDateTime.now();
                dbService.insertHistory(now, "EMAIL", r.getEmail(), true, "");
                HistoryEntry h = new HistoryEntry(now, "EMAIL", r.getEmail(), true, "");
                Platform.runLater(() -> historyData.add(h));
            } catch (Exception ex) {
                ex.printStackTrace();
                LocalDateTime now = LocalDateTime.now();
                dbService.insertHistory(now, "EMAIL", r.getEmail(), false, ex.getMessage());
                HistoryEntry h = new HistoryEntry(now, "EMAIL", r.getEmail(), false, ex.getMessage());
                Platform.runLater(() -> historyData.add(h));
            }
        }
    }

    /** Envoie un SMS personnalisé à chaque destinataire via Orange (ou MTN). */
    private void sendSmss(List<Recipient> list, String template, String provider) {
        SmsProvider serviceSms;
        if (provider.equals("OrangeCameroon")) {
            serviceSms = orangeSmsService;
        } else {
            // Pour l’instant, on redirige vers OrangeSmsService
            serviceSms = orangeSmsService;
        }

        for (Recipient r : list) {
            try {
                String personalized = template.replace("{name}", r.getName());
                // Exemple de numéro : "+2376XXXXXXXX"
                serviceSms.sendSms(r.getPhone(), personalized);
                LocalDateTime now = LocalDateTime.now();
                dbService.insertHistory(now, "SMS", r.getPhone(), true, "");
                HistoryEntry h = new HistoryEntry(now, "SMS", r.getPhone(), true, "");
                Platform.runLater(() -> historyData.add(h));
                // Petite pause pour respecter d’éventuelles limites
                Thread.sleep(200);
            } catch (Exception ex) {
                ex.printStackTrace();
                LocalDateTime now = LocalDateTime.now();
                dbService.insertHistory(now, "SMS", r.getPhone(), false, ex.getMessage());
                HistoryEntry h = new HistoryEntry(now, "SMS", r.getPhone(), false, ex.getMessage());
                Platform.runLater(() -> historyData.add(h));
            }
        }
    }

    /** Recharge l’o nglet Historique depuis la base SQLite. */
    @FXML
    private void handleRefreshHistory(ActionEvent event) {
        loadHistory();
    }

    /** Charge l’historique depuis SQLite dans historyData. */
    private void loadHistory() {
        historyData.clear();
        java.util.List<HistoryEntry> list = dbService.fetchAllHistory();
        historyData.addAll(list);
    }

    /** Affiche une alerte d’erreur sur l’UI thread. */
    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
