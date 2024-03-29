package be.tlibert.epublib.reader;

import be.tlibert.epublib.domain.Book;
import be.tlibert.epublib.domain.BookDate;
import be.tlibert.epublib.domain.BookIdentifier;
import be.tlibert.epublib.domain.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;

public class EpubParser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

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

    public Book parse(String filename) {
        logger.trace("Going to parse epub {} ...", filename);

        if (Files.notExists(Path.of(filename))) {
            return null;
        }

        AtomicReference<Book> book = new AtomicReference<>(new Book());
        AtomicLong uncompressedSize = new AtomicLong();

        try (ZipFile zipFile = new ZipFile(filename)) {

            zipFile.stream().forEach(zipEntry -> {
                if (zipEntry.getName().toLowerCase().endsWith("content.opf")) {
                    logger.trace("Content.opf found; processing ...");
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
        book.get().setFilesize(uncompressedSize.get());
        book.get().setFilename(filename);
        return book.get();
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

                    if (metadataNode.getNodeName().equalsIgnoreCase("dc:language")) {
                        book.getLanguages().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:creator")) {
                        book.getCreators().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:description")) {
                        book.getDescriptions().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:contributor")) {
                        book.getContributors().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:source")) {
                        book.setSource(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:subject")) {
                        book.getSubjects().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("meta")) {
                        Node nameNode = metadataNode.getAttributes().getNamedItem("name");
                        if (nameNode != null) {
                            String key = nameNode.getTextContent();
                            Node valueNode = metadataNode.getAttributes().getNamedItem("content");
                            String value = valueNode.getTextContent();
                            book.getMetadataList().add(new Metadata(key, value));
                        }
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:title")) {
                        book.getTitles().add(contents);
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:date")) {
                        Node nameNode = metadataNode.getAttributes().getNamedItem("opf:event");
                        String dateType;
                        if (nameNode != null) {
                            dateType = nameNode.getTextContent();
                        } else {
                            dateType = "publication";
                        }
                        book.getBookDates().add(new BookDate(dateType, contents));
                    } else if (metadataNode.getNodeName().equalsIgnoreCase("dc:identifier")) {
                        Node nameNode = metadataNode.getAttributes().getNamedItem("opf:scheme");
                        book.getIdentifiers().add(new BookIdentifier(nameNode.getTextContent(), contents));
                    }
                }
            }
        }

        return book;
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