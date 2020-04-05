package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> result =  db.execute("CALL db.index.fulltext.listAvailableAnalyzers() YIELD analyzer, description RETURN analyzer, description")
                .stream().filter(map -> map.get("analyzer").equals(analyzerName)).findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(description, result.get("description"));
    }
}
