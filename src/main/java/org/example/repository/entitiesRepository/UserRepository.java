package org.example.repository.entitiesRepository;

import org.example.domain.entitiesAssociatedWithUser.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.repository.JdbcUtils;
import org.example.repository.interfaces.IUserRepository;
import org.example.repository.crypto.CryptoUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class UserRepository implements IUserRepository {
    private final JdbcUtils jdbcUtils;
    private static final Logger log = LogManager.getLogger(UserRepository.class);

    public UserRepository(Properties properties) {
        log.info("Initializing UserDbRepository...");
        this.jdbcUtils = new JdbcUtils(properties);
    }

    @Override
    public Optional<User> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null.");
        }

        String sql = ""
                + "SELECT user_id, first_name, last_name, password, occupation "
                + "FROM User WHERE user_id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long userId       = rs.getLong("user_id");
                    String firstName  = rs.getString("first_name");
                    String lastName   = rs.getString("last_name");
                    String encrypted  = rs.getString("password");
                    String password   = CryptoUtil.decrypt(encrypted);
                    String occupation = rs.getString("occupation");

                    return Optional.of(
                            new User(userId, firstName, lastName, password, occupation)
                    );
                }
            }
        } catch (SQLException e) {
            log.error("Error finding User with id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = ""
                + "SELECT user_id, first_name, last_name, password, occupation "
                + "FROM User";

        try (Connection conn = jdbcUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Long   id         = rs.getLong("user_id");
                String firstName  = rs.getString("first_name");
                String lastName   = rs.getString("last_name");
                String encrypted  = rs.getString("password");
                String password   = CryptoUtil.decrypt(encrypted);
                String occupation = rs.getString("occupation");

                users.add(new User(id, firstName, lastName, password, occupation));
            }
        } catch (SQLException e) {
            log.error("Error retrieving all users", e);
        }
        return users;
    }

    @Override
    public Optional<User> save(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }

        String sql = ""
                + "INSERT INTO User (first_name, last_name, password, occupation) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getFirstName());
            stmt.setString(2, entity.getLastName());
            stmt.setString(3, CryptoUtil.encrypt(entity.getPassword()));
            stmt.setString(4, entity.getOccupation());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                log.warn("No rows inserted for {}", entity);
                return Optional.of(entity);
            }

            // retrieve the generated user_id and set it on the entity
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }

            log.info("Inserted User {} with id={}", entity, entity.getId());
            return Optional.empty();
        } catch (SQLException e) {
            log.error("Error saving User: {}", entity, e);
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<User> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null.");
        }

        Optional<User> before = findOne(id);
        if (before.isEmpty()) {
            return Optional.empty();
        }

        String sql = "DELETE FROM User WHERE user_id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            log.info("Deleted {} row(s) for user_id={}", rows, id);
        } catch (SQLException e) {
            log.error("Error deleting User with id {}", id, e);
        }
        return before;
    }

    @Override
    public Optional<User> update(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }

        String sql = ""
                + "UPDATE User "
                + "SET first_name = ?, last_name = ?, password = ?, occupation = ? "
                + "WHERE user_id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getFirstName());
            stmt.setString(2, entity.getLastName());
            stmt.setString(3, CryptoUtil.encrypt(entity.getPassword()));
            stmt.setString(4, entity.getOccupation());
            stmt.setLong(5, entity.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                log.warn("No User found with id={} to update.", entity.getId());
                return Optional.of(entity);
            }
            log.info("Updated {} row(s) for user_id={}", rows, entity.getId());
            return Optional.empty();
        } catch (SQLException e) {
            log.error("Error updating User: {}", entity, e);
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<User> findUserByCredentials(User potentialUser){
        if (potentialUser == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        String sql = ""
                + "SELECT user_id, first_name, last_name, password, occupation "
                + "FROM User WHERE first_name = ? AND last_name = ? AND password = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, potentialUser.getFirstName());
            stmt.setString(2, potentialUser.getLastName());
            stmt.setString(3, CryptoUtil.encrypt(potentialUser.getPassword()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("user_id");
                    String firstNameFound = rs.getString("first_name");
                    String lastNameFound   = rs.getString("last_name");
                    String encrypted  = rs.getString("password");
                    String passwordFound  = CryptoUtil.decrypt(encrypted);
                    String occupation = rs.getString("occupation");

                    return Optional.of(
                            new User(id, firstNameFound, lastNameFound, passwordFound, occupation)
                    );
                }
            }
        } catch (SQLException e) {
            log.error("Error finding User with credentials", e);
        }
        return Optional.empty();
    }
}
