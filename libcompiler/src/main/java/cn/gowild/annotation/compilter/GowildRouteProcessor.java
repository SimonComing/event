package cn.gowild.annotation.compilter;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import cn.gowild.annotation.GowildRoute;

/**
 * Created by SimonSun on 2018/6/21.
 */
@AutoService(Processor.class)
public class GowildRouteProcessor extends AbstractProcessor {

    // 跟文件相关的辅助类
    private Filer mFiler;
    // 跟元素相关的辅助类
    private Elements mElements;
    // 跟日志相关的辅助类
    private Messager mMessager;

    private javax.lang.model.util.Types mTypeUtils;

    private final String ISERVICE_TYPE_NAME = "com.gowild.eremite.abstracts.IService";
    private final String EREACTIVITY_TYPE_NAME = "com.gowild.eremite.app.EreActivity";

    // IService 集合
    private Map<String, RouteProxyInfo> mServiceProxyInfoMap = new HashMap<>();
    // EreActivity 集合
    private Map<String, RouteProxyInfo> mEreActivityProxyInfoMap = new HashMap<>();

    private String mModuleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
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
        annotationTypes.add(GowildRoute.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mEreActivityProxyInfoMap.clear();
        mServiceProxyInfoMap.clear();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GowildRoute.class);
        for (Element element : elements) {

            if (!checkAnnotationVarid(element)) {
                return false;
            }

            TypeElement typeElement = (TypeElement) element;
            boolean isIServiceImp = checkIsService(typeElement);
            /*List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            for (TypeMirror typeMirror : interfaces) {
                String type = typeMirror.toString();
                if (ISERVICE_TYPE_NAME.equals(type)) {
                    isIServiceImp = true;
                    break;
                }
                TypeElement superTypeElement = mElements.getTypeElement(type);
                List<? extends TypeMirror> superInterfaces = superTypeElement.getInterfaces();

                for (TypeMirror superTypeMirror : superInterfaces){

                }
            }*/

            // 非IService实现类，再来检查是否为EreActivity子类
            boolean isEreActivitySubclass = false;
            if (!isIServiceImp) {

                TypeElement typeElementTmp = typeElement;
                boolean isContinue = true;
                do {

                    TypeMirror superClassType = typeElementTmp.getSuperclass();
                    if (EREACTIVITY_TYPE_NAME.equals(superClassType.toString())) {
                        isEreActivitySubclass = true;
                        isContinue = false;
                    }
                    typeElementTmp = (TypeElement) mTypeUtils.asElement(superClassType);
                } while (isContinue);

                if (!isEreActivitySubclass) {
                    continue;
                }
            }

            GowildRoute route = element.getAnnotation(GowildRoute.class);
            String action = route.action();
            String alias = route.alias();
            int lanchLevel = route.launchLevel();

            // IService必须有别名
            if (isIServiceImp && alias.isEmpty()) {
                throw new RuntimeException("IService miss alias.");
            }

            Map<String, RouteProxyInfo> infoMap = isIServiceImp ? mServiceProxyInfoMap : mEreActivityProxyInfoMap;

            RouteProxyInfo proxyInfo = infoMap.get(action);
            if (proxyInfo == null) {
                proxyInfo = new RouteProxyInfo(typeElement.getQualifiedName().toString(), typeElement.getSimpleName().toString
                        (), action, alias, lanchLevel);
                infoMap.put(action, proxyInfo);
            } else {
                throw new RuntimeException("Path repetition");
            }

        }

        // 创建加载服务路径方法
        ClassName routeClassType = ClassName.get("com.gowild.eremite.abstracts", "IService");
        createMethodHeader("loadIServiceRoutePath", routeClassType);
        for (String key : mServiceProxyInfoMap.keySet()) {
            RouteProxyInfo routeProxyInfo = mServiceProxyInfoMap.get(key);
            routeProxyInfo.generateIServiceJavaCode(constantBuilders, loadPathMethodBuilder);
        }
        MethodSpec loadIServiceMethod = createMethodEnd();

        // 创建EreActivity路径方法
        routeClassType = ClassName.get("com.gowild.eremite.am", "EreActivityInfo");
        createMethodHeader("loadEreActivityRoutePath", routeClassType);
        for (String key : mEreActivityProxyInfoMap.keySet()) {
            RouteProxyInfo routeProxyInfo = mEreActivityProxyInfoMap.get(key);
            routeProxyInfo.generateEreActivityJavaCode(constantBuilders,loadPathMethodBuilder);
        }
        MethodSpec loadEreActivityMethod = createMethodEnd();

        writeJavaCode(loadIServiceMethod, loadEreActivityMethod);

        return false;
    }

    private boolean checkIsService(TypeElement typeElement){
        boolean isService = false;
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            String type = typeMirror.toString();
            if (ISERVICE_TYPE_NAME.equals(type)) {
                isService = true;
                break;
            }
            TypeElement superTypeElement = mElements.getTypeElement(type);
            isService = checkIsService(superTypeElement);
            if (isService){
                break;
            }
        }
        return isService;
    }

    private boolean checkAnnotationVarid(Element element) {

        if (element.getKind() != ElementKind.CLASS) {
            return false;
        }

        if (element.getModifiers().contains("private")) {
            return false;
        }

        return true;
    }

    private MethodSpec.Builder loadPathMethodBuilder;
    private ArrayList<FieldSpec> constantBuilders = new ArrayList<>();

    private void createMethodHeader(String methodName, ClassName routeClassType) {
        ClassName mapType = ClassName.get("java.util", "HashMap");
        ClassName strType = ClassName.get("java.lang", "String");
        ClassName exceptType = ClassName.get("java.lang", "Exception");
        ClassName contextType = ClassName.get("android.content", "Context");

        TypeName methodInfoMapType = ParameterizedTypeName.get(mapType, strType, routeClassType);

        loadPathMethodBuilder = MethodSpec.methodBuilder(methodName)
                // 添加方法修饰词
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(contextType, "context")
                .addException(exceptType)
                // 返回类型
                .returns(methodInfoMapType)
                .addStatement("$T pathMap = new $T<>()", methodInfoMapType, mapType);

    }

    private MethodSpec createMethodEnd() {
        loadPathMethodBuilder.addStatement("return pathMap");

        MethodSpec methodSpec = loadPathMethodBuilder.build();

        return methodSpec;
    }

    private void writeJavaCode(MethodSpec methodSpec, MethodSpec methodSpec2) {

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder("RouteActionCenter$"+mModuleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(methodSpec)
                .addMethod(methodSpec2);
        for (FieldSpec fieldSpec : constantBuilders) {
            typeSpecBuilder.addField(fieldSpec);
        }
        TypeSpec typeSpec = typeSpecBuilder.build();

        try {
            JavaFile javaFile = JavaFile.builder("cn.gowild.api", typeSpec).build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
