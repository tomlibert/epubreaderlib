package be.tlibert.epublib.domain;

public class BookIdentifier {

    private String scheme;
    private String value;

    public BookIdentifier(String scheme, String value) {
        this.scheme = scheme;
        this.value = value;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}