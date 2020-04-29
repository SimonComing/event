package cn.gowild.annotation.compilter;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileManager;

import cn.gowild.annotation.GowildSubscribe;

/**
 * Created by SimonSun on 2018/6/21.
 */
@AutoService(Processor.class)
public class GowildEventProcessor extends AbstractProcessor {

    // 跟文件相关的辅助类
    private Filer mFiler;
    // 跟元素相关的辅助类
    private Elements mElements;
    // 跟日志相关的辅助类
    private Messager mMessager;

    private Map<String, EventProxyInfo> mProxyInfoMap = new HashMap<>();

    private String mModuleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();

        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            mModuleName = options.get("moduleName");
        }
        if (mModuleName != null && !mModuleName.isEmpty()) {
            mModuleName = mModuleName.replaceAll("[^0-9a-zA-Z_]+", "");
        }else {
            throw new RuntimeException("there a module have not option of 'moduleName'");
        }

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(GowildSubscribe.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyInfoMap.clear();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GowildSubscribe.class);
        if (elements.isEmpty()) {
            return false;
        }

        for (Element element : elements) {

            if (!checkAnnotationVarid(element)) {
                return false;
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
            String qlName = typeElement.getQualifiedName().toString();

            GowildSubscribe subscribe = element.getAnnotation(GowildSubscribe.class);
            String[] subscribePath = subscribe.eventPath();
            GowildSubscribe.FunctionType functionType = subscribe.functionType();

            for (String path : subscribePath) {
                path = toAscii(path);
                EventProxyInfo proxyInfo = mProxyInfoMap.get(path);

                if (proxyInfo == null) {
                    proxyInfo = new EventProxyInfo(mElements, typeElement);
                    mProxyInfoMap.put(path, proxyInfo);
                }


                ArrayList<ExecutableElement> executableElementArrayList = proxyInfo.executeableElementMap.get(path);
                if (executableElementArrayList == null) {
                    executableElementArrayList = new ArrayList<>();
                }
                executableElementArrayList.add(executableElement);

                proxyInfo.setFunctionType(executableElement.getSimpleName().toString(), functionType);

                proxyInfo.executeableElementMap.put(path, executableElementArrayList);

            }
        }

        createHeader();
        for (String key : mProxyInfoMap.keySet()) {
            EventProxyInfo proxyInfo = mProxyInfoMap.get(key);
            proxyInfo.generateJavaCode(processingEnv, loadPathMethodBuilder, methodInfoType);
        }
        createEnd();

        return false;
    }

    private boolean checkAnnotationVarid(Element element) {

        if (element.getKind() != ElementKind.METHOD) {
            return false;
        }

        if (element.getModifiers().contains("private")) {
            return false;
        }

        return true;
    }

    private ClassName methodInfoType;
    private MethodSpec.Builder loadPathMethodBuilder;

    private void createHeader() {
        ClassName mapType = ClassName.get("java.util", "HashMap");
        ClassName strType = ClassName.get("java.lang", "String");
        ClassName listType = ClassName.get("java.util", "ArrayList");
        methodInfoType = ClassName.get("cn.gowild.api", "MethodInfo");
        ClassName exceptType = ClassName.get("java.lang", "Exception");
        TypeName methodInfoListType = ParameterizedTypeName.get(listType, methodInfoType);
        TypeName methodInfoMapType = ParameterizedTypeName.get(mapType, strType, methodInfoListType);

        loadPathMethodBuilder = MethodSpec.methodBuilder("loadEventPath")
                // 添加方法修饰词
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addException(exceptType)
                // 回类型U
                .returns(methodInfoMapType)
                .addStatement("$T pathMap = new $T<>()", methodInfoMapType, mapType);

    }

    private void createEnd() {
        loadPathMethodBuilder.addStatement("return pathMap");

        MethodSpec methodSpec = loadPathMethodBuilder.build();

        TypeSpec typeSpec = TypeSpec.classBuilder("EventPathCenter$"+mModuleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(methodSpec)
                .build();

        try {
            JavaFile javaFile = JavaFile.builder("cn.gowild.api", typeSpec).build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toAscii(String raw) {
        char[] chars = raw.toCharArray();
        StringBuffer buffer = new StringBuffer("g");
        for (char c : chars) {
            buffer.append((int) c + "_");
        }
        return buffer.toString();
    }

}
