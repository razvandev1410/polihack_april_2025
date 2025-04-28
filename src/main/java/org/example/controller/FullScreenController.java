package org.example.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.domain.entitiesAssociatedWithUser.User;
import org.example.domain.graphComponents.EdgeData;
import org.example.domain.graphComponents.NodeData;
import org.example.service.AllServices;
import org.example.service.ServicesException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main full-screen view. Shows dashboard by default,
 * and replaces it with a pannable/zoomable gene-interaction graph upon search.
 */
public class FullScreenController {
    private AllServices services;
    private Optional<User> currentUser = Optional.empty();

    // Pan & zoom state for graph canvas
    private double panX = 0, panY = 0, zoom = 1.0;
    private double lastMouseX, lastMouseY;

    // In-memory graph model (if needed beyond the domain.graphComponents)
    private final List<NodeData> nodes = new ArrayList<>();
    private final List<EdgeData> edges = new ArrayList<>();

    // FXML-injected UI elements
    @FXML private BorderPane rootPane;
    @FXML private HBox searchContainer;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ImageView userIcon;

    @FXML private ScrollPane dashboardPane;
    @FXML private ScrollPane graphPane;
    @FXML private Canvas graphCanvas;

    @FXML private AnchorPane userMenuPopup;
    @FXML private VBox userMenuContent;
    @FXML private Button myAccountButton;
    @FXML private Button geneSearchesButton;
    @FXML private Button settingsButton;
    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private Button logOutButton;

    // Suggestion dropdown for gene search
    private final ObservableList<String> suggestions = javafx.collections.FXCollections.observableArrayList();
    private final ContextMenu suggestionsMenu = new ContextMenu();

    /**
     * Set shared services (called by application startup).
     */
    public void setServices(AllServices services) {
        this.services = services;
    }

    /**
     * Set the current user and update menu visibility.
     */
    public void setCurrentUser(Optional<User> user) {
        this.currentUser = user;
        updateUserMenuVisibility();
    }

    private void updateUserMenuVisibility() {
        boolean isLoggedIn = currentUser.isPresent();
        myAccountButton.setVisible(isLoggedIn);
        myAccountButton.setManaged(isLoggedIn);
        geneSearchesButton.setVisible(isLoggedIn);
        geneSearchesButton.setManaged(isLoggedIn);
        settingsButton.setVisible(isLoggedIn);
        settingsButton.setManaged(isLoggedIn);
        logOutButton.setVisible(isLoggedIn);
        logOutButton.setManaged(isLoggedIn);

        loginButton.setVisible(!isLoggedIn);
        loginButton.setManaged(!isLoggedIn);
        signUpButton.setVisible(!isLoggedIn);
        signUpButton.setManaged(!isLoggedIn);
    }

