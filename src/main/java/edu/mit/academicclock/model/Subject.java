package edu.mit.academicclock.model;

/**
 * Unit I: Classes, Objects, Methods, Encapsulation
 */
public class Subject extends BaseEntity {
    private String code;
    private String name;
    private int units;
    private int progress; // 0 to 100
    private int attended;
    private int total;

    public Subject() {
        super();
    }

    public Subject(String id, String code, String name, int units, int progress, int attended, int total) {
        super(id);
        this.code = code;
        this.name = name;
        this.units = units;
        this.progress = progress;
        this.attended = attended;
        this.total = total;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public int getAttended() {
        return attended;
    }

    public void setAttended(int attended) {
        this.attended = attended;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAttendancePercentage() {
        if (total <= 0) return 100;
        return (int) Math.round(((double) attended / total) * 100.0);
    }

    public boolean isBelowThreshold() {
        return getAttendancePercentage() < 75;
    }
}
