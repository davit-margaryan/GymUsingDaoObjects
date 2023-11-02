package com.example.gym.dao.impl;

import com.example.gym.dao.TraineeDAO;
import com.example.gym.dto.TraineeRequestDto;
import com.example.gym.exception.InvalidInputException;
import com.example.gym.exception.UserNotFoundException;
import com.example.gym.models.Trainee;
import com.example.gym.models.User;
import com.example.gym.service.InMemoryStorage;
import com.example.gym.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the TraineeDAO interface for managing Trainee entities.
 */
@Repository
public class TraineeDAOImpl implements TraineeDAO {

    private Map<UUID, Trainee> traineeStorage;

    private Map<UUID, User> userStorage;

    private UtilService utilService;

    private static final Logger logger = LoggerFactory.getLogger(TraineeDAOImpl.class);

    @Autowired
    public void setTraineeStorage(InMemoryStorage inMemoryStorage) {
        this.traineeStorage = inMemoryStorage.getTraineeStorage();
    }

    @Autowired
    public void setUtilService(UtilService utilService) {
        this.utilService = utilService;
    }

    @Autowired
    public void setUserStorage(InMemoryStorage inMemoryStorage) {
        this.userStorage = inMemoryStorage.getUserStorage();
    }

    @Override
    public Trainee save(TraineeRequestDto traineeRequestDto) {
        Trainee trainee = new Trainee();
        User user = new User();
        if (!utilService.isValidName(traineeRequestDto.getFirstName()) || !utilService.isValidName(traineeRequestDto.getLastName())) {
            logger.error("Invalid firstname or lastname ");
            throw new InvalidInputException("Invalid firstname or lastname");
        }
        user.setId(utilService.generateUniqueKey(userStorage));
        trainee.setId(utilService.generateUniqueKey(traineeStorage));
        trainee.setUserId(user.getId());
        user.setFirstName(traineeRequestDto.getFirstName());
        user.setLastName(traineeRequestDto.getLastName());
        trainee.setAddress(traineeRequestDto.getAddress());
        utilService.generateUsername(user.getFirstName(), user.getLastName(), userStorage);
        utilService.generateRandomPassword(10);
        traineeStorage.put(trainee.getId(), trainee);
        userStorage.put(user.getId(), user);
        logger.info("Trainee successfully created");
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(UUID id) {
        return Optional.ofNullable(traineeStorage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return (List<Trainee>) traineeStorage.values();
    }

    @Override
    public void delete(UUID id) {
        Optional<Trainee> traineeOptional = findById(id);
        if (traineeOptional.isPresent()) {
            userStorage.remove(traineeOptional.get().getUserId());
            traineeStorage.remove(id);
        } else {
            logger.error("Trainee not found");
        }
    }

    @Override
    public Trainee update(UUID id, TraineeRequestDto traineeRequestDto) {
        if (!traineeStorage.containsKey(id)) {
            throw new UserNotFoundException("Trainee not found with ID: " + id);
        }
        Trainee trainee = traineeStorage.get(id);
        UUID userId = trainee.getUserId();
        User user = userStorage.get(userId);
        utilService.updateFirstName(user, traineeRequestDto.getFirstName());
        utilService.updateLastName(user, traineeRequestDto.getLastName());
        utilService.updateUsername(user, traineeRequestDto.getUsername(), userStorage);
        utilService.updatePassword(user, traineeRequestDto.getPassword());
        if (utilService.isValid(traineeRequestDto.getAddress())) {
            trainee.setAddress(traineeRequestDto.getAddress());
        }
        traineeStorage.put(id, trainee);
        userStorage.put(userId, user);
        logger.info("Trainee successfully updated");
        return trainee;
    }

}
