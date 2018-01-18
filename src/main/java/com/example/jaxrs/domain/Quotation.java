package com.example.jaxrs.domain;

public class Quotation {

	private int courseId;
	private int price;
	private int months;

	public Quotation() {
		
	}

	public Quotation(int courseId, int months, int price) {
		this.courseId = courseId;
		this.months = months;
		this.price = price;
	}

	public int getCourseId() {
		return courseId;
	}

	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getMonths() {
		return months;
	}

	public void setMonths(int months) {
		this.months = months;
	}

}
