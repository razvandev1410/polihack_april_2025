package org.example.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * A non-Application class that can be instantiated with a chosen startNode.
 * When you call showWindow(), it will pop up a JavaFX visualization with inbound BFS (left)
 * and outbound BFS (right).
 */
public class GraphVisualise {
    private static final String JSON_FILE_PATH = "src/main/resources/data/direct_connections.json";
    private static final String GENE_TO_DRUG_JSON = "src/main/resources/data/gene_to_drug.json";

    private Map<String, List<String>> adjacencyMap;
    private Map<String, List<String>> reverseAdjMap;

    private static final String DIRECT_CONNECTIONS_JSON = "src/main/resources/data/direct_connections.json";

    private Map<String, List<String>> geneToDrugMap;

    private List<List<String>> rightLevels;
    private List<List<String>> leftLevels;

    private final String startNode;

    private static final double RADIUS = 20;
    private static final double LAYER_SPACING = 200;
    private static final double VERTICAL_SPACING = 60;

    private static final double CENTER_X = 600;
    private static final double CENTER_Y = 300;

    private static boolean javaFxStarted = false;

    public GraphVisualise(String startNode) {
        this.startNode = startNode;

        try {
            adjacencyMap = parseJsonToMap(JSON_FILE_PATH);
            reverseAdjMap = buildReverseAdjacency(adjacencyMap);
            geneToDrugMap = parseGeneToDrugMap(GENE_TO_DRUG_JSON);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        rightLevels = bfsLevels(adjacencyMap, this.startNode);
        leftLevels  = bfsLevelsLimited(reverseAdjMap, this.startNode, 4);
    }

    public void showWindow() {
        Group graphGroup = buildGraphGroup();

        ScrollPane scrollPane = new ScrollPane(graphGroup);
        scrollPane.setPannable(true);

        Scene scene = new Scene(scrollPane, 1400, 900, Color.WHITE);
        Stage stage = new Stage();
        stage.setTitle("Graph BFS from " + startNode);
        stage.setScene(scene);
        stage.show();
    }

    public void showCompoundListWindow() {
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (int i = 0; i < rightLevels.size(); i++) {
            Set<String> drugsRight = new HashSet<>();
            for (String gene : rightLevels.get(i)) {
                List<String> geneDrugs = geneToDrugMap.get(gene);
                if (geneDrugs != null) {
                    drugsRight.addAll(geneDrugs);
                }
            }
            Label rightLabel = new Label("Level " + i + " compound list on the right: " + drugsRight);
            rightLabel.setWrapText(true);
            rightLabel.setFont(Font.font(14));
            contentBox.getChildren().add(rightLabel);
        }

        for (int i = 0; i < leftLevels.size(); i++) {
            Set<String> drugsLeft = new HashSet<>();
            for (String gene : leftLevels.get(i)) {
                List<String> geneDrugs = geneToDrugMap.get(gene);
                if (geneDrugs != null) {
                    drugsLeft.addAll(geneDrugs);
                }
            }
            Label leftLabel = new Label("Level " + i + " compound list on the left: " + drugsLeft);
            leftLabel.setWrapText(true);
            leftLabel.setFont(Font.font(14));
            contentBox.getChildren().add(leftLabel);
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setPrefSize(400, 300);

        Stage compoundStage = new Stage();
        compoundStage.setTitle("Compound Lists for BFS Levels");
        compoundStage.setScene(new Scene(scrollPane));
        compoundStage.show();
    }

    private void createAndShowStage() {
        Group graphGroup = buildGraphGroup();

        ScrollPane scrollPane = new ScrollPane(graphGroup);
        scrollPane.setPannable(true);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                double zoomFactor = (e.getDeltaY() > 0) ? 1.1 : 1/1.1;
                graphGroup.setScaleX(graphGroup.getScaleX() * zoomFactor);
                graphGroup.setScaleY(graphGroup.getScaleY() * zoomFactor);
                e.consume();
            }
        });

        Scene scene = new Scene(scrollPane, 1400, 900, Color.WHITE);

