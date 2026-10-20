import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.cityparks.MainApp;
import com.sun.javafx.stage.StageHelper;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;

public class FxCheck extends Application {
    private static int exitCode;
    private Stage primary;
    private MainApp app;
    private int number;

    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        System.out.println("PASS: " + message);
    }
    private Button button(Parent root, String label) {
        for (Node node : root.lookupAll(".button")) {
            if (node instanceof Button && ((Button) node).getText().equals(label)) return (Button) node;
        }
        throw new AssertionError("Button not found: " + label);
    }
    private Stage dialog() {
        for (Stage stage : new ArrayList<>(StageHelper.getStages())) {
            if (stage != primary && stage.getScene() != null && stage.getScene().lookup("#cityNameField") != null) return stage;
        }
        throw new AssertionError("Edit dialog not found");
    }
    private void text(Parent root, String id, String value) { ((TextField) root.lookup("#" + id)).setText(value); }
    private void closeAlerts() {
        for (Stage stage : new ArrayList<>(StageHelper.getStages())) {
            if (stage != primary && stage.getScene().lookup("#cityNameField") == null) {
                if (number == 8) require(hasDarkTheme(stage), "Validation alert uses dark theme");
                stage.close();
            }
        }
    }
    private boolean hasDarkTheme(Stage stage) {
        return stage.getScene().getStylesheets().stream().anyMatch(value -> value.endsWith("/css/dark.css"));
    }
    private void verifyTheme(Parent root) throws Exception {
        MenuBar bar = (MenuBar) root.lookup(".menu-bar");
        CheckMenuItem toggle = (CheckMenuItem) bar.getMenus().stream()
                .flatMap(menu -> menu.getItems().stream())
                .filter(item -> "darkThemeMenuItem".equals(item.getId())).findFirst().get();
        BooleanProperty property = (BooleanProperty) MainApp.class.getMethod("darkThemeProperty").invoke(app);
        property.set(false);
        require(!toggle.isSelected() && !hasDarkTheme(primary), "Light theme and menu are synchronized");
        screenshot(root, "main-light.png");
        toggle.setSelected(true);
        require(property.get() && hasDarkTheme(primary), "Menu switches the scene to dark theme");
        MainApp reopened = new MainApp();
        require(((BooleanProperty) MainApp.class.getMethod("darkThemeProperty").invoke(reopened)).get(), "Dark theme choice survives application recreation");
        root.applyCss();
        javafx.scene.paint.Color heading = (javafx.scene.paint.Color) ((Label) root.lookup(".section-title")).getTextFill();
        require(heading.getBrightness() > 0.8, "Dark theme headings remain readable");
        screenshot(root, "main-dark.png");
        property.set(false);
        require(!toggle.isSelected() && !hasDarkTheme(primary), "Switching back removes dark CSS");
        toggle.setSelected(true);
    }
    private void input(Parent root, String name, String population, String area) {
        text(root, "cityNameField", name);
        text(root, "cityStatusField", "тестовый статус");
        text(root, "populationField", population);
        text(root, "parkNameField", "Тестовый парк");
        text(root, "parkTypeField", "городской");
        text(root, "parkAreaField", area);
        ((CheckBox) root.lookup("#hasWaterCheckBox")).setSelected(true);
        ((DatePicker) root.lookup("#buildDatePicker")).setValue(LocalDate.of(2020, 2, 29));
    }
    private void screenshot(Parent root, String filename) throws Exception {
        root.applyCss();
        root.layout();
        ImageIO.write(SwingFXUtils.fromFXImage(root.snapshot(null, null), null), "png", new File("verification/" + filename));
    }
    @Override public void start(Stage stage) {
        primary = stage;
        number = Integer.parseInt(getParameters().getRaw().get(0));
        Platform.setImplicitExit(false);
        app = new MainApp();
        try { app.start(stage); }
        catch (Throwable ex) { ex.printStackTrace(); exitCode = 1; Platform.exit(); return; }
        PauseTransition wait = new PauseTransition(Duration.millis(250));
        wait.setOnFinished(event -> Platform.runLater(() -> {
            try { verify(); }
            catch (Throwable ex) { ex.printStackTrace(); exitCode = 1; }
            finally {
                for (Stage s : new ArrayList<>(StageHelper.getStages())) s.close();
                Platform.exit();
            }
        }));
        wait.play();
    }
    @SuppressWarnings("unchecked") private void verify() throws Exception {
        new File("verification").mkdirs();
        Parent root = primary.getScene().getRoot();
        TableView<Object> table = (TableView<Object>) root.lookup("#cityTable");
        require(table != null, "FXML loaded; MVC view present");
        require(table.getItems().size() == (number == 3 ? 0 : 3), "Correct data for this stage");
        if (number >= 6) {
            require(!primary.getIcons().isEmpty() && !primary.getIcons().get(0).isError(), "Application icon loaded");
            require(!primary.getScene().getStylesheets().isEmpty(), "CSS connected");
        }
        if (number >= 5) {
            table.getSelectionModel().select(0);
            require(((Label) root.lookup("#cityNameLabel")).getText().equals("Ростов-на-Дону"), "Selection displays details");
        }
        if (number == 8) verifyTheme(root);
        screenshot(root, "main.png");
        if (number >= 5) {
            Platform.runLater(() -> {
                Stage edit = dialog();
                Parent form = edit.getScene().getRoot();
                if (number == 8) require(hasDarkTheme(edit), "Edit dialog uses dark theme");
                input(form, "Проверочный город", "12345", "15.5");
                try { screenshot(form, "dialog.png"); } catch (Exception ex) { throw new RuntimeException(ex); }
                button(form, "OK").fire();
            });
            button(root, "Добавить").fire();
            require(table.getItems().size() == 4, "Add city with all park fields");
            table.getSelectionModel().select(3);
            Platform.runLater(() -> {
                Parent form = dialog().getScene().getRoot();
                input(form, "Измененный город", "54321", "25.5");
                button(form, "OK").fire();
            });
            button(root, "Изменить").fire();
            require(((Label) root.lookup("#cityNameLabel")).getText().equals("Измененный город"), "Edit updates details");
            Platform.runLater(() -> {
                Parent form = dialog().getScene().getRoot();
                text(form, "cityNameField", "Отмененное изменение");
                button(form, "Отмена").fire();
            });
            button(root, "Изменить").fire();
            require(((Label) root.lookup("#cityNameLabel")).getText().equals("Измененный город"), "Cancel preserves original data");
            for (String[] bad : new String[][] {{"", "100", "10"}, {"Test", "-1", "10"}, {"Test", "100", "Infinity"}}) {
                Platform.runLater(() -> {
                    Parent form = dialog().getScene().getRoot();
                    input(form, bad[0], bad[1], bad[2]);
                    Platform.runLater(() -> closeAlerts());
                    button(form, "OK").fire();
                    require(dialog().isShowing(), "Invalid input keeps dialog open");
                    button(form, "Отмена").fire();
                });
                button(root, "Добавить").fire();
                require(table.getItems().size() == 4, "Invalid input not added");
            }
            table.getSelectionModel().select(3);
            button(root, "Удалить").fire();
            require(table.getItems().size() == 3, "Delete removes selected record");
        }
        if (number >= 7) {
            Method save = MainApp.class.getMethod("saveCityDataToFile", File.class);
            Method load = MainApp.class.getMethod("loadCityDataFromFile", File.class);
            File xml = new File("verification/roundtrip.xml").getAbsoluteFile();
            Object first = table.getItems().get(0);
            Object park = first.getClass().getMethod("getPark").invoke(first);
            park.getClass().getMethod("setArea", double.class).invoke(park, 123.45);
            save.invoke(app, xml);
            table.getItems().clear();
            load.invoke(app, xml);
            require(table.getItems().size() == 3, "JAXB XML roundtrip restores records");
            park = table.getItems().get(0).getClass().getMethod("getPark").invoke(table.getItems().get(0));
            require((double) park.getClass().getMethod("getArea").invoke(park) == 123.45, "XML restores modified park fields");
            require(xml.equals(MainApp.class.getMethod("getCityFilePath").invoke(app)), "Last file persisted in Preferences");
            MainApp reopened = new MainApp();
            Stage second = new Stage();
            reopened.start(second);
            if (number == 8) require(hasDarkTheme(second), "New window restores saved dark theme");
            Object reopenedCity = ((List<?>) MainApp.class.getMethod("getCityData").invoke(reopened)).get(0);
            Object reopenedPark = reopenedCity.getClass().getMethod("getPark").invoke(reopenedCity);
            require((double) reopenedPark.getClass().getMethod("getArea").invoke(reopenedPark) == 123.45, "Startup loads last saved XML automatically");
            second.close();
            Files.write(Paths.get("verification/bad.xml"), "<cities><city><name>Bad</name></city></cities>".getBytes(StandardCharsets.UTF_8));
            try { load.invoke(app, new File("verification/bad.xml")); throw new AssertionError("Bad XML was accepted"); }
            catch (java.lang.reflect.InvocationTargetException expected) { }
            require(table.getItems().size() == 3, "Invalid XML preserves existing records");
            MainApp.class.getMethod("setCityFilePath", File.class).invoke(app, new Object[] {null});
        }
        if (number == 8) ((BooleanProperty) MainApp.class.getMethod("darkThemeProperty").invoke(app)).set(false);
        System.out.println("RESULT: ALL CHECKS PASSED FOR LAB 1." + number);
    }
    public static void main(String[] args) { launch(args); System.exit(exitCode); }
}
