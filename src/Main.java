import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static Database db;

    public static void main(String[] args) throws IOException, InvalidTypeException, InvalidPageException, InvalidRecordException {
        db = new Database("test");
        populateRecords();
//        populateTypes();
//        deleteRecords();
//        listAllRecords();
//        listAllTypes();
//        searchRecord();
    }

    private static void searchRecord() {
        Record r = db.getRecord("student", "2005");
        System.out.println(r.getValueOf("name"));
    }

    private static void listAllTypes() {
        for (Type type : db.getTypes()) {
            System.out.println(type.getName());
        }
    }

    private static void listAllRecords() {
        for (Record record : db.getRecordsByType("student")) {
            System.out.println(record.getKeyValue());
        }
    }

    private static void deleteRecords() throws IOException {
        db.deleteRecord("student", "2011404105");
        db.deleteRecord("student", "2011404107");
        db.deleteRecord("student", "2011404110");
    }

    private static void populateRecords() throws IOException, InvalidRecordException {
        for (int id = 2000; id < 2100; ++id) {
            Record rec = db.getRecord("student", String.valueOf(id));
//            if (rec != null) continue;

            HashMap<String, String> values = new HashMap<>();
            values.put("name", "Longenough");
            values.put("id", String.valueOf(id));
            db.createRecord("student", values);
        }
    }

    private static void populateTypes() throws IOException, InvalidTypeException, InvalidPageException {
        ArrayList<String> fields = new ArrayList<>();
        fields.add("id");
        fields.add("name");
        db.createType("student", fields, "id");

        fields.add("dept");
        db.createType("student", fields, "id");
    }
}
