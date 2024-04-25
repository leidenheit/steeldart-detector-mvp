package de.leidenheit.steeldartdetectormvp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;
import java.nio.file.Path;

public class SteeldartDetectorApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/home", "main.fxml"));
        System.out.println("home.fxml found");
        Scene scene = new Scene(fxmlLoader.load(), 1280, 960);
        System.out.println("home.fxml loaded");

        stage.setTitle("leidenheit MVP");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();

        // differentiate library to load
        if (os.contains("win")) {
            System.out.println("Executing on Windows");
            loadOpenCVWindows();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            System.out.println("Executing on Linux");
            loadOpenCV();
        } else {
            System.out.println("Unsupported Operating System");
            System.exit(4711);
        }
        launch();
    }

    private static void loadOpenCV() {
        try {
            System.load(Path.of("libs/libopencv_java490.so").toAbsolutePath().toString());
            System.out.printf("OpenCV v%s successfully loaded\n%s\n", Core.VERSION, Core.getBuildInformation());
        } catch (Exception e) {
            System.out.printf("Could not load OpenCV -> error: %s\n", e.getMessage());
            System.exit(4711);
        }
    }

    private static void loadOpenCVWindows() {
        try {
            System.load(Path.of("libs/libopencv_java490.dll").toAbsolutePath().toString());
             System.out.printf("OpenCV v%s successfully loaded\n%s\n", Core.VERSION, Core.getBuildInformation());
        } catch (Exception e) {
            System.out.printf("Could not load OpenCV -> error: %s\n", e.getMessage());
            System.exit(4711);
        }
    }
}
