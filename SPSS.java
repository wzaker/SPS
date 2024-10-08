package spss;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Wahida Zaker
 * SPSS class manages student submissions and their test scores.
 */

public class SPSS {

  private int numTests;
  private List<String> students;
  private List<Submission> submissions;

  /**
   * Inner class representing a submission by a student.
   */

  private class Submission {
    private String studentName;
    private List<Integer> testResults;
    private int totalScore;

    /**
     * Constructs a new Submission object with the name and test results
     * 
     * @param studentName The name of the student making the submission.
     * @param testResults The list of test results for the submission.
     */
    public Submission(String studentName, List<Integer> testResults) {
      this.studentName = studentName;
      this.testResults = new ArrayList<>(testResults);
      calculateTotalScore();
    }

    /**
     * Calculates the total score of the submission based on the test results.
     * 
     * @return The total score of the submission.
     */
    public int calculateTotalScore() {
      totalScore = 0;
      for (int score : testResults) {
        totalScore += score;
      }
      return totalScore;
    }

    /**
     * Checks if this submission belongs to the given student.
     * 
     * @param name The name of the student to check.
     * @return true if the submission belongs to the student, false otherwise.
     */
    public boolean hasStudent(String name) {
      return studentName.equals(name);
    }

    /**
     * Checks if all test results in the submission are passing.
     * 
     * @return true if all test results are passing, false otherwise.
     */
    public boolean allTestsPassed() {
      for (int score : testResults) {
        if (score == 0) {
          return false;
        }
      }
      return true;
    }
    /**
     * Checks if the submission is satisfactory 
     * based on the number of passing tests.
     * @param numTests The total number of tests in the project.
     * @return true if the submission is satisfactory, false otherwise.
     */

    public boolean isSatisfactory(int numTests) {
      int passingTests = 0;

      for (int score : testResults) {
        if (score > 0) {
          passingTests++;
        }
      }
      return passingTests >= numTests / 2;
    }

  }

  /**
   * Constructs a new SPSS object with the given number of tests..
   * 
   * @param numTests The total number of tests in the project.
   * @throws IllegalArgumentException if numTests is less than or equal to zero.

   */
  public SPSS(int numTests) {
    if (numTests <= 0)
      throw new IllegalArgumentException();

    this.numTests = numTests;
    this.students = new ArrayList<>();
    this.submissions = new ArrayList<>();
  }

  /**
   * Adds a new student to the SPSS system.
   * 
   * @param newStudent The name of the student to add.
   * @return true if the student was successfully added, false otherwise.
   */
  public boolean addStudent(String newStudent) {
    if (newStudent == null || newStudent.isEmpty()) {
      return false;
    }

    synchronized (students) {
      // Checks if student already exists
      if (students.contains(newStudent)) {
        return false;
      }
      students.add(newStudent);
      return true;
    }
  }

  /**
   * Returns the number of students in the SPSS system.
   * 
   * @return The number of students.
   */
  public int numStudents() {
    return students.size();
  }

  /**
   * Adds a new submission to the SPSS system.
   * 
   * @param name        The name of the student making the submission.
   * @param testResults The list of test results for the submission.
   * @return true if the submission was successfully added, false otherwise.
   */
  public boolean addSubmission(String name, List<Integer> testResults) {
    if (name == null || !students.contains(name)) {
      return false;
    }

    if (testResults == null || testResults.isEmpty() ||
        testResults.size() != numTests) {
      return false;
    }

    // Check if any test score is negative
    for (int score : testResults) {
      if (score < 0) {
        return false;
      }
    }

    synchronized (submissions) {
      // Add new submission
      Submission newSubmission = new Submission(name, testResults);
      submissions.add(newSubmission);
    }
    return true;
  }

  /**
   * Reads submissions concurrently from multiple files.
   *
   * @param fileNames The list of file names containing submissions.
   * @return true if submissions were read successfully, false otherwise.
   */
  public boolean readSubmissionsConcurrently(List<String> fileNames) {
    if (fileNames == null || fileNames.isEmpty())
      return false;

    List<SubmissionsThread> threads = new ArrayList<>();
    for (String fileName : fileNames) {
      SubmissionsThread thread = new SubmissionsThread(fileName);
      thread.start();
      threads.add(thread);
    }

    // Wait for all threads to finish
    for (SubmissionsThread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.err.println("Thread interrupted: " + e.getMessage());
      }
    }

