package org.example.repository.populateDB;

import java.sql.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GeneDatabase {

    // Helper method: extract aliases from the description string.
    // Assumes the aliases are before the semicolon.
    public static String[] extractAliases(String desc) {
        String aliasesPart = desc.split(";")[0]; // take the part before the semicolon
        String[] aliases = aliasesPart.split(",");
        for (int i = 0; i < aliases.length; i++) {
            aliases[i] = aliases[i].trim();
        }
        return aliases;
    }

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement insertGeneStmt = null;
        PreparedStatement insertAliasStmt = null;
        PreparedStatement selectByIdStmt = null;
        PreparedStatement selectByAliasStmt = null;
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to (or create) the SQLite database file.
            conn = DriverManager.getConnection("jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db");
            conn.setAutoCommit(false); // use manual commit

            Statement stmt = conn.createStatement();

            // Create Genes table: Each gene is stored only once.
            String createGenes = "CREATE TABLE IF NOT EXISTS Genes (" +
                    "gene_id TEXT PRIMARY KEY" +
                    ")";
            stmt.execute(createGenes);

            // Create GeneAliases table: Each alias is stored in its own row.
            String createGeneAliases = "CREATE TABLE IF NOT EXISTS GeneAliases (" +
                    "gene_id TEXT, " +
                    "gene_alias TEXT, " +
                    "PRIMARY KEY (gene_id, gene_alias), " +
                    "FOREIGN KEY (gene_id) REFERENCES Genes(gene_id)" +
                    ")";
            stmt.execute(createGeneAliases);

            // Read JSON data from an external file called "genes.json"
            // Ensure that the file is in the working directory or specify the full path.
            InputStream is = new FileInputStream("src/main/resources/data/gene_map.json");
            JSONTokener tokener = new JSONTokener(is);
            JSONObject jsonData = new JSONObject(tokener);
            is.close();

            // Prepare SQL statements for inserting data.
            String insertGeneSQL = "INSERT OR IGNORE INTO Genes (gene_id) VALUES (?)";
            insertGeneStmt = conn.prepareStatement(insertGeneSQL);

            String insertAliasSQL = "INSERT OR IGNORE INTO GeneAliases (gene_id, gene_alias) VALUES (?, ?)";
            insertAliasStmt = conn.prepareStatement(insertAliasSQL);

            // Iterate over the JSON object and insert the data.
            Iterator<String> keys = jsonData.keys();
            while (keys.hasNext()) {
                String geneId = keys.next();
                String description = jsonData.getString(geneId);

                // Insert the gene into the Genes table.
                insertGeneStmt.setString(1, geneId);
                insertGeneStmt.executeUpdate();

                // Extract aliases and insert them into the GeneAliases table.
                String[] aliases = extractAliases(description);
                for (String alias : aliases) {
                    insertAliasStmt.setString(1, geneId);
                    insertAliasStmt.setString(2, alias);
                    insertAliasStmt.executeUpdate();
                }
            }

            conn.commit();

            // --- Example Queries ---

            // Query by gene_id
            String geneQuery = "hsa:339451";
            String queryByIdSQL = "SELECT g.gene_id, ga.gene_alias " +
                    "FROM Genes g " +
                    "LEFT JOIN GeneAliases ga ON g.gene_id = ga.gene_id " +
                    "WHERE g.gene_id = ?";
            selectByIdStmt = conn.prepareStatement(queryByIdSQL);
            selectByIdStmt.setString(1, geneQuery);
            ResultSet rs = selectByIdStmt.executeQuery();

            System.out.println("Results for gene_id '" + geneQuery + "':");
            while (rs.next()) {
                String id = rs.getString("gene_id");
                String alias = rs.getString("gene_alias");
                System.out.println(id + " -> " + alias);
            }
            rs.close();

            // Query by gene alias
            String aliasQuery = "KLHL17";
            String queryByAliasSQL = "SELECT g.gene_id, ga.gene_alias " +
                    "FROM Genes g " +
                    "JOIN GeneAliases ga ON g.gene_id = ga.gene_id " +
                    "WHERE ga.gene_alias = ?";
            selectByAliasStmt = conn.prepareStatement(queryByAliasSQL);
            selectByAliasStmt.setString(1, aliasQuery);
            rs = selectByAliasStmt.executeQuery();

            System.out.println("\nResults for gene alias '" + aliasQuery + "':");
            while (rs.next()) {
                String id = rs.getString("gene_id");
                String alias = rs.getString("gene_alias");
                System.out.println(id + " -> " + alias);
            }
            rs.close();

            // Clean up resources.
            insertGeneStmt.close();
            insertAliasStmt.close();
            selectByIdStmt.close();
            selectByAliasStmt.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
