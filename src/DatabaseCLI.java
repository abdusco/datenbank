import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DatabaseCLI {

    private final int COMMAND_EXIT = 0;
    private final int COMMAND_CREATE_TYPE = 1;
    private final int COMMAND_DELETE_TYPE = 2;
    private final int COMMAND_LIST_TYPES = 3;
    private final int COMMAND_CREATE_RECORD = 4;
    private final int COMMAND_DELETE_RECORD = 5;
    private final int COMMAND_FIND_RECORD = 6;
    private final int COMMAND_LIST_RECORDS = 7;
    private final int MAX_COMMAND = 7;

    private Database db;
    private Scanner scanner;

    public DatabaseCLI(Database db) {
        this.db = db;
        scanner = new Scanner(System.in).useDelimiter("\\n");
    }

    public void listen() throws InvalidPageException, IOException, InvalidTypeException, InvalidRecordException {
        displayBanner();
        while (true) {
            int command = getCommandFromUser();
            if (command == COMMAND_EXIT) break;
            if (command > MAX_COMMAND) {
                System.out.println("Invalid command");
                continue;
            }

            runCommand(command);
            System.out.println("Done.\n\n");
        }
    }

    private void displayBanner() {
        System.out.println("Datenbank v1.0");
        System.out.println("==============");
    }

    private int getCommandFromUser() {
        System.out.println("PICK AN ACTION:");
        System.out.println(String.format("\t%d - Exit", COMMAND_EXIT));
        System.out.println("# DDL Operations");
        System.out.println(String.format("\t%d - Create a type", COMMAND_CREATE_TYPE));
        System.out.println(String.format("\t%d - Delete a type", COMMAND_DELETE_TYPE));
        System.out.println(String.format("\t%d - List all types", COMMAND_LIST_TYPES));
        System.out.println("# DML Operations");
        System.out.println(String.format("\t%d - Create a record", COMMAND_CREATE_RECORD));
        System.out.println(String.format("\t%d - Delete a record", COMMAND_DELETE_RECORD));
        System.out.println(String.format("\t%d - Find a record", COMMAND_FIND_RECORD));
        System.out.println(String.format("\t%d - List all records", COMMAND_LIST_RECORDS));

        return scanner.nextInt();
    }

    private void runCommand(int command) throws InvalidTypeException, IOException, InvalidPageException, InvalidRecordException {
        switch (command) {
            case COMMAND_CREATE_TYPE:
                createType();
                break;
            case COMMAND_DELETE_TYPE:
                deleteType();
                break;
            case COMMAND_LIST_TYPES:
                listTypes();
                break;
            case COMMAND_CREATE_RECORD:
                createRecord();
                break;
            case COMMAND_DELETE_RECORD:
                deleteRecord();
                break;
            case COMMAND_FIND_RECORD:
                findRecord();
                break;
            case COMMAND_LIST_RECORDS:
                listRecords();
                break;
        }
    }

    private void createType() throws InvalidPageException, IOException, InvalidTypeException {
        System.out.println("Type in a name for the type");
        String name = scanner.next();

        System.out.println("Type in comma separated field names (max 8 fields, field names at most 8 chars)");
        System.out.println("Example: 'id,name,make' (without quotes)");
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(scanner.next().split("\\s*,\\s*")));

        System.out.println("Type in the key field:");
        String keyField = scanner.next();

        if (!fields.contains(keyField)) {
            System.out.println("Invalid key field");
            createType();
            return;
        }

        db.createType(name, fields, keyField);
    }

    private Type getType() {
        System.out.println("Type in the type name");
        String typeName = scanner.next();
        Type type = db.getType(typeName);
        if (type == null) {
            System.out.println("No such type exists");
            return getType();
        }
        return type;
    }

    private void deleteType() throws IOException {
        displayAvailableTypes();
        String name = getType().getName();
        db.deleteType(name);
    }

    private void displayAvailableTypes() {
        System.out.println("Available types:");
        for (Type type : db.getTypes()) {
            System.out.println(String.format("\t%s", type.getName()));
        }
    }

    private void listTypes() {
        for (Type type : db.getTypes()) {
            String fields = type.getFields().stream()
                    .collect(Collectors.joining(", "));

            System.out.println(type.getName());
            System.out.println(String.format("\tFields: %s", fields));
            System.out.println(String.format("\tKey: %s", type.getKeyField()));
            System.out.println(String.format("\tRecords: %d", type.getRecords().size()));
        }
    }

    private void createRecord() throws IOException, InvalidRecordException {
        displayAvailableTypes();

        Type type = getType();

        System.out.println("Type in the field values");
        HashMap<String, String> values = new HashMap<>();
        for (String fieldName : type.getFields()) {
            System.out.println(String.format("%s:", fieldName));
            String value = scanner.next();
            values.put(fieldName, value);
        }
        db.createRecord(type.getName(), values);
    }

    private void deleteRecord() throws IOException {
        displayAvailableTypes();
        Type type = getType();

        System.out.println(String.format("Type in a '%s' value of for the record of type '%s'", type.getKeyField(), type.getName()));
        String key = scanner.next();
        db.deleteRecord(type.getName(), key);
    }

    private void findRecord() {
        displayAvailableTypes();
        Type type = getType();

        System.out.println(String.format("Type in a '%s' value of for the record of type '%s'", type.getKeyField(), type.getName()));
        String key = scanner.next();
        Record record = db.getRecord(type.getName(), key);
        if (record == null) {
            System.out.println(String.format("Cannot find a '%s' record with key '%s'", type.getName(), key));
            return;
        }
        System.out.println("Found:");
        printRecord(type, record);
    }

    private void listRecords() {
        displayAvailableTypes();
        Type type = getType();
        System.out.println(String.format("Listing records for type '%s'", type.getName()));
        for (Record record : type.getRecords()) {
            printRecord(type, record);
        }
    }

    private void printRecord(Type type, Record record) {
        System.out.println(String.format("Key(%s): %s", type.getKeyField(), record.getKeyValue()));
        for (String fieldName : type.getFields()) {
            System.out.println(String.format("\t%s: %s", fieldName, record.getValueOf(fieldName)));
        }
    }
}
