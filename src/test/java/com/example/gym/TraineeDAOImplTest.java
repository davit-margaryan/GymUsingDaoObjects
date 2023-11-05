package com.example.gym;

import com.example.gym.dao.impl.TraineeDAOImpl;
import com.example.gym.dto.TraineeRequestDto;
import com.example.gym.exception.InvalidInputException;
import com.example.gym.exception.NotFoundException;
import com.example.gym.models.Trainee;
import com.example.gym.models.User;
import com.example.gym.service.InMemoryStorage;
import com.example.gym.util.UtilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeDAOImplTest {

    @InjectMocks
    private TraineeDAOImpl traineeDAO = new TraineeDAOImpl();

    @Mock
    private InMemoryStorage inMemoryStorage;

    @Mock
    private UtilService utilService;

    @Spy
    private Map<UUID, User> userStorage = new HashMap<>();

    @Spy
    private Map<UUID, Trainee> traineeStorage = new HashMap<>();

    private User user;

    private Trainee trainee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        inMemoryStorage = mock(InMemoryStorage.class);
        when(inMemoryStorage.getUserStorage()).thenReturn(userStorage);
        when(inMemoryStorage.getTraineeStorage()).thenReturn(traineeStorage);
    }

    @Test
    void testSaveTrainee() {
        try {
            mockUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TraineeRequestDto traineeRequestDto = new TraineeRequestDto();
        traineeRequestDto.setFirstName("John");
        traineeRequestDto.setLastName("Doe");
        traineeRequestDto.setAddress("123 Main St");
        when(utilService.isValidName("John")).thenReturn(true);
        when(utilService.isValidName("Doe")).thenReturn(true);
        when(utilService.generateUniqueKey(traineeStorage)).thenReturn(UUID.fromString("d87c669f-3cb0-4d6a-9cca-d2ce64968a8c"));
        when(utilService.generateUniqueKey(userStorage)).thenReturn(UUID.fromString("732200fc-d2f1-45c0-b3dd-fb148cfcc1e5"));
        when(utilService.generateUsername("John", "Doe", userStorage)).thenReturn("Davo");
        when(utilService.generateRandomPassword(10)).thenReturn("randomPassword");

        Trainee savedTrainee = traineeDAO.save(traineeRequestDto);

        assertNotNull(savedTrainee);
        assertEquals(userStorage.get(UUID.fromString("732200fc-d2f1-45c0-b3dd-fb148cfcc1e5")).getFirstName(), user.getFirstName());
        assertEquals(1, userStorage.size());
        assertEquals("123 Main St", savedTrainee.getAddress());
    }

    @Test
    void testSaveInvalidLastName() {
        TraineeRequestDto traineeRequestDto = new TraineeRequestDto();
        traineeRequestDto.setFirstName("John");
        traineeRequestDto.setLastName("");
        traineeRequestDto.setAddress("123 Main St");

        assertThrows(InvalidInputException.class, () -> traineeDAO.save(traineeRequestDto));
    }


    @Test
    void testFindById() {
        try {
            mockTrainee();
        } catch (Exception e) {
            e.printStackTrace();
        }
        traineeStorage.put(trainee.getId(), trainee);

        Optional<Trainee> foundTrainee = traineeDAO.findById(trainee.getId());

        assertTrue(foundTrainee.isPresent());
        assertEquals(trainee.getId(), foundTrainee.get().getId());
    }

    @Test
    void testFindByIdTraineeNotFound() {
        UUID nonExistentTraineeId = UUID.randomUUID();

        Optional<Trainee> foundTrainee = traineeDAO.findById(nonExistentTraineeId);

        assertTrue(foundTrainee.isEmpty());
    }

    @Test
    void testFindAll() {
        Trainee trainee1 = new Trainee();
        Trainee trainee2 = new Trainee();
        trainee1.setId(UUID.randomUUID());
        trainee2.setId(UUID.randomUUID());
        traineeStorage.put(trainee1.getId(), trainee1);
        traineeStorage.put(trainee2.getId(), trainee2);

        List<Trainee> trainees = traineeDAO.findAll();

        assertEquals(2, trainees.size());
    }

    @Test
    void testDelete() {
        try {
            mockTrainee();
        } catch (Exception e) {
            e.printStackTrace();
        }
        traineeStorage.put(trainee.getId(), trainee);
        traineeDAO.delete(trainee.getId());

        assertFalse(traineeStorage.containsKey(trainee.getId()));
        assertFalse(userStorage.containsKey(trainee.getUserId()));
    }

    @Test
    void testDeleteTraineeNotFound() {
        UUID nonExistentTraineeId = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> traineeDAO.delete(nonExistentTraineeId));
    }

    @Test
    void testUpdate() {
        try {
            mockUser();
            mockTrainee();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Trainee traineeUnderTest = new Trainee();
        traineeUnderTest.setId(trainee.getId());
        traineeUnderTest.setUserId(user.getId());
        traineeUnderTest.setAddress("Set Address");

        traineeStorage.put(traineeUnderTest.getId(), traineeUnderTest);

        doNothing().when(utilService).updateLastName(any(User.class), anyString());
        doNothing().when(utilService).updateUsername(any(User.class), anyString(), anyMap());
        doNothing().when(utilService).updatePassword(any(User.class), anyString());

        when(traineeStorage.get(any(UUID.class))).thenReturn(traineeUnderTest);
        when(userStorage.get(any(UUID.class))).thenReturn(user);

        TraineeRequestDto updatedDto = mock(TraineeRequestDto.class);
        when(updatedDto.getAddress()).thenReturn("New Address");
        when(updatedDto.getFirstName()).thenReturn("Abraham");
        when(utilService.isValid(anyString())).thenReturn(true);

        Trainee updatedTrainee = traineeDAO.update(traineeUnderTest.getId(), updatedDto);

        assertNotNull(updatedTrainee);

        verify(utilService).updateFirstName(user, "Abraham");
        assertEquals("New Address", traineeStorage.get(updatedTrainee.getId()).getAddress());
    }


    @Test
    void testUpdateTraineeNotFound() {
        UUID nonExistentTraineeId = UUID.randomUUID();
        TraineeRequestDto updatedDto = new TraineeRequestDto();

        assertThrows(NotFoundException.class, () -> traineeDAO.update(nonExistentTraineeId, updatedDto));
    }

    @Test
    void testUpdateInvalidAddress() {
        UUID traineeId = UUID.randomUUID();
        Trainee trainee = new Trainee();
        trainee.setId(traineeId);
        traineeStorage.put(traineeId, trainee);

        TraineeRequestDto updatedDto = new TraineeRequestDto();
        updatedDto.setAddress(null);

        assertThrows(InvalidInputException.class, () -> traineeDAO.update(traineeId, updatedDto));
    }


    private void mockUser() throws Exception {
        user = PowerMockito.mock(User.class);
        PowerMockito.whenNew(User.class).withNoArguments().thenReturn(user);
        when(user.getId()).thenReturn(UUID.fromString("732200fc-d2f1-45c0-b3dd-fb148cfcc1e5"));
        when(user.getUsername()).thenReturn("Davo");
        when(user.getFirstName()).thenReturn("John");
        when(user.getPassword()).thenReturn("randomPassword");
        when(user.isActive()).thenReturn(true);
        when(user.getLastName()).thenReturn("Doe");
    }

    private void mockTrainee() throws Exception {
        trainee = PowerMockito.mock(Trainee.class);
        PowerMockito.whenNew(Trainee.class).withNoArguments().thenReturn(trainee);
        when(trainee.getUserId()).thenReturn(UUID.fromString("732200fc-d2f1-45c0-b3dd-fb148cfcc1e5"));
        when(trainee.getId()).thenReturn(UUID.fromString("a7c393f4-7a51-11ee-b962-0242ac120002"));
        trainee.setAddress("Set Address");
        when(trainee.getAddress()).thenReturn("Set Address");
    }

}

