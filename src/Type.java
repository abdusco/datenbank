import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Type {
    private static final int MAX_NAME_LENGTH = 10;
    private static final int MAX_FIELD_NAME_LENGTH = 8;
    private final int MAX_FIELD_COUNT = 8;
    private String name;
    private ArrayList<String> fields;
    private String keyField;
    private ArrayList<Page> pages;

    Type(String name, ArrayList<String> fields, String keyField) throws InvalidTypeException {
        if (hasNoFields(fields)) {
            throw new InvalidTypeException("A type must have at least 1 field");
        }

        if (hasTooManyFields(fields)) {
            throw new InvalidTypeException(String.format("Cannot have more than %d fields", MAX_FIELD_COUNT));
        }

        if (isFieldNamesNotEncodedProperly(fields)) {
            throw new InvalidTypeException("Field names must be alphanumeric");
        }

        if (isFieldNamesTooLong(fields)) {
            throw new InvalidTypeException(String.format("Field names cannot be longer than %d chars", MAX_FIELD_NAME_LENGTH));
        }

        if (isKeyFieldNotValid(fields, keyField)) {
            throw new InvalidTypeException("Key field is not in fields list");
        }

        this.name = name;
        this.fields = fields;
        this.keyField = keyField;
        pages = new ArrayList<>();
    }


    private boolean hasNoFields(ArrayList<String> fields) {
        return fields.size() == 0;
    }

    private boolean hasTooManyFields(ArrayList<String> fields) {
        return fields.size() > MAX_FIELD_COUNT;
    }

    private boolean isFieldNamesNotEncodedProperly(ArrayList<String> fields) {
        for (String field : fields) {
            if (!field.matches("\\w+")) return true;
        }
        return false;
    }

    private boolean isFieldNamesTooLong(ArrayList<String> fields) {
        for (String fieldName : fields) {
            if (fieldName.length() > MAX_FIELD_NAME_LENGTH) return true;
        }
        return false;
    }

    private boolean isKeyFieldNotValid(ArrayList<String> fields, String keyField) {
        return !fields.contains(keyField);
    }

    static Type createFromSerialized(String serialized) throws InvalidTypeException, IOException, InvalidPageException {
        String regex = "^(\\w+)\\s*" // name + right pad
                + "!" // separator
                + "(\\d+):" // field count
                + "(\\w+)\\s*:" // key field + right pad
                + "((\\w+\\s*,?)+)$"; // comma separated field names with padding

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(serialized);
        if (!matcher.matches()) {
            throw new InvalidTypeException("Type does not have proper encoding");
        }

        String name = matcher.group(1).trim();
        int fieldCount = Integer.valueOf(matcher.group(2));
        String keyField = matcher.group(3).trim();
        String[] fields = matcher.group(4).trim()
                .split("\\s*,\\s*", fieldCount);
        ArrayList<String> fieldList = new ArrayList<>(Arrays.asList(fields));

        Type type = new Type(name, fieldList, keyField);
        type.getAllPages();
        return type;
    }

    void createRecord(HashMap<String, String> values) throws IOException, InvalidRecordException {
        Record record = getRecord(values.get(keyField));
        if (record != null) {
            Logger.log(String.format("Record with key '%s' already exists.", values.get(keyField)));
            return;
        }

        record = new Record(this, values);
        Page page = getEmptyOrNewPage();
        page.addRecord(record);
        addOrUpdatePage(page);
        savePages();
        Logger.log(String.format("No existing record with key '%s'", values.get(getKeyField())));
        Logger.log(String.format("Inserting at Page #%d", page.getId()));
        Logger.log(String.format("A '%s' record has been created", name));
        // TODO: update catalog with page count
    }

    private void addOrUpdatePage(Page page) {
        for (int i = 0; i < pages.size(); i++) {
            Page p = pages.get(i);
            if (p.getId() == page.getId()) {
                pages.set(i, page);
                return;
            }
        }
        pages.add(page);
    }

    private Page getEmptyOrNewPage() {
        for (Page page : pages) {
            if (page.hasSpace()) return page;
        }

        return new Page(getNewPageId());
    }

    private int getNewPageId() {
        int id = 0;
        for (Page page : pages) {
            if (page.getId() > id) id = page.getId();
        }
        return ++id;
    }

    ArrayList<Record> getRecords() {
        ArrayList<Record> records = new ArrayList<>();
        for (Page page : pages) {
            records.addAll(page.getRecords());
        }
        return records;
    }

    Record getRecord(String key) {
        for (Page page : pages) {
            Logger.log(String.format("Reading Page #%d", page.getId()));
            Record record = page.getRecord(key);
            if (record != null) return record;
        }
        return null;
    }

    void deleteRecord(String key) throws IOException {
        for (Page page : pages) {
            Logger.log(String.format("Reading Page #%d", page.getId()));
            Record record = page.getRecord(key);
            if (record == null) continue;

            page.deleteRecord(record);
            savePages();
            Logger.log(String.format("A '%s' record with key '%s' has been deleted", name, key));
            return;
        }
    }

    private void savePages() throws IOException {
        emptyTypeFile();
        writePagesToTypeFile();
    }

    private void writePagesToTypeFile() throws IOException {
        FileWriter fw = new FileWriter(getFilename(), true);
        for (Page page : pages) {
            if (page.isEmpty()) continue;
            fw.write(page.serialize() + System.lineSeparator());
        }
        fw.close();
    }

    private void emptyTypeFile() throws FileNotFoundException {
        new PrintWriter(getFilename()).close();
    }

    private ArrayList<Page> getAllPages() throws IOException, InvalidPageException {
        pages = new ArrayList<>();
        FileInputStream stream = new FileInputStream(getFilename());
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String encodedPage;

        while ((encodedPage = br.readLine()) != null) {
            pages.add(Page.createFromSerialized(this, encodedPage));
        }

        return pages;
    }

    String serialize() {
        return String.format("%s!%d:%s:%s",
                StringPadder.padRight(name, MAX_NAME_LENGTH),
                fields.size(),
                StringPadder.padRight(keyField, MAX_FIELD_NAME_LENGTH),
                getSerializedFields()
        );
    }

    private String getSerializedFields() {
        return fields.stream()
                .map(f -> StringPadder.padRight(f, MAX_FIELD_NAME_LENGTH))
                .collect(Collectors.joining(","));
    }

    String getFilename() {
        return String.format("%s.type.txt", name.toLowerCase());
    }

    boolean isNamed(String name) {
        return name.toLowerCase().equals(this.name);
    }

    String getKeyField() {
        return keyField;
    }

    ArrayList<String> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }
}
