package io.github.pwszpl.mongo.parser;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.pwszpl.mongo.util.TestBuilder;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

class MongoSearchEngineParserTest {
    private static MongoSearchEngineParser parser;

    private static MongoCollection<Document> collection;

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:4.4"));

    @BeforeAll
    static void setUpData(){
        mongo.start();
        MongoClient mongoClient = MongoClients.create(mongo.getConnectionString());
        MongoDatabase database = mongoClient.getDatabase("test");
        collection = database.getCollection("testCollection");

        TestBuilder.setCollection(collection);
    }

    @AfterAll
    static void tearDown(){
        mongo.stop();
    }

    @AfterEach
    void dropCollection(){
        collection.drop();
    }

    @Test
    void shouldEvaluateFieldComparision(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField='z'")
                .assertBsonResult(Filters.eq("stringField","z"))
                .assertBsonDbResult(0)
                .assertCriteriaResult(Criteria.where("stringField").is("z"));

        TestBuilder.build("stringField<>'z'")
                .assertBsonResult(Filters.ne("stringField","z"))
                .assertBsonDbResult(3)
                .assertCriteriaResult(Criteria.where("stringField").ne("z"));

        TestBuilder.build("numField=20")
                .assertBsonResult(Filters.eq("numField",20))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("numField").is(20));

        TestBuilder.build("numField<>20")
                .assertBsonResult(Filters.ne("numField",20))
                .assertBsonDbResult(2)
                .assertCriteriaResult(Criteria.where("numField").ne(20));

