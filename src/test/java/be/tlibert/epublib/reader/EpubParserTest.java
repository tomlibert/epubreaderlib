package be.tlibert.epublib.reader;

import be.tlibert.epublib.domain.Book;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class EpubParserTest {

    public String getTestFile(String fn) {
        return this.getClass().getClassLoader().getResource("epub2_unit_test1.epub").getFile().substring(1);
    }

    @Test
    void parse() {
        String fn = getTestFile("epub2_unit_test1.epub");

        for (int i=0; i < 1; ++i) {
            long start = System.currentTimeMillis();
            Book book = EpubParser.getInstance().parse(fn);
            long stop = System.currentTimeMillis();
            System.out.println("Parsing of book took " + (stop - start) + " ms");
            assertNotNull(book);
            assertNotNull(book.getLanguages());
            assertEquals("en", book.getLanguages().get(0));
            assertTrue(book.getFilesize() > 0);
            System.out.println("filesize= " + book.getFilesize());
        }
    }

    @Test
    void parseExternalFile() {
        String fn = "D:\\epubweb\\test\\3rdWorldProductsInc04.epub";

        long start = System.currentTimeMillis();
        Book book = EpubParser.getInstance().parse(fn);
        long stop = System.currentTimeMillis();
        System.out.println("Parsing of book took " + (stop - start) + " ms");
        assertNotNull(book);
        assertNotNull(book.getLanguages());
        assertEquals("en", book.getLanguages().get(0));
        assertTrue(book.getFilesize() > 0);
        System.out.println("filesize= " + book.getFilesize());
   }
}