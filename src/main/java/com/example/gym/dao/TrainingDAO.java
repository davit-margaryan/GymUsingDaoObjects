package com.example.gym.dao;

import com.example.gym.dto.TrainingRequestDto;
import com.example.gym.models.Training;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingDAO {
    Training save(TrainingRequestDto trainingRequestDto);

    Optional<Training> findById(UUID id);

    List<Training> findAll();

    void delete(UUID id);

    Training update(UUID id, TrainingRequestDto trainingRequestDto);
}