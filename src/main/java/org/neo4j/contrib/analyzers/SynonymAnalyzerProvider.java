package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.graphdb.schema.AnalyzerProvider;

import java.io.IOException;

@ServiceProvider
public class SynonymAnalyzerProvider extends AnalyzerProvider {

    public static final String DESCRIPTION = "analyzer using synonyms";
    public static final String ANALYZER_NAME = "synonym";

    public SynonymAnalyzerProvider() {
        super(ANALYZER_NAME);
    }

    public Analyzer createAnalyzer() {
        try {
            return CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(SynonymFilterFactory.class, "synonyms", "gene_symbols.txt", "ignoreCase", "true")
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
