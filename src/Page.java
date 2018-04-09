import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Page {
    private static final int MAX_RECORD_COUNT = 20;
    private int id;
    private ArrayList<Record> records;

    Page(int id) {
        this.id = id;
        this.records = new ArrayList<>();
    }

    private Page(int id, ArrayList<Record> records) {
        this.id = id;
        this.records = records;
    }

    static Page createFromSerialized(Type type, String serialized) throws InvalidPageException {
        String regex = "^(\\d+\\s*),(\\d+\\s*),(\\d+\\s*)" // header
                + "!" // records separator
                + "(.*)$"; // records

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(serialized.trim());
        if (!matcher.matches()) {
            throw new InvalidPageException("Page does not have proper encoding");
        }

        int id = Integer.valueOf(matcher.group(1).trim());
        // int size = Integer.valueOf(matcher.group(2).trim());
        // int used = Integer.valueOf(matcher.group(3).trim());
        String[] rawRecords = matcher.group(4).trim()
                .split("\\|");

        ArrayList<Record> records = Arrays.stream(rawRecords)
                .map(raw -> {
                    try {
                        return Record.createFromSerialized(type, raw);
                    } catch (InvalidRecordException ignored) {
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        return new Page(id, records);
    }

    void addRecord(Record record) {
        if (!hasSpace()) {
            Logger.log(String.format("Page '%d' is full", id));
            return;
        }
        records.add(record);
    }

    void deleteRecord(Record record) {
        records.removeIf(r -> r.isKey(record.getKeyValue()));
    }

    Record getRecord(String key) {
        for (Record record : records) {
            if (record.isKey(key)) return record;
        }
        return null;
    }

    boolean hasSpace() {
        return records.size() < MAX_RECORD_COUNT;
    }

    String serialize() {
        return String.format("%s!%s",
                serializeHeader(),
                serializeRecords());
    }

    private String serializeRecords() {
        return getRecords().stream()
                .map(Record::serialize)
                .collect(Collectors.joining("|"));
    }

    private String serializeHeader() {
        return String.format("%-4d,%-2d,%-2d",
                id,
                getSize(),
                getUsedSize());
    }

    private int getUsedSize() {
        return records.size();
    }

    private int getSize() {
        return MAX_RECORD_COUNT;
    }

    ArrayList<Record> getRecords() {
        return records;
    }

    boolean isEmpty() {
        return records.size() == 0;
    }

    int getId() {
        return id;
    }
}
