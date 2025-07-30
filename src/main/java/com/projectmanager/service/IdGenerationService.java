package com.projectmanager.service;

import java.util.List;

public interface IdGenerationService {
    public String generateId(String entityName, String prefix);
    public List<String> generateBatchIds(String entityName, String prefix, int count);
    public void resetCounter(String entityName, Long value);
    public String generateUserId();
    public String generateProjectId();
    public String generateTaskId();
    public String generateFileId();
    public String generateTimeEntryId();
}
