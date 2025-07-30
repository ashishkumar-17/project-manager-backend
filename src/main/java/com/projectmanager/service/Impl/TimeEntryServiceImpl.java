package com.projectmanager.service.Impl;

import com.projectmanager.dto.TimeEntryRequestDTO;
import com.projectmanager.dto.TimeEntryResponseDTO;
import com.projectmanager.entity.TimeEntry;
import com.projectmanager.repository.TimeEntryRepository;
import com.projectmanager.service.TimeEntryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public List<TimeEntryResponseDTO> getAllEntry() {
        return timeEntryRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public TimeEntryResponseDTO createEntry(TimeEntryRequestDTO entry) {
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setTaskId(entry.getTaskId());
        timeEntry.setUserId(entry.getUserId());
        timeEntry.setDescription(entry.getDescription());
        timeEntry.setStartTime(entry.getStartTime());
        timeEntry.setEndTime(entry.getEndTime());
        timeEntry.setDuration(entry.getDuration());
        timeEntry.setDate(entry.getDate());

        return mapToDTO(timeEntryRepository.save(timeEntry));
    }

    public TimeEntryResponseDTO mapToDTO(TimeEntry entry){
        return new TimeEntryResponseDTO(
                entry.getId(),
                entry.getTaskId(),
                entry.getUserId(),
                entry.getDescription(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getDuration(),
                entry.getDate()
        );
    }
}