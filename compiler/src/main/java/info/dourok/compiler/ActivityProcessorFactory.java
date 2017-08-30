package info.dourok.compiler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import info.dourok.compiler.parameter.ActivityParameterWriter;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.EasyActivity;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by tiaolins on 2017/8/30.
 */

public class ActivityProcessorFactory {
  private Filer filer;
  private Messager messager;
  private Types types;
  private Elements elements;
  private final TypeElement baseActivityBuilder;
  private final TypeElement activity;

  public ActivityProcessorFactory(ProcessingEnvironment processingEnvironment) {

    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    types = processingEnvironment.getTypeUtils();
    elements = processingEnvironment.getElementUtils();

    activity = elements.getTypeElement(Activity.class.getName());
    baseActivityBuilder =
        elements.getTypeElement("info.dourok.esactivity.BaseActivityBuilder");
  }

  public boolean isEasyActivity(Element element) {
    return element.getAnnotation(EasyActivity.class) != null
        && element instanceof TypeElement
        && types.isSubtype(element.asType(), activity.asType());
  }

  public ActivityProcessor create(TypeElement element) {
    return new ActivityProcessor(element);
  }

  public class ActivityProcessor {
    private TypeElement easyActivity;
    private PackageElement packageElement;
    private List<ActivityParameterWriter> parameters;

    public ActivityProcessor(TypeElement element) {
      easyActivity = element;
      packageElement = elements.getPackageOf(element);
      parameters = findParameters();
    }

    private List<ActivityParameterWriter> findParameters() {
      List<ActivityParameterWriter> list = new LinkedList<>();
      for (Element element : easyActivity.getEnclosedElements()) {
        if (element.getKind() == ElementKind.FIELD) {
          ActivityParameter activityParameter = element.getAnnotation(ActivityParameter.class);
          if (activityParameter != null) {
            list.add(ActivityParameterWriter.newWriter(activityParameter,
                (VariableElement) element));
          }
        }
      }
      return list;
    }

    public void generate() {
      TypeSpec.Builder builder = generateBuilder();
      TypeSpec.Builder helper = generateHelper();
      try {
        JavaFile.builder(packageElement.getQualifiedName().toString(), builder.build())
            .build()
            .writeTo(filer);
        JavaFile.builder(packageElement.getQualifiedName().toString(), helper.build())
            .build()
            .writeTo(filer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * 创建 Builder 的基本骨架
     *
     * @throws IOException
     */
    private TypeSpec.Builder generateBuilder() {
      ClassName builderClass =
          ClassName.get(packageElement.getQualifiedName().toString(),
              easyActivity.getSimpleName() + "Builder");

      MethodSpec constructor = MethodSpec.constructorBuilder()
          .addParameter(ClassName.get(Activity.class), "activity")
          .addStatement("super($L)", "activity")
          .addStatement("setIntent(new $T($L, $T.class))", Intent.class, "activity", easyActivity)
          .build();

      MethodSpec self = MethodSpec.methodBuilder("self")
          .addAnnotation(Override.class)
          .returns(builderClass)
          .addModifiers(Modifier.PROTECTED)
          .addStatement("return this")
          .build();

      TypeSpec.Builder builder = TypeSpec.classBuilder(builderClass)
          .superclass(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder), builderClass))
          .addMethod(constructor)
          .addMethod(self);
      for (ActivityParameterWriter parameterWriter : parameters) {
        MethodSpec.Builder setter = MethodSpec.methodBuilder(parameterWriter.getName())
            .returns(builderClass)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(parameterWriter.getTypeMirror()),
                parameterWriter.getName());
        parameterWriter.writeSetter(setter);
        setter.addStatement("return this");
        builder.addMethod(setter.build());
      }

      return builder;
    }

    private TypeSpec.Builder generateHelper() {
      TypeSpec.Builder helper =
          TypeSpec.classBuilder(ClassName.get(packageElement.getQualifiedName().toString(),
              easyActivity.getSimpleName() + "Helper"));

      MethodSpec.Builder helperInject = MethodSpec.methodBuilder("inject")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(easyActivity), "activity")
          .addStatement("$T intent = $L.getIntent()", Intent.class, "activity");

      MethodSpec.Builder helperRestore = MethodSpec.methodBuilder("restore")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(easyActivity), "activity")
          .addParameter(Bundle.class, "savedInstanceState");

      MethodSpec.Builder helperSave = MethodSpec.methodBuilder("save")
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(easyActivity), "activity")
          .addParameter(Bundle.class, "savedInstanceState");

      for (ActivityParameterWriter parameterWriter : parameters) {
        parameterWriter.writeInject(helperInject, "activity", "intent");
        parameterWriter.writeRestore(helperRestore, "activity", "savedInstanceState");
        parameterWriter.writeSave(helperSave, "activity", "savedInstanceState");
      }
      helper.addMethod(helperInject.build())
          .addMethod(helperRestore.build())
          .addMethod(helperSave.build());
      return helper;
    }
  }
}
