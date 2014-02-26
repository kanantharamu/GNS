/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands;

import edu.umass.cs.gns.clientsupport.UpdateOperation;
import static edu.umass.cs.gns.clientsupport.Defs.*;

/**
 *
 * @author westy
 */
public class RemoveListSelf extends AbstractUpdateList {

  public RemoveListSelf(CommandModule module) {
    super(module);
  }

  @Override
  public UpdateOperation getUpdateOperation() {
    return UpdateOperation.REMOVE;
  }

  @Override
  public String getCommandName() {
    return REMOVELIST;
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, FIELD, VALUE, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandDescription() {
    return "Removes all the values from the key value pair for the given GUID. Value is a list of items formated as a JSON list.";
  }
}
