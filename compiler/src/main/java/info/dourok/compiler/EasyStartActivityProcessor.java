package info.dourok.compiler;

import android.app.Activity;
import android.content.Intent;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.esactivity.EasyActivity;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.AbstractElementVisitor8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import sun.misc.Unsafe;

@SupportedAnnotationTypes("info.dourok.esactivity.EasyActivity")
@AutoService(Processor.class)
public class EasyStartActivityProcessor extends AbstractProcessor {
  private Filer filer;
  private Messager messager;
  private Types types;
  private Elements elements;

  @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    types = processingEnvironment.getTypeUtils();
    elements = processingEnvironment.getElementUtils();
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations,
      RoundEnvironment env) {

    TypeElement activity = elements.getTypeElement("android.app.Activity");
    for (Element element : env.getElementsAnnotatedWith(EasyActivity.class)) {
      if (element instanceof TypeElement) {
        if (types.isSubtype(element.asType(), activity.asType())) {
          handleEasyActivity((TypeElement) element);
        } else {
          warn("annotate "
              + EasyActivity.class.getSimpleName()
              + " to not Activity subclass make no sense!", element);
        }
      }
    }

    return false;
  }

  private void handleEasyActivity(TypeElement element) {
    try {

      TypeElement baseActivityBuilder =
          elements.getTypeElement("info.dourok.esactivity.BaseActivityBuilder");
      createBuilder(baseActivityBuilder, element);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createBuilder(TypeElement baseActivityBuilder,
      TypeElement element)
      throws IOException {
    PackageElement packageElement = elements.getPackageOf(element);
    ClassName builderClass =
        ClassName.get(packageElement.getQualifiedName().toString(),
            element.getSimpleName() + "Builder");

    element.accept(new AbstractElementVisitor8<Object, Void>() {
      @Override public Object visitPackage(PackageElement packageElement, Void o) {
        log("visitPackage");
        return null;
      }

      @Override public Object visitType(TypeElement typeElement, Void o) {
        log("visitType");
        return null;
      }

      @Override public Object visitVariable(VariableElement variableElement, Void o) {
        log("visitVariable");
        return null;
      }

      @Override public Object visitExecutable(ExecutableElement executableElement, Void o) {
        log("visitExecutable");
        return null;
      }

      @Override
      public Object visitTypeParameter(TypeParameterElement typeParameterElement, Void o) {
        log("visitTypeParameter");
        return null;
      }
    },null);

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addParameter(ClassName.get(Activity.class), "activity")
        .addStatement("super($L)", "activity")
        .addStatement("setIntent(new $T($L, $T.class))", Intent.class, "activity", element)
        .build();

    MethodSpec self = MethodSpec.methodBuilder("self")
        .addAnnotation(Override.class)
        .returns(builderClass)
        .addModifiers(Modifier.PROTECTED)
        .addStatement("return this")
        .build();

    TypeSpec builder = TypeSpec.classBuilder(builderClass)
        .superclass(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder), builderClass))
        .addMethod(constructor)
        .addMethod(self)
        .build();

    JavaFile.builder(packageElement.getQualifiedName().toString(), builder)
        .build().writeTo(filer);
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public void log(String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg);
  }

  public void log(String msg, Element element) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
  }

  public void warn(String msg) {
    messager.printMessage(Diagnostic.Kind.WARNING, msg);
  }

  public void warn(String msg, Element element) {
    messager.printMessage(Diagnostic.Kind.WARNING, msg, element);
  }
}
