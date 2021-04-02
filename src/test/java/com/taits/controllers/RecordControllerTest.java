package com.taits.controllers;

import com.taits.domain.Record;
import com.taits.domain.User;
import com.taits.repository.RecordRepository;
import com.taits.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.*;
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
class RecordControllerTest {

    @InjectMocks
    RecordController recordController;

    @Mock
    UserRepository userRepository;

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

    @Test
    void createRecord() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Record record1 = new Record(1, "Alex", "Gussin", "222222", 1);
        Record record2 = new Record(2, "Alex", "Gussin", "222222", 2);
        Record record3 = new Record(3, "Lokesh", "Gupta", "333333", null);

        User user = new User(1, "user1", "user1", "123123123");

        Integer testId1 = 1;
        Integer testId2 = 2;

        when(userRepository.findById(testId1)).thenReturn(Optional.of(user));
        when(userRepository.findById(testId2)).thenReturn(Optional.empty());

        ResponseEntity<Record> responseEntity1 = recordController.createRecord(record1);
        ResponseEntity<Record> responseEntity2 = recordController.createRecord(record2);
        ResponseEntity<Record> responseEntity3= recordController.createRecord(record3);

        assertThat(responseEntity1.getStatusCodeValue(), equalTo(201));
        assertThat(requireNonNull(responseEntity1.getHeaders().getLocation()).getPath(), equalTo("/api/records/1"));

        assertThat(responseEntity2.getStatusCodeValue(), equalTo(400));

        assertThat(responseEntity3.getStatusCodeValue(), equalTo(400));
    }

    @Test
    void updateRecord() {
        Record record = new Record(1, "Lokesh", "Gupta", "333333", 1);

        Record update1 = new Record("upd1", "Gussin", "222222", 1);
        Record update2 = new Record("upd2", null, "11", 2);
        Record update3 = new Record(null, "Gupta", "00000", null);

        User user = new User(1, "user1", "user1", "123123123");

        Integer testId1 = 1;
        Integer testId2 = 2;

        when(userRepository.findById(testId1)).thenReturn(Optional.of(user));
        when(userRepository.findById(testId2)).thenReturn(Optional.empty());
        when(recordRepository.findById(testId1)).thenReturn(Optional.of(record));
        when(recordRepository.findById(testId2)).thenReturn(Optional.empty());
        when(recordRepository.save(any(Record.class))).thenAnswer(i->i.getArgument(0));


        Record result1 = recordController.updateRecord(testId2, update1);

        assertThat(result1.getFirstName(), equalTo(update1.getFirstName()));
        assertThat(result1.getLastName(), equalTo(update1.getLastName()));
        assertThat(result1.getPhoneNumber(), equalTo(update1.getPhoneNumber()));
        assertThat(result1.getOwner(), equalTo(update1.getOwner()));


        Record result2 = recordController.updateRecord(testId2, update2);

        assertThat(result2, equalTo(null));


        Record result3 = recordController.updateRecord(testId2, update3);

        assertThat(result3, equalTo(null));


        Record result4 = recordController.updateRecord(testId1, update2);

        assertThat(result4.getId(), equalTo(record.getId()));
        assertThat(result4.getFirstName(), equalTo(update2.getFirstName()));
        assertThat(result4.getLastName(), equalTo(record.getLastName()));
        assertThat(result4.getPhoneNumber(), equalTo(update2.getPhoneNumber()));
        assertThat(result4.getOwner(), equalTo(record.getOwner()));
    }
}