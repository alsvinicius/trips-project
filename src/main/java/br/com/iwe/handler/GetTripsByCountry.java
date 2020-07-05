package br.com.iwe.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import br.com.iwe.dao.TripRepository;
import br.com.iwe.model.HandlerRequest;
import br.com.iwe.model.HandlerResponse;
import br.com.iwe.model.Trip;
import com.amazonaws.util.StringUtils;

public class GetTripsByCountry implements RequestHandler<HandlerRequest, HandlerResponse> {

	private final TripRepository repository = new TripRepository();

	@Override
	public HandlerResponse handleRequest(HandlerRequest request, Context context) {

		final String country = request.getPathParameters().get("country");
		final Map<String, String> parameters = request.getQueryStringParameters();

		List<Trip> trips = new ArrayList<>();

		if (StringUtils.isNullOrEmpty(country)) {
			return HandlerResponse.builder().setStatusCode(400).setRawBody("Invalid country!").build();
		}

		if (parameters == null) {
			context.getLogger().log("Searching for registered trips to " + country);
			trips = this.repository.findByCountry(country);
		} else {
			final String city = (parameters == null) ? "" : parameters.get("city");
			final String starts = request.getQueryStringParameters().get("starts");
			final String ends = request.getQueryStringParameters().get("ends");

			if (StringUtils.isNullOrEmpty(city) && StringUtils.isNullOrEmpty(starts)) {
				return HandlerResponse.builder().setStatusCode(400).setRawBody("No valid parameters found!").build();
			}

			if (!StringUtils.isNullOrEmpty(city)) {
				context.getLogger()
						.log("Searching for registered trips to " + city + " in " + country);
				trips = this.repository.findByCity(country, city);
			}

			if (!StringUtils.isNullOrEmpty(starts)) {
				if (StringUtils.isNullOrEmpty(ends)) {
					return HandlerResponse.builder().setStatusCode(400).setRawBody("Both starts and ends parameters are required!").build();
				}

				context.getLogger().log("Searching for registered trips " + " between " + starts + " and " + ends);
				trips = this.repository.findByPeriod(country, starts, ends);
			}
		}

		if (trips == null || trips.isEmpty()) {
			return HandlerResponse.builder().setStatusCode(404).build();
		}

		return HandlerResponse.builder().setStatusCode(200).setObjectBody(trips).build();
	}
}