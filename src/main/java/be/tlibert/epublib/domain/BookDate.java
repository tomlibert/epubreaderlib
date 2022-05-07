package be.tlibert.epublib.domain;

import java.io.Serializable;

public class BookDate implements Serializable {

    static final long serialVersionUID = 1L;

    private String dateType;
    private String dateValue;

    public BookDate(String dateType, String dateValue) {
        this.dateType = dateType;
        this.dateValue = dateValue;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getDateValue() {
        return dateValue;
    }

    public void setDateValue(String dateValue) {
        this.dateValue = dateValue;
    }

    @Override
    public String toString() {
        return "BookDate{" +
                "dateType='" + dateType + '\'' +
                ", dateValue='" + dateValue + '\'' +
                '}';
    }
}