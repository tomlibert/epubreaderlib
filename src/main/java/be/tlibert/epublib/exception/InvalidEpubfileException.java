package be.tlibert.epublib.exception;

public class InvalidEpubfileException extends Exception {
    public InvalidEpubfileException(String filename) {
        super("Epub file is invalid: " + filename);
    }
}
