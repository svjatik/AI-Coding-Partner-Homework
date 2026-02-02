package com.workshop.ticketsystem.parser;

import com.workshop.ticketsystem.exception.FileParseException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParserFactory {

    private final Map<String, FileParser> parsers;

    public ParserFactory(List<FileParser> parserList) {
        this.parsers = parserList.stream()
                .collect(Collectors.toMap(
                        parser -> parser.getSupportedFormat().toLowerCase(),
                        Function.identity()
                ));
    }

    public FileParser getParser(String format) {
        FileParser parser = parsers.get(format.toLowerCase());
        if (parser == null) {
            throw new FileParseException("Unsupported file format: " + format +
                    ". Supported formats: " + String.join(", ", parsers.keySet()));
        }
        return parser;
    }
}
