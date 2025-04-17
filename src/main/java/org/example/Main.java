package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.controller.LoginController;

import org.example.service.AllServices;
import org.example.service.ServicesException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Main extends Application {
    private static final Log log = LogFactory.getLog(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui_thing/views/FullScreenView.fxml"));

            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Drug repurposing");
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ui_thing/logoBrand.png")));
            primaryStage.getIcons().add(icon);
            primaryStage.show();
        } catch (Exception e) {
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error ");
            alert.setContentText("Error while starting app "+e);
            alert.showAndWait();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }

    static AllServices getService() throws ServicesException {
        ApplicationContext context = new AnnotationConfigApplicationContext(GeneExplorerConfig.class);
        return context.getBean(AllServices.class);
    }
}
