/* Copyright (1c) 2016 University of Massachusetts
 * 
 * Licensed under the Apache License, Version 2.0 (1the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Initial developer(s): Westy */
package edu.umass.cs.gnsclient.client;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gnsclient.client.util.GuidEntry;
import edu.umass.cs.gnsclient.client.util.GuidUtils;
import edu.umass.cs.gnsclient.client.util.KeyPairUtils;
import edu.umass.cs.gnsclient.client.util.Password;
import edu.umass.cs.gnscommon.AclAccessType;
import edu.umass.cs.gnscommon.CommandType;
import edu.umass.cs.gnscommon.SharedGuidUtils;
import edu.umass.cs.gnscommon.exceptions.client.ClientException;
import edu.umass.cs.gnscommon.exceptions.client.InvalidGuidException;
import edu.umass.cs.gnscommon.packets.AdminCommandPacket;
import edu.umass.cs.gnscommon.packets.CommandPacket;
import edu.umass.cs.gnscommon.utils.Base64;
import edu.umass.cs.gnscommon.GNSProtocol;

/**
 * @author arun
 *
 * A helper class with static methods to help construct GNS commands.
 */
public class GNSCommand extends CommandPacket {

  /* GNSCommand constructors must remain private */

  /**
   *
   * @param command
   */

  protected GNSCommand(JSONObject command) {
    this(
            /* arun: we just generate a random value here because it is not easy (or
		 * worth trying) to guarantee non-conflicting IDs here. Conflicts will
		 * either result in an IOException further down or the query will be
		 * transformed to carry a different ID if */
            randomLong(), command);
  }

  /**
   *
   * @param id
   * @param command
   */
  protected GNSCommand(long id, JSONObject command) {
    super(id, command);
  }

  // Need this for the HTTP server. Since we can't make the methods above public.
  /**
   * Creates a GNS Command from a JSON Object.
   * 
   * @param command
   * @return 
   */
  public static GNSCommand createGNSCommandFromJSONObject(JSONObject command) {
    return new GNSCommand(command);
  }
  /**
   * Constructs a command of type {@code type} issued by the {@code querier}
   * using the variable length array {@code keysAndValues}. If {@code querier}
   * is non-null, the returned command will be signed by the querier's private
   * key. If {@code querier} is null, the command will succeed only if the
   * operation is open to all, which is normally true only for fields that are
   * readable by anyone.
   *
   * @param type
   * @param querier
   * The GNSProtocol.GUID.toString() issuing this query.
   * @param keysAndValues
   * A variable length array of even size containing a sequence of
   * key and value pairs.
   * @return A {@link CommandPacket} constructed using the supplied arguments.
   * @throws ClientException
   */
  public static CommandPacket getCommand(CommandType type, GuidEntry querier,
          Object... keysAndValues) throws ClientException {
    JSONObject command = CommandUtils.createAndSignCommand(type, querier,
            keysAndValues);
    if (CommandPacket.getJSONCommandType(command).isMutualAuth()) {
      return new AdminCommandPacket(randomLong(), command);
    }
    return new GNSCommand(command);
  }

  /**
   * @param type
   * @param keysAndValues
   * @return CommandPacket
   * @throws ClientException
   */
  public static CommandPacket getCommand(CommandType type,
          Object... keysAndValues) throws ClientException {
    return getCommand(type, null, keysAndValues);
  }

  /**
   * We just use a random long here as we will either get an IOException if
   * there is a conflicting request ID, i.e., if another unequal request is
   * awaiting a response in the async client unless ENABLE_ID_TRANSFORM is
   * true.
   */
  private static long randomLong() {
    return (long) (Math.random() * Long.MAX_VALUE);
  }

