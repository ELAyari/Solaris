package com.esprit.controllers;

import com.esprit.Services.UserService;
import com.esprit.models.User;
import com.esprit.models.UserRole;
import com.esprit.models.UserStatus;
import com.esprit.tests.MainprogGUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Date;

/**
 * Controller for the admin dashboard fragment displayed inside the layout.
 * It loads some basic statistics about users.
 */
public class AdminHomeController {

    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblActiveUsers;
    @FXML
    private Label lblNewUsers;
    @FXML
    private Label lblBannedUsers;

    @FXML
    private PieChart userStatusPieChart;

    @FXML
    private Label lblNewUsersToday;
    @FXML
    private Label lblActiveLastWeek;
    @FXML
    private Label lblTotalAdmins;
    @FXML
    private Label lblAverageSession;

    @FXML
    private LineChart<String, Number> userRegistrationChart;
    @FXML
    private BarChart<String, Number> userRoleChart;
    @FXML
    private AreaChart<String, Number> userActivityChart;

    @FXML
    private TableView<UserActivity> tblRecentActivity;
    @FXML
    private TableColumn<UserActivity, String> colUser;
    @FXML
    private TableColumn<UserActivity, String> colAction;
    @FXML
    private TableColumn<UserActivity, Date> colTimestamp;

    private UserService userService;

    @FXML
    private void initialize() {
        try {
            userService = new UserService();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        setupDashboardStats();
        setupUserStatusChart();
        setupAdditionalCharts();
        loadRecentActivity();
    }

    private void setupDashboardStats() {
        int totalUsers = userService.getTotalUsers();
        int activeUsers = userService.getActiveUsers();
        int newUsers = userService.getNewUsers();
        int bannedUsers = userService.getBannedUsers();

        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(totalUsers));
        if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(activeUsers));
        if (lblNewUsers != null) lblNewUsers.setText(String.valueOf(newUsers));
        if (lblBannedUsers != null) lblBannedUsers.setText(String.valueOf(bannedUsers));
    }

    private void setupUserStatusChart() {
        int confirmed = 0;
        int notConfirmed = 0;
        int banned = 0;
        int admins = 0;

        for (User user : userService.get()) {
            if (user.getStatus() == UserStatus.CONFIRMED) confirmed++;
            else if (user.getStatus() == UserStatus.NON_CONFIRMED) notConfirmed++;
            else if (user.getStatus() == UserStatus.BANNED) banned++;

            if (user.getRole() == UserRole.ADMIN) admins++;
        }

        if (lblTotalAdmins != null) lblTotalAdmins.setText(String.valueOf(admins));
        if (lblNewUsersToday != null) lblNewUsersToday.setText(String.valueOf(userService.getNewUsers()));
        if (lblActiveLastWeek != null) lblActiveLastWeek.setText(String.valueOf(userService.getActiveUsers()));

        if (userStatusPieChart != null) {
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                    new PieChart.Data("Confirmed", confirmed),
                    new PieChart.Data("Not Confirmed", notConfirmed),
                    new PieChart.Data("Banned", banned)
            );
            userStatusPieChart.setData(data);
        }
    }

    /**
     * Populate the additional charts (registration trend, role distribution and
     * activity metrics) with simple aggregated data from the user service.
     * These charts are mainly illustrative but provide at-a-glance insight.
     */
    private void setupAdditionalCharts() {
        if (userRegistrationChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (User user : userService.get()) {
                String month = new java.text.SimpleDateFormat("MMM").format(
                        user.getBirth_date() != null ? user.getBirth_date() : new java.util.Date());
                boolean found = false;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getXValue().equals(month)) {
                        data.setYValue(data.getYValue().intValue() + 1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    series.getData().add(new XYChart.Data<>(month, 1));
                }
            }
            userRegistrationChart.getData().add(series);
        }

        if (userRoleChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int admins = 0, clients = 0, owners = 0;
            for (User u : userService.get()) {
                if (u.getRole() == UserRole.ADMIN) admins++; else if (u.getRole() == UserRole.CLIENT) clients++; else owners++;
            }
            series.getData().add(new XYChart.Data<>("Admin", admins));
            series.getData().add(new XYChart.Data<>("Client", clients));
            series.getData().add(new XYChart.Data<>("Owner", owners));
            userRoleChart.getData().add(series);
        }

        if (userActivityChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Mon", 5));
            series.getData().add(new XYChart.Data<>("Tue", 3));
            series.getData().add(new XYChart.Data<>("Wed", 8));
            series.getData().add(new XYChart.Data<>("Thu", 4));
            series.getData().add(new XYChart.Data<>("Fri", 6));
            userActivityChart.getData().add(series);
        }
    }

    private void loadRecentActivity() {
        if (tblRecentActivity == null) return;

        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        ObservableList<UserActivity> activities = FXCollections.observableArrayList();
        // For brevity we just provide a sample activity list
        activities.add(new UserActivity("user1@example.com", "Login", new Date()));
        activities.add(new UserActivity("admin@example.com", "User Ban", new Date()));

        tblRecentActivity.setItems(activities);
    }

    /** Convenience actions triggered from the dashboard cards */
    @FXML
    private void handleAddUser() {
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.showAdminAddUserScene();
        }
    }

    @FXML
    private void handleGenerateReports() {
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.showReportsScene();
        }
    }

    @FXML
    private void handleSettings() {
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.showSettingsScene();
        }
    }

    public static class UserActivity {
        private final String username;
        private final String action;
        private final Date timestamp;

        public UserActivity(String username, String action, Date timestamp) {
            this.username = username;
            this.action = action;
            this.timestamp = timestamp;
        }

        public String getUsername() { return username; }
        public String getAction() { return action; }
        public Date getTimestamp() { return timestamp; }
    }
}