    return true;
  }

  /**
   * Inner class representing a thread to read submissions from a file.
   */

  private class SubmissionsThread extends Thread {
    private String fileName;

    public SubmissionsThread(String fileName) {
      this.fileName = fileName;
    }

    @Override
    public void run() {
      try (BufferedReader reader = new BufferedReader(
          new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
          List<Integer> testResults = new ArrayList<>();
          StringBuilder nameBuilder = new StringBuilder();
          boolean readingName = true;
          int start = 0;

          for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
              String substring = line.substring(start, i).trim();
              if (!substring.isEmpty()) {
                if (readingName) {
                  nameBuilder.append(substring);
                  readingName = false;
                } else {
                  testResults.add(Integer.parseInt(substring));
                }
              }
              start = i + 1;
            }
          }
          // Add the last test result
          String substring = line.substring(start).trim();
          if (!substring.isEmpty()) {
            testResults.add(Integer.parseInt(substring));
          }

          synchronized (SPSS.this) {
            addSubmission(nameBuilder.toString(), testResults);
          }
        }
      } catch (IOException e) {
        System.err.println("Error reading file " + fileName 
            + ": " + e.getMessage());
      }
    }
  }

  /**
   * Calculates the score for the given student.
   * 
   * @param name The name of the student.
   * @return The score of the student's best submission, or -1 if the student 
   * is not found.
   */
  public int score(String name) {
    // Check if name is null or empty
    if (name == null || name.isEmpty()) {
      return -1;
    }

    synchronized (submissions) {
      // Find the best submission score for the student
      int bestScore = 0;
      boolean found = false;

      for (Submission submission : submissions) {
        if (submission.hasStudent(name)) {
          bestScore = Math.max(bestScore, submission.calculateTotalScore());
          found = true;
        }
      }
      // Return -1 if student not found, otherwise return the best score
      if (!found) {
        return -1;
      } else {
        return bestScore;
      }
    }
  }

  /**
   * Returns the number of submissions made by the given student.
   * 
   * @param name The name of the student.
   * @return The number of submissions made by the student.
   */
  public int numSubmissions(String name) {
    // Check if name is null or empty
    if (name == null || name.isEmpty()) {
      return -1;
    }

    int numSubmissions = 0;

    synchronized (submissions) {
      // Find the number of submissions for the student
      for (Submission submission : submissions) {
        if (submission.hasStudent(name)) {
          numSubmissions++;
        }
      }
      return numSubmissions;
    }
  }

  /**
   * Returns the total number of submissions in the SPSS system.
   * 
   * @return The total number of submissions.
   */
  public int numSubmissions() {
    return submissions.size();
  }

  /**
   * Checks if the given student's best submission is satisfactory.
   * 
   * @param name The name of the student.
   * @return true if the student submission is satisfactory, false otherwise.
   */
  public boolean satisfactory(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }

    int bestScore = 0;

    synchronized (submissions) {
      for (Submission submission : submissions) {
        if (submission.hasStudent(name)) {
          bestScore = Math.max(bestScore,
              submission.calculateTotalScore());

          if (submission.isSatisfactory(numTests)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks if the given student's submission has extra credit.
   * 
   * @param name The name of the student.
   * @return true if the student submission has extra credit, false otherwise.
   */
  public boolean gotExtraCredit(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }

    boolean hasExtraCredit = false;
    int submissionsMade = 0; 

    synchronized (submissions) {
      for (Submission submission : submissions) {
        if (submission.hasStudent(name)) {
          // Check to see if all tests are passed
          if (submission.allTestsPassed()) {
            hasExtraCredit = true;

          }
          submissionsMade++;
        }
      }
    }
    // Assure all tests are passed within first submission
    return hasExtraCredit && (submissionsMade == 1);
  }

}
