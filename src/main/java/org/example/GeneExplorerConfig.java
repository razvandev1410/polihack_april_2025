package org.example;

import org.example.repository.entitiesRepository.GeneDiseaseDrugCompoundRepository;
import org.example.repository.entitiesRepository.UserRepository;
import org.example.repository.interfaces.IGeneDiseaseDrugCompoundRepository;
import org.example.repository.interfaces.IUserRepository;
import org.example.service.AllServices;
import org.example.service.GeneDiseaseDrugCompundService;
import org.example.service.UserService;
import org.example.service.interfaces.IGeneDiseaseDrugCompoundService;
import org.example.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class GeneExplorerConfig {
    @Bean
    Properties getProperties() {
        Properties properties = new Properties();
        try {
            // Load from classpath (src/main/resources)
            properties.load(getClass().getClassLoader().getResourceAsStream("bd.config"));
        } catch(IOException e) {
            System.err.println("Failed to load bd.config: " + e.getMessage());
            throw new RuntimeException("Configuration file bd.config not found", e);
        }
        return properties;
    }

    @Bean
    IGeneDiseaseDrugCompoundRepository geneDiseaseDrugCompoundRepository() {
        return new GeneDiseaseDrugCompoundRepository(getProperties());
    }

    @Bean
    IUserRepository userRepository() {
        return new UserRepository(getProperties());
    }

    @Bean
    AllServices services(){
        return new AllServices(
                new UserService(userRepository()),
                new GeneDiseaseDrugCompundService(geneDiseaseDrugCompoundRepository(), userRepository())
        );
    }

}
