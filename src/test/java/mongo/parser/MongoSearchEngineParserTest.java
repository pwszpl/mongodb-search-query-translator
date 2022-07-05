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
        assertBsonResult("x.z=20", Filters.eq("x.z",20));
        assertBsonResult("x.z=20.0", Filters.eq("x.z",20.0));
        assertBsonResult("x.z>20.0", Filters.gt("x.z",20.0));
        assertBsonResult("x.z<20.0", Filters.lt("x.z",20.0));
        assertBsonResult("x.z<=20.0", Filters.lte("x.z",20.0));
        assertBsonResult("x.z>=20.0", Filters.gte("x.z",20.0));

        assertCriteriaResult("x.z='z'", Criteria.where("x.z").is("z"));
        assertCriteriaResult("x.z=20", Criteria.where("x.z").is(20));
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
        assertBsonResult("exists(x.z) = true", Filters.exists("x.z"));
        assertBsonResult("exists(x.z) = false", Filters.exists("x.z",false));

        assertCriteriaResult("exists(x.z) = true", Criteria.where("x.z").exists(true));
        assertCriteriaResult("exists(x.z) = false", Criteria.where("x.z").exists(false));
    }
    @Test
    void shouldEvaluteTypeFunction(){
        assertBsonResult("type(x.z) = 'string'", Filters.type("x.z","string"));

        assertCriteriaResult("type(x.z) = 'string'", Criteria.where("x.z").type(JsonSchemaObject.Type.of("string")));
    }

    @Test
    void shouldEvaluteSizeFunction(){
        assertBsonResult("size(x.z) = 10", Filters.size("x.z",10));

        assertCriteriaResult("size(x.z) = 10", Criteria.where("x.z").size(10));
    }

    @Test
    void shouldEvaluateRegexFunction(){
        assertBsonResult("x.y like 'abc'", Filters.regex("x.y","abc"));

        assertCriteriaResult("x.y like 'abc'", new Criteria("x.y").regex("abc"));
    }

    @Test
    void shouldEvaluateModFunction(){
        assertBsonResult("mod(x.z,2) = 1", Filters.mod("x.z",2,1));

        assertCriteriaResult("mod(x.z,2) = 1", Criteria.where("x.z").mod(2,1));
    }
    @Test
    void shouldEvaluateAndStatement() {
        assertBsonResult("x.y='z' and x.z='z'", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' && x.z=99", Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z",99)));

        assertCriteriaResult("x.y='z' and x.z='z'", new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z"))));
        assertCriteriaResult("x.y='z' && x.z=99", new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is(99))));
    }

    @Test
    void shouldEvaluateOrStatement() {
        assertBsonResult("x.y='z' or x.z='z'", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' || x.z=99", Filters.or(Filters.eq("x.y","z"),Filters.eq("x.z",99)));

        assertCriteriaResult("x.y='z' or x.z='z'", new Criteria().orOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z"))));
        assertCriteriaResult("x.y='z' || x.z=99", new Criteria().orOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is(99))));
    }
    @Test
    void shouldEvaluateNorStatement() {
        assertBsonResult("x.y='z' NOR x.z='z'", Filters.nor(Filters.eq("x.y","z"),Filters.eq("x.z","z")));
        assertBsonResult("x.y='z' NOR x.z=99", Filters.nor(Filters.eq("x.y","z"),Filters.eq("x.z",99)));

        assertCriteriaResult("x.y='z' NOR x.z='z'", new Criteria().norOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z"))));
        assertCriteriaResult("x.y='z' NOR x.z=99", new Criteria().norOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is(99))));
    }
    @Test
    void shouldEvaluateNotStatement(){
        assertBsonResult("not(exists(x.z) = true)", Filters.not(Filters.exists("x.z")));
        assertBsonResult("!(type(x.z) = 'string')", Filters.not(Filters.type("x.z","string")));

        assertCriteriaResult("not(exists(x.z) = true)", Criteria.where("x.z").not().exists(true));
        assertCriteriaResult("!(type(x.z) = 'string')", Criteria.where("x.z").not().type(JsonSchemaObject.Type.of("string")));
    }
    @Test
    void shouldEvaluateInOperator() {
        assertBsonResult("x.y in ('a' 'b' 'c')", Filters.in("x.y",new String[] {"a","b","c"}));
        assertBsonResult("x.y in (1 2 3)", Filters.in("x.y",new Integer[] {1,2,3}));

        assertCriteriaResult("x.y in ('a' 'b' 'c')", Criteria.where("x.y").in(new String[] {"a","b","c"}));
        assertCriteriaResult("x.y in (1 2 3)", Criteria.where("x.y").in(new Integer[] {1,2,3}));
    }

    @Test
    void shouldEvaluateNinOperator() {
        assertBsonResult("x.y nin ('a' 'b' 'c')", Filters.nin("x.y",new String[] {"a","b","c"}));
        assertBsonResult("x.y nin (1 2 3)", Filters.nin("x.y",new Integer[] {1,2,3}));

        assertCriteriaResult("x.y nin ('a' 'b' 'c')", Criteria.where("x.y").nin(new String[] {"a","b","c"}));
        assertCriteriaResult("x.y nin (1 2 3)", Criteria.where("x.y").nin(new Integer[] {1,2,3}));
    }

    @Test
    void shouldEvaluateAllOperator() {
        assertBsonResult("x.y all ('a' 'b' 'c')", Filters.all("x.y",new String[] {"a","b","c"}));
        assertBsonResult("x.y all (1 2 3)", Filters.all("x.y",new Integer[] {1,2,3}));

        assertCriteriaResult("x.y all ('a' 'b' 'c')", Criteria.where("x.y").all(new String[] {"a","b","c"}));
        assertCriteriaResult("x.y all (1 2 3)", Criteria.where("x.y").all(new Integer[] {1,2,3}));
    }

    @Test
    void shouldEvaluateElemMatchOperator(){
        assertBsonResult("x.y matches (c='b')", Filters.elemMatch("x.y",Filters.eq("c","b")));
        assertBsonResult("x.y matches (c='b' and d=1)", Filters.elemMatch("x.y",
                Filters.and(
                        Filters.eq("c","b"),
                        Filters.eq("d",1))
                ));

        assertCriteriaResult("x.y matches (c='b')", Criteria.where("x.y").elemMatch(Criteria.where("c").is("b")));
        assertCriteriaResult("x.y matches (c='b' and d=1)", Criteria.where("x.y").elemMatch(
                new Criteria().andOperator(
                        Criteria.where("c").is("b"),
                        Criteria.where("d").is(1))
                ));
    }
    @Test
    void shouldKeepLogicalPrecedence(){
        assertBsonResult("x.y='z' or x.y='y' and x.z=99 or x.z=1200",
                Filters.or(
                        Filters.or(
                            Filters.eq("x.y","z"),
                            Filters.and(
                                Filters.eq("x.y","y"),
                                Filters.eq("x.z",99)
                            )
                        ),
                        Filters.eq("x.z",1200))
                );
        assertBsonResult("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)",
                Filters.and(
                        Filters.or(
                                Filters.eq("x.y","z"),
                                Filters.eq("x.y","y")
                        ),
                        Filters.or(
                                Filters.eq("x.z",99),
                                Filters.eq("x.z",1200)
                        )
                ));

        assertCriteriaResult("x.y='z' or x.y='y' and x.z=99 or x.z=1200",
                new Criteria().orOperator(
                            new Criteria().orOperator(
                                    Criteria.where("x.y").is("z"),
                                    new Criteria().andOperator(
                                            Criteria.where("x.y").is("y"),
                                            Criteria.where("x.z").is(99)
                                    )
                            ),
                            Criteria.where("x.z").is(1200)
                )
        );

        assertCriteriaResult("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)",
                new Criteria().andOperator(
                        new Criteria().orOperator(
                                Criteria.where("x.y").is("z"),
                                Criteria.where("x.y").is("y")
                        ),
                        new Criteria().orOperator(
                                Criteria.where("x.z").is(99),
                                Criteria.where("x.z").is(1200)
                        )
                )
        );

    }

    private void assertBsonResult(String s, Bson expected){
        parser = new MongoSearchEngineParser(new StringReader(s));
        try {
            Bson result = (Bson) parser.parse();
            Assertions.assertEquals(expected.toBsonDocument().toJson(),result.toBsonDocument().toJson());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertCriteriaResult(String s, Criteria expected){
        parser = new MongoSearchEngineParser(new StringReader(s));
        parser.setCriteraMode();
        try {
            Criteria c = (Criteria)parser.parse();
            Assertions.assertEquals(expected.getCriteriaObject().toBsonDocument().toJson(),c.getCriteriaObject().toBsonDocument().toJson());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}