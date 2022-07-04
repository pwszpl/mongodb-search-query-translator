package mongo.search.transform;

import mongo.parser.Token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static mongo.parser.MongoSearchEngineParserConstants.*;

public class TokenFactory  {
    public static Token newToken(int kind, String image) {
        switch (kind) {
            case EQ: return getCustomToken(kind,image,FunctionsMapping.EQ);
            case NE: return getCustomToken(kind,image,FunctionsMapping.NE);
            case AND: return getCustomToken(kind,image,FunctionsMapping.AND);
            case OR: return getCustomToken(kind,image,FunctionsMapping.OR);
            case GT: return getCustomToken(kind,image,FunctionsMapping.GT);
            case LT: return getCustomToken(kind,image,FunctionsMapping.LT);
            case LE: return getCustomToken(kind,image,FunctionsMapping.LE);
            case GE: return getCustomToken(kind,image,FunctionsMapping.GE);
            case IN: return getCustomToken(kind,image,FunctionsMapping.IN);
            case NIN: return getCustomToken(kind,image,FunctionsMapping.NIN);
            case EXISTS_FUNCTION: return getCustomToken(kind,image,FunctionsMapping.EXISTS);
            case TYPE_FUNCTION: return getCustomToken(kind,image,FunctionsMapping.TYPE);
            case SIZE_FUNCTION: return getCustomToken(kind,image,FunctionsMapping.SIZE);
            case NOR: return getCustomToken(kind,image,FunctionsMapping.NOR);
            case LIKE: return getCustomToken(kind,image,FunctionsMapping.LIKE);
            case MOD_FUNCTION: return getCustomToken(kind,image,FunctionsMapping.MOD);
            default: return new Token(kind,image);
        }
    }

    private static TransformToken getCustomToken(int kind, String image, FunctionsMapping mapping){
        return new TransformToken(kind,image,mapping.getFiltersFunction(),mapping.getCriteraFunction());
    }

}

