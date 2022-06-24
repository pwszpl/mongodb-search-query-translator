package mongo.search.util;

import mongo.parser.Token;

public class StringUtil {
    public static Token unescape(Token token ) {
        if(token.image.startsWith("'")) return new Token(token.kind,token.image.substring(1,token.image.length()-1));
        else return token;
    }
}
