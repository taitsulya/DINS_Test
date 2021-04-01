package com.taits.repository;

import com.taits.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Integer> {
    List<Record> findByOwner(Integer owner);
    List<Record> findByPhoneNumber(String phoneNumber);

}
