package org.example.repository.populateDB;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DiseaseDrugInserter {
    private static final String DB_URL    = "jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db";
    private static final String JSON_PATH = "src/main/resources/data/all_disease_drugs.json";

    public static void main(String[] args) {
        // Step 1: Read the disease–drug map from JSON
        Map<String, List<String>> diseaseDrugs = readDiseaseDrugsFromJson(JSON_PATH);

        Connection connection = null;
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the SQLite database (will create file if it doesn't exist)
            connection = DriverManager.getConnection(DB_URL);

            Statement stmt = connection.createStatement();
            // Create the join table
            String createJoinTable = ""
                    + "CREATE TABLE IF NOT EXISTS DiseaseDrugs ("
                    + "  disease_id TEXT NOT NULL, "
                    + "  drug_id    TEXT NOT NULL, "
                    + "  PRIMARY KEY(disease_id, drug_id), "
                    + "  FOREIGN KEY(disease_id) REFERENCES Diseases(id), "
                    + "  FOREIGN KEY(drug_id)    REFERENCES Drugs(id)"
                    + ")";
            stmt.executeUpdate(createJoinTable);

            // Prepare the insert statement
            String insertSQL =
                    "INSERT OR IGNORE INTO DiseaseDrugs (disease_id, drug_id) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertSQL);

            // Step 2: Insert each disease–drug pair
            for (Map.Entry<String, List<String>> entry : diseaseDrugs.entrySet()) {
                String diseaseId = entry.getKey();
                List<String> drugs = entry.getValue();
                if (drugs == null) continue;
                for (String drugId : drugs) {
                    ps.setString(1, diseaseId);
                    ps.setString(2, drugId);
                    ps.executeUpdate();
                }
            }

            System.out.println("All disease–drug pairs inserted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close connection
            if (connection != null) {
                try { connection.close(); }
                catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Reads the disease→drugs map from a JSON file using Gson.
     * @param filename Path to all_disease_drugs.json
     * @return Map from disease ID to list of drug IDs
     */
    private static Map<String, List<String>> readDiseaseDrugsFromJson(String filename) {
        try (Reader reader = new FileReader(filename)) {
            return new Gson().fromJson(
                    reader,
                    new TypeToken<Map<String, List<String>>>() {}.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();  // empty map on failure
        }
    }
}

