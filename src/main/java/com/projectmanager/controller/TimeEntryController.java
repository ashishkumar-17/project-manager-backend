package com.projectmanager.controller;

import com.projectmanager.dto.TimeEntryRequestDTO;
import com.projectmanager.service.TimeEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-entry")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEntry(@RequestBody TimeEntryRequestDTO entry){
        try{
            return ResponseEntity.ok(timeEntryService.createEntry(entry));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to create Time Entry.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEntry(){
        try{
            return ResponseEntity.ok(timeEntryService.getAllEntry());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("No Entry Found" + e.getMessage());
        }
    }
}
