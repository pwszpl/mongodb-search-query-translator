package mongo.search.transform;

import mongo.parser.Token;

import java.util.List;

public class TransformObject {
    public String functionName;
    public List<Object> params;

    public TransformObject(Token token, List<Object> params){
        if(token instanceof TransformToken){
            this.functionName = ((TransformToken) token).getFunction();
        } else {
            throw new RuntimeException(String.format("Token '%s' is not supported for transform operation.", token.image));
        }
        this.params = params;
    }
}
