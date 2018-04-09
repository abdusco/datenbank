import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Record {
    private static final int MAX_FIELD_VALUE_LENGTH = 10;
    private Type type;
    private HashMap<String, String> values;

    Record(Type type, HashMap<String, String> fieldValues) throws InvalidRecordException {
        this.type = type;
        if (fieldValuesTooLong(fieldValues)) {
            throw new InvalidRecordException("Record values exceed maximum length");
        }
        this.values = fieldValues;
    }

    private boolean fieldValuesTooLong(HashMap<String, String> fieldValues) {
        for (String val : fieldValues.values()) {
            if (val.length() > MAX_FIELD_VALUE_LENGTH) return true;
        }
        return false;
    }

    static Record createFromSerialized(Type type, String serialized) throws InvalidRecordException {
        String regex = "^(\\d+)\\s*,(\\d*)\\s*:(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(serialized);
        if (!matcher.matches()) {
            throw new InvalidRecordException("Record does not have proper encoding");
        }

        int fieldCount = Integer.valueOf(matcher.group(1).trim());
        int valueSize = Integer.valueOf(matcher.group(2).trim());
        String[] rawValues = matcher.group(3).trim().split(",");

        if (rawValues.length != type.getFields().size()) {
            throw new InvalidRecordException("Record does not have proper encoding");
        }

        HashMap<String, String> values = new HashMap<>();
        for (int i = 0; i < type.getFields().size(); i++) {
            String field = type.getFields().get(i);
            values.put(field, rawValues[i].trim());
        }

        return new Record(type, values);
    }

    String serialize() {
        return String.format("%s:%s",
                serializeHeader(),
                serializeFields()
        );
    }

    private String serializeHeader() {
        // header format: # of fields, size
        return String.format("%-2d,%-2d",
                getValueCount(),
                getValueSize()
        );
    }

    private int getValueCount() {
        return values.values().size();
    }

    private int getValueSize() {
        return serializeFields().length();
    }

    private String serializeFields() {
        StringBuilder out = new StringBuilder();
        for (String field : type.getFields()) {
            out.append(StringPadder.padRight(getValueOf(field), MAX_FIELD_VALUE_LENGTH)).append(",");
        }
        // remove last comma
        return out.substring(0, out.length() - 1);
    }

    boolean isKey(String key) {
        return values.get(type.getKeyField()).equals(key);
    }

    String getKeyValue() {
        return values.get(type.getKeyField());
    }

    String getValueOf(String key) {
        return values.getOrDefault(key, "");
    }
}
