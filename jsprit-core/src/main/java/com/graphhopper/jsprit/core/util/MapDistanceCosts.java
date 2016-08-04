package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/*
 * we'll use a database storage somewhere down the road for this process
 * 
 */
public class MapDistanceCosts extends AbstractForwardVehicleRoutingTransportCosts implements TransportDistance{

	//how many meters per minute? 30 meters per minute
	public int speed = 30; 

    private Locations locations;

    public MapDistanceCosts(Locations locations) {
        super();
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "[name=MapDistanceCosts]";
    }
    
	@Override
	public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
		return calculateDistance(from, to);
	}
	
	private double calculateDistance(Location fromLocation, Location toLocation) {
        Coordinate from = null;
        Coordinate to = null;
        if (fromLocation.getCoordinate() != null & toLocation.getCoordinate() != null) {
            from = fromLocation.getCoordinate();
            to = toLocation.getCoordinate();
        } else if (locations != null) {
            from = locations.getCoord(fromLocation.getId());
            to = locations.getCoord(toLocation.getId());
        }
        if (from == null || to == null) throw new NullPointerException();
        return calculateDistance(from, to);
    }
	
	//in the future we can override this
	private double calculateDistance(Coordinate from, Coordinate to) {
        return MapDistanceRetrievalFromFile.getDistanceFromFile(from, to);
    }

	@Override
    public double getTransportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = calculateDistance(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("map distance is not in data registry");
        }
        return distance / speed;
	}

	@Override
	public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
		double distance;
        try {
            distance = calculateDistance(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("map distance is not in data registry");
        }
        double costs = distance;
        if (vehicle != null) {
            if (vehicle.getType() != null) {
                costs = distance * vehicle.getType().getVehicleCostParams().perDistanceUnit;
            }
        }
        return costs;
	}

}
