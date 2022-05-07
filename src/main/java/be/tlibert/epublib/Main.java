package be.tlibert.epublib;

import be.tlibert.epublib.reader.EpubParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {

        EpubParser epubParser = EpubParser.getInstance();


        try(Stream<Path> stream = Files.walk(Path.of("D:\\epubweb\\test"))) {
            stream.map(path -> epubParser.parse(path.toString())).filter(book -> book.getLanguages().isEmpty()).forEach(System.out::println);
        }

    }
}