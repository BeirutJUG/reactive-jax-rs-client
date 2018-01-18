package com.example.jaxrs.domain;

public class CourseRecommendation {

	private String courseName;
	private String courseDescription;
	private String tutorName;
	private double rating;
	private int price;

	public CourseRecommendation() {
	}
	
	public CourseRecommendation(Course course, Tutor tutor, Quotation quotation) {
		this.courseName = course.getName();
		this.courseDescription = course.getDescription();
		this.tutorName = tutor.getName();
		this.rating = tutor.getRating();
		this.price = quotation.getPrice();
	}

	
	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getCourseDescription() {
		return courseDescription;
	}

	public void setCourseDescription(String courseDescription) {
		this.courseDescription = courseDescription;
	}

	public String getTutorName() {
		return tutorName;
	}

	public double getRating() {
		return rating;
	}

	public CourseRecommendation setTutor(Tutor tutor) {
		this.tutorName = tutor.getName();
		this.rating = tutor.getRating();
		return this;
	}

	public int getPrice() {
		return price;
	}

	public CourseRecommendation setQuotation(Quotation quotation) {
		this.price = quotation.getPrice();
		return this;
	}
}
