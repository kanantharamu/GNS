package edu.umass.cs.gns.nameserver.recordmap;

import edu.umass.cs.gns.database.CassandraRecords;
import edu.umass.cs.gns.main.GNS;
import edu.umass.cs.gns.main.StartNameServer;
import edu.umass.cs.gns.nameserver.NameRecord;
import edu.umass.cs.gns.nameserver.NameServer;
import edu.umass.cs.gns.nameserver.fields.Field;
import edu.umass.cs.gns.nameserver.recordExceptions.FieldNotFoundException;
import edu.umass.cs.gns.nameserver.recordExceptions.RecordNotFoundException;
import edu.umass.cs.gns.nameserver.replicacontroller.ReplicaControllerRecord;
import edu.umass.cs.gns.util.ConfigFileInfo;
import edu.umass.cs.gns.util.HashFunction;
import edu.umass.cs.gns.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class CassandraRecordMap extends BasicRecordMap {

  private String collectionName;

  public CassandraRecordMap(String collectionName) {
    this.collectionName = collectionName;
  }

  @Override
  public String getNameRecordField(String name, String key) {
    CassandraRecords records = CassandraRecords.getInstance();
    String result = records.lookup(collectionName, name, key);
    if (result != null) {
      GNS.getLogger().finer(records.toString() + ":: Retrieved " + name + "/" + key + ": " + result);
      return result;
    } else {
      GNS.getLogger().finer(records.toString() + ":: No record named " + name + " with key " + key);
      return null;
    }
  }

  @Override
  public ArrayList<String> getNameRecordFields(String name, ArrayList<String> keys) {
    CassandraRecords records = CassandraRecords.getInstance();
    ArrayList<String> result = records.lookup(collectionName, name, keys);
    if (result != null) {
      GNS.getLogger().finer(records.toString() + ":: Retrieved " + name + "/" + keys + ": " + result);
      return result;
    } else {
      GNS.getLogger().finer(records.toString() + ":: No record named " + name + " with key " + keys);
      return null;
    }
  }

  @Override
  public void updateNameRecordListValue(String name, String key, ArrayList<String> value) {
    CassandraRecords records = CassandraRecords.getInstance();
    GNS.getLogger().finer(records.toString() + ":: Writing list " + name + "/" + key + ": " + value.toString());
    records.updateField(collectionName, name, key, value);
  }

  @Override
  public void updateNameRecordListValueInt(String name, String key, Set<Integer> value) {
    CassandraRecords records = CassandraRecords.getInstance();
    GNS.getLogger().finer(records.toString() + ":: Writing int list " + name + "/" + key + ": " + value.toString());
    records.updateField(collectionName, name, key, value);
  }

  @Override
  public void updateNameRecordFieldAsString(String name, String key, String string) {
    CassandraRecords records = CassandraRecords.getInstance();
    GNS.getLogger().finer(records.toString() + ":: Writing string " + name + "/" + key + ": " + string);
    records.updateField(collectionName, name, key, string);
  }
  
  @Override
  public void updateNameRecordFieldAsMap(String name, String key, Map map) {
    CassandraRecords records = CassandraRecords.getInstance();
    GNS.getLogger().finer(records.toString() + ":: Writing map " + name + "/" + key + ": " + map);
    records.updateField(collectionName, name, key, map);
  }
  
  @Override
  public void updateNameRecordFieldAsCollection(String name, String key, Collection collection) {
    CassandraRecords records = CassandraRecords.getInstance();
    GNS.getLogger().finer(records.toString() + ":: Writing collection " + name + "/" + key + ": " + collection);
    records.updateField(collectionName, name, key, collection);
  }

  @Override
  public Set<String> getAllRowKeys() {
    CassandraRecords records = CassandraRecords.getInstance();
    return records.keySet(collectionName);
  }

  @Override
  public Set<String> getAllColumnKeys(String name) {
    if (!containsName(name)) {
      try {
        CassandraRecords records = CassandraRecords.getInstance();
        JSONObject json = records.lookup(collectionName, name);
        return JSONUtils.JSONArrayToSetString(json.names());
      } catch (JSONException e) {
        GNS.getLogger().severe("Error updating json record: " + e);
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public NameRecord getNameRecordLazy(String name) {
    if (CassandraRecords.getInstance().contains(collectionName, name)) {
      //GNS.getLogger().info("Creating lazy name record for " + name);
      return new NameRecord(name);
//      return new NameRecord(name, this);
    } else {
      return null;
    }
  }

  @Override
  public NameRecord getNameRecordLazy(String name, ArrayList<String> keys) {
    throw new UnsupportedOperationException("Not supported yet.");
//    ArrayList<String> values = CassandraRecords.getInstance().lookup(collectionName,name,keys);
//    if (values == null) return null;
//    return new NameRecord(name, this, keys, values);

  }

  @Override
  public HashMap<Field, Object> lookup(String name, Field nameField, ArrayList<Field> fields1) throws RecordNotFoundException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public HashMap<Field, Object> lookup(String name, Field nameField, ArrayList<Field> fields1,
                           Field valuesMapField, ArrayList<Field> valuesMapKeys) throws RecordNotFoundException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void update(String name, Field nameField, ArrayList<Field> fields1, ArrayList<Object> values1) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void update(String name, Field nameField, ArrayList<Field> fields1, ArrayList<Object> values1,
                     Field valuesMapField, ArrayList<Field> valuesMapKeys, ArrayList<Object> valuesMapValues) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void increment(String name, ArrayList<Field> fields1, ArrayList<Object> values1) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void increment(String name, ArrayList<Field> fields1, ArrayList<Object> values1, Field votesMapField, ArrayList<Field> votesMapKeys, ArrayList<Object> votesMapValues) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getIterator(Field nameField, ArrayList<Field> fields) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public HashMap<Field, Object> next(Object iterator, Field nameField, ArrayList<Field> fields) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Object getIterator(Field nameField) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public JSONObject next(Object iterator, Field nameField) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void returnIterator() {
    return;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public NameRecord getNameRecord(String name) {
    try {
      JSONObject json = CassandraRecords.getInstance().lookup(collectionName, name);
      if (json == null) {
        return null;
      } else {
        return new NameRecord(json);
      }
    } catch (JSONException e) {
      GNS.getLogger().severe("Error getting name record " + name + ": " + e);
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void addNameRecord(NameRecord recordEntry) {
    if (StartNameServer.debugMode) {
      try {
        GNS.getLogger().fine("Start addNameRecord " + recordEntry.getName());
      } catch (FieldNotFoundException e) {
        GNS.getLogger().severe("Field not found exception. " + e.getMessage());
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        return;
      }
    }
    try {
      addNameRecord(recordEntry.toJSONObject());
      //CassandraRecords.getInstance().insert(collectionName, recordEntry.getName(), recordEntry.toJSONObject());
    } catch (JSONException e) {
      e.printStackTrace();
      GNS.getLogger().severe("Error adding name record: " + e);
      return;
    }
  }

  @Override
  public void addNameRecord(JSONObject json) {
    CassandraRecords records = CassandraRecords.getInstance();
    try {
      String name = json.getString(NameRecord.NAME.getFieldName());
      records.insert(collectionName, name, json);
      GNS.getLogger().finer(records.toString() + ":: Added " + name);
    } catch (JSONException e) {
      GNS.getLogger().severe(records.toString() + ":: Error adding name record: " + e);
      e.printStackTrace();
    }
  }

  @Override
  public void updateNameRecord(NameRecord recordEntry) {
    try {
      CassandraRecords.getInstance().update(collectionName, recordEntry.getName(), recordEntry.toJSONObject());
    } catch (JSONException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (FieldNotFoundException e) {
      GNS.getLogger().severe("Field found found exception: " + e.getMessage());
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  @Override
  public void removeNameRecord(String name) {
    CassandraRecords.getInstance().remove(collectionName, name);
  }

  @Override
  public boolean containsName(String name) {
    return CassandraRecords.getInstance().contains(collectionName, name);
  }

  @Override
  public Set<NameRecord> getAllNameRecords() {
    //CassandraRecords.getInstance().keySet(collectionName);
    CassandraRecords records = CassandraRecords.getInstance();
    Set<NameRecord> result = new HashSet<NameRecord>();
    for (JSONObject json : records.retrieveAllEntries(collectionName)) {
      try {
        result.add(new NameRecord(json));
      } catch (JSONException e) {
        GNS.getLogger().severe(records.toString() + ":: Error getting name record: " + e);
        e.printStackTrace();
      }
    }
    
    return result;
  }

  @Override
  public void reset() {
    CassandraRecords.getInstance().reset(collectionName);
  }

  @Override
  public ReplicaControllerRecord getNameRecordPrimary(String name) {
    try {
      JSONObject json = CassandraRecords.getInstance().lookup(collectionName, name);
      if (json == null) {
        return null;
      } else {
        return new ReplicaControllerRecord(json);
      }
    } catch (JSONException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }
  
  @Override
  public ReplicaControllerRecord getNameRecordPrimaryLazy(String name) {
    if (CassandraRecords.getInstance().contains(collectionName, name)) {
      //GNS.getLogger().info("Creating lazy name record for " + name);
      return new ReplicaControllerRecord(name);
    } else {
      return null;
    }
  }

  @Override
  public void addNameRecordPrimary(ReplicaControllerRecord recordEntry) {
//    if (StartNameServer.debugMode) {
//      GNS.getLogger().fine("Start addNameRecord " + recordEntry.getName());
//    }

    try {
      CassandraRecords.getInstance().insert(collectionName, recordEntry.getName(), recordEntry.toJSONObject());
    } catch (JSONException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      return;
    } catch (FieldNotFoundException e) {
      GNS.getLogger().severe("Field not found " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void updateNameRecordPrimary(ReplicaControllerRecord recordEntry) {
    try {
      CassandraRecords.getInstance().update(collectionName, recordEntry.getName(), recordEntry.toJSONObject());
    } catch (JSONException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (FieldNotFoundException e) {
      GNS.getLogger().severe("Field not found " + e.getMessage());
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  @Override
  public Set<ReplicaControllerRecord> getAllPrimaryNameRecords() {
    //CassandraRecords.getInstance().keySet(collectionName);
    CassandraRecords records = CassandraRecords.getInstance();
    Set<ReplicaControllerRecord> result = new HashSet<ReplicaControllerRecord>();
    for (JSONObject json : records.retrieveAllEntries(collectionName)) {
      try {
        result.add(new ReplicaControllerRecord(json));
      } catch (JSONException e) {
        GNS.getLogger().severe(records.toString() + ":: Error getting name record: " + e);
        e.printStackTrace();
      }
    }
    return result;
//        return MongoRecordMap.g;
  }

  // test code
  public static void main(String[] args) throws Exception {
    NameServer.nodeID = 4;
    retrieveFieldTest();
    //System.exit(0);
  }

  private static void retrieveFieldTest() throws Exception {
    ConfigFileInfo.readHostInfo("ns1", NameServer.nodeID);
    HashFunction.initializeHashFunction();
    BasicRecordMap recordMap = new CassandraRecordMap(CassandraRecords.DBNAMERECORD);
    System.out.println(recordMap.getNameRecordFieldAsIntegerSet("1A434C0DAA0B17E48ABD4B59C632CF13501C7D24", NameRecord.PRIMARY_NAMESERVERS.getFieldName()));
    recordMap.updateNameRecordFieldAsIntegerSet("1A434C0DAA0B17E48ABD4B59C632CF13501C7D24", "FRED", new HashSet<Integer>(Arrays.asList(1, 2, 3)));
    System.out.println(recordMap.getNameRecordFieldAsIntegerSet("1A434C0DAA0B17E48ABD4B59C632CF13501C7D24", "FRED"));
  }
}