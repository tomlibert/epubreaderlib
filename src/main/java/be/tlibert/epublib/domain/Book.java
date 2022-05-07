package be.tlibert.epublib.domain;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private List<String> languages;
    private List<String> subjects;
    private List<String> descriptions;
    private List<String> creators;
    private List<String> contributors;
    private List<BookIdentifier> identifiers;
    private List<Metadata> metadataList;
    private List<BookDate> bookDates;

    private List<String> titles;
    private Long id;

    private long filesize;

    private String filename;
    private String source;
    private String epubVersion;

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public List<BookIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<BookIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book() {
        this.setCreators(new ArrayList<>());
        this.setIdentifiers(new ArrayList<>());
        this.setDescriptions(new ArrayList<>());
        this.setContributors(new ArrayList<>());
        this.setSubjects(new ArrayList<>());
        this.setLanguages(new ArrayList<>());
        this.setMetadataList(new ArrayList<>());
        this.setBookDates(new ArrayList<>());
        this.setTitles(new ArrayList<>());
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setEpubVersion(String version) {
        this.epubVersion = version;
    }

    public String getEpubVersion() {
        return epubVersion;
    }

    public List<Metadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<Metadata> metadataList) {
        this.metadataList = metadataList;
    }

    public List<BookDate> getBookDates() {
        return bookDates;
    }

    public void setBookDates(List<BookDate> bookDates) {
        this.bookDates = bookDates;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
}