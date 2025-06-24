package com.esprit.controllers;

import com.esprit.Services.EvenementService;
import com.esprit.Services.PosterGenerationService;
import com.esprit.models.Evenement;
import com.esprit.models.User;
import com.esprit.utils.UserSession;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AjoutEvent {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField adresseField;
    @FXML private TextField posterImageUrlField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private Button validerButton;
    @FXML private Button backButton;
    @FXML private Button browseButton;
    @FXML private ImageView posterPreview;
    @FXML private ProgressIndicator previewSpinner;
    @FXML private Spinner<Integer> reservationLimitSpinner;
    @FXML private BorderPane mainContainer;
    @FXML private Spinner<Double> priceSpinner;
    @FXML private Button aiGenerateButton;
    @FXML private Button timePickerButton;



    private String mode = "add";
    private Evenement evenementToEdit;
    private EventController eventController;

    private final EvenementService service = new EvenementService();

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMainContainer(BorderPane mainContainer) {
        this.mainContainer = mainContainer;
    }

    public void setEventController(EventController controller) {
        this.eventController = controller;
    }

    public void setEvenementToEdit(Evenement evt) {
        this.evenementToEdit = evt;
    }

    @FXML
    public void initialize() {
        // 1) Preview on URL change
        posterImageUrlField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.isEmpty()) {
                try {
                    posterPreview.setImage(new Image(newV, true));
                } catch (Exception ignored) {
                    posterPreview.setImage(null);
                }
            } else {
                posterPreview.setImage(null);
            }
        });

        // 2) Reservation limit spinner
        SpinnerValueFactory<Integer> resFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0);
        reservationLimitSpinner.setValueFactory(resFactory);

        // 3) Price spinner
        SpinnerValueFactory<Double> priceFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        0.0, 10_000.0, 0.0, 0.50
                );
        priceSpinner.setValueFactory(priceFactory);
        priceSpinner.setEditable(true);

        // 4) File chooser for manual poster selection
        browseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Sélectionnez une image de poster");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            Window win = mainContainer.getScene().getWindow();
            File file = chooser.showOpenDialog(win);
            if (file != null) {
                String uri = file.toURI().toString();
                posterImageUrlField.setText(uri);
                posterPreview.setImage(new Image(uri, true));
            }
        });

        // 5) Back and validate buttons
        backButton.setOnAction(e -> retourAEventsView());
        validerButton.setOnAction(e -> {
            if (isValid()) {
                if ("add".equals(mode)) ajouterEvenement();
                else modifierEvenement();
            }
        });
        timePickerButton.setOnAction(e -> showTimePickerDialog());


        // 6) AI‐powered poster generation
        aiGenerateButton.setOnAction(e -> {
            String title = titreField.getText().trim();
            String desc  = descriptionField.getText().trim();
            if (title.isEmpty() || desc.isEmpty()) {
                showAlert("Erreur IA", "Titre et description requis pour générer l’affiche.");
                return;
            }

            // Build prompt
            String prompt = title + ": " + desc;

            // Show spinner & disable button
            previewSpinner.setVisible(true);
            aiGenerateButton.setDisable(true);

            // Background task for the network + file I/O
            Task<String> genTask = new Task<>() {
                @Override protected String call() throws Exception {
                    PosterGenerationService svc = new PosterGenerationService();
                    return svc.generateFromPrompt(
                            prompt,
                            (evenementToEdit != null) ? evenementToEdit.getId() : 0
                    );
                }
            };

            genTask.setOnSucceeded(ev -> {
                String uri = genTask.getValue();
                posterPreview.setImage(new Image(uri, true));
                posterImageUrlField.setText(uri);
                previewSpinner.setVisible(false);
                aiGenerateButton.setDisable(false);
            });

            genTask.setOnFailed(ev -> {
                Throwable ex = genTask.getException();
                ex.printStackTrace();
                showAlert("Erreur IA", "Impossible de générer l’affiche : " + ex.getMessage());
                previewSpinner.setVisible(false);
                aiGenerateButton.setDisable(false);
            });

            new Thread(genTask, "AI-Poster-Thread").start();
        });
    }



    public void prefillFieldsIfEdit() {
        if (evenementToEdit != null && "edit".equals(mode)) {
            titreField.setText(evenementToEdit.getTitre());
            descriptionField.setText(evenementToEdit.getDescription());
            adresseField.setText(evenementToEdit.getAdresse());
            reservationLimitSpinner.getValueFactory().setValue(evenementToEdit.getReservationLimit());

            if (evenementToEdit.getDate() != null) {
                datePicker.setValue(evenementToEdit.getDate().toLocalDate());
                // Format the time properly
                String formattedTime = evenementToEdit.getDate().toLocalTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                );
                heureField.setText(formattedTime);
            }

            String url = evenementToEdit.getPosterImageUrl();
            posterImageUrlField.setText(url);
            if (url != null && !url.isEmpty()) {
                posterPreview.setImage(new Image(url, true));
            }
            priceSpinner.getValueFactory()
                    .setValue(evenementToEdit.getPriceCents() / 100.0);
        }
    }
    private void ajouterEvenement() {
        var evt = buildEvenementFromForm();
        service.add(evt);
        showAlert("Succès", "Événement ajouté !");
        retourAEventsView();
    }

    private void modifierEvenement() {
        if (evenementToEdit == null) return;
        var updated = buildEvenementFromForm();
        updated.setId(evenementToEdit.getId());
        updated.setOrganisateur(evenementToEdit.getOrganisateur());
        service.update(updated);
        showAlert("Succès", "Événement modifié !");
        retourAEventsView();
    }

    private Evenement buildEvenementFromForm() {
        LocalDate date = datePicker.getValue();
        LocalTime time = parseHeure(heureField.getText());
        LocalDateTime dateTime = (date != null && time != null)
                ? LocalDateTime.of(date, time)
                : null;
        Evenement e = new Evenement();
        e.setTitre(titreField.getText());
        e.setDescription(descriptionField.getText());
        e.setAdresse(adresseField.getText());
        e.setDate(dateTime);
        e.setPosterImageUrl(posterImageUrlField.getText());
        e.setReservationLimit(reservationLimitSpinner.getValue());
        double euros = priceSpinner.getValue();
        e.setPriceCents((long) Math.round(euros * 100));

        User organiser = UserSession.getInstance().getUser();
        e.setOrganisateur(organiser);

        return e;
    }

    private void retourAEventsView() {
        if (eventController != null) {
            eventController.reloadEvents();
            eventController.goToMainView();
        }
    }

    private boolean isValid() {
        String titre = titreField.getText();
        String desc = descriptionField.getText();
        String adresse = adresseField.getText();
        String heure = heureField.getText();
        LocalDate date = datePicker.getValue();
        if (titre.isEmpty() || desc.isEmpty() || adresse.isEmpty()
                || heure.isEmpty() || date == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return false;
        }
        LocalTime parsedHeure = parseHeure(heure);
        if (parsedHeure == null) return false;
        LocalDateTime fullDateTime = LocalDateTime.of(date, parsedHeure);
        if ("add".equals(mode) && fullDateTime.isBefore(LocalDateTime.now())) {
            showAlert("Erreur", "La date et l'heure doivent être dans le futur.");
            return false;
        }
        return true;
    }

    private LocalTime parseHeure(String heureStr) {
        try {
            // First try standard format
            return LocalTime.parse(heureStr);
        } catch (Exception e) {
            try {
                // Try to handle various input formats
                if (heureStr.matches("\\d{1,2}:\\d{2}")) {
                    String[] parts = heureStr.split(":");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                        return LocalTime.of(hour, minute);
                    }
                }
            } catch (Exception ex) {
                // Fall through to error
            }
            showAlert("Erreur", "Format d'heure invalide (ex: 14:30)");
            return null;
        }
    }

    private void showAlert(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    private void showTimePickerDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Time");
        dialog.initStyle(StageStyle.UTILITY);

        // Create a stylish header
        Label headerLabel = new Label("Select Event Time");
        headerLabel.getStyleClass().add("dialog-header-title");

        // Create a container for the clock icon and header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(15, 20, 15, 20));
        headerBox.getStyleClass().add("dialog-header");

        SVGPath clockIcon = new SVGPath();
        clockIcon.setContent("M12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22C6.47,22 2,17.5 2,12A10,10 0 0,1 12,2M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z");
        clockIcon.getStyleClass().add("dialog-header-icon");

        headerBox.getChildren().addAll(clockIcon, headerLabel);

        // Create styled time spinners
        Spinner<Integer> hourSpinner = new Spinner<>();
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        hourSpinner.setEditable(true);
        hourSpinner.getStyleClass().add("time-spinner");
        hourSpinner.setPrefWidth(100);

        Label colonLabel = new Label(":");
        colonLabel.getStyleClass().add("colon-label");

        Spinner<Integer> minuteSpinner = new Spinner<>();
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        minuteSpinner.setEditable(true);
        minuteSpinner.getStyleClass().add("time-spinner");
        minuteSpinner.setPrefWidth(100);

        // Parse current time if possible
        try {
            String[] parts = heureField.getText().split(":");
            if (parts.length == 2) {
                hourSpinner.getValueFactory().setValue(Integer.parseInt(parts[0]));
                minuteSpinner.getValueFactory().setValue(Integer.parseInt(parts[1]));
            }
        } catch (Exception ex) {
            // Ignore parsing errors and use defaults
        }

        // Layout for time picker with some AM/PM labels for context
        HBox timeBox = new HBox(15);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setPadding(new Insets(25, 20, 30, 20));
        timeBox.getChildren().addAll(hourSpinner, colonLabel, minuteSpinner);

        // Dialog structure
        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(headerBox, timeBox);

        // Style the dialog and buttons
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getStylesheets().add("/styles/AjoutEvent.css");
        dialog.getDialogPane().getStyleClass().add("time-picker-dialog");

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Style the buttons
        Button selectButton = (Button) dialog.getDialogPane().lookupButton(selectButtonType);
        selectButton.getStyleClass().add("select-time-button");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("cancel-time-button");

        // Handle result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return String.format("%02d:%02d", hourSpinner.getValue(), minuteSpinner.getValue());
            }
            return null;
        });

        // Update field with selected time
        dialog.showAndWait().ifPresent(time -> heureField.setText(time));
    }
}
