/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.nsdesign.replicationframework;


/**
 * We choose the type of replication needed. The options that currently work are LOCATION, RANDOM, and STATIC.
 * STATIC keeps replicas at same locations as replica controllers.
 * LOCATION chooses replica locations for a name at those name servers that are close to the local name servers
 * sending requests for tha name.
 * RANDOM chooses replica locations randomly.
 * Both LOCATION and RANDOM choose same number of replicas for a name; this number is determined by the read-to-write
 * ratio of that name.
 *
 * @author westy, Abhigyan
 */
public enum ReplicationFrameworkType {

  STATIC,
  RANDOM,
  LOCATION,
  BEEHIVE,
  OPTIMAL;

  public static ReplicationFrameworkInterface instantiateReplicationFramework(ReplicationFrameworkType type) {
    ReplicationFrameworkInterface framework;
    // what type of replication?
    switch (type) {
      case LOCATION:
        framework = new LocationBasedReplication();
        break;
      case RANDOM:
        framework = new RandomReplication();
        break;
      case STATIC:
        framework = null;
        break;
      case BEEHIVE:
        // Abhigyan: we will enable beehive if we again need to run experiments with it.
        throw new UnsupportedOperationException();
//        BeehiveReplication.generateReplicationLevel(StartNameServer.C,
//                StartNameServer.regularWorkloadSize + StartNameServer.mobileWorkloadSize,
//                StartNameServer.alpha, StartNameServer.base);
//        framework = new RandomReplication();
//        break;
      case OPTIMAL:
        throw new UnsupportedOperationException();
      default:
        throw new RuntimeException("Invalid replication framework");
    }
    return framework;
  }
}
