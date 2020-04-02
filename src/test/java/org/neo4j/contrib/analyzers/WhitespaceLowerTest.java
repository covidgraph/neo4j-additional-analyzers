package org.neo4j.contrib.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.index.fulltext.AnalyzerProvider;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.helpers.collection.Iterators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WhitespaceLowerTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule();

    @Test
    public void checkTokenStream() {
        AnalyzerProvider provider = AnalyzerProvider.getProviderByName("whitespace_lower");
        assertNotNull(provider);
        assertEquals(WhitespaceLowerAnalyzerProvider.DESCRIPTION, provider.description());
        Analyzer analzyer = provider.createAnalyzer();
        assertThat(TestUtils.analyze("CAN Open Bus Logo", analzyer), contains("can", "open", "bus", "logo"));
        assertThat(TestUtils.analyze("_Modbus", analzyer), contains("_modbus"));
        assertThat(TestUtils.analyze("abc x-270°", analzyer), contains("abc", "x-270°"));

        assertThat(TestUtils.analyze("There's a gene called PNAS-108", analzyer), contains("gene", "called", "pnas-108"));
        assertThat(TestUtils.analyze("There's a gene called PNAS-108", new StandardAnalyzer()), contains("there's", "gene", "called", "pnas", "108"));
    }

    @Test
    public void checkAnalyzerIsAvailable() {
        TestUtils.checkForAnalyzer(neo4j.getGraphDatabaseService(), "whitespace_lower", WhitespaceLowerAnalyzerProvider.DESCRIPTION);
    }

    @Test
    public void checkSearchForTermContainingDash() {
        GraphDatabaseService db = neo4j.getGraphDatabaseService();
        db.execute("CALL db.index.fulltext.createNodeIndex('myIndex', ['Article'], ['title'], {analyzer: 'whitespace_lower'})");
        db.execute( "CREATE (:Article{title:'abc x-270°'})");

        assertEquals(1, Iterators.count(db.execute("CALL db.index.fulltext.queryNodes('myIndex', 'x\\\\-270°') yield node, score return node.title as text, score")));
        assertEquals(0, Iterators.count(db.execute("CALL db.index.fulltext.queryNodes('myIndex', '\\\\-270°') yield node, score return node.title as text, score")));
    }
}
