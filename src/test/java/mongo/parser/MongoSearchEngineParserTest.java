package mongo.parser;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.StringReader;

class MongoSearchEngineParserTest {
    private static MongoSearchEngineParser parser;

    @Test
    void shouldEvaluateFieldComparision(){
        assertBsonResult("x.z='z'", Filters.eq("x.z","z"));
        assertBsonResult("x.z=20", Filters.eq("x.z",20.0));
        assertBsonResult("x.z=20.0", Filters.eq("x.z",20.0));
        assertBsonResult("x.z>20.0", Filters.gt("x.z",20.0));
        assertBsonResult("x.z<20.0", Filters.lt("x.z",20.0));
        assertBsonResult("x.z<=20.0", Filters.lte("x.z",20.0));
        assertBsonResult("x.z>=20.0", Filters.gte("x.z",20.0));

        assertCriteriaResult("x.z='z'", Criteria.where("x.z").is("z"));
        assertCriteriaResult("x.z=20", Criteria.where("x.z").is(20.0));
        assertCriteriaResult("x.z=20.0", Criteria.where("x.z").is(20.0));
        assertCriteriaResult("x.z>20.0", Criteria.where("x.z").gt(20.0));
        assertCriteriaResult("x.z<20.0", Criteria.where("x.z").lt(20.0));
        assertCriteriaResult("x.z<=20.0", Criteria.where("x.z").lte(20.0));
        assertCriteriaResult("x.z>=20.0", Criteria.where("x.z").gte(20.0));
    }

    @Test
    void shouldEvaluteNegativeNumbers(){
        assertBsonResult("x.z=-10", Filters.eq("x.z",-10.0));

        assertCriteriaResult("x.z=-10", Criteria.where("x.z").is(-10.0));
    }

    @Test
    void shouldEvaluateAndStatement() {
        assertBsonResult("x.y='z' and x.z='z'", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' && x.z=99", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));

        assertCriteriaResult("x.y='z' and x.z='z'", Criteria.where("x.y").is("z").andOperator(Criteria.where("x.z").is("z")));
        assertCriteriaResult("x.y='z' && x.z=99", Criteria.where("x.y").is("z").andOperator(Criteria.where("x.z").is(99.0)));
    }

    @Test
    void shouldEvaluateOrStatement() {
        assertBsonResult("x.y='z' or x.z='z'", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' || x.z=99", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));

        assertCriteriaResult("x.y='z' or x.z='z'", Criteria.where("x.y").is("z").orOperator(Criteria.where("x.z").is("z")));
        assertCriteriaResult("x.y='z' || x.z=99", Criteria.where("x.y").is("z").orOperator(Criteria.where("x.z").is(99.0)));
    }

    @Test
    void shouldKeepLogicalPrecedence(){
        assertBsonResult("x.y='z' or x.y='y' and x.z=99 or x.z=1200",
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
        assertBsonResult("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)",
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

        assertCriteriaResult("x.y='z' or x.y='y' and x.z=99 or x.z=1200",
                new Criteria().orOperator(
                            new Criteria().orOperator(
                                    Criteria.where("x.y").is("z"),
                                    new Criteria().andOperator(
                                            Criteria.where("x.y").is("y"),
                                            Criteria.where("x.z").is(99.0)
                                    )
                            ),
                            Criteria.where("x.z").is(1200.0)
                )
        );

        assertCriteriaResult("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)",
                new Criteria().andOperator(
                        new Criteria().orOperator(
                                Criteria.where("x.y").is("z"),
                                Criteria.where("x.y").is("y")
                        ),
                        new Criteria().orOperator(
                                Criteria.where("x.z").is(99.0),
                                Criteria.where("x.z").is(1200.0)
                        )
                )
        );

    }

    private void assertBsonResult(String s, Bson expected){
        parser = new MongoSearchEngineParser(new StringReader(s));
        try {
            Assertions.assertEquals(expected,parser.parse());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertCriteriaResult(String s, Criteria expected){
        parser = new MongoSearchEngineParser(new StringReader(s));
        parser.setCriteraMode();
        try {
            Criteria c = (Criteria)parser.parse();
            Assertions.assertEquals(expected.getCriteriaObject(),c.getCriteriaObject());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}