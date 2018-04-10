import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private Catalog catalog;
    private String name;

    public Database(String name) throws IOException, InvalidTypeException, InvalidPageException {
        this.name = name;
        this.catalog = new Catalog(name);
    }

    public ArrayList<Type> getTypes() {
        return catalog.getTypes();
    }

    public Type getType(String typeName) {
        return catalog.getType(typeName);
    }

    public void createType(String typeName, ArrayList<String> fields, String keyField) throws IOException, InvalidTypeException, InvalidPageException {
        catalog.createType(typeName, fields, keyField);
    }

    public void deleteType(String typeName) throws IOException {
        catalog.deleteType(typeName);
    }

    public void createRecord(String typeName, HashMap<String, String> fieldValues) throws IOException, InvalidRecordException {
        Type type = catalog.getType(typeName);
        type.createRecord(fieldValues);
    }

    public void deleteRecord(String typeName, String key) throws IOException {
        Type type = catalog.getType(typeName);
        type.deleteRecord(key);
    }

    public Record getRecord(String typeName, String key) {
        Type type = catalog.getType(typeName);
        return type.getRecord(key);
    }

    public ArrayList<Record> getRecordsByType(String typeName) {
        Type type = catalog.getType(typeName);
        return type.getRecords();
    }
}
