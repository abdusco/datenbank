import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static Database db;

    public static void main(String[] args) throws IOException, InvalidTypeException, InvalidPageException, InvalidRecordException {
        db = new Database("Datenbank");
        DatabaseCLI cli = new DatabaseCLI(db);
        cli.listen();
    }
}
