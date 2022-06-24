package mongo.search.transform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FiltersConsumer implements Function {
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
        List<Method> mList = filtersMethods.get(transform.functionName);
        Method method = null;
        for(Method m : mList){
            if(m.getParameterCount() == transform.params.size()){
                Class<?>[] varList = m.getParameterTypes();
                boolean valid = true;
                for(int i=0; i< varList.length;i++){
                    Class targetClass = varList[i];
                    Class sourceClass = transform.params.get(i).getClass();
                    if(!targetClass.isAssignableFrom(sourceClass)) valid=false;
                }
                if(valid) method = m;
            }
        }
        if(method == null){
            throw new RuntimeException("Couldn't find acceptable transformation for input object.");
        }
        try {
            return method.invoke(null,transform.params.toArray());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
