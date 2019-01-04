package com.yong.cugyong.processor;

import com.google.auto.service.AutoService;
import com.yong.cugyong.annotation.AnnotationTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class) // 自动为ProcessorTest注册，生成META-INF文件
public class ProcessorTest extends AbstractProcessor{

    // 打印日志
    private Messager mMessager;
    // 生成java文件的工具类
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotationTypes = new HashSet<>();
        supportAnnotationTypes.add(AnnotationTest.class.getCanonicalName());
        return supportAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        mMessager.printMessage(Diagnostic.Kind.NOTE, "process start");
        Map<String, List<String>> collectInfos = new HashMap<>();
        for (TypeElement annotation: annotations){
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            for (Element element: elements){
                if (!checkValid(element)){
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "checkValid not pass");
                    return false;
                }else {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    // 获取被注解的方法所在的类
                    TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
                    // 获取类的全名，包括包名
                    String classFullName = typeElement.getQualifiedName().toString();
                    // 被注解的方法的名字
                    String methodName = executableElement.getSimpleName().toString();
                    List<String> methods = collectInfos.get(classFullName);
                    if (methods == null){
                        methods = new ArrayList<>();
                        collectInfos.put(classFullName, methods);
                    }
                    methods.add(methodName);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry: collectInfos.entrySet()){
            mMessager.printMessage(Diagnostic.Kind.NOTE, entry.getKey());
            createJavaFile(entry.getKey(), entry.getValue());
        }

        return true;
    }


    private boolean checkValid(Element element){
        if (!(element instanceof ExecutableElement)){ // element是否是方法类型
            mMessager.printMessage(Diagnostic.Kind.ERROR, "we should find methods");
            return false;
        }else {
            ExecutableElement executableElement = (ExecutableElement) element;
            // 注解是否是AnnotationTest类型
            Annotation annotationTest = executableElement.getAnnotation(AnnotationTest.class);
            if (annotationTest == null){
                return false;
            }else {
                // 方法必须是public、没有参数
                Set<Modifier> modifiers = executableElement.getModifiers();
                if (!modifiers.contains(Modifier.PUBLIC)){
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "method should public");
                    return false;
                }
                List<? extends VariableElement> params = executableElement.getParameters();
                if (params != null && !params.isEmpty()){
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "method params should empty");
                    return false;
                }
            }
        }
        return true;
    }

    private void createJavaFile(String className, List<String> methods){
        BufferedWriter writer = null;
        try {
            JavaFileObject sourceFile = mFiler.createSourceFile(className + "$$Proxy");
            int period = className.lastIndexOf('.');
            String myPackage = period > 0 ? className.substring(0, period) : null;
            String clazz = className.substring(period + 1);
            writer = new BufferedWriter(sourceFile.openWriter());
            if (myPackage != null) {
                writer.write("package " + myPackage + ";\n\n");
            }
            writer.write("import com.yong.cugyong.testlib.ProxyMethod;\n\n");
            writer.write("public class " + clazz + "$$Proxy implements ProxyMethod<" + clazz + ">{\n");
            writer.write("@Override\n");
            writer.write("public void proxy(" + clazz + " source){\n");
            for (String methodName: methods){
                writer.write("source." + methodName + "();");
            }
            writer.write("}\n");
            writer.write("}");
        }catch (IOException e){
            mMessager.printMessage(Diagnostic.Kind.ERROR, "create file error");
            e.printStackTrace();
        }finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    //Silent
                }
            }
        }

    }
}
