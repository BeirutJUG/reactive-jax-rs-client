package com.example.jaxrs.service;

import java.util.Random;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.ManagedAsync;

import com.example.jaxrs.Helper;
import com.example.jaxrs.domain.Quotation;

@Path("/remote/quotation")
@Produces("application/json")
public class QuotationResource {

	@GET
    @ManagedAsync
    @Path("course/{courseId}/months/{months}")
    public Quotation calculation(@PathParam("courseId") final int courseId, @PathParam("months") @DefaultValue("3") final int months) {
        // Simulate long-running operation.
        Helper.sleep(350);

        return new Quotation(courseId, months, new Random().nextInt(10000));
    }
}
