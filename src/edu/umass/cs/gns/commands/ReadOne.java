/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands;

import static edu.umass.cs.gns.clientsupport.Defs.*;

/**
 *
 * @author westy
 */
public class ReadOne extends Read {

  public ReadOne(CommandModule module) {
    super(module);
  }

  @Override
  public String getCommandName() {
    return READONE;
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, FIELD, READER, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandDescription() {
    return "Returns one key value pair from the GNS for the given guid after authenticating that the readerguid (GUID making request) has access authority. "
            + "Treats the value of key value pair as a singleton item and returns that item."
            + " Specify " + ALLFIELDS + " as the <field> to return all fields. ";
  }
}