        TestBuilder.build("numField>20.0")
                .assertBsonResult(Filters.gt("numField",20.0))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("numField").gt(20.0));

        TestBuilder.build("numField<20.0")
                .assertBsonResult(Filters.lt("numField",20.0))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("numField").lt(20.0));

        TestBuilder.build("numField<=20.0")
                .assertBsonResult(Filters.lte("numField",20.0))
                .assertBsonDbResult(2)
                .assertCriteriaResult(Criteria.where("numField").lte(20.0));

        TestBuilder.build("numField>=20.0")
                .assertBsonResult(Filters.gte("numField",20.0))
                .assertBsonDbResult(2)
                .assertCriteriaResult(Criteria.where("numField").gte(20.0));
    }

    @Test
    void shouldEvaluateSpringTokens(){
        TestBuilder.build("stringField=?1")
                .assertBsonResult(Filters.eq("stringField","?1"))
                .assertCriteriaResult(new Criteria("stringField").is("?1"));
    }

    @Test
    void shouldEvaluateDates(){
        collection.insertOne(new Document("dateField", Date.from(Instant.parse("2022-01-01T00:00:00.00Z"))));
        collection.insertOne(new Document("dateField",  Date.from(Instant.parse("2021-01-01T00:00:00.00Z"))));

        TestBuilder.build("dateField>=2022-01-01")
                .assertBsonResult(Filters.gte("dateField",Date.from(Instant.parse("2022-01-01T00:00:00.00Z"))))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("dateField").gte(Date.from(Instant.parse("2022-01-01T00:00:00.00Z"))));
    }

    @Test
    void shouldEvaluateTimestamps(){
        collection.insertOne(new Document("dateField", Date.from(Instant.parse("2022-01-01T23:00:00.00Z"))));
        collection.insertOne(new Document("dateField",  Date.from(Instant.parse("2022-01-01T21:00:00.00Z"))));

        TestBuilder.build("dateField>=2022-01-01T22:00:00")
                .assertBsonResult(Filters.gte("dateField",Date.from(Instant.parse("2022-01-01T22:00:00.00Z"))))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("dateField").gte(Date.from(Instant.parse("2022-01-01T22:00:00.00Z"))));
    }

    @Test
    void shouldEvaluteNegativeNumbers(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("numField=-10")
                .assertBsonResult(Filters.eq("numField",-10.0))
                .assertBsonDbResult(0)
                .assertCriteriaResult( Criteria.where("numField").is(-10.0));
    }
    @Test
    void shouldEvaluteExistsFunction(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("exists(stringField) = true")
                .assertBsonResult(Filters.exists("stringField",true))
                .assertBsonDbResult(3)
                .assertCriteriaResult(Criteria.where("stringField").exists(true));

        TestBuilder.build("exists(stringField) = false")
                .assertBsonResult(Filters.exists("stringField",false))
                .assertBsonDbResult(0)
                .assertCriteriaResult(Criteria.where("stringField").exists(false));
    }
    @Test
    void shouldEvaluteTypeFunction(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("type(stringField) = 'string'")
                .assertBsonResult(Filters.type("stringField","string"))
                .assertBsonDbResult(3)
                .assertCriteriaResult( Criteria.where("stringField").type(JsonSchemaObject.Type.of("string")));
    }

    @Test
    void shouldEvaluteSizeFunction(){
        collection.insertOne(new Document("title","array1").append("arrayField",Arrays.stream(new Integer[]{1,2,3}).collect(Collectors.toList())));
        collection.insertOne(new Document("title","array2").append("arrayField",Arrays.stream(new String[]{"a","b","c"}).collect(Collectors.toList())));

        TestBuilder.build("size(arrayField) = 3")
                .assertBsonResult(Filters.size("arrayField",3))
                .assertBsonDbResult(2)
                .assertCriteriaResult( Criteria.where("arrayField").size(3));
    }

    @Test
    void shouldEvaluateRegexFunction(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField like 'testString.*'")
                .assertBsonResult(Filters.regex("stringField","testString.*"))
                .assertBsonDbResult(3)
                .assertCriteriaResult(new Criteria("stringField").regex("testString.*"));
    }

    @Test
    void shouldEvaluateModFunction(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",21));

        TestBuilder.build("mod(numField,2) = 1")
                .assertBsonResult(Filters.mod("numField",2,1))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("numField").mod(2,1));
    }
    @Test
    void shouldEvaluateAndStatement() {
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField='testString' and numField=100")
                .assertBsonResult(Filters.and(Filters.eq("stringField","testString"),Filters.eq("numField",100)))
                .assertBsonDbResult(1)
                .assertCriteriaResult(new Criteria().andOperator(Criteria.where("stringField").is("testString"),(Criteria.where("numField").is(100))));

        TestBuilder.build("stringField='testString' and numField=99")
                .assertBsonResult(Filters.and(Filters.eq("stringField","testString"),Filters.eq("numField",99)))
                .assertBsonDbResult(0)
                .assertCriteriaResult(new Criteria().andOperator(Criteria.where("stringField").is("testString"),(Criteria.where("numField").is(99))));
    }

    @Test
    void shouldEvaluateOrStatement() {
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField='testString' or numField=0")
                .assertBsonResult(Filters.or(Filters.eq("stringField","testString"),Filters.eq("numField",0)))
                .assertBsonDbResult(3)
                .assertCriteriaResult(new Criteria().orOperator(Criteria.where("stringField").is("testString"),(Criteria.where("numField").is(0))));

        TestBuilder.build("stringField='testString2' or numField=99")
                .assertBsonResult(Filters.or(Filters.eq("stringField","testString2"),Filters.eq("numField",99)))
                .assertBsonDbResult(0)
                .assertCriteriaResult(new Criteria().orOperator(Criteria.where("stringField").is("testString2"),(Criteria.where("numField").is(99))));
    }
    @Test
    void shouldEvaluateNorStatement() {
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField='testString' nor numField=0")
                .assertBsonResult(Filters.nor(Filters.eq("stringField","testString"),Filters.eq("numField",0)))
                .assertBsonDbResult(0)
                .assertCriteriaResult(new Criteria().norOperator(Criteria.where("stringField").is("testString"),(Criteria.where("numField").is(0))));

        TestBuilder.build("stringField='testString' nor numField=99")
                .assertBsonResult(Filters.nor(Filters.eq("stringField","testString"),Filters.eq("numField",99)))
                .assertBsonDbResult(1)
                .assertCriteriaResult(new Criteria().norOperator(Criteria.where("stringField").is("testString"),(Criteria.where("numField").is(99))));
    }
    @Test
    void shouldEvaluateNotStatement(){
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("not(exists(stringField1) = true)")
                .assertBsonResult(Filters.not(Filters.exists("stringField1")))
                .assertBsonDbResult(3)
                .assertCriteriaResult(Criteria.where("stringField1").not().exists(true));

        TestBuilder.build("!(type(stringField) = 'string')")
                .assertBsonResult(Filters.not(Filters.type("stringField","string")))
                .assertBsonDbResult(0)
                .assertCriteriaResult(Criteria.where("stringField").not().type(JsonSchemaObject.Type.of("string")));
    }
    @Test
    void shouldEvaluateInOperator() {
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField in ('testString' 'testString3' 'testString2')")
                .assertBsonResult(Filters.in("stringField",new String[] {"testString","testString3","testString2"}))
                .assertBsonDbResult(3)
                .assertCriteriaResult(Criteria.where("stringField").in(new String[] {"testString","testString3","testString2"}));
    }

    @Test
    void shouldEvaluateNinOperator() {
        collection.insertOne(new Document("stringField","testString").append("numField",100));
        collection.insertOne(new Document("stringField","testString").append("numField",20));
        collection.insertOne(new Document("stringField","testString3").append("numField",0));

        TestBuilder.build("stringField nin ('testString' 'testString3' 'testString2')")
                .assertBsonResult(Filters.nin("stringField",new String[] {"testString","testString3","testString2"}))
                .assertBsonDbResult(0)
                .assertCriteriaResult(Criteria.where("stringField").nin(new String[] {"testString","testString3","testString2"}));
    }

    @Test
    void shouldEvaluateAllOperator() {
        collection.insertOne(new Document("title","array1").append("arrayField",Arrays.stream(new Integer[]{1,2,3}).collect(Collectors.toList())));
        collection.insertOne(new Document("title","array2").append("arrayField",Arrays.stream(new String[]{"a","b","c"}).collect(Collectors.toList())));

        TestBuilder.build("arrayField all ('a' 'b' 'c')")
                .assertBsonResult(Filters.all("arrayField",new String[] {"a","b","c"}))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("arrayField").all(new String[] {"a","b","c"}));

        TestBuilder.build("arrayField all (1 2 3)")
                .assertBsonResult(Filters.all("arrayField",new Integer[] {1,2,3}))
                .assertBsonDbResult(1)
                .assertCriteriaResult(Criteria.where("arrayField").all(new Integer[] {1,2,3}));
    }

    @Test
    void shouldEvaluateElemMatchOperator(){
        TestBuilder.build("x.y matches (c='b')")
                .assertBsonResult(Filters.elemMatch("x.y",Filters.eq("c","b")))
                .assertCriteriaResult(Criteria.where("x.y").elemMatch(Criteria.where("c").is("b")));

        TestBuilder.build("x.y matches (c='b' and d=1)")
                .assertBsonResult(Filters.elemMatch("x.y",
                        Filters.and(
                                Filters.eq("c","b"),
                                Filters.eq("d",1))
                        )
                )
                .assertCriteriaResult(Criteria.where("x.y").elemMatch(
                        new Criteria().andOperator(
                                Criteria.where("c").is("b"),
                                Criteria.where("d").is(1))
                        )
                );
    }
    @Test
    void shouldKeepLogicalPrecedence(){
        TestBuilder.build("x.y='z' or x.y='y' and x.z=99 or x.z=1200").assertBsonResult(
                Filters.or(
                        Filters.or(
                                Filters.eq("x.y","z"),
                                Filters.and(
                                        Filters.eq("x.y","y"),
                                        Filters.eq("x.z",99)
                                )
                        ),
                        Filters.eq("x.z",1200))
        ).assertCriteriaResult(
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

        TestBuilder.build("(x.y='z' or x.y='y') and (x.z=99 or x.z=1200)").assertBsonResult(
                Filters.and(
                        Filters.or(
                                Filters.eq("x.y","z"),
                                Filters.eq("x.y","y")
                        ),
                        Filters.or(
                                Filters.eq("x.z",99),
                                Filters.eq("x.z",1200)
                        )
                )
        ).assertCriteriaResult(
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

}