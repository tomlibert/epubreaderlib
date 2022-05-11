package be.tlibert.epublib.reader;

import be.tlibert.epublib.domain.Book;
import be.tlibert.epublib.exception.InvalidEpubfileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class EpubParserTest {

    final Logger logger = LoggerFactory.getLogger(EpubParserTest.class);

    public String getTestFile(String fn) {
        return this.getClass().getClassLoader().getResource(fn).getFile().substring(1);
    }

    @Test
    void parseValidFile() {
        String fn = getTestFile("epub2_unit_test1.epub");

        try {
            Book book = EpubParser.getInstance().parse(fn);
            assertNotNull(book);
            assertNotNull(book.getLanguages());
            assertEquals("en", book.getLanguages().get(0));
            assertTrue(book.getCompressedFilesize() > 0);
            assertTrue(book.getCompressedFilesize() > 0);
            logger.info("book= {}", book);
        } catch (FileNotFoundException | InvalidEpubfileException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testParseNotAnEpubFile() {
        String fn = getTestFile("notAnEpub.txt");

        Assertions.assertThrows(InvalidEpubfileException.class, () -> {
            EpubParser.getInstance().parse(fn);
        });
    }

    @Test
    void testParseNotExistingFile() {
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            EpubParser.getInstance().parse("notExistingFile.epub");
        });
    }

/*
    For debugging purposes
    @Test
    void testExternalFile() {
        try {
            Book book = EpubParser.getInstance().parse("D:\\epubweb\\shelf\\UNDEFINED\\UNDEFINED\\2401.epub");
            assertNotNull(book);
            assertFalse(book.getLanguages().isEmpty());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvalidEpubfileException e) {
            throw new RuntimeException(e);
        }
    }*/
}