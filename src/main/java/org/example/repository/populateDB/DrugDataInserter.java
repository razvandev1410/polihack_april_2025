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

import org.example.domain.Drug;
import org.example.repository.JdbcUtils;

public class DrugDataInserter {
    public static void main(String[] args) {
        JdbcUtils jdbcUtils;
        // Step 1: Read drug data from the JSON file.
        List<Drug> drugs = readDrugsFromJson("src/main/resources/data/drugs.json");

        // Step 2: Set up the SQLite database and tables.

        Connection connection = null;
        try {
            // Load the SQLite JDBC driver (ensure the driver is in the classpath).
            Class.forName("org.sqlite.JDBC");

            // Connect to (or create) the SQLite database file.
            connection = DriverManager.getConnection("jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db");

            Statement stmt = connection.createStatement();
            // Create the Drugs table if it doesn't already exist.
            String createDrugsTable = "CREATE TABLE IF NOT EXISTS Drugs ("
                    + "id TEXT PRIMARY KEY)";
            stmt.executeUpdate(createDrugsTable);

            // Create the Aliases table if it doesn't already exist.
            String createAliasesTable = "CREATE TABLE IF NOT EXISTS Aliases ("
                    + "drug_id TEXT, "
                    + "alias TEXT, "
                    + "FOREIGN KEY(drug_id) REFERENCES Drugs(id))";
            stmt.executeUpdate(createAliasesTable);

            // Step 3: Prepare statements for inserting data.
            String insertDrugSQL = "INSERT OR IGNORE INTO Drugs (id) VALUES (?)";
            String insertAliasSQL = "INSERT INTO DrugAliases (drug_id, alias) VALUES (?, ?)";
            PreparedStatement drugStmt = connection.prepareStatement(insertDrugSQL);
            PreparedStatement aliasStmt = connection.prepareStatement(insertAliasSQL);

            // Step 4: Iterate over each drug and insert the data.
            for (Drug drug : drugs) {
                String drugId = drug.getId();

                // Insert the drug ID into the Drugs table.
                drugStmt.setString(1, drugId);
                drugStmt.executeUpdate();

                // Split description into aliases based on the semicolon separator.
                String[] aliases = drug.getAliases().split(";");
                for (String alias : aliases) {
                    alias = alias.trim();
                    // Insert only non-empty aliases.
                    if (!alias.isEmpty()) {
                        aliasStmt.setString(1, drugId);
                        aliasStmt.setString(2, alias);
                        aliasStmt.executeUpdate();
                    }
                }
            }

            System.out.println("Data inserted successfully into drugs.db.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the database connection.
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads the drugs from a JSON file using Gson.
     *
     * @param filename The JSON file name.
     * @return A list of Drug objects.
     */
    private static List<Drug> readDrugsFromJson(String filename) {
        List<Drug> drugs = new ArrayList<>();
        try (Reader reader = new FileReader(filename)) {
            Gson gson = new Gson();
            drugs = gson.fromJson(reader, new TypeToken<List<Drug>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drugs;
    }
}
