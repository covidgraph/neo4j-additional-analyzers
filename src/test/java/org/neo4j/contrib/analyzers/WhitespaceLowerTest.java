package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.index.fulltext.AnalyzerProvider;
import org.neo4j.harness.junit.Neo4jRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WhitespaceLowerTest {

    @Rule
    public Neo4jRule db = new Neo4jRule();

    @Test
    public void checkTokenStream() {
        AnalyzerProvider provider = AnalyzerProvider.getProviderByName("whitespace_lower");
        assertNotNull(provider);
        assertEquals(WhitespaceLowerAnalyzerProvider.DESCRIPTION, provider.description());
        Analyzer analzyer = provider.createAnalyzer();
        assertThat(analyze("CAN Open Bus Logo", analzyer), contains("can", "open", "bus", "logo"));
        assertThat(analyze("_Modbus", analzyer), contains("_modbus"));
    }

    @Test
    public void checkAnalyzerIsAvailable() {
        Map<String, Object> result =  db.getGraphDatabaseService().execute("CALL db.index.fulltext.listAvailableAnalyzers() YIELD analyzer, description RETURN analyzer, description")
                .stream().filter(map -> map.get("analyzer").equals("whitespace_lower")).findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(WhitespaceLowerAnalyzerProvider.DESCRIPTION, result.get("description"));
    }

    private List<String> analyze(String text, Analyzer analyzer) {
        try {
            List<String> result = new ArrayList<String>();
            TokenStream tokenStream = analyzer.tokenStream("dummy", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(attr.toString());
            }
            tokenStream.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
