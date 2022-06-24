package mongo.search.transform;

import mongo.parser.Token;

public class TransformToken extends Token {
    private String function;

    public TransformToken(int kind, String image, String function) {
        super(kind, image);
        this.function = function;
    }

    public String getFunction() {
        return function;
    }
}
