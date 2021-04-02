package com.taits.controllers;

import com.taits.domain.Record;
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
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Record> getAllRecords(String phoneNumber) {
        if (phoneNumber == null) {
            return recordRepository.findAll();
        } else {
            return recordRepository.findByPhoneNumber(phoneNumber);
        }
    }

    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Optional<Record> getRecordById(@PathVariable("recordId") Integer recordId) {
        return recordRepository.findById(recordId);
    }

    @RequestMapping(value = "/{recordId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecordById(@PathVariable("recordId") Integer recordId) {
        if (recordRepository.findById(recordId).isPresent()) {
            recordRepository.delete(recordRepository.findById(recordId).get());
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Record> createRecord(Record record) {
        if (record.getOwner() != null && userRepository.findById(record.getOwner()).isPresent()) {
            recordRepository.save(record);
            URI locationUri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/api/records/{recordId}")
                    .buildAndExpand(record.getId())
                    .toUri();
            return ResponseEntity.created(locationUri).body(record);
        } else return ResponseEntity.badRequest().body(null);
    }

    @RequestMapping(value = "/{recordId}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Record updateRecord(@PathVariable("recordId") Integer recordId, Record record) {
        if (recordRepository.findById(recordId).isPresent()) {
            Record recordInDB = recordRepository.findById(recordId).get();
            if (record.getFirstName() != null) {
                recordInDB.setFirstName(record.getFirstName());
            }
            if (record.getLastName() != null) {
                recordInDB.setLastName(record.getLastName());
            }
            if (record.getPhoneNumber() != null) {
                recordInDB.setPhoneNumber(record.getPhoneNumber());
            }
            return recordRepository.save(recordInDB);
        } else if (record.getOwner() != null && userRepository.findById(record.getOwner()).isPresent()) {
            Record newRecord = new Record(record.getFirstName(), record.getLastName(), record.getPhoneNumber(), record.getOwner());
            return recordRepository.save(newRecord);
        } else return null;
    }


}

