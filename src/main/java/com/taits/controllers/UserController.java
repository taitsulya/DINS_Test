package com.taits.controllers;

import com.taits.domain.Record;
import com.taits.domain.User;
import com.taits.repository.RecordRepository;
import com.taits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAllUsers(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return userRepository.findAll();
        } else if (firstName == null) {
            return userRepository.findByLastNameContainingIgnoreCase(lastName);
        } else if (lastName == null) {
            return userRepository.findByFirstNameContainingIgnoreCase(firstName);
        } else {
            return userRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
        }
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Optional<User> getUserById(@PathVariable("userId") Integer userId) {
        return userRepository.findById(userId);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable("userId") Integer userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.delete(userRepository.findById(userId).get());
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createUser(User user) {
        userRepository.save(user);
        URI locationUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/users/{userId}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(locationUri).body(user);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User updateUserById(@PathVariable("userId") Integer userId, User user) {
        if (userRepository.findById(userId).isEmpty()) {
            User newUser = new User(user.getFirstName(), user.getLastName(), user.getPhoneNumber());
            return userRepository.save(newUser);
        }
        User userInDB = userRepository.findById(userId).get();
        if (user.getFirstName() != null) {
            userInDB.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            userInDB.setLastName(user.getLastName());
        }
        if (user.getPhoneNumber() != null) {
            userInDB.setPhoneNumber(user.getPhoneNumber());
        }
        return userRepository.save(userInDB);
    }

    @RequestMapping(value = "/{userId}/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Record> getRecordsByOwner(@PathVariable("userId") Integer userId) {
        return recordRepository.findByOwner(userId);
    }

    @RequestMapping(value = "/{userId}/records", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Record> createRecord(@PathVariable("userId") Integer userId, Record record) {
        if (userRepository.findById(userId).isPresent()) {
            record.setOwner(userId);
            recordRepository.save(record);
            URI locationUri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/api/records/{recordId}")
                    .buildAndExpand(record.getId())
                    .toUri();
            return ResponseEntity.created(locationUri).body(record);
        } else return ResponseEntity.badRequest().body(null);
    }

}

