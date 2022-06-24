package mongo.parser;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

class MongoSearchEngineParserTest {
    private static MongoSearchEngineParser parser;

    @Test
    void shouldEvaluateFieldComparision(){
        assertParserResult("x.z='z'", Filters.eq("x.z","z"));
        assertParserResult("x.z=20", Filters.eq("x.z",20.0));
        assertParserResult("x.z=20.0", Filters.eq("x.z",20.0));
        assertParserResult("x.z>20.0", Filters.gt("x.z",20.0));
        assertParserResult("x.z<20.0", Filters.lt("x.z",20.0));
        assertParserResult("x.z<=20.0", Filters.lte("x.z",20.0));
        assertParserResult("x.z>=20.0", Filters.gte("x.z",20.0));
    }

    @Test
    void shouldEvaluteNegativeNumbers(){
        assertParserResult("x.z=-10", Filters.eq("x.z",-10.0));
    }

    @Test
    void shouldEvaluateAndStatement() {
        assertParserResult("x.y='z' and x.z='z'", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertParserResult("x.y='z' && x.z=99", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));
    }

    @Test
    void shouldEvaluateOrStatement() {
        assertParserResult("x.y='z' or x.z='z'", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertParserResult("x.y='z' || x.z=99", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));
    }

    @Test
    void shouldKeepLogicalPrecedence(){
        assertParserResult("x.y='z' or x.y='y' and x.z=99 or x.z=1200",
                Filters.or(
                        Filters.or(
                            Filters.eq("x.y","z"),
                            Filters.and(
                                Filters.eq("x.y","y"),
                                Filters.eq("x.z",99.0)
                            )
                        ),
                        Filters.eq("x.z",1200.0))
                );
        assertParserResult("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)",
                Filters.and(
                        Filters.or(
                                Filters.eq("x.y","z"),
                                Filters.eq("x.y","y")
                        ),
                        Filters.or(
                                Filters.eq("x.z",99.0),
                                Filters.eq("x.z",1200.0)
                        )
                ));
    }

    private void assertParserResult(String s, Bson expected){
        parser = new MongoSearchEngineParser(new StringReader(s));
        try {
            Assertions.assertEquals(expected,parser.parse());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}