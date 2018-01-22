package com.example.jaxrs.domain;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorResponse {

	private List<Course> enrolled = new ArrayList<>();
	private List<CourseRecommendation> recommended = new ArrayList<>();
	private long processingTime;
	private List<String> errors = new ArrayList<>();

	public OrchestratorResponse() {
	}


	public List<Course> getEnrolled() {
		return enrolled;
	}

	public OrchestratorResponse setEnrolled(List<Course> enrolled) {
		this.enrolled = enrolled;
		return this;
	}
	
	public List<CourseRecommendation> getRecommended() {
		return recommended;
	}

	public OrchestratorResponse setRecommended(List<CourseRecommendation> recommended) {
		this.recommended = recommended;
		return this;
	}

	public long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
}
