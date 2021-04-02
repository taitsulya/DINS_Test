package com.taits.controllers;

import com.taits.domain.Record;
import com.taits.domain.User;
import com.taits.repository.RecordRepository;
import com.taits.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class UserControllerTest {

    @InjectMocks
    UserController userController;

    @Mock
    UserRepository userRepository;

    @Mock
    RecordRepository recordRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void getAllUsers() {

        User user1 = new User(1, "Lokesh", "Gupta", "333333");
        User user2 = new User(2, "Alex", "Gussin", "222222");
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        String testFirstName = "Ale";
        String testLastName = "g";

        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.findByFirstNameContainingIgnoreCase(testFirstName)).thenReturn(users.subList(1, 2));
        when(userRepository.findByLastNameContainingIgnoreCase(testLastName)).thenReturn(users);
        when(userRepository
                .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(testFirstName, testLastName))
                .thenReturn(users.subList(1, 2));

        List<User> resultWithoutParams = userController.getAllUsers(null, null);
        List<User> resultWithFirstName = userController.getAllUsers(testFirstName, null);
        List<User> resultWithLastName = userController.getAllUsers(null, testLastName);
        List<User> resultWithFirstAndLastName = userController.getAllUsers(testFirstName, testLastName);

        assertThat(resultWithoutParams.size(), equalTo(2));
        assertThat(resultWithFirstName.size(), equalTo(1));
        assertThat(resultWithLastName.size(), equalTo(2));
        assertThat(resultWithFirstAndLastName.size(), equalTo(1));

        assertThat(resultWithoutParams.get(0).getFirstName(), equalTo(user1.getFirstName()));
        assertThat(resultWithoutParams.get(1).getFirstName(), equalTo(user2.getFirstName()));

        assertThat(resultWithFirstName.get(0).getLastName(), equalTo(user2.getLastName()));

        assertThat(resultWithLastName.get(0).getLastName(), equalTo(user1.getLastName()));
        assertThat(resultWithLastName.get(1).getLastName(), equalTo(user2.getLastName()));

        assertThat(resultWithFirstAndLastName.get(0).getPhoneNumber(), equalTo(user2.getPhoneNumber()));
    }

    @Test
    void getUserById() {
        User user1 = new User(1, "Lokesh", "Gupta", "333333");
        User user2 = new User(2, "Alex", "Gussin", "222222");
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Integer testId1 = 1;
        Integer testId2 = 3;

        when(userRepository.findById(testId1)).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(testId2)).thenReturn(Optional.empty());

        Optional<User> result1 = userController.getUserById(testId1);
        Optional<User> result2 = userController.getUserById(testId2);

        assertThat(result1, equalTo(Optional.of(user1)));
        assertThat(result2, equalTo(Optional.empty()));
    }

    @Test
    void deleteUserById() {
        User user = new User(1, "Lokesh", "Gupta", "333333");

        Integer testId = 1;

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));

        userController.deleteUserById(testId);

        verify(userRepository).delete(userCaptor.capture());

        User deletedUser = userCaptor.getValue();

        assertThat(deletedUser, equalTo(user));
    }

    @Test
    void createUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        User user = new User(1, "Lokesh", "Gupta", "333333");

        ResponseEntity<User> responseEntity = userController.createUser(user);

        assertThat(responseEntity.getStatusCodeValue(), equalTo(201));
        assertThat(requireNonNull(responseEntity.getHeaders().getLocation()).getPath(), equalTo("/api/users/1"));
    }

    @Test
    void updateUserById() {
        User user = new User(1, "Lokesh", "Gupta", "333333");
        User update = new User(146, "Alex", null, null);

        Integer testId = 1;

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i->i.getArgument(0));

        User result = userController.updateUserById(testId, update);

        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getFirstName(), equalTo(update.getFirstName()));
        assertThat(result.getLastName(), equalTo(user.getLastName()));
        assertThat(result.getPhoneNumber(), equalTo(user.getPhoneNumber()));
    }

    @Test
    void getRecordsByOwner() {
        Record record1 = new Record(1, "Lokesh", "Gupta", "333333", 1);
        Record record2 = new Record(2, "Alex", "Gussin", "222222", 0);
        List<Record> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        Integer testOwner1 = 1;
        Integer testOwner2 = 3;

        when(recordRepository.findByOwner(testOwner1)).thenReturn(records.subList(0, 1));
        when(recordRepository.findByOwner(testOwner2)).thenReturn(new ArrayList<>());

        List<Record> result1 = userController.getRecordsByOwner(testOwner1);
        List<Record> result2 = userController.getRecordsByOwner(testOwner2);

        assertThat(result1.size(), equalTo(1));
        assertThat(result2.size(), equalTo(0));

        assertThat(result1.get(0), equalTo(record1));
    }

    @Test
    void createRecord() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        User user = new User(1, "Alex", "Gussin", "222222");
        Record record = new Record(2, "Lokesh", "Gupta", "333333", null);

        Integer testId1 = 1;
        Integer testId2 = 2;

        when(userRepository.findById(testId1)).thenReturn(Optional.of(user));
        when(userRepository.findById(testId2)).thenReturn(Optional.empty());

        ResponseEntity<Record> responseEntity1 = userController.createRecord(1, record);
        ResponseEntity<Record> responseEntity2 = userController.createRecord(2, record);

        assertThat(responseEntity1.getStatusCodeValue(), equalTo(201));
        assertThat(requireNonNull(responseEntity1.getHeaders().getLocation()).getPath(), equalTo("/api/records/2"));

        assertThat(responseEntity2.getStatusCodeValue(), equalTo(400));

    }
}