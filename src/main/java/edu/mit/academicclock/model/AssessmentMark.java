package edu.mit.academicclock.model;

/**
 * Unit I & II: Assessment Marks Tracker Model
 */
public class AssessmentMark extends BaseEntity {
    private String subject;
    private String assessment;
    private double score;
    private double outOf;

    public AssessmentMark() {
        super();
    }

    public AssessmentMark(String id, String subject, String assessment, double score, double outOf) {
        super(id);
        this.subject = subject;
        this.assessment = assessment;
        this.score = score;
        this.outOf = outOf;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getOutOf() {
        return outOf;
    }

    public void setOutOf(double outOf) {
        this.outOf = outOf;
    }

    public double getPercentage() {
        if (outOf <= 0) return 0;
        return (score / outOf) * 100.0;
    }
}
