package com.example.jaxrs.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.Uri;

import com.example.jaxrs.domain.Course;
import com.example.jaxrs.domain.CourseRecommendation;
import com.example.jaxrs.domain.OrchestratorResponse;
import com.example.jaxrs.domain.Quotation;
import com.example.jaxrs.domain.Tutor;

@Path("/learning/async")
@Produces("application/json")
public class AsyncLearningOrchestrator {

	@Uri("remote/course")
	private WebTarget courseTarget;

	@Uri("remote/tutor/{tutorId}")
	private WebTarget tutorTarget;

	@Uri("remote/quotation/course/{courseId}/months/{months}")
	private WebTarget quotationTarget;

	@GET
	@ManagedAsync
	public void async(@Suspended final AsyncResponse async) {
		final long time = System.nanoTime();

		final OrchestratorResponse response = new OrchestratorResponse();

		final CountDownLatch outerLatch = new CountDownLatch(1);
		final Queue<String> errors = new ConcurrentLinkedQueue<>();

		courseTarget.path("recommended")
					.request()
					.async().get(new InvocationCallback<List<Course>>() {
					@Override
					public void completed(final List<Course> recommended) {
						final CountDownLatch innerLatch = new CountDownLatch(recommended.size() * 2);

						// Tutors. (depend on recommended courses)
						final Map<Integer, Tutor> tutors = Collections.synchronizedMap(new HashMap<>());
						for (final Course course : recommended) {
							tutorTarget.resolveTemplate("tutorId", course.getTutorId()).request().async()
									.get(new InvocationCallback<Tutor>() {
										@Override
										public void completed(final Tutor tutor) {
											tutors.put(course.getId(), tutor);
											innerLatch.countDown();
										}

										@Override
										public void failed(final Throwable throwable) {
											errors.offer("Tutor: " + throwable.getMessage());
											innerLatch.countDown();
										}
									});
						}

						// Quotations. (depend on recommended courses)
						final List<Future<Quotation>> futures = recommended.stream()
								.map(course -> quotationTarget.resolveTemplate("courseId", course.getId())
										.resolveTemplate("months", 3).request().async().get(Quotation.class))
								.collect(Collectors.toList());

						final Map<Integer, Quotation> quotations = new HashMap<>();
						while (!futures.isEmpty()) {
							final Iterator<Future<Quotation>> iterator = futures.iterator();

							while (iterator.hasNext()) {
								final Future<Quotation> future = iterator.next();
								if (future.isDone()) {
									try {
										final Quotation quotation = future.get();
										quotations.put(quotation.getCourseId(), quotation);

										innerLatch.countDown();
									} catch (final Throwable t) {
										errors.offer("Quotation: " + t.getMessage());
										innerLatch.countDown();
									} finally {
										iterator.remove();
									}
								}
							}
						}

						// Wait until dependent requests are complete
						try {
							if (!innerLatch.await(10, TimeUnit.SECONDS)) {
								errors.offer("Inner: Waiting for requests to complete has timed out.");
							}
						} catch (final InterruptedException e) {
							errors.offer("Inner: Waiting for requests to complete has been interrupted.");
						}

						final List<CourseRecommendation> recommendations = new ArrayList<>(recommended.size());
						for (final Course course : recommended) {
							final Tutor tutor = tutors.get(course.getId());
							final Quotation quotation = quotations.get(course.getId());

							CourseRecommendation courseRecommendation = new CourseRecommendation();
							courseRecommendation.setCourseName(course.getName());
							courseRecommendation.setCourseDescription(course.getDescription());
							courseRecommendation.setTutor(tutor);
							courseRecommendation.setQuotation(quotation);
							recommendations.add(courseRecommendation);
						}
						response.setRecommended(recommendations);
						outerLatch.countDown();
					}

					@Override
					public void failed(final Throwable throwable) {
						errors.offer("Recommended: " + throwable.getMessage());
						outerLatch.countDown();
					}
				});

		try {
			if (!outerLatch.await(10, TimeUnit.SECONDS)) {
				errors.offer("Outer: Waiting for requests to complete has timed out.");
			}
		} catch (final InterruptedException e) {
			errors.offer("Outer: Waiting for requests to complete has been interrupted.");
		}

		response.setErrors(new ArrayList<>(errors));

		response.setProcessingTime((System.nanoTime() - time) / 1000000);
		async.resume(response);
	}
}
