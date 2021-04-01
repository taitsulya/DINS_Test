package com.taits.controllers;

import com.taits.domain.Record;
import com.taits.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class RecordControllerTest {

    @InjectMocks
    RecordController recordController;

    @Mock
    RecordRepository recordRepository;

    @Captor
    private ArgumentCaptor<Record> recordCaptor;

    @Test
    void getAllRecords() {
        Record record1 = new Record(1, "Lokesh", "Gupta", "333333", 1);
        Record record2 = new Record(2, "Alex", "Gussin", "222222", 0);
        List<Record> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        String testPhoneNumber = "222222";

        when(recordRepository.findAll()).thenReturn(records);
        when(recordRepository.findByPhoneNumber(testPhoneNumber)).thenReturn(records.subList(1, 2));

        List<Record> resultWithoutParams = recordController.getAllRecords(null);
        List<Record> resultWithPhoneNumber = recordController.getAllRecords(testPhoneNumber);

        assertThat(resultWithoutParams.size(), equalTo(2));
        assertThat(resultWithPhoneNumber.size(), equalTo(1));

        assertThat(resultWithoutParams.get(0).getFirstName(), equalTo(record1.getFirstName()));
        assertThat(resultWithoutParams.get(1).getFirstName(), equalTo(record2.getFirstName()));

        assertThat(resultWithPhoneNumber.get(0).getLastName(), equalTo(record2.getLastName()));
    }

    @Test
    void getRecordById() {
        Record record1 = new Record(1, "Lokesh", "Gupta", "333333", 1);
        Record record2 = new Record(2, "Alex", "Gussin", "222222", 0);
        List<Record> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        Integer testId1 = 1;
        Integer testId2 = 3;

        when(recordRepository.findById(testId1)).thenReturn(Optional.of(records.get(0)));
        when(recordRepository.findById(testId2)).thenReturn(Optional.empty());

        Optional<Record> result1 = recordController.getRecordById(testId1);
        Optional<Record> result2 = recordController.getRecordById(testId2);

        assertThat(result1, equalTo(Optional.of(record1)));
        assertThat(result2, equalTo(Optional.empty()));

    }

    @Test
    void deleteRecordById() {
        Record record = new Record(1, "Lokesh", "Gupta", "333333", 1);

        Integer testId = 1;

        when(recordRepository.findById(testId)).thenReturn(Optional.of(record));

        recordController.deleteRecordById(testId);

        verify(recordRepository).delete(recordCaptor.capture());

        Record deletedRecord = recordCaptor.getValue();

        assertThat(deletedRecord, equalTo(record));

    }
}