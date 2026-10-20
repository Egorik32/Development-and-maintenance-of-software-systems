package com.example.cityparks;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.prefs.Preferences;

import com.example.cityparks.controller.CityEditDialogController;
import com.example.cityparks.controller.CityOverviewController;
import com.example.cityparks.model.City;
import com.example.cityparks.model.Park;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;

    private final ObservableList<Park> parkData = FXCollections.observableArrayList();
    private final ObservableList<City> cityData = FXCollections.observableArrayList();

    public MainApp() {
        Park gorky = new Park("Парк имени Горького", "городской", 19.5, true, LocalDate.of(1928, 8, 12));
        Park revolution = new Park("Парк Революции", "культурный", 17.0, false, LocalDate.of(1926, 5, 1));
        Park olympic = new Park("Олимпийский парк", "спортивный", 200.0, true, LocalDate.of(2014, 2, 7));
        parkData.addAll(gorky, revolution, olympic);
        cityData.addAll(
                new City("Ростов-на-Дону", "областной центр", 1142162, gorky),
                new City("Шахты", "город областного значения", 226452, revolution),
                new City("Сочи", "курортный город", 466078, olympic));
    }

    public ObservableList<City> getCityData() {
        return cityData;
    }

    public ObservableList<Park> getParkData() {
        return parkData;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Города и парки");
        initRootLayout();
        showCityOverview();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);


            primaryStage.show();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void showCityOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/CityOverview.fxml"));
            AnchorPane cityOverview = loader.load();
            rootLayout.setCenter(cityOverview);

            CityOverviewController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean showCityEditDialog(City city) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/CityEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование города");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(page));

            CityEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCity(city);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
