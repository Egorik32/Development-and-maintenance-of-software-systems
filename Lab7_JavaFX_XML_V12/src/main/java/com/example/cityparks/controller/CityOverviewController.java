package com.example.cityparks.controller;

import com.example.cityparks.MainApp;
import com.example.cityparks.model.City;
import com.example.cityparks.model.Park;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CityOverviewController {
    @FXML
    private TableView<City> cityTable;
    @FXML
    private TableColumn<City, String> nameColumn;
    @FXML
    private TableColumn<City, String> statusColumn;

    @FXML
    private Label cityNameLabel;
    @FXML
    private Label cityStatusLabel;
    @FXML
    private Label populationLabel;
    @FXML
    private Label parkNameLabel;
    @FXML
    private Label parkTypeLabel;
    @FXML
    private Label parkAreaLabel;
    @FXML
    private Label waterLabel;
    @FXML
    private Label buildDateLabel;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        showCityDetails(null);
        cityTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showCityDetails(newValue));
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        cityTable.setItems(mainApp.getCityData());
    }

    private void showCityDetails(City city) {
        if (city == null) {
            cityNameLabel.setText("");
            cityStatusLabel.setText("");
            populationLabel.setText("");
            parkNameLabel.setText("");
            parkTypeLabel.setText("");
            parkAreaLabel.setText("");
            waterLabel.setText("");
            buildDateLabel.setText("");
            return;
        }
        Park park = city.getPark();
        cityNameLabel.setText(city.getName());
        cityStatusLabel.setText(city.getStatus());
        populationLabel.setText(Integer.toString(city.getPopulation()));
        parkNameLabel.setText(park == null ? "" : park.getName());
        parkTypeLabel.setText(park == null ? "" : park.getType());
        parkAreaLabel.setText(park == null ? "" : String.format("%.2f", park.getArea()));
        waterLabel.setText(park != null && park.isHasWater() ? "есть" : "нет");
        buildDateLabel.setText(park == null ? "" : park.getBuildDate().toString());
    }

    @FXML
    private void handleDeleteCity() {
        int selectedIndex = cityTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            cityTable.getItems().remove(selectedIndex);
        } else {
            showWarning("Не выбран город", "Выберите город в таблице для удаления.");
        }
    }

    @FXML
    private void handleNewCity() {
        City city = new City();
        boolean okClicked = mainApp.showCityEditDialog(city);
        if (okClicked) {
            mainApp.getCityData().add(city);
        }
    }

    @FXML
    private void handleEditCity() {
        City selectedCity = cityTable.getSelectionModel().getSelectedItem();
        if (selectedCity != null) {
            boolean okClicked = mainApp.showCityEditDialog(selectedCity);
            if (okClicked) {
                showCityDetails(selectedCity);
            }
        } else {
            showWarning("Не выбран город", "Выберите город в таблице для редактирования.");
        }
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
