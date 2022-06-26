package mongo.search.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class ReflectionUtil {
    public static Object findAppropriateMethodAndCall(String methodName, List<Object> params, Map<String,List<Method>> methods){
        List<Method> mList = methods.get(methodName);
        Method method = null;
        for(Method m : mList){
            boolean valid = isValidCandidate(m,params);
            if(valid) {
                method = m;
                break;
            }
        }
        if(method == null){
            throw new RuntimeException("Couldn't find acceptable transformation for input object.");
        }
        return invokeMethod(method,params);
    }

    private static Object invokeMethod(Method m,List<Object> params){
        Object invokingObject = null;
        List<Object> invokingParams = null;
        if(isMethodStatic(m)){
            invokingParams = params;
        }   else {
            invokingObject = params.get(0);
            invokingParams = params.subList(1,params.size());
        }

        try {
            return m.invoke(invokingObject,invokingParams.toArray());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValidCandidate(Method m, List<Object> params) {
        if(isMethodStatic(m)){
            if(m.getParameterCount() != params.size()) return false;

            Class<?>[] varList = m.getParameterTypes();
            for(int i=0; i< varList.length;i++){
                Class targetClass = varList[i];
                Class sourceClass = params.get(i).getClass();
                if(!targetClass.isAssignableFrom(sourceClass)) return false;
            }
        } else {
            if(!m.getDeclaringClass().isAssignableFrom(params.get(0).getClass())) return false;
            if(m.getParameterCount() != params.size()-1) return false;

            Class<?>[] varList = m.getParameterTypes();
            for(int i=0; i< varList.length;i++){
                Class targetClass = varList[i];
                Class sourceClass = params.get(i+1).getClass();
                if(!targetClass.isAssignableFrom(sourceClass)) return false;
            }
        }

        return true;
    }

    public static boolean isMethodStatic(Method m){
        return Modifier.isStatic(m.getModifiers());
    }
}