    @FXML
    public void initialize() {
        if (searchField == null) {
            throw new IllegalStateException("searchField was not injected—check your FXML fx:id!");
        }

        updateUserMenuVisibility();

        // Dismiss user menu when clicking outside
        rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!userMenuPopup.isVisible()) return;
            Node target = (Node) event.getTarget();
            boolean inPopup = false, onIcon = false;
            for (Node n = target; n != null; n = n.getParent()) {
                if (n == userMenuPopup) inPopup = true;
                if (n == userIcon) onIcon = true;
            }
            if (!inPopup && !onIcon) {
                userMenuPopup.setVisible(false);
                userMenuPopup.setManaged(false);
            }
        });

        // Suggestion menu width
        suggestionsMenu.prefWidthProperty().bind(searchContainer.widthProperty());

        // Autocomplete listener
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.isEmpty()) {
                Platform.runLater(() -> {
                    List<String> raw = fetchGeneSuggestions(newV);
                    List<String> matches = raw.stream()
                            .filter(s -> s.toLowerCase().startsWith(newV.toLowerCase()))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .toList();
                    if (!matches.isEmpty()) {
                        populateSuggestions(matches);
                        if (!suggestionsMenu.isShowing()) {
                            suggestionsMenu.show(searchContainer, Side.BOTTOM, 0, 0);
                        }
                    } else {
                        suggestionsMenu.hide();
                    }
                });
            } else {
                suggestionsMenu.hide();
            }
        });
        searchField.focusedProperty().addListener((obs, wasF, isF) -> {
            if (!isF) suggestionsMenu.hide();
        });

        // Pressing Enter in field triggers search
        searchField.setOnAction(evt -> doSearch(searchField.getText()));

        // Canvas pan & zoom
        graphCanvas.setOnMousePressed(this::onMousePressed);
        graphCanvas.setOnMouseDragged(this::onMouseDragged);
        graphCanvas.addEventHandler(ScrollEvent.SCROLL, this::onScrollZoom);
    }

    // Mouse handlers
    private void onMousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }
    private void onMouseDragged(MouseEvent e) {
        panX += e.getX() - lastMouseX;
        panY += e.getY() - lastMouseY;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        redraw();
    }
    private void onScrollZoom(ScrollEvent e) {
        zoom *= (e.getDeltaY() > 0 ? 1.1 : 0.9);
        redraw();
        e.consume();
    }

    private void populateSuggestions(List<String> list) {
        suggestionsMenu.getItems().clear();
        for (String gene : list) {
            Label lbl = new Label(gene);
            CustomMenuItem item = new CustomMenuItem(lbl, true);
            item.getStyleClass().add("suggestion-item");
            item.setOnAction(evt -> {
                searchField.setText(gene);
                doSearch(gene);
                suggestionsMenu.hide();
            });
            suggestionsMenu.getItems().add(item);
        }
    }

    private List<String> fetchGeneSuggestions(String prefix) {
        if (services == null) {
            var mock = new ArrayList<String>();
            if (prefix.toLowerCase().startsWith("b")) {
                mock.add("BRCA1"); mock.add("BRCA2"); mock.add("BRAF");
            } else if (prefix.toLowerCase().startsWith("t")) {
                mock.add("TP53"); mock.add("TNF"); mock.add("TERT");
            } else {
                mock.add(prefix + "_GENE1"); mock.add(prefix + "_GENE2");
            }
            return mock;
        }
        try {
            return services.getGeneDiseaseDrugCompoundService().getGeneNameSuggestion(prefix);
        } catch (ServicesException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Perform a gene search: if blank, show dashboard; otherwise show graph.
     */
    private void doSearch(String gene) {
        if (gene == null || gene.isBlank()) {
            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);
            graphPane.setVisible(false);
            graphPane.setManaged(false);
            return;
        }
        try {
            List<Interaction> interactions =
                    services.getInteractionService().findBySourceGene(gene);

            // rebuild model
            nodes.clear();
            edges.clear();

            double cw = graphCanvas.getWidth(), ch = graphCanvas.getHeight();
            double cx = cw/2, cy = ch/2;
            NodeData center = new NodeData(gene, cx, cy);
            nodes.add(center);

            int n = interactions.size();
            double radius = 200;
            for (int i = 0; i < n; i++) {
                var it = interactions.get(i);
                double angle = 2*Math.PI*i/n;
                double x = cx + radius*Math.cos(angle);
                double y = cy + radius*Math.sin(angle);
                NodeData tgt = new NodeData(it.getTargetName(), x, y);
                nodes.add(tgt);
                edges.add(new EdgeData(center, tgt));
            }

            // toggle views
            dashboardPane.setVisible(false);
            dashboardPane.setManaged(false);
            graphPane.setVisible(true);
            graphPane.setManaged(true);

            // reset pan & zoom
            zoom = 1.0;
            panX = 0;
            panY = 0;

            redraw();
        } catch (ServicesException ex) {
            ex.printStackTrace();
        }
    }

    /** Clear and redraw the graphCanvas with pan/zoom, edges, and nodes. */
    private void redraw() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.setTransform(new Affine());
        gc.clearRect(0,0,graphCanvas.getWidth(), graphCanvas.getHeight());
        gc.setTransform(new Affine(zoom,0,panX, 0,zoom,panY));

        // draw edges
        gc.setStroke(Color.GRAY);
        for (EdgeData e : edges) {
            gc.strokeLine(e.getA().getX(), e.getA().getY(),
                    e.getB().getX(), e.getB().getY());
        }
        // draw nodes with labels
        for (NodeData n : nodes) {
            gc.setFill(Color.web("#5a67d8",0.6));
            gc.fillOval(n.getX()-10, n.getY()-10, 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(n.getName(), n.getX()+12, n.getY()+4);
        }
    }

    @FXML public void showUserMenu(MouseEvent event) {
        boolean now = !userMenuPopup.isVisible();
        userMenuPopup.setVisible(now);
        userMenuPopup.setManaged(now);
        if (now) {
            Bounds b = userIcon.localToScene(userIcon.getBoundsInLocal());
            double x = b.getMinX() + userIcon.getFitWidth()
                    - userMenuPopup.getPrefWidth() - 170;
            userMenuPopup.setLayoutX(x);
            userMenuPopup.setLayoutY(b.getMaxY() - 5);
            userMenuPopup.toFront();
        }
        event.consume();
    }

    @FXML
    public void handleLogin(ActionEvent e) {
        System.out.println("Login clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui_thing/views/LoginDialog.fxml"));
            Parent root = loader.load();

            LoginDialogController controller = loader.getController();
            controller.setServices(services);
            controller.setOnLoginSuccess(this::setCurrentUser);

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Login");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/ui_thing/views/style.css").toExternalForm());
            dialogStage.setScene(scene);

            // Center the dialog on the main window
            Stage mainStage = (Stage) rootPane.getScene().getWindow();
            dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - 400) / 2);
            dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - 300) / 2);

            dialogStage.showAndWait();
        } catch (IOException ex) {
            System.err.println("Error loading login dialog: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleSignUp(ActionEvent e) {
        System.out.println("Sign Up clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui_thing/views/SignUpDialog.fxml"));
            Parent root = loader.load();

            SignUpDialogController controller = loader.getController();
            controller.setServices(services);
            controller.setOnSignUpSuccess(this::setCurrentUser);

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Sign Up");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/ui_thing/views/style.css").toExternalForm());
            dialogStage.setScene(scene);

            // Center the dialog on the main window
            Stage mainStage = (Stage) rootPane.getScene().getWindow();
            dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - 400) / 2);
            dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - 350) / 2);

            dialogStage.showAndWait();
        } catch (IOException ex) {
            System.err.println("Error loading signup dialog: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        System.out.println("Logout clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        // Clear the current user
        setCurrentUser(Optional.empty());
    }

    @FXML
    public void handleMyAccount(ActionEvent e) {
        System.out.println("My Account clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        // TODO: Navigate to account screen
    }

    @FXML
    public void handleGeneSearches(ActionEvent e) {
        System.out.println("Gene Searches clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        // TODO: Navigate to gene searches screen
    }

    @FXML
    public void handleSettings(ActionEvent e) {
        System.out.println("Settings clicked");
        userMenuPopup.setVisible(false);
        userMenuPopup.setManaged(false);

        // TODO: Navigate to settings screen
    }
}