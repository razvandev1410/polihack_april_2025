package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class FullScreenController {

    public ImageView userIcon;
    @FXML
    private ImageView logoImageView;

    @FXML
    private TextField searchField;

    // This field simulates whether user is logged in or not
    // In a real app, you would retrieve login status from a service or session.
    private boolean isLoggedIn = false;

    @FXML
    public void initialize() {

    }
}

