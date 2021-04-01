package com.taits.controllers;

import com.taits.domain.Record;
import com.taits.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/records")
public class RecordController {

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


}

