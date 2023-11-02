package com.example.gym.dao;

import com.example.gym.dto.TraineeRequestDto;
import com.example.gym.models.Trainee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraineeDAO {
    Trainee save(TraineeRequestDto traineeRequestDto);

    Optional<Trainee> findById(UUID id);

    List<Trainee> findAll();

    void delete(UUID id);

    Trainee update(UUID id, TraineeRequestDto traineeRequestDto);
}