package com.example.jaxrs.orchestrator;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.server.Uri;

import com.example.jaxrs.domain.Course;
import com.example.jaxrs.domain.CourseRecommendation;
import com.example.jaxrs.domain.OrchestratorResponse;
import com.example.jaxrs.domain.Quotation;
import com.example.jaxrs.domain.Tutor;

@Path("/learning/sync")
@Produces("application/json")
public class SyncLearningOrchestrator {

	@Uri("remote/course")
    private WebTarget courseTarget;

	@Uri("remote/tutor/{tutorId}")
    private WebTarget tutorTarget;

	@Uri("remote/quotation/course/{courseId}/months/{months}")
    private WebTarget quotationTarget;

	@GET
	public OrchestratorResponse sync() {
		final long time = System.nanoTime();

		OrchestratorResponse response = new OrchestratorResponse();
		
		List<Course> enrolledCourses = courseTarget.path("courses")
				.request()
				.header("Rx-User", "Sync")
				.get(new GenericType<List<Course>>() {});

		List<Course> recommendedCourses = courseTarget.path("recommended")
					.request()
					.header("Rx-User", "Sync")
					.get(new GenericType<List<Course>>() {});


		List<CourseRecommendation> recommendedList = new ArrayList<>();
		for(Course course : recommendedCourses) {
			Tutor tutor = tutorTarget.resolveTemplate("tutorId", course.getTutorId())
					   .request()
					   .get(Tutor.class);

			Quotation quotation = quotationTarget
					.resolveTemplate("courseId", course.getId())
					.resolveTemplate("months", 3)
					.request()
					.get(Quotation.class);
			
			CourseRecommendation recommended = new CourseRecommendation();
			recommended.setCourseName(course.getName());
			recommended.setCourseDescription(course.getDescription());
			recommended.setTutor(tutor);
			recommended.setQuotation(quotation);

			recommendedList.add(recommended);
		}

		response.setEnrolled(enrolledCourses);
		response.setRecommended(recommendedList);
		response.setProcessingTime((System.nanoTime() - time) / 1000000);
		return response;
	}
}
