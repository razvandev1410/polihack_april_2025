package org.example.repository.populateDB;

import java.sql.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TransposeGenePathways {

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement insertGeneStmt = null;
        PreparedStatement insertPathwayStmt = null;
        PreparedStatement insertGenePathwayStmt = null;

        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to (or create) the SQLite database file
            conn = DriverManager.getConnection("jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db");
            conn.setAutoCommit(false); // We'll commit changes manually

            // Create database tables
            Statement stmt = conn.createStatement();

            // Create Genes table
            String createGenesTable = "CREATE TABLE IF NOT EXISTS Genes ("
                    + "gene_id TEXT PRIMARY KEY"
                    + ")";
            stmt.execute(createGenesTable);

            // Create Pathways table
            String createPathwaysTable = "CREATE TABLE IF NOT EXISTS Pathways ("
                    + "pathway_id TEXT PRIMARY KEY"
                    + ")";
            stmt.execute(createPathwaysTable);

            // Create GenePathways table (join table)
            String createGenePathwaysTable = "CREATE TABLE IF NOT EXISTS GenePathways ("
                    + "gene_id TEXT, "
                    + "pathway_id TEXT, "
                    + "PRIMARY KEY (gene_id, pathway_id), "
                    + "FOREIGN KEY (gene_id) REFERENCES Genes(gene_id), "
                    + "FOREIGN KEY (pathway_id) REFERENCES Pathways(pathway_id)"
                    + ")";
            stmt.execute(createGenePathwaysTable);
            stmt.close();

            // Prepare SQL statements for inserting data
            String insertGeneSQL = "INSERT OR IGNORE INTO Genes (gene_id) VALUES (?)";
            insertGeneStmt = conn.prepareStatement(insertGeneSQL);

            String insertPathwaySQL = "INSERT OR IGNORE INTO Pathways (pathway_id) VALUES (?)";
            insertPathwayStmt = conn.prepareStatement(insertPathwaySQL);

            String insertGenePathwaySQL = "INSERT OR IGNORE INTO GenePathways (gene_id, pathway_id) VALUES (?, ?)";
            insertGenePathwayStmt = conn.prepareStatement(insertGenePathwaySQL);

            // Read JSON data from an external file called "gene_pathways.json"
            // Adjust the file path as needed. For example, if placed in src/main/resources, use getResourceAsStream.
            InputStream is = new FileInputStream("src/main/resources/data/pathways_output.json");
            JSONTokener tokener = new JSONTokener(is);
            JSONObject jsonData = new JSONObject(tokener);
            is.close();

            // Insert unique pathways into the Pathways table
            JSONArray uniquePathwaysArray = jsonData.getJSONArray("unique_pathways");
            for (int i = 0; i < uniquePathwaysArray.length(); i++) {
                String pathway = uniquePathwaysArray.getString(i);
                insertPathwayStmt.setString(1, pathway);
                insertPathwayStmt.executeUpdate();
            }

            // Insert genes and gene-to-pathway associations
            JSONObject geneToPathwaysObj = jsonData.getJSONObject("gene_to_pathways");
            Iterator<String> geneKeys = geneToPathwaysObj.keys();
            while (geneKeys.hasNext()) {
                String geneId = geneKeys.next();
                // Insert gene into Genes table
                insertGeneStmt.setString(1, geneId);
                insertGeneStmt.executeUpdate();

                // Retrieve the array of pathways for this gene
                JSONArray pathwaysArray = geneToPathwaysObj.getJSONArray(geneId);
                for (int j = 0; j < pathwaysArray.length(); j++) {
                    String pathway = pathwaysArray.getString(j);
                    // Insert the association into GenePathways
                    insertGenePathwayStmt.setString(1, geneId);
                    insertGenePathwayStmt.setString(2, pathway);
                    insertGenePathwayStmt.executeUpdate();
                }
            }

            // Commit all changes
            conn.commit();
            System.out.println("Database successfully populated!");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (insertGeneStmt != null)
                    insertGeneStmt.close();
                if (insertPathwayStmt != null)
                    insertPathwayStmt.close();
                if (insertGenePathwayStmt != null)
                    insertGenePathwayStmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}

