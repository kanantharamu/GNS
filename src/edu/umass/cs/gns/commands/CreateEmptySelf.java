/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands;

import static edu.umass.cs.gns.clientprotocol.Defs.*;

/**
 * Command that adds an empty field to the GNS for the given GUID.
 * 
 * @author westy
 */
public class CreateEmptySelf extends Create {

  public CreateEmptySelf(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, FIELD, SIGNATURE, "message"};
  }

  @Override
  public String getCommandName() {
    return CREATE;
  }

  @Override
  public String getCommandDescription() {
    return "Adds an empty field to the GNS for the given GUID.";
  }
}