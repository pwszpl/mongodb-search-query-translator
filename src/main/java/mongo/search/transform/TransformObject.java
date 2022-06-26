package mongo.search.transform;

import mongo.parser.Token;

import java.util.List;

public class TransformObject {
    private TransformToken functionalToken;
    private List<Object> params;

    public TransformObject(Token token, List<Object> params){
        if(token instanceof TransformToken){
            this.functionalToken = (TransformToken)token;
        } else {
            throw new RuntimeException(String.format("Token '%s' is not supported for transform operation.", token.image));
        }
        this.params = params;
    }

    public List<Object> getParams() {
        return params;
    }

    public TransformToken getFunctionalToken() {
        return functionalToken;
    }
}
