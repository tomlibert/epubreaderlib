package be.tlibert.epublib.reader;

import be.tlibert.epublib.domain.Book;
import be.tlibert.epublib.domain.BookDate;
import be.tlibert.epublib.domain.BookIdentifier;
import be.tlibert.epublib.domain.Metadata;
import be.tlibert.epublib.exception.InvalidEpubfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;

public class EpubParser {

    private static final Logger logger = LoggerFactory.getLogger(EpubParser.class);

    private DocumentBuilder db;

    public static EpubParser getInstance() {
        return new EpubParser();
    }

    private DocumentBuilderFactory createSecuredDocumentBuilderFactory() {
        logger.trace("Going to create a secured document builder factory...");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // to be compliant, completely disable DOCTYPE declaration:
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            dbf.setExpandEntityReferences(false);
            dbf.setIgnoringComments(true);

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (Exception e) {
            logger.error("Problem securing securedDocumentBuilderFactory: {}", e.getMessage());
        }
        return dbf;
    }

    public Book parse(String filename) throws FileNotFoundException, InvalidEpubfileException {
        logger.trace("Going to parse epub {} ...", filename);

        validateFile(filename);

        AtomicReference<Book> book = new AtomicReference<>(new Book());
        AtomicLong uncompressedSize = new AtomicLong();

        try (ZipFile zipFile = new ZipFile(filename)) {
            zipFile.stream().forEach(zipEntry -> {
                if (zipEntry.getName().toLowerCase().endsWith(".opf")) {
                    logger.trace("Opf file found; processing ...");
                    Document doc;
                    try {
                        doc = createDocument(zipFile.getInputStream(zipEntry));
                        book.set(retrieveFromContent(doc));
                    } catch (IOException e) {
                        logger.error("Problem occured during retrieving tags; {}", e.getMessage());
                    }
                }
                uncompressedSize.addAndGet(zipEntry.getSize());
            });
        } catch (Exception e) {
            logger.error("Problem occured processing epub: {}", e.getMessage());
        }
        book.get().setUncompressedFilesize(uncompressedSize.get());
        try {
            book.get().setCompressedFilesize(Files.size(Path.of(filename)));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        book.get().setFilename(filename);
        return book.get();
    }

    private void validateFile(String filename) throws InvalidEpubfileException, FileNotFoundException {

        if (filename == null || filename.isBlank() || !filename.endsWith(".epub")) {
            throw new InvalidEpubfileException(filename);
        }

        if (Files.notExists(Path.of(filename))) {
            throw new FileNotFoundException(filename);
        }
    }

    private Book retrieveFromContent(Document doc) {
        Book book = new Book();

        Element element = doc.getDocumentElement();

       book.setEpubVersion(element.getAttribute("version"));

        NodeList packageChildNodesList = element.getChildNodes();
        for (int i=0; i< packageChildNodesList.getLength(); ++i) {
            Node childNode = packageChildNodesList.item(i);
            if (childNode.getNodeName().equalsIgnoreCase("metadata") || childNode.getNodeName().equalsIgnoreCase("opf:metadata")) {
                NodeList metadataChildNodes = childNode.getChildNodes();

                for (int j=0; j < metadataChildNodes.getLength(); ++j) {
                    Node metadataNode = metadataChildNodes.item(j);
                    String contents = clean(metadataNode.getTextContent());

                    addTitle(book, metadataNode, contents);
                    addCreator(book, metadataNode, contents);
                    addDescription(book, metadataNode, contents);
                    addContributor(book, metadataNode, contents);
                    addSource(book, metadataNode, contents);
                    addSubject(book, metadataNode, contents);
                    addMeta(book, metadataNode);
                    addDate(book, metadataNode, contents);
                    addIdentifiers(book, metadataNode, contents);
                }
            }
        }

        return book;
    }

    private void addIdentifiers(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:identifier")) {

            NamedNodeMap attributes = metadataNode.getAttributes();
            for (int i=0; i < attributes.getLength(); ++i) {
                if (attributes.item(i).getNodeName().toLowerCase().contains("scheme")) {
                    book.getIdentifiers().add(new BookIdentifier(attributes.item(i).getTextContent(), contents));
                }
            }
        }
    }

    private void addDate(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:date")) {
            Node nameNode = metadataNode.getAttributes().getNamedItem("opf:event");
            String dateType;
            if (nameNode != null) {
                dateType = nameNode.getTextContent();
            } else {
                dateType = "publication";
            }
            book.getBookDates().add(new BookDate(dateType, contents));
        }
    }

    private void addMeta(Book book, Node metadataNode) {
        if (metadataNode.getNodeName().equalsIgnoreCase("meta")) {
            Node nameNode = metadataNode.getAttributes().getNamedItem("name");
            if (nameNode != null) {
                String key = nameNode.getTextContent();
                Node valueNode = metadataNode.getAttributes().getNamedItem("content");
                String value = valueNode.getTextContent();
                book.getMetadataList().add(new Metadata(key, value));
            }
        }
    }

    private void addSubject(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:subject")) {
            book.getSubjects().add(contents);
        }
    }

    private void addSource(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:source")) {
            book.setSource(contents);
        }
    }

    private void addContributor(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:contributor")) {
            book.getContributors().add(contents);
        }
    }

    private void addDescription(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:description")) {
            book.getDescriptions().add(contents);
        }
    }

    private void addCreator(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:creator")) {
            book.getCreators().add(contents);
        }
    }

    private void addTitle(Book book, Node metadataNode, String contents) {
        if (metadataNode.getNodeName().equalsIgnoreCase("dc:language")) {
            book.getLanguages().add(contents);
        }
    }

    private String clean(String textContent) {
        return textContent.replaceAll("\\s+", "");
    }

    private Document createDocument(InputStream is) {
        Document doc = null;

        try {
            doc = db.parse(is);
            doc.getDocumentElement().normalize();

        } catch (IOException | SAXException e) {
            logger.error("{}", e.getMessage());
        }

        return doc;
    }

    public EpubParser() {
        DocumentBuilderFactory documentBuilderFactory = createSecuredDocumentBuilderFactory();
        try {
            db = documentBuilderFactory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
        }
    }
}