package org.example.repository.entitiesRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.Compound;
import org.example.domain.Disease;
import org.example.domain.Drug;
import org.example.domain.Gene;
import org.example.repository.JdbcUtils;
import org.example.repository.RepositoryException;
import org.example.repository.interfaces.IGeneDiseaseDrugCompoundRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class GeneDiseaseDrugCompoundRepository implements IGeneDiseaseDrugCompoundRepository {
    private final JdbcUtils jdbcUtils;
    private static final Logger log = LogManager.getLogger(GeneDiseaseDrugCompoundRepository.class);

    public GeneDiseaseDrugCompoundRepository(Properties properties) {
        log.info("Initializing GeneDiseaseDrugCompoundRepository...");
        this.jdbcUtils = new JdbcUtils(properties);
    }

    private Connection connect() throws SQLException {
        return jdbcUtils.getConnection();
    }

    private String findCanonicalId(String table, String alias) throws RepositoryException {
        String sql = "SELECT id FROM " + table + " WHERE alias = ? LIMIT 1";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Error finding canonical id in {} for alias={}", table, alias, e);
            throw new RepositoryException(e);
        }
    }

    private String fetchFromUrl(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // --- existence checks via alias tables ---

    @Override
    public boolean existGene(Gene potentialGene) throws RepositoryException {
        if (potentialGene.getGeneName() == null) throw new IllegalArgumentException("geneName must not be null");
        return findCanonicalId("GeneAliases", potentialGene.getGeneName()) != null;
    }

    @Override
    public boolean existDrug(Drug potentialDrug) throws RepositoryException {
        if (potentialDrug.getName() == null) throw new IllegalArgumentException("drugName must not be null");
        return findCanonicalId("DrugAliases", potentialDrug.getName()) != null;
    }

    @Override
    public boolean existDisease(Disease potentialDisease) throws RepositoryException {
        if (potentialDisease.getName() == null) throw new IllegalArgumentException("diseaseName must not be null");
        return findCanonicalId("DiseaseAliases", potentialDisease.getName()) != null;
    }

    @Override
    public boolean existCompound(Compound potentialCompound) throws RepositoryException {
        if (potentialCompound.getName() == null) throw new IllegalArgumentException("compoundName must not be null");
        return findCanonicalId("CompoundAliases", potentialCompound.getName()) != null;
    }

    // --- retrieve info via KEGG REST or alias table ---

    @Override
    public HashMap<String, String> retrieveInfoAboutGene(Gene potentialGene) throws RepositoryException {
        String id = findCanonicalId("GeneAliases", potentialGene.getGeneName());
        if (id == null) return new HashMap<>();

        String url = "http://rest.kegg.jp/get/" + id;
        try {
            String keggData = fetchFromUrl(url);
            HashMap<String,String> info = new HashMap<>();
            info.put("id", id);
            info.put("keggEntry", keggData);
            return info;
        } catch (IOException e) {
            log.error("Error fetching gene info from KEGG for id={}", id, e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutDisease(Disease potentialDisease) throws RepositoryException {
        String id = findCanonicalId("DiseaseAliases", potentialDisease.getName());
        if (id == null) return new HashMap<>();

        String url = "https://rest.kegg.jp/get/disease:" + id;
        try {
            String keggData = fetchFromUrl(url);
            HashMap<String,String> info = new HashMap<>();
            info.put("id", id);
            info.put("keggEntry", keggData);
            return info;
        } catch (IOException e) {
            log.error("Error fetching disease info from KEGG for id={}", id, e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutDrug(Drug potentialDrug) throws RepositoryException {
        String id = findCanonicalId("DrugAliases", potentialDrug.getName());
        if (id == null) return new HashMap<>();

        String url = "https://rest.kegg.jp/get/dr:" + id;
        try {
            String keggData = fetchFromUrl(url);
            HashMap<String,String> info = new HashMap<>();
            info.put("id", id);
            info.put("keggEntry", keggData);
            return info;
        } catch (IOException e) {
            log.error("Error fetching drug info from KEGG for id={}", id, e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutCompound(Compound potentialCompound) throws RepositoryException {
        // No public KEGG REST endpoint was specified for compounds here,
        // so we'll just return the canonical ID + alias list.
        String id = findCanonicalId("CompoundAliases", potentialCompound.getName());
        if (id == null) return new HashMap<>();

        // Gather all synonyms
        String sql = "SELECT alias FROM CompoundAliases WHERE compound_id = ?";
        List<String> syns = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) syns.add(rs.getString("alias"));
            }
        } catch (SQLException e) {
            log.error("Error retrieving compound aliases for id={}", id, e);
            throw new RepositoryException(e);
        }

        HashMap<String,String> info = new HashMap<>();
        info.put("id", id);
        info.put("aliases", String.join(",", syns));
        return info;
    }

    // --- suggestions via alias tables ---

    private List<String> suggestFrom(String table, String prefix) throws RepositoryException {
        String sql = "SELECT DISTINCT alias FROM " + table + " WHERE alias LIKE ? LIMIT 10";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (rs.next()) list.add(rs.getString("alias"));
                return list;
            }
        } catch (SQLException e) {
            log.error("Error fetching suggestions from {} for prefix={}", table, prefix, e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<String> getGeneNameSuggestion(Gene potentialGene) throws RepositoryException {
        return suggestFrom("GeneAliases", potentialGene.getGeneName());
    }

    @Override
    public List<String> getCompoundNameSuggestion(Compound potentialCompound) throws RepositoryException {
        return suggestFrom("CompoundAliases", potentialCompound.getName());
    }

    @Override
    public List<String> getDrugNameSuggestion(Drug potentialDrug) throws RepositoryException {
        return suggestFrom("DrugAliases", potentialDrug.getName());
    }
}
