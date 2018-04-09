class StringPadder {
    static String padLeft(String string, int size) {
        return String.format("%1$" + size + "s", string);
    }

    static String padRight(String string, int size) {
        return String.format("%1$-" + size + "s", string);
    }
}
