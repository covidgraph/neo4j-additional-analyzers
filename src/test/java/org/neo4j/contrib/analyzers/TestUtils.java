package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.schema.AnalyzerProvider;
import org.neo4j.internal.helpers.collection.Iterators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUtils {

    static List<String> analyze(String text, Analyzer analyzer) {
        System.out.println("analyzing: " + text);
        try {
            List<String> result = new ArrayList<String>();
            TokenStream tokenStream = analyzer.tokenStream("dummy", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(attr.toString());
                System.out.println("term: "  + attr.toString());
            }
            tokenStream.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void checkForAnalyzer(GraphDatabaseService db, String analyzerName, String description) {
        db.executeTransactionally("CALL db.index.fulltext.listAvailableAnalyzers() YIELD analyzer, description RETURN analyzer, description",
                Collections.emptyMap(), result -> {
                    Map<String, Object> row = result.stream().filter(map -> map.get("analyzer").equals(analyzerName)).findFirst().orElse(null);
                    assertNotNull(row);
                    assertEquals(description, row.get("description"));
                    return null;
                });
    }

    public static AnalyzerProvider analyzerProviderByName(String analyzerName) {
        return StreamSupport.stream(ServiceLoader.load(AnalyzerProvider.class).spliterator(), false)
                    .filter(analyzerProvider -> analyzerName.equals(analyzerProvider.getName()))
                    .findFirst().get();
    }

    public static void assertResultCount(GraphDatabaseService db, String cypher, long expected) {
        assertEquals(expected, (long)db.executeTransactionally(cypher, Collections.emptyMap(), Iterators::count));
    }
}
