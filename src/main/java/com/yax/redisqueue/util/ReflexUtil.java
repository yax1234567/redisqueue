package com.yax.redisqueue.util;



import java.lang.reflect.Method;

/**
 * @author yax
 * @create 2019-04-02 9:27
 **/
public class ReflexUtil {
    /*public static String[] getParameterNmaes(Class clz, Method method,Class ... paramTypes) throws Exception {
        return getParameterNmaes(clz,method.getName(),paramTypes);
    }
    public static String[] getParameterNmaes(Class clz, String methodName,Class ... paramTypes ) throws Exception {
        ClassPool pool= ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(ReflexUtil.class));
        CtClass ct=pool.get(clz.getName());
        CtClass[] cCtClass=null;
        if(paramTypes!=null){
            int length=paramTypes.length;
            String[] classNames=new String[length];
            for(int i=0;i<length;i++){
                classNames[i]=paramTypes[i].getName();
            }
            cCtClass=pool.get(classNames);
        }
        CtMethod m;
        if(cCtClass==null){
            m=ct.getDeclaredMethod(methodName);
        }else{
            m=ct.getDeclaredMethod(methodName,cCtClass);
        }
        // 使用javaassist的反射方法获取方法的参数名
        MethodInfo methodInfo = m.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        String[] variableNames ;
        variableNames = new String[m.getParameterTypes().length];
        int staticIndex = Modifier.isStatic(m.getModifiers()) ? 0 : 1;
        for (int i = 0; i < variableNames.length; i++) {
            variableNames[i] = attr.variableName(i + staticIndex);
        }
        return variableNames;
    }*/
    public static Method getMethodByMethodName(Class clz,String methodName){
          Method[] methods= clz.getDeclaredMethods();
          for(Method method:methods){
              if(methodName.equals(method.getName())){
                  return method;
              }
          }
          return null;
    }
}
