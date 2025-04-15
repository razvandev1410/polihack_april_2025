package org.example;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.utils.GraphVisualise;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class Main extends Application {

    private Stage primaryStage;
    private Scene mainScene;



    private TextField searchBox;
    private VBox optionsBox;

    private VBox geneInfoBox;
    private Label geneLabel;
    private Label descLabel;
    private Label chromLabel;
    private Label aliasLabel;
    private Label sumLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        searchBox = new TextField();
        searchBox.setPromptText("Caută...");
        searchBox.setVisible(false);
        searchBox.setPrefWidth(120);
        searchBox.setMaxWidth(250);
        searchBox.setMaxHeight(10);
        searchBox.setAlignment(Pos.TOP_LEFT);

        searchBox.getStyleClass().add("popup-box");
        searchBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        searchBox.setOnAction(e -> {
            String geneName = searchBox.getText();
            if (geneName != null && !geneName.trim().isEmpty()) {
                Map<String, String> geneData = fetchGeneData(geneName.trim());
                showGeneInfoInsideMain(geneData);
                searchBox.setVisible(false);
            }
        });

        optionsBox = new VBox(5);
        optionsBox.setVisible(false);
        optionsBox.getStyleClass().add("popup-box");
        optionsBox.setPrefWidth(120);
        optionsBox.setMaxWidth(120);
        optionsBox.setMaxHeight(100);
        optionsBox.setPadding(new Insets(5));
        optionsBox.setAlignment(Pos.TOP_LEFT);

        optionsBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        Button abonamentBtn = new Button("Abonament");
        Button contactBtn = new Button("Contact");
        Button despreBtn = new Button("Despre");

        abonamentBtn.setOnAction(e -> showAbonamentScene());
        contactBtn.setOnAction(e -> showContactScene());
        despreBtn.setOnAction(e -> showDespreScene());

        abonamentBtn.setMaxWidth(Double.MAX_VALUE);
        contactBtn.setMaxWidth(Double.MAX_VALUE);
        despreBtn.setMaxWidth(Double.MAX_VALUE);

        optionsBox.getChildren().addAll(abonamentBtn, contactBtn, despreBtn);

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        BorderPane headerLayout = createHeader(searchBox, optionsBox);

        ImageView backgroundImage1 = new ImageView(new Image("./ui_thing/background1.png"));
        backgroundImage1.setPreserveRatio(false);
        backgroundImage1.setSmooth(true);
        backgroundImage1.setFitWidth(200);
        backgroundImage1.setFitHeight(500);

        StackPane backgroundPane1 = new StackPane(backgroundImage1);

        TextField sloganTextField = new TextField("ADN-ul tău, cheia sănătății");
        sloganTextField.setEditable(false);
        sloganTextField.getStyleClass().add("text-slogan");

        TextField infoTextField = new TextField("Află informațiile genei > ");
        infoTextField.setEditable(false);
        infoTextField.setMaxWidth(215);
        infoTextField.getStyleClass().add("text-info");

        Button myButton = new Button("MediGene");
        myButton.getStyleClass().add("custom-button");
        myButton.setOnAction(e -> showMediGeneScene());

        HBox secondLine = new HBox(0);
        secondLine.setAlignment(Pos.CENTER_LEFT);
        secondLine.getChildren().addAll(infoTextField, myButton);

        VBox overlayContainer = new VBox(5);
        overlayContainer.setAlignment(Pos.CENTER_LEFT);
        overlayContainer.setPadding(new Insets(10));
        overlayContainer.getChildren().addAll(sloganTextField, secondLine);
        StackPane.setAlignment(overlayContainer, Pos.CENTER_LEFT);
        backgroundPane1.getChildren().add(overlayContainer);

        ImageView backgroundImage2 = new ImageView(new Image("./ui_thing/background2.png"));
        backgroundImage2.setPreserveRatio(false);
        backgroundImage2.setSmooth(true);
        backgroundImage2.setFitWidth(200);
        backgroundImage2.setFitHeight(500);

        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setPadding(new Insets(20));
        textContainer.setMaxWidth(300);
        textContainer.getStyleClass().add("scroll-text-container");

        String[] lines = {
                "Genele, bolile și medicamentele sunt",
                "profund interconectate.",
                "Informația genetică determină modul în",
                "care funcționează organismul și cum",
                "reacționează la diferite substanțe.",
                "Anumite mutații genetice pot crește riscul",
                "de boli precum cancerul, diabetul sau",
                "bolile cardiovasculare.",
                "În același timp, analiza genetică permite",
                "dezvoltarea unor tratamente",
                "personalizate,",
                "adaptate fiecărui individ, ceea ce crește",
                "eficiența medicamentelor și reduce",
                "efectele adverse.",
                "Astfel, genetica devine cheia medicinei",
                "moderne și personalizate."
        };
        for (String line : lines) {
            Label textLine = new Label(line);
            textLine.getStyleClass().add("fade-line");
            textLine.setOpacity(0);
            textContainer.getChildren().add(textLine);
        }

        StackPane backgroundPane2 = new StackPane(backgroundImage2, textContainer);

        VBox scrollableContent = new VBox(headerLayout, backgroundPane1, backgroundPane2);
        scrollableContent.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefWidth(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        backgroundImage1.fitWidthProperty().bind(scrollPane.widthProperty());
        backgroundImage2.fitWidthProperty().bind(scrollPane.widthProperty());

        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            double scrollY = newVal.doubleValue();
            double startTrigger = 0.3;
            for (int i = 0; i < textContainer.getChildren().size(); i++) {
                Label line = (Label) textContainer.getChildren().get(i);
                double triggerPoint = startTrigger + i * 0.04;
                if (scrollY > triggerPoint && line.getOpacity() == 0) {
                    animateLine(line);
                } else if (scrollY < triggerPoint && line.getOpacity() == 1) {
                    FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(300), line);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.play();
                }
            }
        });


        ImageView chatLogo = new ImageView(new Image("./ui_thing/chatLogo.png", 80, 80, true, true));
        Button chatButton = new Button("", chatLogo);
        chatButton.getStyleClass().add("floating-button");
        chatButton.setOnAction(e -> showChatbotScene());
        StackPane.setAlignment(chatButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatButton, new Insets(0, 10, 10, 0));


        geneInfoBox = new VBox(10);
        geneInfoBox.setPadding(new Insets(10));
        geneInfoBox.setMaxWidth(300);
        geneInfoBox.setMaxHeight(220);

        geneInfoBox.getStyleClass().add("gene-info-box");
        geneInfoBox.setVisible(false);

        geneLabel = new Label("Gene name:");
        descLabel = new Label("Description:");
        chromLabel = new Label("Chromosome:");
        aliasLabel = new Label("Other aliases:");
        sumLabel  = new Label("Summary:");

        Button closeInfoBtn = new Button("Close");
        closeInfoBtn.setOnAction(ev -> geneInfoBox.setVisible(false));

        geneInfoBox.getChildren().addAll(geneLabel, descLabel, chromLabel, aliasLabel, sumLabel, closeInfoBtn);


        StackPane.setAlignment(geneInfoBox, Pos.CENTER);


        StackPane mainRoot = new StackPane(scrollPane, searchBox, optionsBox, chatButton, geneInfoBox);
        mainRoot.setStyle("-fx-background-color: #dcdcdc;");
        mainRoot.setPrefSize(400, 750);

        mainScene = new Scene(mainRoot, 450, 700);
        mainScene.getStylesheets().add(getClass().getResource("/ui_thing/style.css").toExternalForm());

        primaryStage.setTitle("MediGene");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Map<String, String> fetchGeneData(String geneName) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("name", geneName);
        resultMap.put("description", "(not found)");
        resultMap.put("chromosome", "(not found)");
        resultMap.put("otheraliases", "(not found)");
        resultMap.put("summary", "(not found)");

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python.exe",
                    "src/main/java/org/example/utils/ScriptsForGettingData/retrieveData.py",
                    geneName
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
                return resultMap;
            }

            String folderPath = "D:\\Facultate\\hackathon2025\\GeneExplorer\\src\\main\\resources\\data\\dataGenes";
            String jsonFilename = folderPath + "\\gene_summary" + geneName + ".json";

            File jsonFile = new File(jsonFilename);
            if (!jsonFile.exists()) {
                System.err.println("JSON file not found: " + jsonFile.getAbsolutePath());
                return resultMap;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONObject root = new JSONObject(sb.toString());
            JSONObject resultObj = root.getJSONObject("result");

            String geneIdKey = null;
            for (String key : resultObj.keySet()) {
                if (!"uids".equals(key)) {
                    geneIdKey = key;
                    break;
                }
            }
            if (geneIdKey == null) {
                System.err.println("No gene ID found in JSON.");
                return resultMap;
            }

            JSONObject geneData = resultObj.getJSONObject(geneIdKey);

            if (geneData.has("name")) {
                resultMap.put("name", geneData.getString("name"));
            }
            if (geneData.has("description")) {
                resultMap.put("description", geneData.getString("description"));
            }
            if (geneData.has("chromosome")) {
                resultMap.put("chromosome", geneData.getString("chromosome"));
            }
            if (geneData.has("otheraliases")) {
                resultMap.put("otheraliases", geneData.getString("otheraliases"));
            }
            if (geneData.has("summary")) {
                resultMap.put("summary", geneData.getString("summary"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return resultMap;
    }

    private void showGeneInfoInsideMain(Map<String, String> geneData) {
        geneLabel.setText("Gene name: " + geneData.getOrDefault("name", "N/A"));
        descLabel.setText("Description: " + geneData.getOrDefault("description", "N/A"));
        chromLabel.setText("Chromosome: " + geneData.getOrDefault("chromosome", "N/A"));
        aliasLabel.setText("Other aliases: " + geneData.getOrDefault("otheraliases", "N/A"));
        sumLabel.setText("Summary: " + geneData.getOrDefault("summary", "N/A"));

        geneInfoBox.setVisible(true);
        geneInfoBox.toFront();
    }

    private void showMediGeneScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        Label titleLabel = new Label("Introdu o genă");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-background-color: transparent;");
        titleLabel.getStyleClass().add("transparent-label");

        TextField geneField = new TextField();
        geneField.setPromptText("Enter gene id (e.g., hsa:7456)");
        geneField.setMaxWidth(250);

        geneField.setOnAction(e -> {
            String geneId = geneField.getText().trim();
            if (!geneId.isEmpty()) {
                GraphVisualise graph = new GraphVisualise(geneId);
                graph.showWindow();
                graph.showCompoundListWindow();
            }
        });
        VBox centerBox = new VBox(15, titleLabel, geneField);
        centerBox.setAlignment(Pos.CENTER);

        StackPane centerPane = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        VBox layout = new VBox(header, centerPane);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene scene = new Scene(root, 450, 700);
        scene.getStylesheets().add(getClass().getResource("/ui_thing/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showChatbotScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        Label helpLabel = new Label("Cu ce te putem ajuta?");
        helpLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-background-color: transparent;");
        helpLabel.getStyleClass().add("transparent-label");

        TextField inputField = new TextField();
        inputField.setPromptText("Scrie un mesaj...");
        inputField.setMaxWidth(250);

        VBox centerBox = new VBox(15, helpLabel, inputField);
        centerBox.setAlignment(Pos.CENTER);

        StackPane centerPane = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        VBox layout = new VBox(header, centerPane);

        StackPane chatbotRoot = new StackPane(layout, searchBox, optionsBox);
        chatbotRoot.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene chatbotScene = new Scene(chatbotRoot, 450, 700);
        chatbotScene.getStylesheets().add(getClass().getResource("/ui_thing/style.css").toExternalForm());
        primaryStage.setScene(chatbotScene);
    }

    private void showDespreScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        Label titleLabel = new Label("Despre");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-background-color: transparent;");
        titleLabel.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(titleLabel);
        centerBox.setAlignment(Pos.CENTER);

        StackPane centerPane = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        VBox layout = new VBox(header, centerPane);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene scene = new Scene(root, 450, 700);
        scene.getStylesheets().add(getClass().getResource("/ui_thing/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private BorderPane createHeader(TextField searchBox, VBox optionsBox) {
        BorderPane headerLayout = new BorderPane();
        headerLayout.getStyleClass().add("header");

        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setPadding(new Insets(0, 0, 0, 20));

        ImageView logoView = new ImageView(new Image("./ui_thing/logoBrand.png", 50, 50, true, true));
        Label appName = new Label("MediGene");
        appName.getStyleClass().add("app-title");

        logoView.setOnMouseClicked(e -> showMainScene());
        appName.setOnMouseClicked(e -> showMainScene());

        leftBox.getChildren().addAll(logoView, appName);
        headerLayout.setLeft(leftBox);

        HBox rightBox = new HBox(20);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setPadding(new Insets(0, 20, 0, 0));

        ImageView searchIcon = new ImageView(new Image("./ui_thing/searchLogo.png", 40, 40, true, true));
        ImageView signInIcon = new ImageView(new Image("./ui_thing/loginLogo.png", 40, 40, true, true));
        ImageView optionsIcon = new ImageView(new Image("./ui_thing/optionsLogo.png", 40, 40, true, true));

        Button searchBtn = new Button("", searchIcon);
        Button loginBtn = new Button("", signInIcon);
        Button optionsBtn = new Button("", optionsIcon);

        searchBtn.getStyleClass().add("icon-button");
        loginBtn.getStyleClass().add("icon-button");
        optionsBtn.getStyleClass().add("icon-button");

        rightBox.getChildren().addAll(searchBtn, loginBtn, optionsBtn);
        headerLayout.setRight(rightBox);

        searchBtn.setOnAction(e -> {
            searchBox.setVisible(!searchBox.isVisible());
            optionsBox.setVisible(false);
        });
        optionsBtn.setOnAction(e -> {
            optionsBox.setVisible(!optionsBox.isVisible());
            searchBox.setVisible(false);
        });
        loginBtn.setOnAction(e -> showLoginScene());

        return headerLayout;
    }

    private void showLoginScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        TextField usernameField = new TextField();
        usernameField.setMaxWidth(200);
        HBox usernameBox = new HBox(10, userLabel, usernameField);
        usernameBox.setAlignment(Pos.CENTER);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(200);
        HBox passwordBox = new HBox(10, passLabel, passwordField);
        passwordBox.setAlignment(Pos.CENTER);

        Label registerText = new Label("Nu ai cont?");
        registerText.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Button registerBtn = new Button("Înregistrare");
        registerBtn.setOnAction(e -> showRegisterScene());
        HBox registerBox = new HBox(5, registerText, registerBtn);
        registerBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(20, usernameBox, passwordBox, registerBox);
        centerBox.setAlignment(Pos.CENTER);

        StackPane content = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox layout = new VBox(header, content);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene loginScene = new Scene(root, 450, 700);
        loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(loginScene);
    }

    private void showRegisterScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);


        Label titleLabel = new Label("Înregistrare");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white;");
        titleLabel.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        TextField usernameField = new TextField();
        usernameField.setMaxWidth(200);
        HBox usernameBox = new HBox(10, userLabel, usernameField);
        usernameBox.setAlignment(Pos.CENTER);


        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(200);
        HBox passwordBox = new HBox(10, passLabel, passwordField);
        passwordBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(20, titleLabel, usernameBox, passwordBox);
        centerBox.setAlignment(Pos.CENTER);

        StackPane content = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox layout = new VBox(header, content);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene scene = new Scene(root, 450, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showMainScene() {
        start(primaryStage);
    }

    private void showContactScene() {
        BorderPane header = createHeader(searchBox, optionsBox);
        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        Label titleLabel = new Label("Contact");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-background-color: transparent;");
        titleLabel.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(titleLabel);
        centerBox.setAlignment(Pos.CENTER);

        StackPane centerPane = new StackPane(backgroundImage, centerBox);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        VBox layout = new VBox(header, centerPane);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene scene = new Scene(root, 450, 700);
        scene.getStylesheets().add(
                getClass().getResource("/ui_thing/style.css").toExternalForm()
        );
        primaryStage.setScene(scene);
    }

    private void showAbonamentScene() {
        BorderPane header = createHeader(searchBox, optionsBox);

        ImageView backgroundImage = new ImageView(new Image("./ui_thing/background3.png"));
        backgroundImage.setFitWidth(450);
        backgroundImage.setFitHeight(620);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(true);

        VBox abonamenteBox = new VBox(20);
        abonamenteBox.setAlignment(Pos.CENTER);

        String[] titluri = {
                "Abonamentul 1 - 10 lei",
                "Abonamentul 2 - 50 lei",
                "Abonamentul 3 - 100 lei"
        };

        for (String titlu : titluri) {
            Label label = new Label(titlu);
            label.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            Button beneficiiBtn = new Button("Beneficii");
            beneficiiBtn.setStyle("-fx-background-radius: 20; -fx-padding: 5 15;");
            beneficiiBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Beneficii");
                alert.setHeaderText(null);
                alert.setContentText("Detalii despre " + titlu);
                alert.showAndWait();
            });

            Button cumparaBtn = new Button("Cumpără");
            cumparaBtn.setStyle("-fx-background-radius: 20; -fx-padding: 5 15;");
            cumparaBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cumpără");
                alert.setHeaderText(null);
                alert.setContentText("Ai selectat: " + titlu);
                alert.showAndWait();
            });

            HBox buttonBox = new HBox(10, beneficiiBtn, cumparaBtn);
            buttonBox.setAlignment(Pos.CENTER);

            VBox abonamentCard = new VBox(5, label, buttonBox);
            abonamentCard.setAlignment(Pos.CENTER);
            abonamenteBox.getChildren().add(abonamentCard);
        }

        StackPane centerPane = new StackPane(backgroundImage, abonamenteBox);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        VBox layout = new VBox(header, centerPane);

        StackPane root = new StackPane(layout, searchBox, optionsBox);
        root.setStyle("-fx-background-color: black;");

        StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
        StackPane.setMargin(searchBox, new Insets(70, 20, 0, 0));
        StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(optionsBox, new Insets(70, 20, 0, 0));

        Scene scene = new Scene(root, 450, 700);
        scene.getStylesheets().add(
                getClass().getResource("/ui_thing/style.css").toExternalForm()
        );
        primaryStage.setScene(scene);
    }

    private void animateLine(Label line) {
        line.setOpacity(1);
        line.setTranslateX(-30);
        TranslateTransition slide = new TranslateTransition(javafx.util.Duration.millis(400), line);
        slide.setFromX(-30);
        slide.setToX(0);

        FadeTransition fade = new FadeTransition(javafx.util.Duration.millis(400), line);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition transition = new ParallelTransition(slide, fade);
        transition.setDelay(Duration.millis(50));
        transition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}