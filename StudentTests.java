package tests;

import org.junit.*;

import spss.SPSS;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StudentTests {
  
  /*
   * Wahida Zaker
   * UID: 118026404
   * Directory ID: wzaker
   * Discussion: 0306
   * 
   * I pledge on my honor that I have not given or received
   * any unauthorized assistance on this assignment
   * 
   * */

  @Test
  public void testReadSubmissionsConcurrently1() {
    SPSS spss = new SPSS(5);
    List<String> fileNames = Arrays.asList("public10-input");
    assertTrue(spss.readSubmissionsConcurrently(fileNames));
  }

  @Test
  public void RMinTestEmptyFileNamesList() {
    SPSS spss = new SPSS(5);
    assertFalse(spss.readSubmissionsConcurrently(Collections.emptyList()));
    assertEquals(0, spss.numSubmissions());
  }


  private void addStudents(SPSS spss) {
    spss.addStudent("GinnyGiraffe");
    spss.addStudent("WallyWalrus");
  }

  @Test
  public void numStudentsTestEmpty() {
    SPSS spss = new SPSS(5);
    assertEquals(0, spss.numStudents());
  }

  @Test
  public void numStudentsTestNonEmpty() {
    SPSS spss = new SPSS(5);
    spss.addStudent("GinnyGiraffe");
    assertEquals(1, spss.numStudents());
  }

  @Test
  public void numSubmissionsTestEmpty() {
    SPSS spss = new SPSS(5);
    assertEquals(0, spss.numSubmissions());
  }

  @Test
  public void numSubmissionsTestNonEmpty() {
    SPSS spss = new SPSS(5);
    addStudents(spss);
    spss.addSubmission("GinnyGiraffe", Arrays.asList(20, 20, 5, 0, 20));
    assertEquals(1, spss.numSubmissions());
  }

  @Test
  public void addStudentTestValid() {
    SPSS spss = new SPSS(5);
    assertTrue(spss.addStudent("GinnyGiraffe"));
  }

  @Test
  public void addStudentTestInvalid() {
    SPSS spss = new SPSS(5);
    assertFalse(spss.addStudent(""));
  }

  @Test
  public void addSubmissionTestValid() {
    SPSS spss = new SPSS(5);
    addStudents(spss);
    assertTrue(spss.addSubmission("GinnyGiraffe", Arrays.asList(20, 20, 5, 0, 20)));
  }

  @Test
  public void addSubmissionTestInvalid() {
    SPSS spss = new SPSS(5);
    addStudents(spss);
    assertFalse(spss.addSubmission("InvalidStudent", Arrays.asList(20, 20, 5, 0, 20)));
  }

  @Test
  public void scoreTestValid() {
    SPSS spss = new SPSS(5);
    addStudents(spss);
    spss.addSubmission("GinnyGiraffe", Arrays.asList(20, 20, 5, 0, 20));
    assertEquals(65, spss.score("GinnyGiraffe"));
  }

  @Test
  public void scoreTestInvalid() {
    SPSS spss = new SPSS(5);
    assertEquals(-1, spss.score("InvalidStudent"));
  }

  @Test
  public void readSubmissionsConcurrentlyTestEmptyFileNames() {
    SPSS spss = new SPSS(5);
    assertFalse(spss.readSubmissionsConcurrently(Collections.emptyList()));
    assertEquals(0, spss.numSubmissions());
  }


}
