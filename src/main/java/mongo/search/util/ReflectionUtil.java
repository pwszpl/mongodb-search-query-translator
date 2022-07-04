package mongo.search.util;

import mongo.parser.Token;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static mongo.parser.MongoSearchEngineParserConstants.NOT;

public class ReflectionUtil {
    private static final Map<Class<?>, Class<?>> primitiveMap = new HashMap<>();
    private static final Map<String, Function> mappers = new HashMap<>();
    static {
        primitiveMap.put(boolean.class, Boolean.class);
        primitiveMap.put(byte.class, Byte.class);
        primitiveMap.put(char.class, Character.class);
        primitiveMap.put(double.class, Double.class);
        primitiveMap.put(float.class, Float.class);
        primitiveMap.put(int.class, Integer.class);
        primitiveMap.put(long.class, Long.class);
        primitiveMap.put(short.class, Short.class);

        mappers.put(getMappersKey(Integer.class,Long.class), x -> Integer.toUnsignedLong((int)x));
        mappers.put(getMappersKey(Integer.class,long.class), x -> Integer.toUnsignedLong((int)x));
    }
;

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

    private static List<Object> castParamsIfNeeded(Method method, List<Object> params) {
        List<Object> nList = new ArrayList<>();
        Class<?>[] varList = method.getParameterTypes();
        for(int i=0;i<varList.length;i++){
            Class sourceClass = varList[i];
            Class targetClass = params.get(i).getClass();
            Function mappingFunction = mappers.get(getMappersKey(sourceClass,targetClass));
            Object newObj = null;
            if(mappingFunction != null){
                newObj = mappingFunction.apply(params.get(i));
            } else {
                newObj = params.get(i);
            }
            nList.add(newObj);
        }
        return nList;
    }

    public static boolean isNotOperator(Object o){
        if(o instanceof Token && ((Token)o).kind == NOT) return true;
        else return false;
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
            List params2 = castParamsIfNeeded(m,invokingParams);
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

            boolean areParametersCompatible = checkParametersCompatibility(m,params);
            if(!areParametersCompatible) return false;
        } else {
            if(!m.getDeclaringClass().isAssignableFrom(params.get(0).getClass())) return false;
            if(m.getParameterCount() != params.size()-1) return false;

            boolean areParametersCompatible = checkParametersCompatibility(m,params.subList(1, params.size()));
            if(!areParametersCompatible) return false;
        }

        return true;
    }

    private static boolean checkParametersCompatibility(Method m, List<Object> params){
        Class<?>[] varList = m.getParameterTypes();
        boolean compatibility = true;
        for(int i=0; i< varList.length;i++){
            Class targetClass = varList[i];
            Class sourceClass = params.get(i).getClass();
            if(targetClass.isAssignableFrom(sourceClass)) continue;
            if(targetClass.isPrimitive() && primitiveMap.get(targetClass).equals(sourceClass)) continue;
            if(mappers.get(getMappersKey(sourceClass,targetClass)) != null) continue;
            compatibility = false;
            if(!compatibility) break;
        }
        return compatibility;
    }

    private static String getMappersKey(Class source, Class target){
        return source.getName() + "->" + target.getName();
    }

    public static boolean isMethodStatic(Method m){
        return Modifier.isStatic(m.getModifiers());
    }
}
