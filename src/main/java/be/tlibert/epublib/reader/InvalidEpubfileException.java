package be.tlibert.epublib.reader;

public class InvalidEpubfileException extends Exception {
    public InvalidEpubfileException(String filename) {
        super("Epub file is invalid: " + filename);
    }
}
