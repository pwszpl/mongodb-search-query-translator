package mongo.search.transform;

import mongo.parser.Token;

import static mongo.parser.MongoSearchEngineParserConstants.*;

public class TokenFactory  {
    public static Token newToken(int kind, String image) {
        switch (kind) {
            case EQ: return getCustomToken(kind,image,"eq");
            case NE: return getCustomToken(kind,image,"ne");
            case AND: return getCustomToken(kind,image,"and");
            case OR: return getCustomToken(kind,image,"or");
            case GT: return getCustomToken(kind,image,"gt");
            case LT: return getCustomToken(kind,image,"lt");
            case LE: return getCustomToken(kind,image,"lte");
            case GE: return getCustomToken(kind,image,"gte");
            default: return new Token(kind,image);
        }
    }

    private static TransformToken getCustomToken(int kind, String image, String function){
        return new TransformToken(kind,image,function);
    }
}

