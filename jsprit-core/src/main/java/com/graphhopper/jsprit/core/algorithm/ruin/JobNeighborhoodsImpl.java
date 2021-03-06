package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by schroeder on 07/01/15.
 */
class JobNeighborhoodsImpl implements JobNeighborhoods {

    private static Logger logger = LoggerFactory.getLogger(JobNeighborhoodsImpl.class);

    private VehicleRoutingProblem vrp;

    private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

    private JobDistance jobDistance;

    private double maxDistance = 0.;

    public JobNeighborhoodsImpl(VehicleRoutingProblem vrp, JobDistance jobDistance) {
        super();
        this.vrp = vrp;
        this.jobDistance = jobDistance;
        logger.debug("intialise {}", this);
    }

    @Override
    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {
        TreeSet<ReferencedJob> tree = distanceNodeTree.get(neighborTo.getId());
        if (tree == null) return new Iterator<Job>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Job next() {
                return null;
            }

            @Override
            public void remove() {

            }
        };
        Iterator<ReferencedJob> descendingIterator = tree.iterator();
        return new NearestNeighborhoodIterator(descendingIterator, nNeighbors);
    }

    @Override
    public void initialise() {
        logger.debug("calculates and memorizes distances from EACH job to EACH job --> n^2 calculations");
        calculateDistancesFromJob2Job();
    }

    @Override
    public double getMaxDistance() {
        return 0;
    }

    private void calculateDistancesFromJob2Job() {
        logger.debug("preprocess distances between locations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int nuOfDistancesStored = 0;
        for (Job i : vrp.getJobs().values()) {
            TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
                new Comparator<ReferencedJob>() {
                    @Override
                    public int compare(ReferencedJob o1, ReferencedJob o2) {
                        if (o1.getDistance() <= o2.getDistance()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            distanceNodeTree.put(i.getId(), treeSet);
            for (Job j : vrp.getJobs().values()) {
                if (i == j) continue;
                double distance = jobDistance.getDistance(i, j);
                if (distance > maxDistance) maxDistance = distance;
                ReferencedJob refNode = new ReferencedJob(j, distance);
                treeSet.add(refNode);
                nuOfDistancesStored++;
            }

        }
        stopWatch.stop();
        logger.debug("preprocessing comp-time: {}; nuOfDistances stored: {}; estimated memory: {}" +
            " bytes", stopWatch, nuOfDistancesStored, (distanceNodeTree.keySet().size() * 64 + nuOfDistancesStored * 92));
    }

}
