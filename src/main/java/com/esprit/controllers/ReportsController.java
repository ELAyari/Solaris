package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.models.UserStatus;
import com.esprit.Services.UserService;
import com.esprit.tests.MainprogGUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    private MainprogGUI mainApp;
    private UserService userService;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colAddress;
    @FXML private TableColumn<User, String> colPhoneNumber;
    @FXML private TableColumn<User, UserStatus> colStatus;

    @FXML private Button btnConfirmUser;
    @FXML private Button btnBanUser;


    private ObservableList<User> nonConfirmedUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = new UserService();
            setupTableColumns();
            loadNonConfirmedUsers();
            styleButtons();
        } catch (SQLException e) {
            showError("Database Error", "Failed to initialize database connection: " + e.getMessage());
        }
    }


    private void styleButtons() {
        if (btnConfirmUser != null) {
            btnConfirmUser.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;"); // green
        }
        if (btnBanUser != null) {
            btnBanUser.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;"); // red
        }
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadNonConfirmedUsers() {
        try {
            List<User> allUsers = userService.get();
            nonConfirmedUsers.clear();
            for (User user : allUsers) {
                if (user.getStatus() == UserStatus.NON_CONFIRMED) {
                    nonConfirmedUsers.add(user);
                }
            }
            userTable.setItems(nonConfirmedUsers);
        } catch (Exception e) {
            showError("Error", "Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("No Selection", "Please select a user to confirm.");
            return;
        }

        try {
            selectedUser.setStatus(UserStatus.CONFIRMED);
            userService.update(selectedUser);
            showInfo("User Confirmed", "User status has been updated to confirmed.");
            loadNonConfirmedUsers();
        } catch (Exception e) {
            showError("Error", "Failed to update user status. Please try again.");
        }
    }

    @FXML
    private void handleBanUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("No Selection", "Please select a user to ban.");
            return;
        }

        try {
            selectedUser.setStatus(UserStatus.BANNED);
            userService.update(selectedUser);
            showInfo("User Banned", "User status has been updated to banned.");
            loadNonConfirmedUsers();
        } catch (Exception e) {
            showError("Error", "Failed to update user status. Please try again.");
        }
    }

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }







    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
