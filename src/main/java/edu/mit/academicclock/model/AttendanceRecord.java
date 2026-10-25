package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit I & II: Model representing daily attendance logging
 */
public class AttendanceRecord extends BaseEntity {
    private String subjectCode;
    private LocalDate recordDate;
    private String status; // "present" or "absent"

    public AttendanceRecord() {
        super();
    }

    public AttendanceRecord(String id, String subjectCode, LocalDate recordDate, String status) {
        super(id);
        this.subjectCode = subjectCode;
        this.recordDate = recordDate;
        this.status = status;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
