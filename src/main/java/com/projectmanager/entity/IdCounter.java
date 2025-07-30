package com.projectmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "id_counters")
public class IdCounter {

    @Id
    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "counter_value")
    private Long counterValue;

    // constructors, getters, setters
    public IdCounter() {}

    public IdCounter(String entityName, Long counterValue) {
        this.entityName = entityName;
        this.counterValue = counterValue;
    }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public Long getCounterValue() { return counterValue; }
    public void setCounterValue(Long counterValue) { this.counterValue = counterValue; }

    @Override
    public String toString() {
        return "IdCounter{" +
                "entityName='" + entityName + '\'' +
                ", counterValue=" + counterValue +
                '}';
    }
}