package com.projectmanager.service;

import com.projectmanager.dto.TimeEntryRequestDTO;
import com.projectmanager.dto.TimeEntryResponseDTO;

import java.util.List;

public interface TimeEntryService {

    List<TimeEntryResponseDTO> getAllEntry();

    TimeEntryResponseDTO createEntry(TimeEntryRequestDTO entry);
}
