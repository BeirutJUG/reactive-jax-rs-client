package com.example.jaxrs.service;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.example.jaxrs.Helper;
import com.example.jaxrs.domain.Course;

@Path("/remote/course")
@Produces("application/json")
public class CourseResource {

	@GET
	@Path("/{courseId}")
	public Course courseById(@PathParam("courseId") final int courseId) {
		return Helper.getCourseById(courseId);
	}

	@GET
	@Path("/courses")
	public List<Course> enrolled(@HeaderParam("Rx-User") @DefaultValue("KO") final String user) {
		Helper.simulateDelay();

		return Helper.enrolledCourses(user);
	}

	@GET
	@Path("/recommended")
	public List<Course> recommended(@HeaderParam("Rx-User") @DefaultValue("KO") final String user) {
		Helper.simulateDelay();

		return Helper.recommendedCourses(user);
	}
}
