package be.tlibert.epublib.reader;

import be.tlibert.epublib.domain.Book;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class EpubParserTest {

    final Logger logger = LoggerFactory.getLogger(EpubParserTest.class);

    public String getTestFile(String fn) {
        return this.getClass().getClassLoader().getResource(fn).getFile().substring(1);
    }

    @Test
    void parse() {
        String fn = getTestFile("epub2_unit_test1.epub");

        long start = System.currentTimeMillis();
        Book book = EpubParser.getInstance().parse(fn);
        long stop = System.currentTimeMillis();
        logger.info("Parsing of book took {} ms", stop - start);
        assertNotNull(book);
        assertNotNull(book.getLanguages());
        assertEquals("en", book.getLanguages().get(0));
        assertTrue(book.getFilesize() > 0);
        logger.info("filesize= {}", book.getFilesize());
    }
}