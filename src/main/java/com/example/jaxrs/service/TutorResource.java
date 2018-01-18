package com.example.jaxrs.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.example.jaxrs.Helper;
import com.example.jaxrs.domain.Tutor;

@Path("/remote/tutor")
@Produces("application/json")
public class TutorResource {

	@GET
	@Path("/{tutorId}")
	public Tutor tutorById(@PathParam("tutorId") final int tutorId) {
		// Simulate long-running operation.
        Helper.sleep(100);
        
		return Helper.getTutorById(tutorId);
	}
	
}
