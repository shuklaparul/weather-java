package io.github.ideaqe.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import javax.ws.rs.NotFoundException;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import io.github.ideaqe.weather.*;

public class TestWeatherService {
	static WeatherService ws;

	protected static void setUp() throws Exception {
		ws = new WeatherService();
	}

	@BeforeClass
	public static void init() throws Exception {
		setUp();
	}

	@Test
	/**
	 * Verify endpoint: getObservationStats(stationId) is returning valid Minimum,Maximum,Average for a given
	 * Station Id.
	 * 
	 * @throws JSONException
	 **/
	public void getValidStationId() throws JSONException {
		try {
			System.out.println("Verify endpoint: getObservationStats(Input : Station Id");
			// Test Setup : Enter Station Id to get temperature
			System.out.println("Enter Station Id");
			Scanner scanner = new Scanner(System.in);
			int stationId = scanner.nextInt();

			// Test Steps : endpoint is provided with Station id as Integer.
			String values = ws.getObservationStats(stationId);

			// Verification : Result is converted as a String object with self
			// explanatory output values
			System.out.println("Station retuns values from EndPoint getObservationStats as :" + values);
		} catch (NotFoundException e) {
			System.err.println("Invalid Number entered Please enter values from acceptable date range i.e : 1 to 9");

		}

	}

	@Test
	/**
	 * Verify endpoint:queryObservationStatsByDate(query) is returning valid Minimum,Maximum,Average for a given
	 * Timeframe for a Station Id.
	 * 
	 * @throws JSONException
	 **/
	public void getValidStationIdwithTimeframe() throws JSONException, ParseException {
		try {
			System.out.println("Verify endpoint:queryObservationStatsByDate(Input : Station Id,StartDate,EndDate)");
			// Test Setup : Enter Station Id and relevant Date range to get
			// temperatures
			System.out.println("Enter Station Id");
			Scanner scanner = new Scanner(System.in);
			int stationId = scanner.nextInt();
			System.out.println("Enter Start Date as YYYYMMDD");
			scanner = new Scanner(System.in);
			int startDate = scanner.nextInt();
			System.out.println("Enter End Date as YYYYMMDD");
			scanner = new Scanner(System.in);
			int endDate = scanner.nextInt();

			// Test Steps : endpoint is provided with data parsed as a JSON
			// object
			QueryObj query = new QueryObj(startDate, endDate, stationId);
			String values = ws.queryObservationStatsByDate(query);

			// Verification : Result is converted as a String object with self
			// explanatory output values
			System.out.println("Data returned from queryObservationStatsByDate is :" + values);
		} catch (NotFoundException e) {
			System.err.println(
					"Invalid Number entered Please enter values from acceptable date range OR acceptable Station ID");

		}

	}

	@Test
    /**
	 * Verify endpoint: getObservation(stationId, observationId)  returns ObservationId wrt Station id
	 * 
	 * @throws JSONException
	 **/
	public void getObservationId() throws JSONException {
		try {
			System.out.println("Verify endpoint: getObservation(Input : Station ID,Observation Id))");
			// Test Setup : Enter Station Id to get temperature
			System.out.println("Enter Station Id");
			Scanner scanner = new Scanner(System.in);
			int stationId = scanner.nextInt();
			System.out.println("Enter Observation Id");
			scanner = new Scanner(System.in);
			int observationId = scanner.nextInt();

			// Test Steps : endpoint is provided with Station id as Integer.
			Observation values = ws.getObservation(stationId, observationId);
			Map<String, Float> stationMetrics = new HashMap<String, Float>();
			stationMetrics.put("Humidity", values.humidity);
			stationMetrics.put("Precipitation", values.precipitation);
			stationMetrics.put("Temperature", values.temperature);

			// Verification : Result is converted as a String object with self
			// explanatory output values
			for (Entry<String, Float> metrics : stationMetrics.entrySet()) {
				String key = metrics.getKey();
				float value = metrics.getValue();
				System.out.println("Station retuns values from EndPoint getObservationStats as :" + key + " " + value);
			}

		} catch (NotFoundException e) {
			System.err.println("Invalid Number entered Please enter values from acceptable date range i.e : 1 to 9");

		}

	}
	
	@Test
	/**
	 * Verify endpoint: createMeasurement(Observation observation)  returns ObservationId wrt Station id
	 * 
	 * @throws JSONException
	 **/
	public void createObservation() throws JSONException, ParseException {
		try {
			System.out.println("Verify endpoint: createMeasurement(Observation observation))");
			// Test Setup : Enter Station Id to get temperature
			System.out.println("Enter Station Id");
			Scanner scanner = new Scanner(System.in);
			int stationId = scanner.nextInt();		
			System.out.println("Enter Observation Id");
			scanner = new Scanner(System.in);
			int observationId = scanner.nextInt();
			System.out.println("Enter Humidity");
			scanner = new Scanner(System.in);
			float humidity = scanner.nextFloat();
			System.out.println("Enter Temperature");
			scanner = new Scanner(System.in);
			float temp = scanner.nextFloat();
			System.out.println("Enter Precipitation");
			scanner = new Scanner(System.in);
			float precp = scanner.nextFloat();
			long time = System.currentTimeMillis();
			String getTime = String.valueOf(time);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	   		Date date = dateFormat.parse(String.valueOf(getTime));
			
	   		//Test Steps 
			Observation ob = new Observation(observationId,stationId,temp,humidity,precp,date);
			ws.createMeasurement(ob);
			// verification
			
			Collection<Observation> values = ws.getObservations(stationId);
			for (Iterator<Observation> iterator = values.iterator(); 
					iterator.hasNext();) 
			{
				Observation ob1 = (Observation) iterator.next();
				int observation = ob1.observationId;
				float humid = ob1.humidity;
				float prescp = ob1.precipitation;
				float tempra = ob1.temperature;
				Date timest = ob1.timestamp;
				
			// Verification : Result is converted as a String object with self
						// explanatory output values
						System.out.println("Station retuns values for new record : Humidity" + humid);
						System.out.println("Station retuns values for new record : Precipitation" + prescp);
						System.out.println("Station values for new records : Temperature" + tempra);
						System.out.println("Station values for new record : Observation" + observation);
						System.out.println("Station values for new record : TimeStamp" + timest);
					}
		} catch (NotFoundException e) {
						System.err.println("Invalid Number entered Please enter values from acceptable date range i.e : 1 to 9");

					}



		}
}

