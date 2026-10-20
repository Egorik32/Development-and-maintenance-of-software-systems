package com.example.cityparks;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.prefs.Preferences;

import com.example.cityparks.controller.CityEditDialogController;
import com.example.cityparks.controller.CityOverviewController;
import com.example.cityparks.model.City;
import com.example.cityparks.model.Park;
import com.example.cityparks.util.XmlDataStore;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private final BooleanProperty darkTheme = new SimpleBooleanProperty();

    private final ObservableList<Park> parkData = FXCollections.observableArrayList();
    private final ObservableList<City> cityData = FXCollections.observableArrayList();

    public MainApp() {
        darkTheme.set(preferences().getBoolean("darkTheme", false));
        darkTheme.addListener((observable, oldValue, newValue) -> {
            preferences().putBoolean("darkTheme", newValue);
            if (primaryStage != null && primaryStage.getScene() != null) {
                applyTheme(primaryStage.getScene());
            }
        });
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
        this.primaryStage.getIcons().add(new javafx.scene.image.Image(MainApp.class.getResourceAsStream("images/park.png")));
        initRootLayout();
        showCityOverview();
        File lastFile = getCityFilePath();
        if (lastFile != null && lastFile.isFile()) {
            try { loadCityDataFromFile(lastFile); }
            catch (Exception ex) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.initOwner(primaryStage);
                alert.setHeaderText("Не удалось открыть последний XML-файл");
                alert.setContentText(ex.getMessage());
                applyTheme(alert.getDialogPane().getScene());
                alert.showAndWait();
            }
        }
        System.out.println("READY: window=" + primaryStage.isShowing() + "; view=" + (rootLayout.getCenter() != null));
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            applyTheme(scene);
            primaryStage.setScene(scene);


            com.example.cityparks.controller.RootLayoutController rootController = loader.getController();
            rootController.setMainApp(this);
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
            Scene dialogScene = new Scene(page);
            applyTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            dialogStage.getIcons().addAll(primaryStage.getIcons());

            CityEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMainApp(this);
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

    public BooleanProperty darkThemeProperty() {
        return darkTheme;
    }

    public void applyTheme(Scene scene) {
        String base = MainApp.class.getResource("css/style.css").toExternalForm();
        String dark = MainApp.class.getResource("css/dark.css").toExternalForm();
        if (!scene.getStylesheets().contains(base)) scene.getStylesheets().add(base);
        scene.getStylesheets().remove(dark);
        if (darkTheme.get()) scene.getStylesheets().add(dark);
    }

    private Preferences preferences() {
        return Preferences.userNodeForPackage(MainApp.class).node(System.getProperty("cityparks.lab", "1.8"));
    }

    public File getCityFilePath() {
        Preferences prefs = preferences();
        String filePath = prefs.get("filePath", null);
        return filePath == null ? null : new File(filePath);
    }

    public void setCityFilePath(File file) {
        Preferences prefs = preferences();
        if (file != null) {
            prefs.put("filePath", file.getAbsolutePath());
            primaryStage.setTitle("Города и парки - " + file.getName());
        } else {
            prefs.remove("filePath");
            primaryStage.setTitle("Города и парки");
        }
    }

    public void loadCityDataFromFile(File file) throws Exception {
        XmlDataStore.load(file, cityData, parkData);
        setCityFilePath(file);
    }

    public void saveCityDataToFile(File file) throws Exception {
        XmlDataStore.save(file, cityData);
        setCityFilePath(file);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
