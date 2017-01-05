package io.github.ideaqe.weather;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryObj {

	public final int startDate, endDate, stationId;
	

    public QueryObj(@JsonProperty("start_date") int startDate, @JsonProperty("end_date") int endDate,
            @JsonProperty("station_id") int stationId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.stationId = stationId;
    }
   

    @Override
    public String toString() {
        return "Query{" +
                ", stationId=" + stationId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
    
}
