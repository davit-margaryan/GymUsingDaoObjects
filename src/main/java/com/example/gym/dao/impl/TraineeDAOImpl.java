package com.example.gym.dao.impl;

import com.example.gym.dao.TraineeDAO;
import com.example.gym.dto.TraineeRequestDto;
import com.example.gym.exception.InvalidInputException;
import com.example.gym.exception.NotFoundException;
import com.example.gym.models.Trainee;
import com.example.gym.models.User;
import com.example.gym.service.InMemoryStorage;
import com.example.gym.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

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
        trainee.setId(utilService.generateUniqueKey(traineeStorage));
        trainee.setAddress(traineeRequestDto.getAddress());
        trainee.setUserId(saveUser(user,traineeRequestDto));
        traineeStorage.put(trainee.getId(), trainee);
        logger.info("Trainee successfully created");
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(UUID id) {
        return Optional.ofNullable(traineeStorage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(traineeStorage.values());
    }

    @Override
    public void delete(UUID id) {
        Optional<Trainee> traineeOptional = findById(id);
        if (traineeOptional.isPresent()) {
            userStorage.remove(traineeOptional.get().getUserId());
            traineeStorage.remove(id);
        } else {
            logger.error("Trainee not found");
            throw new NotFoundException("Trainee not found");
        }
    }

    @Override
    public Trainee update(UUID id, TraineeRequestDto traineeRequestDto) {
        if (!traineeStorage.containsKey(id)) {
            throw new NotFoundException("Trainee not found with ID: " + id);
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

    private UUID saveUser(User user,TraineeRequestDto traineeRequestDto){
        user.setId(utilService.generateUniqueKey(userStorage));
        user.setFirstName(traineeRequestDto.getFirstName());
        user.setLastName(traineeRequestDto.getLastName());
        user.setUsername(utilService.generateUsername(user.getFirstName(), user.getLastName(), userStorage));
        user.setPassword(utilService.generateRandomPassword(10));
        user.setActive(true);
        userStorage.put(user.getId(), user);
        return user.getId();
    }
}
