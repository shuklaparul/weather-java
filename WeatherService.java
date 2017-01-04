package io.github.ideaqe.weather;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Path("/weather")
public class WeatherService {
	 private WeatherService weatherService;

    @Created
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public void createMeasurement(Observation observation) {
        Observations.getInstance().add(observation);
    }
    
    @GET
    @Path("/{stationId}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public String getObservationStats(@PathParam("stationId") int stationId) throws JSONException {
    	List<Float> getTemprature = new ArrayList<Float>();
		Collection<Observation> coll = Observations.getInstance().getObservations(stationId);

		for (Iterator<Observation> iterator = coll.iterator(); iterator.hasNext();) {
			Observation ob = (Observation) iterator.next();
			getTemprature.add(ob.temperature);
		}
		
		JSONObject response = getMinMaxAvg(getTemprature, stationId);
		return response.toString();
    }
    
    @POST
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryObservationStatsByDate(QueryObj query) throws ParseException, JSONException {
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
   		Date startDate = dateFormat.parse(String.valueOf(query.startDate));
   		Date endDate = dateFormat.parse(String.valueOf(query.endDate+1));
   		Collection<Observation> coll = Observations.getInstance().getObservations(query.stationId);
   		List<Float> tempList = new ArrayList<Float>();
   		for (Iterator<Observation> iterator = coll.iterator(); iterator.hasNext();) {
			Observation ob = (Observation) iterator.next();
			if (ob.timestamp.before(endDate) && ob.timestamp.after(startDate)) {
				tempList.add(ob.temperature);
			}
		}
   		
    	return getMinMaxAvg(tempList, query.stationId).toString();
    }

    @GET
    @Path("/{stationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Observation> getObservations(@PathParam("stationId") int stationId) {
        return Observations.getInstance().getObservations(stationId);
    }

    @GET
    @Path("/{stationId}/{observationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Observation getObservation(@PathParam("stationId") int stationId,
            @PathParam("observationId") int observationId) {
    	
        return Observations.getInstance().getObservation(stationId, observationId);
    }
    
    @ThreadSafe
    private static final class Observations {

        private final Map<Integer, Map<Integer, Observation>> observations = new ConcurrentHashMap();
        private static Logger logger = LoggerFactory.getLogger(Observations.class);
        private static final Observations INSTANCE = new Observations();


        private Observations() {
            initialize();
        }

        public static Observations getInstance() {
            return INSTANCE;
        }

        private void initialize() {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            CsvMapper mapper = new CsvMapper();
            ObjectReader reader = mapper.readerFor(Observation.class).with(schema);
            try {
                MappingIterator<Observation> csvData =
                        reader.readValues(Observations.class.getResourceAsStream("/data.csv"));
                csvData.readAll()
                        .stream()
                        .forEach(observation ->
                                observations.computeIfAbsent(observation.stationId, key -> new ConcurrentHashMap<>())
                                        .put(observation.observationId, observation));
            } catch (IOException ex) {
                logger.warn("Could not initialize with prepared CSV file.", ex);
            }
        }

        public Collection<Observation> getObservations(int stationId) {
            ensureExistence(stationId);
            return observations.get(stationId).values();
        }

        public void add(Observation observation) {
            Observation nullIfAssociated = observations
                    .computeIfAbsent(observation.stationId, key -> new ConcurrentHashMap<>())
                    .putIfAbsent(observation.observationId, observation);

            if (nullIfAssociated != null) {
                throw new CollisionException(
                        String.format("Observation for station %s with id %s already exists.",
                                observation.stationId, observation.observationId));
            }
        }

        public Observation getObservation(int stationId, int observationId) {
            ensureExistence(stationId, observationId);
            return observations.get(stationId).get(observationId);
        }

        private void ensureExistence(int stationId, int observationId) {
            ensureExistence(stationId);
            if (!observations.get(stationId).containsKey(observationId)) {
                throw new NotFoundException();
            }
        }

        private void ensureExistence(int stationId) {
            if (!observations.containsKey(stationId)) {
                throw new NotFoundException();
            }
        }
    }
    
    private JSONObject getMinMaxAvg(List<Float> temprature, int stationId) throws JSONException {
    	JSONObject response = new JSONObject();
		float sum = 0;
		float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
		for (Float temp : temprature) {
			if (temp > max) {
				max = temp;
			}
			if (temp < min) {
				min = temp;
			}
			sum +=temp;
		}
		int len = temprature.size();
		double average = sum/len;
		if (len == 0) {
			return response;
		}
		response.put("stationId", stationId);
		response.put("min", min);
		response.put("max", max);
		response.put("average", average);
		return response;
    }
    
}
 

