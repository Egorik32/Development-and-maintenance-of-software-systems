package com.example.cityparks.controller;

import java.io.File;

import com.example.cityparks.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.FileChooser;

public class RootLayoutController {
    private MainApp mainApp;
    @FXML private CheckMenuItem darkThemeMenuItem;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        darkThemeMenuItem.selectedProperty().bindBidirectional(mainApp.darkThemeProperty());
    }

    @FXML
    private void handleNew() {
        mainApp.getCityData().clear();
        mainApp.getParkData().clear();
        mainApp.setCityFilePath(null);
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть XML-файл городов");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (file != null) {
            try {
                mainApp.loadCityDataFromFile(file);
            } catch (Exception ex) {
                showError("Не удалось загрузить данные", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        File file = mainApp.getCityFilePath();
        if (file != null) {
            try {
                mainApp.saveCityDataToFile(file);
            } catch (Exception ex) {
                showError("Не удалось сохранить данные", ex.getMessage());
            }
        } else {
            handleSaveAs();
        }
    }

    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить XML-файл городов");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
        if (file != null) {
            if (!file.getPath().toLowerCase().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            try {
                mainApp.saveCityDataToFile(file);
            } catch (Exception ex) {
                showError("Не удалось сохранить данные", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(message);
        mainApp.applyTheme(alert.getDialogPane().getScene());
        alert.showAndWait();
    }
}
