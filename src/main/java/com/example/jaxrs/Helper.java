package com.example.jaxrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.jaxrs.domain.Course;
import com.example.jaxrs.domain.Tutor;


public class Helper {


    private static final List<Course> COURSES = new ArrayList<>();
    private static final List<Tutor> TUTORS = new ArrayList<>();

    static {
        COURSES.add(new Course(1, "RxJava", "Reactive programming using RxJava 2", 1, 20));
        COURSES.add(new Course(2, "Spring Data", "Data persistence using Spring Data", 6, 30));
        COURSES.add(new Course(3, "FP / Haskel", "Functional programming in Haskel", 13, 25));
        COURSES.add(new Course(4, "Apache Spark", "Data analytics cluster-computing framework", 9, 20));
        COURSES.add(new Course(5, "OpenCV", "Image processing using OpenCV", 8, 15));
        COURSES.add(new Course(6, "JVM Memory Management", "Guide to JVM memory management", 11, 35));

        TUTORS.add(new Tutor(1, "Roger Patrick", 4.7d));
        TUTORS.add(new Tutor(6, "Sean Dean", 4d));
        TUTORS.add(new Tutor(13, "Francois Pierre", 4.5d));
        TUTORS.add(new Tutor(9, "Anna S", 4.6d));
        TUTORS.add(new Tutor(8, "Olivier C", 4.1d));
        TUTORS.add(new Tutor(11, "John M. Moyer", 4.9d));
    }

    public static Course getCourseById(int courseId) {
        for (Course course : COURSES) {
            if (courseId == course.getId()) {
                return course;
            }
        }
        return null;
    }

    public static void simulateDelay() {
        sleep(500);
    }

    public static List<Course> enrolledCourses(String user) {
        return Arrays.asList(COURSES.get(0));
    }

    public static List<Course> recommendedCourses(String user) {
        return COURSES;
    }

    public static Tutor getTutorById(int tutorId) {
        for (Tutor tutor : TUTORS) {
            if (tutorId == tutor.getId()) {
                return tutor;
            }
        }
        return null;
    }

    public static void sleep(long amount) {
        try {
            Thread.sleep(amount);
        } catch (InterruptedException e) {
        }
    }

}
