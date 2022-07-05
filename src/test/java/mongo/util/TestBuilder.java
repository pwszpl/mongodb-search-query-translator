package mongo.util;

import com.mongodb.client.MongoCollection;
import mongo.parser.MongoSearchEngineParser;
import mongo.parser.ParseException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TestBuilder {
    String query;
    static MongoCollection<Document> collection;

    private TestBuilder(String q){
        query = q;
    }

    public static TestBuilder build(String q){
        return new TestBuilder(q);
    }

    public static void setCollection(MongoCollection<Document> c){
        collection = c;
    }

    public TestBuilder assertBsonResult(Bson expected){
        MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(query));
        try {
            Bson result = (Bson) parser.parse();
            Assertions.assertEquals(expected.toBsonDocument().toJson(),result.toBsonDocument().toJson());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public TestBuilder assertCriteriaResult(Criteria expected){
        MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(query));
        parser.setCriteraMode();
        try {
            Criteria c = (Criteria)parser.parse();
            Assertions.assertEquals(expected.getCriteriaObject().toBsonDocument().toJson(),c.getCriteriaObject().toBsonDocument().toJson());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public TestBuilder assertBsonDbResult(int expectedDbCount){
        MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(query));
        Bson result = null;
        try {
            result = (Bson) parser.parse();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<Document> list = new ArrayList<>();
        collection.find(result).forEach(e -> list.add(e));
        Assertions.assertEquals(expectedDbCount,list.size());
        return this;
    }
}
