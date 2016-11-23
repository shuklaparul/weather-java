package io.github.ideaqe.weather;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/weather")
public class WeatherService {

    @Created
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public void createMeasurement(Observation observation) {
        Observations.getInstance().add(observation);
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
            Map<Integer, Observation> result =
                    observations.computeIfAbsent(observation.stationId, key -> new ConcurrentHashMap<>());
            if (result.containsKey(observation.observationId)) {
                throw new CollisionException(
                        String.format("Observation for station %s with id %s already exists.",
                                observation.stationId, observation.observationId));
            }
            result.put(observation.observationId, observation);
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
}
