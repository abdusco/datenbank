import java.io.*;
import java.util.ArrayList;

class Catalog {
    private String name;
    private ArrayList<Type> types;

    Catalog(String name) throws IOException, InvalidTypeException, InvalidPageException {
        this.name = name;
        createCatalogFileIfNotExists();
        readCatalogFile();
    }

    private void readCatalogFile() throws IOException, InvalidTypeException, InvalidPageException {
        types = new ArrayList<>();
        FileInputStream stream = new FileInputStream(getCatalogFilename());
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            types.add(Type.createFromSerialized(line));
        }
    }

    private void createCatalogFileIfNotExists() throws IOException {
        String catalogName = getCatalogFilename();
        File catalog = new File(catalogName);
        catalog.createNewFile();
    }

    private String getCatalogFilename() {
        return String.format("%s.catalog.txt", name);
    }

    ArrayList<Type> getTypes() {
        return types;
    }

    Type getType(String name) {
        for (Type type : types) {
            if (type.isNamed(name)) return type;
        }
        return null;
    }

    void createType(String name, ArrayList<String> fields, String keyField) throws IOException, InvalidTypeException {
        if (getType(name) != null) {
            Logger.log(String.format("Type '%s' already exists", name));
            return;
        }

        Type type = new Type(name, fields, keyField);
        types.add(type);
        updateCatalogFile();
        createTypeFile(type.getFilename());
        Logger.log(String.format("Created type '%s'", name));
    }

    private void createTypeFile(String filename) throws IOException {
        File typeFile = new File(filename);
        typeFile.createNewFile();
    }

    private void updateCatalogFile() throws IOException {
        emptyCatalogFile();

        FileWriter fw = new FileWriter(getCatalogFilename(), true);
        for (Type type : types) {
            fw.write(type.serialize() + System.lineSeparator());
        }
        fw.close();
    }

    void deleteType(String name) throws IOException {
        for (Type type : types) {
            if (!type.isNamed(name)) continue;

            types.remove(type);
            deleteTypeFile(type.getFilename());
            updateCatalogFile();
            Logger.log(String.format("Type '%s' has been deleted", name));
            return;
        }
    }

    private void deleteTypeFile(String filename) {
        File file = new File(filename);
        file.delete();
    }

    private void emptyCatalogFile() throws FileNotFoundException {
        new PrintWriter(getCatalogFilename()).close();
    }
}
