package com.example.cityparks.controller;

import com.example.cityparks.model.City;
import com.example.cityparks.model.Park;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CityEditDialogController {
    @FXML private TextField cityNameField;
    @FXML private TextField cityStatusField;
    @FXML private TextField populationField;
    @FXML private TextField parkNameField;
    @FXML private TextField parkTypeField;
    @FXML private TextField parkAreaField;
    @FXML private CheckBox hasWaterCheckBox;
    @FXML private DatePicker buildDatePicker;
    private Stage dialogStage;
    private City city;
    private boolean okClicked;

    public void setDialogStage(Stage stage) { dialogStage = stage; }
    public boolean isOkClicked() { return okClicked; }

    public void setCity(City value) {
        city = value;
        cityNameField.setText(city.getName());
        cityStatusField.setText(city.getStatus());
        populationField.setText(city.getPopulation() == 0 ? "" : Integer.toString(city.getPopulation()));
        Park park = city.getPark();
        if (park != null) {
            parkNameField.setText(park.getName());
            parkTypeField.setText(park.getType());
            parkAreaField.setText(Double.toString(park.getArea()));
            hasWaterCheckBox.setSelected(park.isHasWater());
            buildDatePicker.setValue(park.getBuildDate());
        }
    }

    @FXML private void handleOk() {
        if (!isInputValid()) return;
        Park park = new Park(parkNameField.getText().trim(), parkTypeField.getText().trim(),
                Double.parseDouble(parkAreaField.getText().trim()), hasWaterCheckBox.isSelected(),
                buildDatePicker.getValue());
        city.setName(cityNameField.getText().trim());
        city.setStatus(cityStatusField.getText().trim());
        city.setPopulation(Integer.parseInt(populationField.getText().trim()));
        city.setPark(park);
        okClicked = true;
        dialogStage.close();
    }

    @FXML private void handleCancel() { dialogStage.close(); }

    private boolean isInputValid() {
        StringBuilder errors = new StringBuilder();
        if (blank(cityNameField.getText())) errors.append("Название города не заполнено.\n");
        if (blank(cityStatusField.getText())) errors.append("Статус города не заполнен.\n");
        if (!positiveInteger(populationField.getText())) errors.append("Население должно быть положительным целым числом.\n");
        if (blank(parkNameField.getText())) errors.append("Название парка не заполнено.\n");
        if (blank(parkTypeField.getText())) errors.append("Тип парка не заполнен.\n");
        if (!positiveDouble(parkAreaField.getText())) errors.append("Площадь должна быть положительным конечным числом.\n");
        if (buildDatePicker.getValue() == null) errors.append("Дата постройки не выбрана.\n");
        if (errors.length() == 0) return true;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText("Проверьте введенные данные");
        alert.setContentText(errors.toString());
        alert.showAndWait();
        return false;
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private boolean positiveInteger(String s) {
        try { return Integer.parseInt(s.trim()) > 0; }
        catch (RuntimeException ex) { return false; }
    }
    private boolean positiveDouble(String s) {
        try { double v = Double.parseDouble(s.trim()); return Double.isFinite(v) && v > 0; }
        catch (RuntimeException ex) { return false; }
    }
}
