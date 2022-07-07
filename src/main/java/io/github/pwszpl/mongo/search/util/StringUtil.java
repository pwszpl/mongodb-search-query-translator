package io.github.pwszpl.mongo.search.util;

import io.github.pwszpl.mongo.parser.Token;

import java.util.Arrays;

public class StringUtil {
    public static Token unescape(Token token ) {
        if(token.image.startsWith("'")) return new Token(token.kind,token.image.substring(1,token.image.length()-1));
        else return token;
    }

    public static boolean isStringInList(String s, String... comp){
        return Arrays.asList(comp).contains(s);
    }
}
