package mongo.parser;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;

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
    void shouldEvaluteExistsFunction(){
        assertBsonResult("exists(x.z,true)", Filters.exists("x.z"));
        assertBsonResult("exists(x.z,false)", Filters.exists("x.z",false));

        assertCriteriaResult("exists(x.z,true)", Criteria.where("x.z").exists(true));
        assertCriteriaResult("exists(x.z,false)", Criteria.where("x.z").exists(false));
    }
    @Test
    void shouldEvaluteTypeFunction(){
        assertBsonResult("type(x.z,'string')", Filters.type("x.z","string"));

        assertCriteriaResult("type(x.z,'string')", Criteria.where("x.z").type(JsonSchemaObject.Type.of("string")));
    }

    @Test
    void shouldEvaluateAndStatement() {
        assertBsonResult("x.y='z' and x.z='z'", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' && x.z=99", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));

        assertCriteriaResult("x.y='z' and x.z='z'", new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z"))));
        assertCriteriaResult("x.y='z' && x.z=99", new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is(99.0))));
    }

    @Test
    void shouldEvaluateOrStatement() {
        assertBsonResult("x.y='z' or x.z='z'", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' || x.z=99", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z",99.0)));

        assertCriteriaResult("x.y='z' or x.z='z'", new Criteria().orOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z"))));
        assertCriteriaResult("x.y='z' || x.z=99", new Criteria().orOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is(99.0))));
    }

    @Test
    void shouldEvaluateInOperator() {
        assertBsonResult("x.y in ('a' 'b' 'c')", Filters.in("x.y",new String[] {"a","b","c"}));
        assertBsonResult("x.y in (1 2 3)", Filters.in("x.y",new Double[] {1.0,2.0,3.0}));

        assertCriteriaResult("x.y in ('a' 'b' 'c')", Criteria.where("x.y").in(new String[] {"a","b","c"}));
        assertCriteriaResult("x.y in (1 2 3)", Criteria.where("x.y").in(new Double[] {1.0,2.0,3.0}));
    }

    @Test
    void shouldEvaluateNinOperator() {
        assertBsonResult("x.y nin ('a' 'b' 'c')", Filters.nin("x.y",new String[] {"a","b","c"}));
        assertBsonResult("x.y nin (1 2 3)", Filters.nin("x.y",new Double[] {1.0,2.0,3.0}));

        assertCriteriaResult("x.y nin ('a' 'b' 'c')", Criteria.where("x.y").nin(new String[] {"a","b","c"}));
        assertCriteriaResult("x.y nin (1 2 3)", Criteria.where("x.y").nin(new Double[] {1.0,2.0,3.0}));
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
            Bson result = (Bson) parser.parse();
            Assertions.assertEquals(expected,result);
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