# Datenbank
This is a primitive database management system (DBMS) implementation in Java. It supports following functionality:

- Creating a type
- Deleting a type
- Finding a type
- Listing all types
- Creating a record
- Deleting a record
- Finding a record
- Listing all records

A command line interface is also implemented to test and perform these operations. 

# API
The `Database` class exposes a minimal API for these operations. 

```java
class Database {
    ArrayList<Type> getTypes() {}
    Type getType(String typeName) {}
    void createType(String typeName, ArrayList<String> fields) {}
    void deleteType(String typeName) {}
    void createRecord(String typeName, HashMap<String, String> values) {}
    void deleteRecord(String typeName, String key) {}
    Record getRecord(String typeName, String key) {}
    ArrayList<Record> getRecordsByType(String typeName) {}
} 
```
