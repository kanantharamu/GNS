/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands;

import edu.umass.cs.gns.client.AccountInfo;
import edu.umass.cs.gns.client.FieldMetaData;
import edu.umass.cs.gns.client.GuidInfo;
import edu.umass.cs.gns.clientprotocol.AccessSupport;
import edu.umass.cs.gns.clientprotocol.ClientUtils;
import static edu.umass.cs.gns.clientprotocol.Defs.*;
import edu.umass.cs.gns.httpserver.Defs;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author westy
 */
public class AddAlias extends GnsCommand {

  public AddAlias(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, NAME, SIGNATURE, "message"};
  }

  @Override
  public String getCommandName() {
    return ADDALIAS;
  }

  @Override
  public String execute(JSONObject json) throws InvalidKeyException, InvalidKeySpecException,
          JSONException, NoSuchAlgorithmException, SignatureException {
    String guid = json.getString(GUID);
    String name = json.getString(NAME);
    String signature = json.getString(SIGNATURE);
    String message = json.getString("message");
    GuidInfo guidInfo;
    if ((guidInfo = accountAccess.lookupGuidInfo(guid)) == null) {
      return BADRESPONSE + " " + BADGUID + " " + guid;
    }
    if (AccessSupport.verifySignature(guidInfo, signature, message)) {
      AccountInfo accountInfo = accountAccess.lookupAccountInfoFromGuid(guid);
      if (!accountInfo.isVerified()) {
        return BADRESPONSE + " " + VERIFICATIONERROR + "Account not verified";
      } else if (accountInfo.getAliases().size() > Defs.MAXALIASES) {
        return BADRESPONSE + " " + TOMANYALIASES;
      } else {
        return accountAccess.addAlias(accountInfo, name);
      }
    } else {
      return BADRESPONSE + " " + BADSIGNATURE;
    }
  }

  @Override
  public String getCommandDescription() {
    return "Adds a additional human readble name to the account associated with the GUID. "
            + "Must be signed by the guid. Returns " + BADGUID + " if the GUID has not been registered.";



  }
}