  /* ********** Start of actual command construction methods ********* */
  /**
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param json
   * The JSONObject representation of the entire record value
   * excluding the targetGUID itself.
   *
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   *
   * @throws ClientException
   */
  public static final CommandPacket update(String targetGUID,
          JSONObject json, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.ReplaceUserJSON, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.USER_JSON.toString(), json.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param json
   * The JSONObject representation of the entire record value
   * excluding the targetGUID itself.
   * @return Refer {@link #update(String, JSONObject, GuidEntry)}
   * @throws ClientException
   */
  public static final CommandPacket update(GuidEntry targetGUID,
          JSONObject json) throws ClientException {
    return update(targetGUID.getGuid(), json, targetGUID);
  }

  /**
   * @param targetGuid
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The value being assigned to targetGUID.field.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return Refer
   * {@link GNSClientCommands#fieldUpdate(String, String, Object, GuidEntry)}
   * .
   * @throws ClientException
   */
  public static final CommandPacket fieldUpdate(String targetGuid,
          String field, Object value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.ReplaceUserJSON, querierGUID, GNSProtocol.GUID.toString(),
            targetGuid, GNSProtocol.USER_JSON.toString(), getJSONObject(field, value).toString(),
            GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  // converts JSONException to ClientException

  /**
   *
   * @param field
   * @param value
   * @return a JSONObject
   * @throws JSONException
   */
  protected static JSONObject makeJSON(String field, Object value)
          throws JSONException {
    return new JSONObject().put(field, value);
  }

  // converts JSONException to ClientException

  /**
   *
   * @param field
   * @param value
   * @return a JSONObject
   * @throws ClientException
   */
  protected static JSONObject getJSONObject(String field, Object value)
          throws ClientException {
    try {
      return makeJSON(field, value);
    } catch (JSONException e) {
      throw new ClientException(e);
    }
  }

  /**
   * Creates an index for a field. {@code targetGUID} is only used for
   * authentication purposes. This command will succeed only the issuer has
   * the credentials to issue this query.
   *
   * @param GUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @param field
   * The field key.
   * @param index
   * The name of the index being created upon {@code field}.
   * @return CommandPacket
   * @throws ClientException
   */
  protected static final CommandPacket fieldCreateIndex(GuidEntry GUID,
          String field, String index) throws ClientException {
    return getCommand(CommandType.CreateIndex, GUID, GUID, GUID.getGuid(),
            GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), index, GNSProtocol.WRITER.toString(), GUID.getGuid());
  }

  /**
   * Updates {@code targetGUID}:{@code field} to {@code value}. Signs the
   * query using the private key of {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The value being assigned to {@code field}.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldUpdate(GuidEntry targetGUID,
          String field, Object value) throws ClientException {
    return fieldUpdate(targetGUID.getGuid(), field, value, targetGUID);
  }

  /**
   * Reads the entire record for {@code targetGUID}. {@code reader} is the
 GNSProtocol.GUID.toString() issuing the query and must be present in the ACL for
 {@code targetGUID}{@code GNSProtocol.ENTIRE_RECORD.toString()} for the query to succeed. If
   * {@code reader} is null, {@code targetGUID}{@code GNSProtocol.ENTIRE_RECORD.toString()} must be
   * globally readable.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket read(String targetGUID,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(querierGUID != null ? CommandType.ReadArray
            : CommandType.ReadArrayUnsigned, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), GNSProtocol.ENTIRE_RECORD.toString(), GNSProtocol.READER.toString(),
            querierGUID != null ? querierGUID.getGuid() : null);
  }

  /**
   * Reads the entire record for {@code targetGUID} implicitly assuming that
   * the querier is also {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @return CommandPacket
   * @throws Exception
   */
  public static final CommandPacket read(GuidEntry targetGUID)
          throws Exception {
    return read(targetGUID.getGuid(), targetGUID);
  }

  /**
   * Checks if {@code field} exists in {@code targetGuid}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldExists(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(querierGUID != null ? CommandType.Read
            : CommandType.ReadUnsigned, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.READER.toString(),
            querierGUID != null ? querierGUID.getGuid() : null);
  }

  /**
   * Checks if {@code field} exists in {@code targetGuid}. The querier GNSProtocol.GUID.toString() is
 assumed to also be {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldExists(GuidEntry targetGUID,
          String field) throws ClientException {
    return fieldExists(targetGUID.getGuid(), field, targetGUID);
  }

  /**
   * Reads the value of {@code targetGUID}:{@code field}. {@code querierGUID},
   * if not null, must be present in the ACL for {@code targetGUID}:
   * {@code field}; if null, {@code targetGUID}:{@code field} must be globally
   * readable.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return a string containing the values in the field
   * @throws ClientException
   */
  public static final CommandPacket fieldRead(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(querierGUID != null ? CommandType.Read
            : CommandType.ReadUnsigned, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.READER.toString(),
            querierGUID != null ? querierGUID.getGuid() : null);
  }

  /**
   * Same as {@link #fieldRead(String, String, GuidEntry)} with
   * {@code querierGUID} set to {@code targetGUID}. The result type of the
   * execution result of this query is {@link GNSCommand.ResultType#MAP}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldRead(GuidEntry targetGUID,
          String field) throws ClientException {
    return fieldRead(targetGUID.getGuid(), field, targetGUID);
  }

  /**
   * Reads {@code targetGUID}:{@code field} for each field in {@code fields}.
   * {@code querierGUID} must be present in the read ACL of every field in
   * {@code fields}. The result type of the execution result of this query is
   * {@link GNSCommand.ResultType#LIST}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param fields
   * The list of field keys being queried.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldRead(String targetGUID,
          ArrayList<String> fields, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(querierGUID != null ? CommandType.ReadMultiField
            : CommandType.ReadMultiFieldUnsigned, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELDS.toString(), fields, GNSProtocol.READER.toString(),
            querierGUID != null ? querierGUID.getGuid() : null);
  }

  /**
   * Same as {@link #fieldRead(String, ArrayList, GuidEntry)} with
   * {@code querierGUID} set to {@code targetGUID}.
   *
   * @param targetGUID
   * @param fields
   * The list of field keys.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldRead(GuidEntry targetGUID,
          ArrayList<String> fields) throws ClientException {
    return fieldRead(targetGUID.getGuid(), fields, targetGUID);
  }

  /**
   * Removes {@code targetGUID}:{@code field}. {@code querierGUID} must be
   * present in the write ACL of {@code targetGUID}:{@code field} for the
   * query to succeed.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldRemove(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.RemoveField, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  // ACCOUNT COMMANDS
  /**
   * Retrieves the GNSProtocol.GUID.toString() of {@code alias}.
   *
   * @param alias
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket lookupGUID(String alias)
          throws ClientException {

    return getCommand(CommandType.LookupGuid, GNSProtocol.NAME.toString(), alias);
  }

  /**
   * If this is a sub-GNSProtocol.GUID.toString(), this command looks up the corresponding account
 GNSProtocol.GUID.toString().
   *
   * @param subGUID
   * @return Account GNSProtocol.GUID.toString() of {@code subGUID}
   * @throws ClientException
   */
  public static final CommandPacket lookupPrimaryGUID(String subGUID)
          throws ClientException {
    return getCommand(CommandType.LookupPrimaryGuid, GNSProtocol.GUID.toString(), subGUID);
  }

  /**
   * Looks up guid metadata for {@code targetGUID}. The result type of the
   * execution result of this query is {@link GNSCommand.ResultType#MAP}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket lookupGUIDRecord(String targetGUID)
          throws ClientException {
    return (getCommand(CommandType.LookupGuidRecord, GNSProtocol.GUID.toString(), targetGUID));
  }

  /**
   * Looks up the the account metadata for {@code accountGUID}.
   *
   * @param accountGUID
   * The account GNSProtocol.GUID.toString() being queried.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket lookupAccountRecord(String accountGUID)
          throws ClientException {
    return getCommand(CommandType.LookupAccountRecord, GNSProtocol.GUID.toString(), accountGUID);
  }

  /**
   * Get the public key for {@code alias}.
   *
   * @param alias
   * @return CommandPacket
   * @throws ClientException
   */
  PublicKey publicKeyLookupFromAlias(String alias)
          throws InvalidGuidException, ClientException, IOException {
    throw new RuntimeException("Unimplementable");
    // String guid = lookupGuid(alias);
    // return publicKeyLookupFromGuid(guid);
  }

  /**
   * Get the public key for a given GNSProtocol.GUID.toString().
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket publicKeyLookupFromGUID(String targetGUID)
          throws ClientException {
    // FIXME: This is not correctly implemented
    return lookupGUIDRecord(targetGUID);
  }

  /**
   * Register a new account GNSProtocol.GUID.toString() with the name {@code alias} and a password
   * {@code password}. Executing this query generates a new GNSProtocol.GUID.toString() and a public
 / private key pair. {@code password} can be used to retrieve account
 information if the client loses the private key corresponding to the
 account GNSProtocol.GUID.toString().
   *
   * @param gnsInstance
   *
   * @param alias
   * Human readable alias for the account GNSProtocol.GUID.toString() being created, e.g.,
 an email address
   * @param password
   * @return CommandPacket
   * @throws Exception
   */
  public static final CommandPacket accountGuidCreate(String gnsInstance,
          String alias, String password) throws Exception {
    GuidEntry entry = GuidUtils.lookupGuidEntryFromDatabase(gnsInstance, alias);
    /* arun: Don't recreate pair if one already exists. Otherwise you can
			 * not get out of the funk where the account creation timed out but
			 * wasn't rolled back fully at the server. Re-using
			 * the same GNSProtocol.GUID.toString() will at least pass verification as opposed to 
			 * incurring an GNSProtocol.ACTIVE_REPLICA_EXCEPTION.toString() for a new (non-existent) GNSProtocol.GUID.toString().
     */
    if (entry == null) {
      KeyPair keyPair = KeyPairGenerator.getInstance(GNSProtocol.RSA_ALGORITHM.toString())
              .generateKeyPair();
      String guid = SharedGuidUtils.createGuidStringFromPublicKey(keyPair
              .getPublic().getEncoded());
      // Squirrel this away now just in case the call below times out.
      KeyPairUtils.saveKeyPair(gnsInstance, alias, guid, keyPair);
      entry = new GuidEntry(alias, guid, keyPair.getPublic(),
              keyPair.getPrivate());
    }
    return accountGuidCreateHelper(alias, entry, password);
  }

  /**
   * Verify an account by sending the verification code back to the server.
   *
   * @param accountGUID
   * The account GNSProtocol.GUID.toString() to verify
   * @param code
   * The verification code
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket accountGuidVerify(GuidEntry accountGUID,
          String code) throws ClientException {
    return getCommand(CommandType.VerifyAccount, accountGUID, GNSProtocol.GUID.toString(),
            accountGUID.getGuid(), GNSProtocol.CODE.toString(), code);
  }

  /**
   * Deletes the account named {@code name}.
   *
   * @param accountGUID
   * GuidEntry for the account being removed.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket accountGuidRemove(GuidEntry accountGUID)
          throws ClientException {
    return getCommand(CommandType.RemoveAccount, accountGUID, GNSProtocol.GUID.toString(),
            accountGUID.getGuid(), GNSProtocol.NAME.toString(), accountGUID.getEntityName());
  }

  /**
   * Creates an new GNSProtocol.GUID.toString() associated with an account on the GNS server.
   *
   * @param gnsInstance
   * The name of the GNS service instance.
   *
   * @param accountGUID
   * The account GNSProtocol.GUID.toString() under which the GNSProtocol.GUID.toString() is being created.
   * @param alias
   * The alias assigned to the GNSProtocol.GUID.toString() being created.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket createGUID(String gnsInstance,
          GuidEntry accountGUID, String alias) throws ClientException {
    try {
      return guidCreateHelper(accountGUID, alias, GuidUtils
              .createAndSaveGuidEntry(alias, gnsInstance).getPublicKey());
    } catch (NoSuchAlgorithmException e) {
      throw new ClientException(e);
    }
  }

  /**
   * Creates a batch of GUIDs listed in {@code aliases} using gigapaxos' batch
   * creation mechanism.
   *
   * @param gnsInstance
   * The name of the GNS service instance.
   *
   * @param accountGUID
   * @param aliases
   * The batch of names being created.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket batchCreateGUIDs(String gnsInstance,
          GuidEntry accountGUID, Set<String> aliases) throws ClientException {

    List<String> aliasList = new ArrayList<>(aliases);
    List<String> publicKeys = null;
    publicKeys = new ArrayList<>();
    for (String alias : aliasList) {
      GuidEntry entry;
      try {
        entry = GuidUtils.createAndSaveGuidEntry(alias, gnsInstance);
      } catch (NoSuchAlgorithmException e) {
        // FIXME: Do we need to roll back created keys?
        throw new ClientException(e);
      }
      byte[] publicKeyBytes = entry.getPublicKey().getEncoded();
      String publicKeyString = Base64.encodeToString(publicKeyBytes,
              false);
      publicKeys.add(publicKeyString);
    }

    return getCommand(CommandType.AddMultipleGuids, accountGUID, GNSProtocol.GUID.toString(),
            accountGUID.getGuid(), GNSProtocol.NAMES.toString(), new JSONArray(aliasList),
            GNSProtocol.PUBLIC_KEYS.toString(), new JSONArray(publicKeys));
  }

  /**
   * Removes {@code targetGUID} that is not an account GNSProtocol.GUID.toString().
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being removed.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket removeGUID(GuidEntry targetGUID)
          throws ClientException {
    return getCommand(CommandType.RemoveGuidNoAccount, targetGUID, GNSProtocol.GUID.toString(),
            targetGUID.getGuid());
  }

  /**
   * Removes {@code targetGUID} given the associated {@code accountGUID}.
   *
   * @param accountGUID
   * @param targetGUID
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket removeGUID(GuidEntry accountGUID,
          String targetGUID) throws ClientException {
    return getCommand(CommandType.RemoveGuid, accountGUID, GNSProtocol.ACCOUNT_GUID.toString(),
            accountGUID.getGuid(), GNSProtocol.GUID.toString(), targetGUID);
  }

  // GROUP COMMANDS
  /**
   * Looks up the list of GUIDs that are members of {@code groupGUID}. The
   * result type of the execution result of this query is
   * {@link GNSCommand.ResultType#LIST}.
   *
   * @param groupGuid
   * The group GNSProtocol.GUID.toString() being queried.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing of the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupGetMembers(String groupGuid,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.GetGroupMembers, querierGUID, GNSProtocol.GUID.toString(),
            groupGuid, GNSProtocol.READER.toString(), querierGUID.getGuid());
  }

  /**
   * Looks up the list of groups of which {@code targetGUID} is a member.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() whose groups are being looked up.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return the list of groups as a JSONArray
   * @throws ClientException
   * if a protocol error occurs or the list cannot be parsed
   */
  public static final CommandPacket guidGetGroups(String targetGUID,
          GuidEntry querierGUID) throws ClientException {

    return getCommand(CommandType.GetGroups, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.READER.toString(), querierGUID.getGuid());
  }

  /**
   * Add a GNSProtocol.GUID.toString() to {@code groupGUID}. Any GNSProtocol.GUID.toString() can be a group GNSProtocol.GUID.toString().
   *
   * @param groupGUID
   * The GNSProtocol.GUID.toString() of the group.
   * @param toAddGUID
   * The GNSProtocol.GUID.toString() being added.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupAddGuid(String groupGUID,
          String toAddGUID, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.AddToGroup, querierGUID, GNSProtocol.GUID.toString(), groupGUID,
            GNSProtocol.MEMBER.toString(), toAddGUID, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Add multiple members to a group.
   *
   * @param groupGUID
   * The GNSProtocol.GUID.toString() of the group.
   * @param members
   * The member GUIDs to add to the group.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupAddGUIDs(String groupGUID,
          Set<String> members, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.AddMembersToGroup, querierGUID, GNSProtocol.GUID.toString(),
            groupGUID, GNSProtocol.MEMBERS.toString(), new JSONArray(members).toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Removes a GNSProtocol.GUID.toString() from a group GNSProtocol.GUID.toString(). Any GNSProtocol.GUID.toString() can be a group GNSProtocol.GUID.toString().
   *
   *
   * @param groupGUID
   * The group GNSProtocol.GUID.toString()
   * @param toRemoveGUID
   * The GNSProtocol.GUID.toString() being removed.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupRemoveGuid(String groupGUID,
          String toRemoveGUID, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.RemoveFromGroup, querierGUID, GNSProtocol.GUID.toString(),
            groupGUID, GNSProtocol.MEMBER.toString(), toRemoveGUID, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Remove a list of members from a group
   *
   * @param groupGUID
   * The group GNSProtocol.GUID.toString()
   * @param members
   * The GUIDs to be removed.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupRemoveGuids(String groupGUID,
          JSONArray members, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.RemoveMembersFromGroup, querierGUID,
            GNSProtocol.GUID.toString(), groupGUID, GNSProtocol.MEMBERS.toString(), members.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Authorize {@code toAuthorizeGUID} to add/remove members from the group
   * {@code groupGUID}. If {@code toAuthorizeGUID} is null, everyone is
   * authorized to add/remove members to the group. Note that this method can
   * only be called by the group owner (private key required).
   *
   * @param groupGUID
   * the group GNSProtocol.GUID.toString() entry
   * @param toAuthorizeGUID
   * the GNSProtocol.GUID.toString() being authorized to change group membership or null
 for anyone
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupAddMembershipUpdatePermission(
          GuidEntry groupGUID, String toAuthorizeGUID) throws ClientException {
    return aclAdd(AclAccessType.WRITE_WHITELIST, groupGUID, GNSProtocol.GROUP_ACL.toString(),
            toAuthorizeGUID);
  }

  /**
   * Unauthorize guidToUnauthorize to add/remove members from the group
   * groupGuid. If guidToUnauthorize is null, everyone is forbidden to
   * add/remove members to the group. Note that this method can only be called
   * by the group owner (private key required). Signs the query using the
   * private key of the group owner.
   *
   * @param groupGuid
   * the group GNSProtocol.GUID.toString() entry
   * @param guidToUnauthorize
   * the guid to authorize to manipulate group membership or null
   * for anyone
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupRemoveMembershipUpdatePermission(
          GuidEntry groupGuid, String guidToUnauthorize)
          throws ClientException {
    return aclRemove(AclAccessType.WRITE_WHITELIST, groupGuid, GNSProtocol.GROUP_ACL.toString(),
            guidToUnauthorize);
  }

  /**
   * Authorize {@code toAuthorizeGUID} to get the membership list from the
   * group {@code groupGUID}. If {@code toAuthorizeGUID} is null, everyone is
   * authorized to list members of the group. Note that this method can only
   * be called by the group owner (private key required).
   *
   * @param groupGUID
   * the group GNSProtocol.GUID.toString() entry
   * @param toAuthorizeGUID
   * the guid to authorize to manipulate group membership or null
   * for anyone
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupAddMembershipReadPermission(
          GuidEntry groupGUID, String toAuthorizeGUID) throws ClientException {
    return aclAdd(AclAccessType.READ_WHITELIST, groupGUID, GNSProtocol.GROUP_ACL.toString(),
            toAuthorizeGUID);
  }

  /**
   * Unauthorize {@code toUnauthorizeGUID} to get the membership list from the
   * group {@code groupGUID}. If {@code toUnauthorizeGUID} is null, everyone
   * is forbidden from querying the group membership. Note that this method
   * can only be called by the group owner (private key required).
   *
   * @param groupGUID
   * the group GNSProtocol.GUID.toString() entry
   * @param toUnauthorizeGUID
   * The GNSProtocol.GUID.toString() to unauthorize to change group membership or null for
 anyone.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket groupRemoveMembershipReadPermission(
          GuidEntry groupGUID, String toUnauthorizeGUID)
          throws ClientException {
    return aclRemove(AclAccessType.READ_WHITELIST, groupGUID, GNSProtocol.GROUP_ACL.toString(),
            toUnauthorizeGUID);
  }

  /* ************* ACL COMMANDS ********************* */
  /**
   * Adds {@code accessorGUID} to the access control list (ACL) of
   * {@code targetGUID}:{@code field}. {@code accessorGUID} can be a GNSProtocol.GUID.toString() of a
 user or a group GNSProtocol.GUID.toString() or null that means anyone can access the field. The
 field can be also be +ALL+ which means the {@code accessorGUID} is being
   * added to the ACLs of all fields of {@code targetGUID}.
   *
   * @param accessType
   * a value from GnrsProtocol.AclAccessType
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param accesserGUID
   * GNSProtocol.GUID.toString() to add to the ACL
   * @return CommandPacket
   * @throws ClientException
   * if the query is not accepted by the server.
   */
  public static final CommandPacket aclAdd(AclAccessType accessType,
          GuidEntry targetGUID, String field, String accesserGUID)
          throws ClientException {
    return aclAdd(accessType.name(), targetGUID, field, accesserGUID);
  }

  /**
   * Removes {@code accessorGUID} from the access control list (ACL) of
   * {@code targetGUID}:{@code field}. {@code accessorGUID} can be a GNSProtocol.GUID.toString() of a
 user or a group GNSProtocol.GUID.toString() or null that means anyone can access the field. The
 field can be also be +ALL+ which means the {@code accessorGUID} is being
   * added to the ACLs of all fields of {@code targetGUID}.
   *
   * @param accessType
   * @param targetGUID
   * @param field
   * The field key.
   * @param accesserGUID
   * The GNSProtocol.GUID.toString() to remove from the ACL.
   * @return CommandPacket
   * @throws ClientException
   * if the query is not accepted by the server.
   */
  public static final CommandPacket aclRemove(AclAccessType accessType,
          GuidEntry targetGUID, String field, String accesserGUID)
          throws ClientException {
    return aclRemove(accessType.name(), targetGUID, field, accesserGUID);
  }

  /**
   * Get the access control list of {@code targetGUID}:{@code field}.
   * {@code accesserGUID} can be a user or group GNSProtocol.GUID.toString() or null meaning that
 anyone can access the field. The field can be also be +ALL+ meaning that
 FIXME: TBD.
   *
   * @param accessType
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   * if the query is not accepted by the server.
   */
  public static final CommandPacket aclGet(AclAccessType accessType,
          GuidEntry targetGUID, String field, String querierGUID)
          throws ClientException {
    return aclGet(accessType.name(), targetGUID, field, querierGUID);
  }

  /* ********************* ALIASES ******************** */
  /**
   * Creates an alias for {@code targetGUID}. The alias can be used just like
 the original GNSProtocol.GUID.toString().
   *
   * @param targetGUID
   * @param name
   * - the alias
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket addAlias(GuidEntry targetGUID, String name)
          throws ClientException {
    return getCommand(CommandType.AddAlias, targetGUID, GNSProtocol.GUID.toString(),
            targetGUID.getGuid(), GNSProtocol.NAME.toString(), name);
  }

  /**
   * Removes the alias {@code name} for {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param name
   * The alias.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket removeAlias(GuidEntry targetGUID,
          String name) throws ClientException {
    return getCommand(CommandType.RemoveAlias, targetGUID, GNSProtocol.GUID.toString(),
            targetGUID.getGuid(), GNSProtocol.NAME.toString(), name);
  }

  /**
   * Retrieve the aliases associated with the given guid.
   *
   * @param guid
   * @return - a JSONArray containing the aliases
   * @throws Exception
   */
  public static final CommandPacket getAliases(GuidEntry guid)
          throws Exception {
    return getCommand(CommandType.RetrieveAliases, guid, GNSProtocol.GUID.toString(),
            guid.getGuid());
  }

  // ///////////////////////////////
  // // PRIVATE METHODS BELOW /////
  // /////////////////////////////
  /**
   * Creates a new GNSProtocol.GUID.toString() associated with an account.
   *
   * @param accountGuid
   * @param name
   * @param publicKey
   * @return a command packet
   * @throws Exception
   */
  private static final CommandPacket guidCreateHelper(GuidEntry accountGuid,
          String name, PublicKey publicKey) throws ClientException {
    byte[] publicKeyBytes = publicKey.getEncoded();
    String publicKeyString = Base64.encodeToString(publicKeyBytes, false);
    return getCommand(CommandType.AddGuid, accountGuid, GNSProtocol.GUID.toString(),
            accountGuid.getGuid(), GNSProtocol.NAME.toString(), name, GNSProtocol.PUBLIC_KEY.toString(), publicKeyString);
  }

  /**
   * Register a new account guid with the corresponding alias and the given
   * public key on the GNS server. Returns a new guid.
   *
   * @param alias
   * the alias to register (usually an email address)
   * @param guidEntry
   * @param password
   * the public key associate with the account
   * @return guid the GNSProtocol.GUID.toString() generated by the GNS
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws ClientException
   * @throws InvalidGuidException
   * if the user already exists
   * @throws NoSuchAlgorithmException
   */
  public static final CommandPacket accountGuidCreateHelper(String alias,
          GuidEntry guidEntry, String password)
          throws UnsupportedEncodingException, IOException, ClientException,
          InvalidGuidException, NoSuchAlgorithmException {
    return getCommand(CommandType.RegisterAccount,
            guidEntry, GNSProtocol.NAME.toString(), alias, GNSProtocol.PUBLIC_KEY.toString(), Base64.encodeToString(
                    guidEntry.publicKey.getEncoded(), false),
            GNSProtocol.PASSWORD.toString(),
            password != null ? Password.encryptAndEncodePassword(password, alias) : "");
  }

  private static final CommandPacket aclAdd(String accessType,
          GuidEntry guid, String field, String accesserGuid)
          throws ClientException {
    return getCommand(CommandType.AclAddSelf, guid, GNSProtocol.ACL_TYPE.toString(), accessType,
            GNSProtocol.GUID.toString(), guid.getGuid(), GNSProtocol.FIELD.toString(), field, GNSProtocol.ACCESSER.toString(),
            accesserGuid == null ? GNSProtocol.ALL_GUIDS.toString() : accesserGuid);
  }

  private static final CommandPacket aclRemove(String accessType,
          GuidEntry guid, String field, String accesserGuid)
          throws ClientException {
    return getCommand(CommandType.AclRemoveSelf, guid, GNSProtocol.ACL_TYPE.toString(),
            accessType, GNSProtocol.GUID.toString(), guid.getGuid(), GNSProtocol.FIELD.toString(), field, GNSProtocol.ACCESSER.toString(),
            accesserGuid == null ? GNSProtocol.ALL_GUIDS.toString() : accesserGuid);
  }

  private static final CommandPacket aclGet(String accessType,
          GuidEntry guid, String field, String readerGuid)
          throws ClientException {
    return getCommand(CommandType.AclRetrieve, guid, GNSProtocol.ACL_TYPE.toString(), accessType,
            GNSProtocol.GUID.toString(), guid.getGuid(), GNSProtocol.FIELD.toString(), field, GNSProtocol.READER.toString(),
            readerGuid == null ? GNSProtocol.ALL_GUIDS.toString() : readerGuid);
  }

  /* ******************* Extended commands ******************** */
  /**
   * Creates anew {@code targetGUID}:{@code field} with the value
   * {@code value}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param list
   * The list.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldCreateList(String targetGUID,
          String field, JSONArray list, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.CreateList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Appends the values in the list {@code value} to the list
   * {@code targetGUID}:{@code field} or creates anew {@code field} with the
   * values in the list {@code value} if it does not exist.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param list
   * The list value.
   * @param querierGUID
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendOrCreateList(
          String targetGUID, String field, JSONArray list,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.AppendOrCreateList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Replaces the values of the field with the list of values or creates a new
   * field with values in the list if it does not exist.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param list
   * The list value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplaceOrCreateList(
          String targetGUID, String field, JSONArray list,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.ReplaceOrCreateList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Appends {@code list} to the list {@code targetGUID}:{@code field}
   * creating the list if it does not exist.
   *
   * @param targetGUID
   * GNSProtocol.GUID.toString() where the field is stored
   * @param field
   * field name
   * @param list
   * list of values
   * @param querierGUID
   * GNSProtocol.GUID.toString() entry of the writer
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppend(String targetGUID,
          String field, JSONArray list, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.AppendListWithDuplication, querierGUID,
            GNSProtocol.GUID.toString(), targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Replaces the list {@code targetGUID}:{@code field} with {@code list}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key
   * @param list
   * The list value.
   * @param querierGUID
   * GNSProtocol.GUID.toString() entry of the writer
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplaceList(String targetGUID,
          String field, JSONArray list, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.ReplaceList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Removes all the values in {@code list} from {@code targetGUID}:
   * {@code field}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated)
   * @param field
   * The field key
   * @param list
   * The list value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldClear(String targetGUID,
          String field, JSONArray list, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.RemoveList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), list.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Removes all values from {@code targetGUID}:{@code field}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the update.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldClear(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.Clear, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Reads the list field {@code targetGUID}:{@code field}. The result type of
   * the execution result of this query is {@link GNSCommand.ResultType#LIST}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReadArray(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(querierGUID != null ? CommandType.ReadArray
            : CommandType.ReadArrayUnsigned, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.READER.toString(),
            querierGUID != null ? querierGUID.getGuid() : null);
  }

  /**
   * Sets the nth value (zero-based) indicated by {@code index} in the list
   * {@code targetGUID}:{@code field} to {@code newValue}. Index must be less
   * than the current size of the list.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param newValue
   * The new value.
   * @param index
   * The index of the array element being updated.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSetElement(String targetGUID,
          String field, String newValue, int index, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.Set, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), newValue, GNSProtocol.N.toString(), Integer.toString(index),
            GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Sets {@code targetGUID}:{@code field} to null. Subsequent reads of the
   * field will return a result type of null.
   *
   * @param targetGUID
   * @param field
   * @param querierGUID
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSetNull(String targetGUID,
          String field, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.SetFieldNull, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /* *********************** SELECT *********************** */
  /**
   * Selects all GNSProtocol.GUID.toString() records that match {@code query}. The result type of the
   * execution result of this query is {@link GNSCommand.ResultType#LIST}.
   *
   * The query syntax is described here:
   * https://gns.name/wiki/index.php?title=Query_Syntax
   *
   * There are some predefined field names such as
   * {@link GNSProtocol.LOCATION_FIELD_NAME.toString()} and
   * {@link GNSProtocol.IPADDRESS_FIELD_NAME.toString()} that are indexed by
   * default.
   *
   * There are links in the wiki page above to find the exact syntax for
   * querying spatial coordinates.
   *
   * @param query
   * The select query being issued.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket selectQuery(String query)
          throws ClientException {
    return getCommand(CommandType.SelectQuery, GNSProtocol.QUERY.toString(), query);
  }

  /**
   * Set up a context-aware group GNSProtocol.GUID.toString() corresponding to the query. Requires
   * {@code accountGuid} and {@code publicKey} that are used to set up the new
 GNSProtocol.GUID.toString() or look it up if it already exists. The result type of the execution
 result of this query is {@link GNSCommand.ResultType#LIST}.
   *
   * The query syntax is described here:
   * https://gns.name/wiki/index.php?title=Query_Syntax
   *
   * @param accountGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @param publicKey
   * @param query
   * The select query.
   * @param interval
   * The refresh interval in seconds (default 60).
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket selectSetupGroupQuery(
          GuidEntry accountGUID, String publicKey, String query, int interval)
          throws ClientException {
    return getCommand(CommandType.SelectGroupSetupQuery, GNSProtocol.GUID.toString(),
            accountGUID.getGuid(), GNSProtocol.PUBLIC_KEY.toString(), publicKey, GNSProtocol.QUERY.toString(), query,
            GNSProtocol.INTERVAL.toString(), interval);
  }

  /**
   * Looks up the membership of a context-aware group GNSProtocol.GUID.toString() created using a
 query. The results may be stale if the queries that happen more quickly
   * than the refresh interval given during setup. The result type of the
   * execution result of this query is {@link GNSCommand.ResultType#LIST}.
   *
   * @param groupGUID
   * The group GNSProtocol.GUID.toString() being queried.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket selectLookupGroupQuery(String groupGUID)
          throws ClientException {
    return getCommand(CommandType.SelectGroupLookupQuery, GNSProtocol.GUID.toString(), groupGUID);
  }

  /**
   * Searches for all GUIDs whose {@code field} has the value {@code value}.
   *
   * @param field
   * The field key.
   * @param value
   * The value that is being searched.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket select(String field, String value)
          throws ClientException {
    return getCommand(CommandType.Select, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value);
  }

  /**
   * If {@code field} is a GeoSpatial field, the query searches for all GUIDs
   * that have fields that are within the bounding box specified by
   * {@code value} as nested JSONArrays of paired tuples: [[LONG_UL,
   * LAT_UL],[LONG_BR, LAT_BR]]. The result type of the execution result of
   * this query is {@link GNSCommand.ResultType#LIST}.
   *
   * @param field
   * The field key.
   * @param value
   * - [[LONG_UL, LAT_UL],[LONG_BR, LAT_BR]]
   * @return CommandPacket
   * @throws Exception
   */
  public static final CommandPacket selectWithin(String field, JSONArray value)
          throws Exception {
    return getCommand(CommandType.SelectWithin, GNSProtocol.FIELD.toString(), field, GNSProtocol.WITHIN.toString(),
            value.toString());
  }

  /**
   * If {@code field} is a GeoSpatial field, the query searches for all GUIDs
   * whose {@code field} is near {@code value} that is a point specified as a
   * two element JSONArray: [LONG, LAT]. {@code maxDistance} is in meters.
   *
   * @param field
   * The field key
   * @param value
   * - [LONG, LAT]
   * @param maxDistance
   * - distance in meters
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket selectNear(String field, JSONArray value,
          Double maxDistance) throws ClientException {
    return getCommand(CommandType.SelectNear, GNSProtocol.FIELD.toString(), field, GNSProtocol.NEAR.toString(),
            value.toString(), GNSProtocol.MAX_DISTANCE.toString(), Double.toString(maxDistance));
  }

  /**
   * Update the location field for {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param longitude
   * the longitude
   * @param latitude
   * the latitude
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   * if a GNS error occurs
   */
  public static final CommandPacket setLocation(String targetGUID,
          double longitude, double latitude, GuidEntry querierGUID)
          throws ClientException {
    return fieldReplaceOrCreateList(targetGUID, GNSProtocol.LOCATION_FIELD_NAME.toString(),
            new JSONArray(Arrays.asList(longitude, latitude)), querierGUID);
  }

  /**
   * Update the location field for {@code targetGUID}.
   *
   * @param longitude
   * the GNSProtocol.GUID.toString() longitude
   * @param latitude
   * the GNSProtocol.GUID.toString() latitude
   * @param targetGUID
   * the GNSProtocol.GUID.toString() to update
   * @return CommandPacket
   * @throws ClientException
   * if a GNS error occurs
   */
  public static final CommandPacket setLocation(GuidEntry targetGUID,
          double longitude, double latitude) throws ClientException {
    return setLocation(targetGUID.getGuid(), longitude, latitude,
            targetGUID);
  }

  /**
   * Get the location of {@code targetGUID} as a JSONArray: [LONG, LAT]
   *
   * @param querierGUID
   * the GNSProtocol.GUID.toString() issuing the request
   * @param targetGUID
   * the GNSProtocol.GUID.toString() that we want to know the location
   * @return a JSONArray: [LONGITUDE, LATITUDE]
   * @throws ClientException
   * if a GNS error occurs
   */
  public static final CommandPacket getLocation(String targetGUID,
          GuidEntry querierGUID) throws ClientException {
    return fieldReadArray(targetGUID, GNSProtocol.LOCATION_FIELD_NAME.toString(), querierGUID);
  }

  /**
   * Get the location of {@code targetGUID} as a JSONArray: [LONG, LAT]
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @return a JSONArray: [LONGITUDE, LATITUDE]
   * @throws ClientException
   * if a GNS error occurs
   */
  public static final CommandPacket getLocation(GuidEntry targetGUID)
          throws ClientException {
    return fieldReadArray(targetGUID.getGuid(), GNSProtocol.LOCATION_FIELD_NAME.toString(),
            targetGUID);
  }

  /* ******************* Active Code ********************** */
  /**
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param action
   * The action corresponding to the active code.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   * @throws IOException
   */
  public static final CommandPacket activeCodeClear(String targetGUID,
          String action, GuidEntry querierGUID) throws ClientException,
          IOException {
    return getCommand(CommandType.ClearCode, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.AC_ACTION.toString(), action, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param action
   * The action triggering the active code.
   * @param code
   * The active code being installed.
   * @param querierGUID
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket activeCodeSet(String targetGUID,
          String action, byte[] code, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.SetCode, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.AC_ACTION.toString(), action, GNSProtocol.AC_CODE.toString(),
            Base64.encodeToString(code, true), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param action
   * The action triggering the active code.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket activeCodeGet(String targetGUID,
          String action, GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.GetCode, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.AC_ACTION.toString(), action, GNSProtocol.READER.toString(), querierGUID.getGuid());
  }

  /* ********************* More extended commands ********************** */
  /**
   * Creates a new field in {@code targetGUID} with the list {@code list}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param list
   * The list value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldCreateList(GuidEntry targetGUID,
          String field, JSONArray list) throws ClientException {
    return fieldCreateList(targetGUID.getGuid(), field, list, targetGUID);
  }

  /**
   * Creates a new one-element field {@code targetGUID}:{@code field} with the
   * string {@code value}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The single-element value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldCreateOneElementList(
          String targetGUID, String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.Create, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Same as
   * {@link #fieldCreateOneElementList(String, String, String, GuidEntry)}
   * with {@code querierGUID} same as {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldCreateOneElementList(
          GuidEntry targetGUID, String field, String value)
          throws ClientException {
    return fieldCreateOneElementList(targetGUID.getGuid(), field, value,
            targetGUID);
  }

  /**
   * Appends the single-element string {@code value} to {@code targetGUID}:
   * {@code field} or creates a new field with the single-value list if it
   * does not exist.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The single-element value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendOrCreate(String targetGUID,
          String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.AppendOrCreate, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value, GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Replaces the value(s) of {@code targetGUID}:{@code field} with the single
   * value {@code value} or creates a new field with a single-element list if
   * it does not exist.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The single-element value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplaceOrCreate(String targetGUID,
          String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.ReplaceOrCreate, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value, GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Replaces the value(s) of {@code targetGUID}:{@code field} with the list
   * {@code value} or creates a new field with the list {@code value} if it
   * does not exist.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param value
   * The list value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplaceOrCreateList(
          GuidEntry targetGUID, String field, JSONArray value)
          throws ClientException {
    return fieldReplaceOrCreateList(targetGUID.getGuid(), field, value,
            targetGUID);
  }

  /**
   * Replaces the value(s) of {@code targetGUID}:{@code field} with the
   * single-element string {@code value} or creates a new field with a single
   * value list if it does not exist.
   *
   * @param targetGUID
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplaceOrCreateList(
          GuidEntry targetGUID, String field, String value)
          throws ClientException {
    return fieldReplaceOrCreate(targetGUID.getGuid(), field, value,
            targetGUID);
  }

  /**
   * Replaces all the values of field with the single value. If the writer is
   * different use addToACL first to allow other the guid to write this field.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplace(String targetGUID,
          String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.Replace, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value, GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Replaces {@code targetGUID}:{@code field} with the single-element string
   * {@code value}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplace(GuidEntry targetGUID,
          String field, String value) throws ClientException {
    return fieldReplace(targetGUID.getGuid(), field, value, targetGUID);
  }

  /**
   * Replaces {@code targetGUID}:{@code field} with the list {@code value}.
   *
   * @param targetGUID
   * @param field
   * The field key.
   * @param value
   * The list value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldReplace(GuidEntry targetGUID,
          String field, JSONArray value) throws ClientException {
    return fieldReplaceList(targetGUID.getGuid(), field, value, targetGUID);
  }

  /**
   * Appends a single-element string {@code value} to {@code targetGUID}:
   * {@code field}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppend(String targetGUID,
          String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.AppendWithDuplication, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value, GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Appends a single-element string {@code value} to {@code targetGUID}:
   * {@code field}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppend(GuidEntry targetGUID,
          String field, String value) throws ClientException {
    return fieldAppend(targetGUID.getGuid(), field, value, targetGUID);
  }

  /**
   * Appends a list {@code value} to {@code targetGUID}{:{@code field} but
   * after first converting the list to a set (removing duplicates).
   *
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The list value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendWithSetSemantics(
          String targetGUID, String field, JSONArray value,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.AppendList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Appends a list {@code value} onto a {@code targetGUID}:{@code field} but
   * first converts the list to a set (removing duplicates).
   *
   * @param targetGUID
   * @param field
   * @param value
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendWithSetSemantics(
          GuidEntry targetGUID, String field, JSONArray value)
          throws ClientException {
    return fieldAppendWithSetSemantics(targetGUID.getGuid(), field, value,
            targetGUID);
  }

  /**
   * Appends a single-element string {@code value} to {@code targetGUID}:
   * {@code field} but first converts the list to set (removing duplicates).
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param value
   * The single-element string value.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendWithSetSemantics(
          String targetGUID, String field, String value, GuidEntry querierGUID)
          throws ClientException {
    return getCommand(CommandType.Append, querierGUID, GNSProtocol.GUID.toString(), targetGUID,
            GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), value.toString(), GNSProtocol.WRITER.toString(),
            querierGUID.getGuid());
  }

  /**
   * Appends a single-element string value to {@code targetGUID}:{@code field}
   * but first converts the list to a set (removing duplicates).
   *
   * @param targetGUID
   * @param field
   * @param value
   * @return CommandPacket
   * @throws IOException
   * @throws ClientException
   */
  public static final CommandPacket fieldAppendWithSetSemantics(
          GuidEntry targetGUID, String field, String value)
          throws IOException, ClientException {
    return fieldAppendWithSetSemantics(targetGUID.getGuid(), field, value,
            targetGUID);
  }

  /**
   * Substitutes {@code targetGUID}:{@code field}'s value with
   * {@code newValue} if the current value is {@code oldValue}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param newValue
   * The new value.
   * @param oldValue
   * The value being substituted.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSubstitute(String targetGUID,
          String field, String newValue, String oldValue,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.Substitute, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), newValue, GNSProtocol.OLD_VALUE.toString(), oldValue,
            GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Same as {@link #fieldSubstitute(GuidEntry, String, String, String)} with
   * {@code querierGUID} same as {@code targetGUID}.
   *
   * @param targetGUID
   * @param field
   * @param newValue
   * @param oldValue
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSubstitute(GuidEntry targetGUID,
          String field, String newValue, String oldValue)
          throws ClientException {
    return fieldSubstitute(targetGUID.getGuid(), field, newValue, oldValue,
            targetGUID);
  }

  /**
   * Pairwise-substitutes all the values in {@code oldValues} with
   * {@code newValues} in the list {@code targetGUID}:{@code field}.
   *
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried (updated).
   * @param field
   * The field key.
   * @param newValue
   * The list of new values.
   * @param oldValue
   * The list of old values being substituted.
   * @param querierGUID
   * The GNSProtocol.GUID.toString() issuing the query.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSubstitute(String targetGUID,
          String field, JSONArray newValue, JSONArray oldValue,
          GuidEntry querierGUID) throws ClientException {
    return getCommand(CommandType.SubstituteList, querierGUID, GNSProtocol.GUID.toString(),
            targetGUID, GNSProtocol.FIELD.toString(), field, GNSProtocol.VALUE.toString(), newValue.toString(),
            GNSProtocol.OLD_VALUE.toString(), oldValue.toString(), GNSProtocol.WRITER.toString(), querierGUID.getGuid());
  }

  /**
   * Same as
   * {@link #fieldSubstitute(String, String, JSONArray, JSONArray, GuidEntry)}
   * with {@code querierGUID} same as {@code targetGUID}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @param newValue
   * The list of new values.
   * @param oldValue
   * The list of old values.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldSubstitute(GuidEntry targetGUID,
          String field, JSONArray newValue, JSONArray oldValue)
          throws ClientException {
    return fieldSubstitute(targetGUID.getGuid(), field, newValue, oldValue,
            targetGUID);
  }

  /**
   * Removes the field {@code targetGUID}:{@code field}.
   *
   * @param targetGUID
   * The GNSProtocol.GUID.toString() being queried.
   * @param field
   * The field key.
   * @return CommandPacket
   * @throws ClientException
   */
  public static final CommandPacket fieldRemove(GuidEntry targetGUID,
          String field) throws ClientException {
    return fieldRemove(targetGUID.getGuid(), field, targetGUID);
  }

//  /**
//   * @return CommandPacket
//   * @throws ClientException
//   */
//  public static final CommandPacket adminEnable()
//          throws ClientException {
//    return getCommand(CommandType.Admin);
//  }

  /**
   * @return The {@link GNSCommand.ResultType} type of the result obtained by
   * executing this query.
   */
  public CommandResultType getResultType() {
    return this.getCommandType().getResultType();
  }
}
