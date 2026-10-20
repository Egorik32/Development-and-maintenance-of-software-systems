import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SceneBuilderCheck {
    private static int exitCode;
    public static void main(String[] args) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        Class<?> platform = Class.forName("javafx.application.Platform");
        Runnable check = () -> {
            try {
                Class<?> editorClass = Class.forName("com.oracle.javafx.scenebuilder.kit.editor.EditorController");
                Path folder = Paths.get(args[0]);
                List<Path> files;
                try (java.util.stream.Stream<Path> stream = Files.walk(folder.resolve("src/main/resources"))) {
                    files = stream.filter(p -> p.toString().endsWith(".fxml")).collect(Collectors.toList());
                }
                for (Path file : files) {
                    Object editor = editorClass.getConstructor().newInstance();
                    String text = Files.readString(file, StandardCharsets.UTF_8);
                    editorClass.getMethod("setFxmlTextAndLocation", String.class, java.net.URL.class)
                            .invoke(editor, text, file.toUri().toURL());
                    Object document = editorClass.getMethod("getFxomDocument").invoke(editor);
                    if (document == null) throw new AssertionError("No Scene Builder document: " + file);
                    Object graphRoot = document.getClass().getMethod("getSceneGraphRoot").invoke(document);
                    if (graphRoot == null) throw new AssertionError("Scene Builder could not create view: " + file);
                    String saved = (String) editorClass.getMethod("getFxmlText", boolean.class).invoke(editor, false);
                    // Preserve the JavaFX 8 namespace for the assignment's runtime after editor serialization.
                    saved = saved.replace("http://javafx.com/javafx/26", "http://javafx.com/javafx/8.0.502");
                    Files.writeString(file, saved, StandardCharsets.UTF_8);
                    System.out.println("PASS: Scene Builder model loaded and saved " + file.getFileName());
                }
                System.out.println("RESULT: SCENE BUILDER CHECKS PASSED");
            } catch (Throwable ex) { ex.printStackTrace(); exitCode = 1; }
            finally { done.countDown(); }
        };
        platform.getMethod("startup", Runnable.class).invoke(null, check);
        if (!done.await(60, TimeUnit.SECONDS)) throw new AssertionError("Scene Builder did not finish");
        platform.getMethod("exit").invoke(null);
        System.exit(exitCode);
    }
}
