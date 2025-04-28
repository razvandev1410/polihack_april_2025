package org.example.service.interfaces;

import org.example.domain.entitiesAssociatedWithUser.User;
import org.example.service.ServicesException;

import java.util.Optional;

public interface IUserService {
    boolean authenticate(String firstName, String lastName, String password) throws ServicesException;
    boolean registerUser(String firstName, String lastName, String password, String ocupation) throws ServicesException;
    Optional<User> getUserByCredentials(String firstName, String lastName, String password) throws ServicesException;
}