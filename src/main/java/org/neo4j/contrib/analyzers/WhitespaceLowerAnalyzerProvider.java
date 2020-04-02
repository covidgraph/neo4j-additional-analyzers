package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.neo4j.graphdb.index.fulltext.AnalyzerProvider;
import org.neo4j.helpers.Service;

import java.io.IOException;

@Service.Implementation(AnalyzerProvider.class)
public class WhitespaceLowerAnalyzerProvider extends AnalyzerProvider {

    public static final String DESCRIPTION = "same as whitespace analyzer, but additionally applies a lower case filter to all tokens";

    public WhitespaceLowerAnalyzerProvider() {
        super("whitespace_lower", new String[0]);
    }

    public Analyzer createAnalyzer() {
        try {
            return CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(StopFilterFactory.class, "format", "snowball", "words", "org/apache/lucene/analysis/snowball/english_stop.txt,org/apache/lucene/analysis/snowball/german_stop.txt", "ignoreCase", "true")
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }
}
