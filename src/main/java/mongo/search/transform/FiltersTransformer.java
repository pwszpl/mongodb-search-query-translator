package mongo.search.transform;

import com.mongodb.client.model.Filters;
import mongo.search.util.ReflectionUtil;
import mongo.search.util.StringUtil;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FiltersTransformer implements Function {
    public static Class filtersClass;
    public static Map<String, List<Method>> filtersMethods;

    static {
        try {
            filtersClass = Class.forName("com.mongodb.client.model.Filters");
            filtersMethods = Arrays.stream(filtersClass.getDeclaredMethods()).collect(Collectors.groupingBy(Method::getName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object apply(Object o) {
        TransformObject transform = (TransformObject) o;
        String function = transform.getFunctionalToken().getFunctionBSON();
        List<Object> functionParam = new ArrayList<>();

        // Create an array of Filters for and/or methods and add as an arguemnt to method invoke
        if(StringUtil.isStringInList(function,"and","or","nor")){
            List<Object> operatorArgument = new ArrayList<>();
            for(int i=0;i<transform.getParams().size();i++){
                operatorArgument.add(transform.getParams().get(i));
            }
            functionParam.add(operatorArgument);
        }
        else if(StringUtil.isStringInList(function,"elemMatch")){
            List elemList = (List)transform.getParams().get(1);
            Bson filter;
            if(elemList.size() > 1) filter = Filters.and((List)transform.getParams().get(1));
            else filter = (Bson) elemList.get(0);

            functionParam.add(transform.getParams().get(0));
            functionParam.add(filter);

        }
        else if(ReflectionUtil.isNotOperator(transform.getParams().get(0))){
            // if second argument is not operator we create new Transform object (without not operator) pass it through apply function to get result
            // and finally apply not globally
            TransformObject transformObject = new TransformObject(transform.getFunctionalToken(),transform.getParams().subList(1,transform.getParams().size()));
            Object res = apply(transformObject);
            return Filters.not((Bson) res);
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
