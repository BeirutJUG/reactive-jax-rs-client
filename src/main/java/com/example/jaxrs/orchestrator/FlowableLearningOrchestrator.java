package com.example.jaxrs.orchestrator;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvoker;
import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvokerProvider;
import org.glassfish.jersey.server.Uri;

import com.example.jaxrs.domain.Course;
import com.example.jaxrs.domain.CourseRecommendation;
import com.example.jaxrs.domain.OrchestratorResponse;
import com.example.jaxrs.domain.Quotation;
import com.example.jaxrs.domain.Tutor;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;


/**
 * Uses RxJava2 Flowable and Jersey Client to obtain the data.
 */
@Path("/learning/flowable")
@Produces("application/json")
public class FlowableLearningOrchestrator {

    @Uri("remote/course")
    private WebTarget courseTarget;

    @Uri("remote/tutor/{tutorId}")
    private WebTarget tutorTarget;

    @Uri("remote/quotation/course/{courseId}/months/{months}")
    private WebTarget quotationTarget;


    public FlowableLearningOrchestrator() {
    }


    @GET
    public void flowable(@Suspended final AsyncResponse async) {
    	final long time = System.nanoTime();
        
        final Queue<String> errors = new ConcurrentLinkedQueue<>();
        
        courseTarget.register(RxFlowableInvokerProvider.class);
        tutorTarget.register(RxFlowableInvokerProvider.class);
        quotationTarget.register(RxFlowableInvokerProvider.class);
        
        Flowable.just(new OrchestratorResponse())
        	.zipWith(enrolled(errors), OrchestratorResponse::setEnrolled)
        	.zipWith(recommended(errors), OrchestratorResponse::setRecommended)
            .observeOn(Schedulers.io()) //observe on another thread than the one processing enrolled or recommended courses
            .subscribe(response -> {
            	response.setProcessingTime((System.nanoTime() - time) / 1000000);
                async.resume(response);
                
            }, async::resume);
    }
    
    private Flowable<List<Course>> enrolled(final Queue<String> errors) {
		return courseTarget.path("enrolled")
				.request()
				.rx(RxFlowableInvoker.class)
				.get(new GenericType<List<Course>>() {})
				.onErrorReturn(throwable -> {
                    errors.offer("Enrolled: " + throwable.getMessage());
                    return Collections.emptyList();
                });
	}
    
    private Flowable<List<CourseRecommendation>> recommended(final Queue<String> errors) {
    	final Flowable<Course> courses = courseTarget.path("recommended")
				.request()
				.rx(RxFlowableInvoker.class)
				.get(new GenericType<List<Course>>() {})
				.onErrorReturn(throwable -> {
                    errors.offer("Recommended: " + throwable.getMessage());
                    return Collections.emptyList();
                })
				//emit recommended courses one-by-one
                .flatMap(Flowable::fromIterable);
    	
    	final Flowable<Tutor> tutors = courses.flatMap(course ->
    		tutorTarget.resolveTemplate("tutorId", course.getTutorId())
			.request()
			.rx(RxFlowableInvoker.class)
			.get(Tutor.class)
			.onErrorReturn(throwable -> {
                errors.offer("Tutor: " + throwable.getMessage());
                return new Tutor();
            }));
    	
    	final Flowable<Quotation> quotations = courses.flatMap(course ->
    		quotationTarget.resolveTemplate("courseId", course.getId())
			.resolveTemplate("months", 3)
			.request()
			.rx(RxFlowableInvoker.class)
			.get(Quotation.class)
			.onErrorReturn(throwable -> {
	            errors.offer("Quotation: " + throwable.getMessage());
	            return new Quotation();
	        }));
    	
    	return Flowable.zip(courses, tutors, quotations, CourseRecommendation::new)
    			.buffer(Integer.MAX_VALUE);
	}

}
