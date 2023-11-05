package com.example.gym.service;

import com.example.gym.models.*;
import com.example.gym.util.JSONData;
import com.example.gym.util.UtilService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Component
public class InMemoryStorage {
    private Map<UUID, Trainer> trainerStorage = new HashMap<>();

    private Map<UUID, Trainee> traineeStorage = new HashMap<>();

    private Map<UUID, Training> trainingStorage = new HashMap<>();

    private Map<UUID, User> userStorage = new HashMap<>();

    private Map<UUID, TrainingType> trainingTypeStorage = new HashMap<>();

    private UtilService utilService;

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);

    @Value("${user.file.path}")
    private String userFilePath;

    public InMemoryStorage(UtilService utilService) {
        this.utilService = utilService;
    }

    @PostConstruct
    public void initializeStorage() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONData[] jsonData = objectMapper.readValue(new File(userFilePath), JSONData[].class);
            for (JSONData data : jsonData) {
                logger.info("Creating User with first name: {} and last name: {}", data.getFirstName(), data.getLastName());
                User newUser = User.builder()
                        .id(utilService.generateUniqueKey(userStorage))
                        .firstName(data.getFirstName())
                        .lastName(data.getLastName())
                        .username(utilService.generateUsername(data.getFirstName(), data.getLastName(), userStorage))
                        .isActive(true)
                        .password(utilService.generateRandomPassword(10))
                        .build();
                if (data.getRole().equalsIgnoreCase("trainee")) {
                    Trainee trainee = Trainee.builder()
                            .id(utilService.generateUniqueKey(traineeStorage))
                            .userId(newUser.getId())
                            .build();
                    traineeStorage.put(trainee.getId(), trainee);
                } else if (data.getRole().equalsIgnoreCase("trainer")) {
                    Trainer trainer = Trainer.builder()
                            .id(utilService.generateUniqueKey(trainerStorage))
                            .specialization(data.getSpecialization())
                            .userId(newUser.getId())
                            .build();
                    trainerStorage.put(trainer.getId(), trainer);
                }
                userStorage.put(newUser.getId(), newUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
