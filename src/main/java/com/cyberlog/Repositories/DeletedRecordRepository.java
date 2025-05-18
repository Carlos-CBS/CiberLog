package com.cyberlog.Repositories;

import com.cyberlog.Models.DeletedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeletedRecordRepository extends JpaRepository<DeletedRecord, UUID> {
}
