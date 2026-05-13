package com.example.mainapp;

        import javafx.application.Application;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.Scene;
        import javafx.stage.Stage;

        import java.io.IOException;

        public class MainApplication extends Application {
            @Override
            public void start(Stage stage) throws IOException {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/main-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
                stage.setTitle("Système de Gestion de Pointage");
                stage.setScene(scene);
                stage.show();
            }

            public static void main(String[] args) {
                launch();
            }
        }