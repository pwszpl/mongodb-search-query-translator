package mongo.search.transform;

import mongo.parser.Token;

public class TransformToken extends Token {
    private String functionBSON;
    private String functionCriteria;

    public TransformToken(int kind, String image, String function, String functionCrit) {
        super(kind, image);
        this.functionBSON = function;
        this.functionCriteria = functionCrit;
    }

    public String getFunctionBSON() {
        return functionBSON;
    }

    public String getFunctionCriteria() {
        return functionCriteria;
    }
}
