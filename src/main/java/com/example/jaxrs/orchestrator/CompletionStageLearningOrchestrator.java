package com.example.jaxrs.orchestrator;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.server.Uri;

import com.example.jaxrs.domain.Course;
import com.example.jaxrs.domain.CourseRecommendation;
import com.example.jaxrs.domain.OrchestratorResponse;
import com.example.jaxrs.domain.Quotation;
import com.example.jaxrs.domain.Tutor;


/**
 * Uses Java 8 CompletionStage and Jersey Client to obtain the data.
 */
@Path("/learning/completion")
@Produces("application/json")
public class CompletionStageLearningOrchestrator {

    @Uri("remote/course")
    private WebTarget courseTarget;

    @Uri("remote/tutor/{tutorId}")
    private WebTarget tutorTarget;

    @Uri("remote/quotation/course/{courseId}/months/{months}")
    private WebTarget quotationTarget;


    public CompletionStageLearningOrchestrator() {
    }


    @GET
    public void completion(@Suspended final AsyncResponse async) {
        final long time = System.nanoTime();
        
        final Queue<String> errors = new ConcurrentLinkedQueue<>();
        
        CompletableFuture.completedFuture(new OrchestratorResponse())
        	.thenCombine(enrolled(errors), OrchestratorResponse::setEnrolled)
        	.thenCombine(recommended(errors), OrchestratorResponse::setRecommended)
	        .whenCompleteAsync((response, throwable) -> {
	            //Do something with errors.
	        	response.setProcessingTime((System.nanoTime() - time) / 1000000);
	            async.resume(throwable == null ? response : throwable);
	        });
    }
    
    private CompletionStage<List<Course>> enrolled(final Queue<String> errors) {
		return courseTarget.path("enrolled")
				.request()
				.rx() //default reactive invoker: CompletionStageRxInvoker
				.get(new GenericType<List<Course>>() {})
				.exceptionally(throwable -> {
					errors.offer("Enrolled: " + throwable.getMessage());
					return Collections.emptyList();
		});
	}
    
    private CompletionStage<List<CourseRecommendation>> recommended(final Queue<String> errors) {
		final CompletionStage<List<Course>> recommendedCourses = courseTarget.path("recommended")
				.request()
				.rx() //default reactive invoker: CompletionStageRxInvoker
				.get(new GenericType<List<Course>>() {})
				.exceptionally(throwable -> {
					errors.offer("Recommended: " + throwable.getMessage());
					return Collections.emptyList();
				});
		
		return recommendedCourses.thenCompose(courses -> {
			//map: Course -> CompletionStage<CourseRecommendation>
			List<CompletionStage<CourseRecommendation>> recommendations = courses.stream().map(course -> {
				CourseRecommendation courseRecommendation = new CourseRecommendation();
				courseRecommendation.setCourseName(course.getName());
				courseRecommendation.setCourseDescription(course.getDescription());
				
				//for each course, obtain the tutor...
				CompletionStage<Tutor> tutor = tutorTarget.resolveTemplate("tutorId", course.getTutorId())
						.request()
						.rx()
						.get(Tutor.class)
						.exceptionally(throwable -> {
                            errors.offer("Tutor: " + throwable.getMessage());
                            return new Tutor();
                        });
				
				//...and the price
				CompletionStage<Quotation> quotation = quotationTarget.resolveTemplate("courseId", course.getId())
						.resolveTemplate("months", 3)
						.request()
						.rx()
						.get(Quotation.class)
						.exceptionally(throwable -> {
                            errors.offer("Quotation: " + throwable.getMessage());
                            return new Quotation();
                        });
				
				//for each course, obtain a CompletionStage<CourseRecommendation>
				return CompletableFuture.completedFuture(courseRecommendation)
						.thenCombine(tutor, CourseRecommendation::setTutor)
						.thenCombine(quotation, CourseRecommendation::setQuotation);
			}).collect(Collectors.toList());
			
			//transform List<CompletionStage<CourseRecommendation>> to CompletionStage<List<CourseRecommendation>>
			return sequence(recommendations);
		});
	}
    
    private <T> CompletionStage<List<T>> sequence(final List<CompletionStage<T>> stages) {
        final CompletableFuture<Void> done = CompletableFuture.allOf(stages.toArray(new CompletableFuture[stages.size()]));

        return done.thenApply(v -> stages.stream()
                        .map(CompletionStage::toCompletableFuture)
                        .map(CompletableFuture::join)
                        .collect(Collectors.<T>toList())
        );
    }

}