        Stage stage = new Stage();
        stage.setTitle("Graph BFS from " + startNode);
        stage.setScene(scene);
        stage.show();
    }

    private Group buildGraphGroup() {
        Group group = new Group();

        drawLeftBFS(group, leftLevels);
        drawRightBFS(group, rightLevels);

        return group;
    }


    private Map<String, List<String>> parseJsonToMap(String filePath) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        JSONObject json = new JSONObject(sb.toString());

        Map<String, List<String>> map = new HashMap<>();
        for (String key : json.keySet()) {
            JSONArray arr = json.getJSONArray(key);
            List<String> neighbors = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                neighbors.add(arr.getString(i));
            }
            map.put(key, neighbors);
        }
        return map;
    }

    private Map<String, List<String>> buildReverseAdjacency(Map<String, List<String>> forward) {
        Map<String, List<String>> reverse = new HashMap<>();
        for (String node : forward.keySet()) {
            reverse.putIfAbsent(node, new ArrayList<>());
        }
        // For each edge A->B in forward, add A to reverse[B]
        for (String src : forward.keySet()) {
            for (String nbr : forward.get(src)) {
                reverse.computeIfAbsent(nbr, k -> new ArrayList<>()).add(src);
            }
        }
        return reverse;
    }

    private List<List<String>> bfsLevels(Map<String, List<String>> adjacency, String startNode) {
        List<List<String>> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        visited.add(startNode);

        Queue<String> queue = new LinkedList<>();
        queue.offer(startNode);

        while (!queue.isEmpty()) {
            int size = queue.size();
            List<String> level = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                level.add(current);

                for (String neighbor : adjacency.getOrDefault(current, Collections.emptyList())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
            result.add(level);
        }
        return result;
    }


    private List<List<String>> bfsLevelsLimited(Map<String, List<String>> adjacency, String startNode, int maxDepth) {
        List<List<String>> levels = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        visited.add(startNode);

        Queue<String> queue = new LinkedList<>();
        queue.offer(startNode);

        int depth = 0;
        while (!queue.isEmpty() && depth <= maxDepth) {
            int size = queue.size();
            List<String> thisLevel = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String cur = queue.poll();
                thisLevel.add(cur);

                if (depth < maxDepth) {
                    for (String nbr : adjacency.getOrDefault(cur, Collections.emptyList())) {
                        if (!visited.contains(nbr)) {
                            visited.add(nbr);
                            queue.offer(nbr);
                        }
                    }
                }
            }
            levels.add(thisLevel);
            depth++;
        }
        return levels;
    }

    private Map<String, List<String>> parseGeneToDrugMap(String filePath) throws IOException, JSONException {
        Map<String, List<String>> geneDrugMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        JSONObject root = new JSONObject(sb.toString());
        JSONArray arr = root.getJSONArray("gene_id_to_drug_id");
        for (int i = 0; i < arr.length(); i++) {
            JSONArray pair = arr.getJSONArray(i);
            if (pair.length() == 2) {
                String geneId = pair.getString(0);
                String drugId = pair.getString(1);
                geneDrugMap.computeIfAbsent(geneId, k -> new ArrayList<>()).add(drugId);
            }
        }
        return geneDrugMap;
    }

    private void drawLeftBFS(Group group, List<List<String>> levels) {
        Map<String, double[]> nodePositions = new HashMap<>();


        for (int d = 0; d < levels.size(); d++) {
            List<String> layerNodes = levels.get(d);
            double x = CENTER_X - (d * LAYER_SPACING);
            double startY = CENTER_Y - (layerNodes.size() * VERTICAL_SPACING / 2.0);

            for (int i = 0; i < layerNodes.size(); i++) {
                String nodeId = layerNodes.get(i);
                double y = startY + i * VERTICAL_SPACING;

                Circle circle = new Circle(x, y, RADIUS);
                circle.setFill(d == 0 ? Color.ORANGE : Color.LIGHTPINK);

                Label label = new Label(nodeId);
                label.setFont(Font.font(12));
                label.setLayoutX(x + RADIUS + 5);
                label.setLayoutY(y - 8);

                group.getChildren().addAll(circle, label);
                nodePositions.put(nodeId, new double[]{x, y});

                circle.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        showGeneDrugWindow(nodeId);
                    }
                });
            }
        }

        for (int d = 0; d < levels.size() - 1; d++) {
            for (String src : levels.get(d)) {
                List<String> neighbors = reverseAdjMap.getOrDefault(src, Collections.emptyList());
                for (String nbr : neighbors) {
                    if (levels.get(d + 1).contains(nbr)) {
                        double[] sPos = nodePositions.get(src);
                        double[] tPos = nodePositions.get(nbr);
                        if (sPos != null && tPos != null) {
                            Line edge = new Line(sPos[0], sPos[1], tPos[0], tPos[1]);
                            edge.setStroke(Color.GRAY);
                            group.getChildren().add(edge);

                            drawArrowhead(group, sPos[0], sPos[1], tPos[0], tPos[1]);
                        }
                    }
                }
            }
        }
    }

    private void drawRightBFS(Group group, List<List<String>> levels) {
        Map<String, double[]> nodePositions = new HashMap<>();

        for (int d = 0; d < levels.size(); d++) {
            List<String> layerNodes = levels.get(d);
            double x = CENTER_X + (d * LAYER_SPACING);
            double startY = CENTER_Y - (layerNodes.size() * VERTICAL_SPACING / 2.0);

            for (int i = 0; i < layerNodes.size(); i++) {
                String nodeId = layerNodes.get(i);
                double y = startY + i * VERTICAL_SPACING;

                Circle circle = new Circle(x, y, RADIUS);
                circle.setFill(d == 0 ? Color.ORANGE : Color.LIGHTBLUE);

                Label label = new Label(nodeId);
                label.setFont(Font.font(12));
                label.setLayoutX(x + RADIUS + 5);
                label.setLayoutY(y - 8);

                group.getChildren().addAll(circle, label);
                nodePositions.put(nodeId, new double[]{x, y});

                circle.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        showGeneDrugWindow(nodeId);
                    }
                });
            }
        }

        for (int d = 0; d < levels.size() - 1; d++) {
            for (String src : levels.get(d)) {
                List<String> neighbors = adjacencyMap.getOrDefault(src, Collections.emptyList());
                for (String nbr : neighbors) {
                    if (levels.get(d + 1).contains(nbr)) {
                        double[] sPos = nodePositions.get(src);
                        double[] tPos = nodePositions.get(nbr);
                        if (sPos != null && tPos != null) {
                            Line edge = new Line(sPos[0], sPos[1], tPos[0], tPos[1]);
                            edge.setStroke(Color.GRAY);
                            group.getChildren().add(edge);

                            drawArrowhead(group, sPos[0], sPos[1], tPos[0], tPos[1]);
                        }
                    }
                }
            }
        }
    }

    private void drawArrowhead(Group group, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);

        double arrowLength = 10;
        double arrowAngle = Math.toRadians(20);

        double xBack = x2 - arrowLength * Math.cos(angle);
        double yBack = y2 - arrowLength * Math.sin(angle);

        double xLeft = xBack - arrowLength * Math.cos(angle + arrowAngle);
        double yLeft = yBack - arrowLength * Math.sin(angle + arrowAngle);

        double xRight = xBack - arrowLength * Math.cos(angle - arrowAngle);
        double yRight = yBack - arrowLength * Math.sin(angle - arrowAngle);

        Line leftLine = new Line(x2, y2, xLeft, yLeft);
        Line rightLine = new Line(x2, y2, xRight, yRight);
        leftLine.setStroke(Color.GRAY);
        rightLine.setStroke(Color.GRAY);

        group.getChildren().addAll(leftLine, rightLine);
    }

    private void showGeneDrugWindow(String geneId) {
        List<String> drugs = geneToDrugMap.getOrDefault(geneId, Collections.emptyList());
        StringBuilder sb = new StringBuilder("Gene: " + geneId + "\nDrugs:\n");
        if (drugs.isEmpty()) {
            sb.append("(No known drugs)");
        } else {
            for (String drug : drugs) {
                sb.append("- ").append(drug).append("\n");
            }
        }
        Label label = new Label(sb.toString());
        label.setFont(Font.font(14));
        // Wrap in a ScrollPane in case there are many drugs
        ScrollPane pane = new ScrollPane(label);
        pane.setPrefSize(300, 200);
        Stage drugStage = new Stage();
        drugStage.setTitle("Drugs for " + geneId);
        drugStage.setScene(new Scene(pane));
        drugStage.show();
    }
}