package com.graphhopper.jsprit.examples;

import java.util.Arrays;
import java.util.Collection;

import org.graphstream.algorithm.measure.ChartMeasure.PlotType;

import com.graphhopper.jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.analysis.toolbox.StopWatch;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.io.VrpXMLReader;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.MapDistanceCosts;
import com.graphhopper.jsprit.core.util.Solutions;

public class MapDistanceExample {

	public static void main(String... args ){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("input/MapDistanceConfig.xml");
        
        //we will manually setup the vehicles and depots
        int nuOfVehicles = 1;
        int capacity = 80;
        Coordinate firstDepotCoord = Coordinate.newInstance(38.9649,77.4152);
        
        int depotCounter = 1;
        //the third vehicle will service all the people that require spanish speaking agents
        
        for (int i = 0; i < nuOfVehicles; i++) {
            VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type")
            		.addCapacityDimension(0, capacity)
            		.setCostPerDistance(1)
            		.setCostPerTransportTime(1)
            		.build();
            VehicleImpl vehicle = VehicleImpl.Builder.newInstance(depotCounter + "_" + (i + 1) + "_vehicle")
            		.setStartLocation(Location.newInstance(firstDepotCoord.getX(), firstDepotCoord.getY()))
            		.setType(vehicleType)
            		.setEarliestStart(0)
            		.setLatestArrival(600)
            		.addSkill("spanish").build();
            vrpBuilder.addVehicle(vehicle);
        }
        
        vrpBuilder.setFleetSize(FleetSize.FINITE);

		/*
         * build the problem
		 */
        vrpBuilder.setRoutingCost(new MapDistanceCosts(vrpBuilder.getLocations()));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "1").buildAlgorithm();
        
        
        vra.getAlgorithmListeners().addListener(new StopWatch(), Priority.HIGH);
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);
        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).setRenderDelay(50).display();
	}
}
