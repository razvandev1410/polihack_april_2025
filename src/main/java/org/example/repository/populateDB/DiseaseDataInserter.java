package org.example.repository.populateDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.example.domain.Disease;

public class DiseaseDataInserter {
    public static void main(String[] args) {
        // 1. Load disease records from JSON
        List<Disease> diseases = readDiseasesFromJson(
                "src/main/resources/data/diseases.json"
        );

        Connection connection = null;
        try {
            // 2. Initialize SQLite connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db"
            );

            Statement stmt = connection.createStatement();
            // 3a. Create Diseases table
            String createDiseasesTable =
                    "CREATE TABLE IF NOT EXISTS Diseases ("
                            + "  id TEXT PRIMARY KEY"
                            + ")";
            stmt.executeUpdate(createDiseasesTable);

            // 3b. Create DiseaseAliases table
            String createAliasesTable =
                    "CREATE TABLE IF NOT EXISTS DiseaseAliases ("
                            + "  disease_id TEXT, "
                            + "  alias TEXT, "
                            + "  FOREIGN KEY(disease_id) REFERENCES Diseases(id)"
                            + ")";
            stmt.executeUpdate(createAliasesTable);

            // 4. Prepare insert statements
            String insertDiseaseSQL =
                    "INSERT OR IGNORE INTO Diseases (id) VALUES (?)";
            String insertAliasSQL =
                    "INSERT INTO DiseaseAliases (disease_id, alias) VALUES (?, ?)";
            PreparedStatement diseaseStmt =
                    connection.prepareStatement(insertDiseaseSQL);
            PreparedStatement aliasStmt =
                    connection.prepareStatement(insertAliasSQL);

            // 5. Iterate and insert
            for (Disease dis : diseases) {
                String diseaseId = dis.getId();
                diseaseStmt.setString(1, diseaseId);
                diseaseStmt.executeUpdate();

                // split description into individual aliases
                String[] aliases = dis.getAliases().split(";");
                for (String alias : aliases) {
                    alias = alias.trim();
                    if (!alias.isEmpty()) {
                        aliasStmt.setString(1, diseaseId);
                        aliasStmt.setString(2, alias);
                        aliasStmt.executeUpdate();
                    }
                }
            }

            System.out.println("Disease data inserted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6. Cleanup
            if (connection != null) {
                try { connection.close(); }
                catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Reads the diseases from a JSON file using Gson.
     *
     * @param filename path to diseases.json
     * @return list of Disease objects
     */
    private static List<Disease> readDiseasesFromJson(String filename) {
        List<Disease> diseases = new ArrayList<>();
        try (Reader reader = new FileReader(filename)) {
            Gson gson = new Gson();
            diseases = gson.fromJson(
                    reader,
                    new TypeToken<List<Disease>>() {}.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diseases;
    }
}
