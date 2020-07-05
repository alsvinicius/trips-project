package br.com.iwe.handler;

import java.io.IOException;

import br.com.iwe.model.Trip;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.iwe.dao.TripRepository;
import br.com.iwe.model.HandlerRequest;
import br.com.iwe.model.HandlerResponse;

public class CreateTrip implements RequestHandler<HandlerRequest, HandlerResponse> {
	
	private final TripRepository repository = new TripRepository();

	@Override
	public HandlerResponse handleRequest(final HandlerRequest request, final Context context) {

		Trip trip = null;
		try {
			trip = new ObjectMapper().readValue(request.getBody(), Trip.class);
		} catch (IOException e) {
			return HandlerResponse.builder().setStatusCode(400).setRawBody("There is a error in your Trip!").build();
		}
		context.getLogger().log(
				new StringBuilder("Creating a new trip to ")
						.append(trip.getCity())
						.append(", ")
						.append(trip.getCountry())
						.toString()
		);
		final Trip tripRecorded = repository.save(trip);
		return HandlerResponse.builder().setStatusCode(201).setObjectBody(tripRecorded).build();
	}
}