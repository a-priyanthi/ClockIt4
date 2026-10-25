package edu.mit.academicclock.model;

/**
 * Unit I: Classes, Objects, Constructors, Encapsulation
 */
public class Student extends BaseEntity {
    private String rollNumber;
    private String name;
    private String department;
    private String programme;
    private int semester;
    private String avatarUrl;
    private boolean doodleApplied;

    public Student() {
        super();
    }

    public Student(String id, String rollNumber, String name, String department, String programme, int semester) {
        super(id);
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.programme = programme;
        this.semester = semester;
        this.doodleApplied = false;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isDoodleApplied() {
        return doodleApplied;
    }

    public void setDoodleApplied(boolean doodleApplied) {
        this.doodleApplied = doodleApplied;
    }
}
