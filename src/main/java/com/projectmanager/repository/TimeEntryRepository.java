package com.projectmanager.repository;

import com.projectmanager.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, String> {
}
