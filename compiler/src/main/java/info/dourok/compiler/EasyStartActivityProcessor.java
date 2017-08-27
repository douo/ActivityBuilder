package info.dourok.compiler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.EasyActivity;
import java.io.IOException;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

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
      log(element.getClass().getCanonicalName());
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
      PackageElement packageElement = elements.getPackageOf(element);

      TypeSpec.Builder builder = createBuilder(baseActivityBuilder, packageElement, element);
      TypeSpec.Builder helper =
          TypeSpec.classBuilder(ClassName.get(packageElement.getQualifiedName().toString(),
              element.getSimpleName() + "Helper"));

      MethodSpec.Builder helperInit = MethodSpec.methodBuilder("init")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(element), "activity")
          .addStatement("$T intent =new $T()", Intent.class, Intent.class);

      MethodSpec.Builder helperRestore = MethodSpec.methodBuilder("init")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(element), "activity")
          .addParameter(Bundle.class, "savedInstanceState");

      MethodSpec.Builder helperSave = MethodSpec.methodBuilder("init")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(element), "activity")
          .addParameter(Bundle.class, "savedInstanceState");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private TypeSpec.Builder createBuilder(TypeElement baseActivityBuilder,
      PackageElement packageElement,
      TypeElement element)
      throws IOException {

    ClassName builderClass =
        ClassName.get(packageElement.getQualifiedName().toString(),
            element.getSimpleName() + "Builder");

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
    return TypeSpec.classBuilder(builderClass)
        .superclass(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder), builderClass))
        .addMethod(constructor)
        .addMethod(self);
  }

  private void generateParameters(TypeElement element, ClassName builderClass,
      TypeSpec.Builder builder,
      MethodSpec.Builder helperInit,
      MethodSpec.Builder helperRestore, MethodSpec.Builder helperSave) {
    for (Element e : element.getEnclosedElements()) {
      if (e instanceof VariableElement) {
        VariableElement variableElement = (VariableElement) e;
        String intentParamsPrefix = getIntentParamsPrefix(e.asType());
        ActivityParameter activityParameter = e.getAnnotation(ActivityParameter.class);
        if (activityParameter != null) {
          MethodSpec setter = MethodSpec.methodBuilder(e.getSimpleName().toString())
              .returns(builderClass)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ClassName.get(variableElement.asType()), e.getSimpleName().toString())
              .addStatement("getIntent().putExtra($S,$L)",
                  activityParameter.key().equals(ActivityParameter.USE_VARIABLE_NAME) ?
                      variableElement.getSimpleName() : activityParameter.key(),
                  e.getSimpleName().toString())
              .addStatement("return this")
              .build();
          builder.addMethod(setter);

          helperInit.addStatement("intent.get$LExtra", getIntentParamsPrefix(e.asType()));
        }
      }
    }
  }

  private String getIntentParamsPrefix(TypeMirror typeMirror) {
    final TypeVisitor<String, Void> visitor = new TypeVisitor<String, Void>() {
      @Override public String visit(TypeMirror typeMirror, Void aVoid) {
        return null;
      }

      @Override public String visit(TypeMirror typeMirror) {
        return null;
      }

      @Override public String visitPrimitive(PrimitiveType primitiveType, Void aVoid) {
        switch (primitiveType.getKind()) {
          case INT:
            return "Int";
          case BYTE:
            return "Byte";
          case CHAR:
            return "Char";
          case LONG:
            return "Long";
          case FLOAT:
            return "Float";
          case BOOLEAN:
            return "Boolean";
          case SHORT:
            return "Short";
          case DOUBLE:
            return "Double";
          default:
            throw new AssertionError();
        }
      }

      @Override public String visitNull(NullType nullType, Void aVoid) {
        return null;
      }

      @Override public String visitArray(ArrayType arrayType, Void aVoid) {
        return null;
      }

      @Override public String visitDeclared(DeclaredType declaredType, Void aVoid) {
        return null;
      }

      @Override public String visitError(ErrorType errorType, Void aVoid) {
        return null;
      }

      @Override public String visitTypeVariable(TypeVariable typeVariable, Void aVoid) {
        return null;
      }

      @Override public String visitWildcard(WildcardType wildcardType, Void aVoid) {
        return null;
      }

      @Override public String visitExecutable(ExecutableType executableType, Void aVoid) {
        return null;
      }

      @Override public String visitNoType(NoType noType, Void aVoid) {
        return null;
      }

      @Override public String visitUnknown(TypeMirror typeMirror, Void aVoid) {
        return null;
      }

      @Override public String visitUnion(UnionType unionType, Void aVoid) {
        return null;
      }

      @Override public String visitIntersection(IntersectionType intersectionType, Void aVoid) {
        return null;
      }
    };

    return typeMirror.accept(visitor, null);
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
