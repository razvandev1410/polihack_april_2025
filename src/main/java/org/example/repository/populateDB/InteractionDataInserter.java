package org.example.repository.populateDB;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

/**
 * Reads pathway‐interaction data from JSON and populates the Interaction and Subtype tables.
 */
public class InteractionDataInserter {
    // Domain classes matching JSON structure
    static class Entry {
        Source source;
        String relation_type;
        List<Subtype> subtypes;
        Target target;
    }
    static class Source {
        String name;
        String type;
    }
    static class Target {
        String name;
        String type;
    }
    static class Subtype {
        String name;
        String value;
    }

    public static void main(String[] args) {
        String jsonPath = "src/main/resources/data/pathway_connections.json";
        String dbUrl   = "jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db";

        // 1) Read JSON into a Map<pathwayKey, List<Entry>>
        Map<String, List<Entry>> allData = readAllEntries(jsonPath);

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false);

            // Prepare the two insert statements.
            String insertInteractionSql = ""
                    + "INSERT INTO Interaction "
                    + "(pathway_id, source_name, source_type, relation_type, target_name, target_type) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            String insertSubtypeSql = ""
                    + "INSERT INTO Subtype (interaction_id, name, value) VALUES (?, ?, ?)";

            try (PreparedStatement interStmt = conn.prepareStatement(
                    insertInteractionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement subtypeStmt = conn.prepareStatement(insertSubtypeSql)) {

                // 2) For each pathway...
                for (Map.Entry<String, List<Entry>> pathBlock : allData.entrySet()) {
                    String pathKey = pathBlock.getKey();
                    // e.g. "path:hsa04623" → extract numeric or lookup your pathway_id
                    String pathwayId = lookupPathwayId(pathKey, conn);

                    // 3) For each JSON entry under that pathway
                    for (Entry e : pathBlock.getValue()) {
                        // split source/target names on whitespace
                        String[] sources = e.source.name.split("\\s+");
                        String[] targets = e.target.name.split("\\s+");

                        // Cartesian product
                        for (String src : sources) {
                            for (String tgt : targets) {
                                // Insert into Interaction
                                interStmt.setString(1, pathwayId);
                                interStmt.setString(2, src);
                                interStmt.setString(3, e.source.type);
                                interStmt.setString(4, e.relation_type);
                                interStmt.setString(5, tgt);
                                interStmt.setString(6, e.target.type);
                                interStmt.executeUpdate();

                                // retrieve generated interaction_id
                                try (ResultSet keys = interStmt.getGeneratedKeys()) {
                                    if (keys.next()) {
                                        int interactionId = keys.getInt(1);
                                        // Insert each subtype for this interaction
                                        for (Subtype st : e.subtypes) {
                                            subtypeStmt.setInt   (1, interactionId);
                                            subtypeStmt.setString(2, st.name);
                                            subtypeStmt.setString(3, st.value);
                                            subtypeStmt.executeUpdate();
                                        }
                                    } else {
                                        throw new SQLException("Failed to retrieve interaction_id.");
                                    }
                                }
                            }
                        }
                    }
                }

                conn.commit();
                System.out.println("All interaction data inserted successfully.");
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads the entire JSON structure into a Map of lists.
     */
    private static Map<String, List<Entry>> readAllEntries(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, List<Entry>>>(){}.getType();
            return gson.fromJson(reader, mapType);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * Lookup or parse the pathway_id from the JSON key.
     * Adjust as needed if you store pathways differently.
     */
    private static String lookupPathwayId(String pathKey, Connection conn) throws SQLException {
        String sql = "SELECT pathway_id FROM Pathways WHERE pathway_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pathKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pathway_id");
                } else {
                    throw new SQLException("No pathway found for " + pathKey);
                }
            }
        }
    }
}
