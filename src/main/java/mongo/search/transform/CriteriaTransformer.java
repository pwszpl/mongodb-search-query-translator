package mongo.search.transform;

import mongo.parser.Token;
import mongo.search.util.ReflectionUtil;
import mongo.search.util.StringUtil;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements function interface to create Criteria objects from MongoSearchEngine tokens.
 */
public class CriteriaTransformer implements Function {
    public static Class filtersClass;
    public static Map<String, List<Method>> filtersMethods;

    public CriteriaTransformer(){
        if(filtersMethods == null){
            try {
                filtersClass = Class.forName("org.springframework.data.mongodb.core.query.Criteria");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            filtersMethods = Arrays.stream(filtersClass.getDeclaredMethods()).collect(Collectors.groupingBy(Method::getName));
        }
    }

    @Override
    public Object apply(Object o) {
        TransformObject transform = (TransformObject) o;
        String function = transform.getFunctionalToken().getFunctionCriteria();
        List<Object> functionParam = new ArrayList<>();

        if(StringUtil.isStringInList(function,"is","ne","gt","gte","lt","lte","in","nin","all")
                && (transform.getParams().get(0) instanceof String)){
            functionParam.add(Criteria.where((String) transform.getParams().get(0)));
            // Create an initial Critera condition for field = value comparision
            for(int i=1;i<transform.getParams().size();i++){
                functionParam.add(transform.getParams().get(i));
            }
        }
        else if(StringUtil.isStringInList(function,"exists","size","regex","mod")){
            Criteria where = null;
            int startNum = 0;
            if(ReflectionUtil.isNotOperator(transform.getParams().get(0))){
                where = Criteria.where((String) transform.getParams().get(1)).not();
                startNum = 2;
            } else {
                where = Criteria.where((String) transform.getParams().get(0));
                startNum = 1;
            }
            functionParam.add(where);
            for(int i=startNum;i<transform.getParams().size();i++){
                functionParam.add(transform.getParams().get(i));
            }
        }
        else if(StringUtil.isStringInList(function,"type")){
            //type function expects list as second argument, so we create it before passing for execution
            Criteria where = null;
            int startNum = 0;
            if(ReflectionUtil.isNotOperator(transform.getParams().get(0))){
                where = Criteria.where((String) transform.getParams().get(1)).not();
                startNum = 2;
            } else {
                where = Criteria.where((String) transform.getParams().get(0));
                startNum = 1;
            }
            functionParam.add(where);
            ArrayList types = new ArrayList();
            types.add(JsonSchemaObject.Type.of((String) transform.getParams().get(startNum)));
            functionParam.add(types);
        }
        else if(StringUtil.isStringInList(function,"elemMatch")){
            List elemList = (List)transform.getParams().get(1);
            Criteria c = null;
            if(elemList.size()>1) c = new Criteria().andOperator(elemList);
            else c = (Criteria) elemList.get(0);

            functionParam.add(Criteria.where((String) transform.getParams().get(0)));
            functionParam.add(c);
        }
        else if(StringUtil.isStringInList(function,"andOperator","orOperator","norOperator")){
            // Create an array of Critera for andOperator/orOperator
            Criteria c = new Criteria();
            functionParam.add(c);
            List<Object> criteria = new ArrayList<>();
            for(int i=0;i<transform.getParams().size();i++){
                criteria.add(transform.getParams().get(i));
            }
            functionParam.add(criteria);
        }
        // otherwise just add all parameters to method invoke
        else {
            for(int i=0;i<transform.getParams().size();i++){
                functionParam.add(transform.getParams().get(i));
            }
        }

        return ReflectionUtil.findAppropriateMethodAndCall(function,functionParam,filtersMethods);
    }
}
