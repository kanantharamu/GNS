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
public class AppendWithDuplicationSelf extends AbstractUpdate {

  public AppendWithDuplicationSelf(CommandModule module) {
    super(module);
  }

  @Override
  public UpdateOperation getUpdateOperation() {
    return UpdateOperation.APPEND_WITH_DUPLICATION;
  }

  @Override
  public String getCommandName() {
    return APPENDWITHDUPLICATION;
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, FIELD, VALUE, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandDescription() {
    return  "Appends the values onto this key value pair for the given GUID. Treats the list as a list, allows dupicates.";
  }
}
