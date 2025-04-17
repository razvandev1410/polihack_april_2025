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

import org.example.domain.Compound;  // your domain class
import org.example.repository.JdbcUtils;

public class CompoundDataInserter {
    public static void main(String[] args) {
        // Step 1: Read compound data from the JSON file.
        List<Compound> compounds = readCompoundsFromJson(
                "src/main/resources/data/compounds.json"
        );

        Connection connection = null;
        try {
            // Load the SQLite JDBC driver.
            Class.forName("org.sqlite.JDBC");

            // Connect to (or create) the SQLite database file.
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:D:\\Facultate\\polihack\\GeneExplorer.db"
            );

            Statement stmt = connection.createStatement();
            // Create the Compounds table.
            String createCompoundsTable = ""
                    + "CREATE TABLE IF NOT EXISTS Compounds ("
                    + "  id TEXT PRIMARY KEY"
                    + ")";
            stmt.executeUpdate(createCompoundsTable);

            // Create the CompoundAliases table.
            String createAliasesTable = ""
                    + "CREATE TABLE IF NOT EXISTS CompoundAliases ("
                    + "  compound_id TEXT, "
                    + "  alias TEXT, "
                    + "  FOREIGN KEY(compound_id) REFERENCES Compounds(id)"
                    + ")";
            stmt.executeUpdate(createAliasesTable);

            // Prepare insert statements.
            String insertCompoundSQL =
                    "INSERT OR IGNORE INTO Compounds (id) VALUES (?)";
            String insertAliasSQL =
                    "INSERT INTO CompoundAliases (compound_id, alias) VALUES (?, ?)";
            PreparedStatement compoundStmt =
                    connection.prepareStatement(insertCompoundSQL);
            PreparedStatement aliasStmt =
                    connection.prepareStatement(insertAliasSQL);

            // Step 2: Insert each compound and its aliases.
            for (Compound cpd : compounds) {
                String compoundId = cpd.getId();
                compoundStmt.setString(1, compoundId);
                compoundStmt.executeUpdate();

                // Split the description into aliases.
                String[] aliases = cpd.getEliases().split(";");
                for (String alias : aliases) {
                    alias = alias.trim();
                    if (!alias.isEmpty()) {
                        aliasStmt.setString(1, compoundId);
                        aliasStmt.setString(2, alias);
                        aliasStmt.executeUpdate();
                    }
                }
            }

            System.out.println("Compound data inserted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close connection.
            if (connection != null) {
                try { connection.close(); }
                catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Reads the compounds from a JSON file using Gson.
     * @param filename Path to compounds.json
     * @return List of Compound objects.
     */
    private static List<Compound> readCompoundsFromJson(String filename) {
        List<Compound> compounds = new ArrayList<>();
        try (Reader reader = new FileReader(filename)) {
            Gson gson = new Gson();
            compounds = gson.fromJson(
                    reader,
                    new TypeToken<List<Compound>>() {}.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compounds;
    }
}
