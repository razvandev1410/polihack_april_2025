package org.example.repository.populateDB;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeneDiseaseDataInserter {

    // Path to your merged JSON file
    private static final String JSON_PATH =
            "src/main/resources/data/GeneToDiseases.json";

    // JDBC URL of your SQLite database
    private static final String DB_URL =
            "jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db";

    public static void main(String[] args) throws Exception {
        // 1) Register SQLite driver & open connection
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 2) Create the junction table if it doesn't exist
            try (Statement st = conn.createStatement()) {
                String ddl = ""
                        + "CREATE TABLE IF NOT EXISTS GeneDiseases ("
                        + "  gene_id     TEXT,"
                        + "  disease_id  TEXT,"
                        + "  PRIMARY KEY(gene_id, disease_id),"
                        + "  FOREIGN KEY(gene_id)    REFERENCES Genes(gene_id),"
                        + "  FOREIGN KEY(disease_id) REFERENCES Diseases(id)"
                        + ")";
                st.executeUpdate(ddl);
            }

            // 3) Prepare insert statement
            String insertSql =
                    "INSERT OR IGNORE INTO GeneDiseases (gene_id, disease_id) "
                            + "VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                // 4) Parse the merged JSON file
                try (Reader reader = new FileReader(JSON_PATH)) {
                    JsonObject root = JsonParser.parseReader(reader)
                            .getAsJsonObject();
                    JsonObject mapping =
                            root.getAsJsonObject("gene_to_diseases");

                    // 5) Iterate each gene → list of diseases
                    for (String geneId : mapping.keySet()) {
                        JsonArray arr = mapping.getAsJsonArray(geneId);
                        for (JsonElement elt : arr) {
                            String raw = elt.getAsString();
                            // strip any prefix (e.g. "ds:" or "disease:")
                            String diseaseId = raw.contains(":")
                                    ? raw.substring(raw.indexOf(':') + 1)
                                    : raw;

                            // bind & execute
                            ps.setString(1, geneId);
                            ps.setString(2, diseaseId);
                            ps.executeUpdate();
                        }
                    }
                }
            }

            System.out.println("All gene→disease mappings inserted.");
        }
    }
}
