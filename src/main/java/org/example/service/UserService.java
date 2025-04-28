package org.example.service;

import org.example.domain.entitiesAssociatedWithUser.User;
import org.example.domain.validation.UserValidator;
import org.example.domain.validation.ValidationException;
import org.example.repository.interfaces.IUserRepository;
import org.example.service.interfaces.IUserService;

import java.util.Optional;

public class UserService implements IUserService {
    private IUserRepository iUserRepository;

    public UserService(IUserRepository iUserRepository) {
        this.iUserRepository = iUserRepository;
    }
    @Override
    public boolean authenticate(String firstName, String lastName, String password) throws ServicesException {
        try {
            // Find user by username (you might need to implement this method in your repository)
            User potentialUser = new User(firstName, lastName, password);
            UserValidator.validate(potentialUser);
            Optional<User> userOpt = iUserRepository.findUserByCredentials(potentialUser);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            return user.getPassword().equals(password);
        } catch (ValidationException e) {
            throw new ServicesException("Validarea user ului a picat", e);
        } catch (Exception e) {
            throw new ServicesException("Authentication failed", e);
        }
    }

    @Override
    public boolean registerUser(String firstName, String lastName, String password, String ocupation) throws ServicesException {
        try {
            User potentialUser = new User(firstName, lastName, password, ocupation);

            UserValidator.validate(potentialUser);

            // Check if user already exists
            if (iUserRepository.findUserByCredentials(potentialUser).isPresent()) {
                return false;
            }

            Optional<User> savedUser = iUserRepository.save(potentialUser);
            return savedUser.isEmpty();
        } catch(ValidationException e) {
            throw new ServicesException("Validarea user ului a picat", e);
        } catch (Exception e){
            throw new ServicesException("Registration failed", e);
        }
    }

    @Override
    public Optional<User> getUserByCredentials(String firstName, String lastName, String password) throws ServicesException {
        try {
            User potentialUser = new User(firstName, lastName, password);
            UserValidator.validate(potentialUser);
            return iUserRepository.findUserByCredentials(potentialUser);
        } catch(ValidationException e) {
            throw new ServicesException("Validarea credentialelor user ului pe care il cauti a picat", e);
        } catch (Exception e) {
            throw new ServicesException("Error checking username existence", e);
        }
    }
}
