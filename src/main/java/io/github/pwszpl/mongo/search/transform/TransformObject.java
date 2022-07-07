package io.github.pwszpl.mongo.search.transform;

import io.github.pwszpl.mongo.parser.MongoSearchEngineParserConstants;
import io.github.pwszpl.mongo.parser.Token;
import io.github.pwszpl.mongo.search.util.StringUtil;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransformObject {
    private TransformToken functionalToken;
    private List<Object> params;

    public TransformObject(Token token, Object... params){
        this(token, Arrays.stream(params).collect(Collectors.toList()));
    }

    public TransformObject(Token token, List<Object> params){
        if(token instanceof TransformToken){
            this.functionalToken = (TransformToken)token;
        } else {
            throw new RuntimeException(String.format("Token '%s' is not supported for transform operation.", token.image));
        }
        this.params = mapList(params);
    }

    public List<Object> getParams() {
        return params;
    }

    public TransformToken getFunctionalToken() {
        return functionalToken;
    }

    private Object mapObject(Object object) {
        if(object instanceof Token){
            Token token = (Token)object;
            switch(token.kind){
                case MongoSearchEngineParserConstants.STRING_LITERAL:
                    return StringUtil.unescape(token).image;
                case MongoSearchEngineParserConstants.R_NUMBER:
                    return Double.valueOf(token.image);
                case MongoSearchEngineParserConstants.INT_NUMBER:
                    return Integer.valueOf(token.image);
                case MongoSearchEngineParserConstants.BOOLEAN:
                    boolean val = Boolean.valueOf(token.image).booleanValue();
                    return val;
                case MongoSearchEngineParserConstants.DATE:
                    // filling to match ISO format
                    return Date.from(Instant.parse(token.image+"T00:00:00.00Z"));
                case MongoSearchEngineParserConstants.TIMESTAMP:
                    // filling to match ISO format
                    return Date.from(Instant.parse(token.image+".00Z"));
                case MongoSearchEngineParserConstants.OBJ_FIELD:
                    return token.image;
            }
        }
        if(object instanceof List && ((List)object).get(0).getClass().isAssignableFrom(Token.class)){
            return mapList((List<Object>) object);
        }
        return object;
    }

    private List<Object> mapList(List<Object> objectList){
        return objectList.stream().map(this::mapObject).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
