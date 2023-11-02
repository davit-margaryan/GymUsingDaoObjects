package com.example.gym.dao.impl;

import com.example.gym.dao.TraineeDAO;
import com.example.gym.dao.TrainerDAO;
import com.example.gym.dao.TrainingDAO;
import com.example.gym.dto.TrainingRequestDto;
import com.example.gym.exception.InvalidInputException;
import com.example.gym.exception.UserNotFoundException;
import com.example.gym.models.Trainee;
import com.example.gym.models.Trainer;
import com.example.gym.models.Training;
import com.example.gym.models.TrainingType;
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
 * Implementation of the TrainingDAO interface for managing Training entities.
 */
@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private Map<UUID, Training> trainingStorage;

    private UtilService utilService;

    private Map<UUID, Trainee> traineeStorage;

    private Map<UUID, Trainer> trainerStorage;

    private Map<UUID, TrainingType> trainingTypeStorage;

    private TraineeDAO traineeDAO;

    private TrainerDAO trainerDAO;

    private static final Logger logger = LoggerFactory.getLogger(TrainingDAOImpl.class);

    @Autowired
    public void setTrainingStorage(InMemoryStorage storage) {
        this.trainingStorage = storage.getTrainingStorage();
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTrainingTypeStorage(InMemoryStorage storage) {
        this.trainingTypeStorage = storage.getTrainingTypeStorage();
    }

    @Autowired
    public void setTrainerStorage(InMemoryStorage storage) {
        this.trainerStorage = storage.getTrainerStorage();
    }

    @Autowired
    public void setTraineeStorage(InMemoryStorage storage) {
        this.traineeStorage = storage.getTraineeStorage();
    }

    @Autowired
    public void setUtilService(UtilService utilService) {
        this.utilService = utilService;
    }

    @Override
    public Training save(TrainingRequestDto trainingRequestDto) {
        if (!areFieldsValid(trainingRequestDto)) {
            throw new InvalidInputException("Missing required fields for creating a training.");
        }
        Training training = new Training();
        training.setId(utilService.generateUniqueKey(trainingStorage));
        training.setDate(trainingRequestDto.getDate());
        training.setDuration(trainingRequestDto.getDuration());
        training.setName(trainingRequestDto.getName());
        TrainingType trainingType = new TrainingType();
        trainingType.setId(utilService.generateUniqueKey(trainingTypeStorage));
        trainingType.setTypeName(trainingRequestDto.getTrainingTypeName());
        training.setTrainingTypeId(trainingType.getId());
        Optional<Trainee> optionalTrainee = traineeDAO.findById(trainingRequestDto.getTraineeId());
        if (optionalTrainee.isEmpty()) {
            throw new UserNotFoundException("Trainee with " + trainingRequestDto.getTraineeId() + " Not found");
        }
        Optional<Trainer> optionalTrainer = trainerDAO.findById(trainingRequestDto.getTrainerId());
        if (optionalTrainer.isEmpty()) {
            throw new UserNotFoundException("Trainer with " + trainingRequestDto.getTraineeId() + " Not found");
        }
        training.setTrainerId(trainingRequestDto.getTrainerId());
        training.setTraineeId(trainingRequestDto.getTraineeId());
        trainingStorage.put(training.getId(), training);
        trainingTypeStorage.put(trainingType.getId(), trainingType);
        return training;
    }

    @Override
    public Optional<Training> findById(UUID id) {
        return Optional.ofNullable(trainingStorage.get(id));
    }

    @Override
    public List<Training> findAll() {
        return (List<Training>) trainingStorage.values();
    }

    @Override
    public void delete(UUID id) {
        Optional<Training> trainingOptional = findById(id);
        if (trainingOptional.isPresent()) {
            trainingStorage.remove(id);
            trainerStorage.remove(trainingOptional.get().getTrainerId());
            traineeStorage.remove(trainingOptional.get().getTraineeId());
            trainingTypeStorage.remove(trainingOptional.get().getTrainingTypeId());
        } else {
            logger.error("Training not found");
        }
    }

    @Override
    public Training update(UUID id, TrainingRequestDto trainingRequestDto) {
        Optional<Training> optionalTraining = findById(id);
        if (optionalTraining.isEmpty()) {
            throw new UserNotFoundException("Training with ID: " + id + " not found !");
        }

        Training training = optionalTraining.get();

        if (trainingRequestDto.getName() != null && !trainingRequestDto.getName().isEmpty()) {
            training.setName(trainingRequestDto.getName());
        }

        if (trainingRequestDto.getDate() != null) {
            training.setDate(trainingRequestDto.getDate());
        }

        if (trainingRequestDto.getDuration() != null) {
            training.setDuration(trainingRequestDto.getDuration());
        }

        if (trainingRequestDto.getTrainingTypeName() != null && !trainingRequestDto.getTrainingTypeName().isEmpty()) {
            TrainingType trainingType = trainingTypeStorage.get(training.getTrainingTypeId());
            trainingType.setTypeName(trainingRequestDto.getTrainingTypeName());
            trainingTypeStorage.put(trainingType.getId(), trainingType);
        }

        if (trainingRequestDto.getTraineeId() != null) {
            Optional<Trainee> optionalTrainee = traineeDAO.findById(trainingRequestDto.getTraineeId());
            if (optionalTrainee.isEmpty()) {
                throw new UserNotFoundException("Trainee with ID: " + trainingRequestDto.getTraineeId() + " not found");
            }
            training.setTraineeId(trainingRequestDto.getTraineeId());
        }

        if (trainingRequestDto.getTrainerId() != null) {
            Optional<Trainer> optionalTrainer = trainerDAO.findById(trainingRequestDto.getTrainerId());
            if (optionalTrainer.isEmpty()) {
                throw new UserNotFoundException("Trainer with ID: " + trainingRequestDto.getTrainerId() + " not found");
            }
            training.setTrainerId(trainingRequestDto.getTrainerId());
        }
        trainingStorage.put(training.getId(), training);
        return training;
    }


    private boolean areFieldsValid(TrainingRequestDto trainingRequestDto) {
        return trainingRequestDto != null &&
                trainingRequestDto.getTraineeId() != null &&
                trainingRequestDto.getTrainerId() != null &&
                trainingRequestDto.getName() != null &&
                trainingRequestDto.getDate() != null &&
                trainingRequestDto.getDuration() != null &&
                trainingRequestDto.getTrainingTypeName() != null &&
                !trainingRequestDto.getName().isEmpty() &&
                !trainingRequestDto.getTrainingTypeName().isEmpty();
    }

}