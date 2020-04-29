package cn.gowild.annotation.compilter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import cn.gowild.annotation.GowildSubscribe;

/**
 * Created by SimonSun on 2018/6/21.
 */

public class EventProxyInfo {

    public Map<String, ArrayList<ExecutableElement>> executeableElementMap = new HashMap<>();

    private Map<String,GowildSubscribe.FunctionType> functionTypeMap = new HashMap<>();
    private Elements mElementUtil;

    public EventProxyInfo(Elements elementUtil, TypeElement typeElement) {
        mElementUtil = elementUtil;
    }

    public void generateJavaCode(ProcessingEnvironment processingEnv,MethodSpec.Builder builder,ClassName methodInfoType) {


        Iterator<Map.Entry<String, ArrayList<ExecutableElement>>> iterator = executeableElementMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList<ExecutableElement>> entry = iterator.next();
            ArrayList<ExecutableElement> executableElementList = entry.getValue();

            String path = entry.getKey();

            String listName = path+"List";
            builder.addStatement("// path : "+toRaw(path));
            builder.addStatement(String.format(Locale.CHINESE,"ArrayList<%s> %s = new ArrayList()",methodInfoType.simpleName(),listName));

            String variableElementTypeNameTmp = null;
            for(ExecutableElement executableElement : executableElementList) {
                StringBuffer statementBuffer = new StringBuffer(String.format(Locale.CHINESE,"%s.add(",listName));

                statementBuffer.append("new ");
                statementBuffer.append(methodInfoType.simpleName());
                statementBuffer.append("(");

                TypeElement supperClassTypeElement = (TypeElement) executableElement.getEnclosingElement();
                statementBuffer.append(supperClassTypeElement.getQualifiedName());
                statementBuffer.append(".class.getMethod(\"");

                String methodName = executableElement.getSimpleName().toString();
                statementBuffer.append(methodName);
                statementBuffer.append("\",");

                // 函数非public修饰则不予以编译通过
                if (!executableElement.getModifiers().contains(Modifier.PUBLIC)){
                    builder.addStatement("function is not public .");
                }

                List<? extends VariableElement> paramsElement = executableElement.getParameters();


                StringBuffer paramsBuffer = new StringBuffer();
                for (VariableElement variableElement : paramsElement) {
                    String typeName = variableElement.asType().toString();

                    statementBuffer.append(typeName);
                    statementBuffer.append(".class,");
                    paramsBuffer.append(typeName);
                    paramsBuffer.append(".class,");
                }

                // 比较同一个路径的处理函数参数类型是否一致，参数类型不一致不予以编译通过
                if (variableElementTypeNameTmp == null){
                    variableElementTypeNameTmp = paramsBuffer.toString();
                }
                else if (!variableElementTypeNameTmp.equals(paramsBuffer.toString())){
                    builder.addStatement("params class type is inconsistency!!!");
                }

                if (paramsBuffer.length() > 0) {
                    paramsBuffer.deleteCharAt(paramsBuffer.length() - 1);
                }
                statementBuffer.deleteCharAt(statementBuffer.length() - 1);

                statementBuffer.append("),");
                statementBuffer.append(supperClassTypeElement.getQualifiedName());
                statementBuffer.append(".class,new Class[]{");
                statementBuffer.append(paramsBuffer);
                statementBuffer.append("},");
                statementBuffer.append(GowildSubscribe.FunctionType.class.getCanonicalName());
                statementBuffer.append(".");
                statementBuffer.append(functionTypeMap.get(methodName));
                statementBuffer.append("))");


                builder.addStatement(statementBuffer.toString());
            }
            builder.addStatement(String.format(Locale.CHINESE,"pathMap.put(\"%s\",%s)",path,listName));

        }



    }

    public void setFunctionType(String functionName,GowildSubscribe.FunctionType functionType){
        functionTypeMap.put(functionName,functionType);
    }

    private String toRaw(String acsiiPath){

        String[] items = acsiiPath.substring(1).split("_");
        StringBuffer buffer = new StringBuffer();
        for (String item : items){
            buffer.append((char) Integer.parseInt(item));
        }
        return buffer.toString();
    }

}
