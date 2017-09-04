package info.dourok.compiler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultWriter;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.EasyActivity;
import info.dourok.esactivity.Result;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
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
  private final TypeElement baseResultConsumer;
  private final TypeElement activity;

  public ActivityProcessorFactory(ProcessingEnvironment processingEnvironment) {

    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    types = processingEnvironment.getTypeUtils();
    elements = processingEnvironment.getElementUtils();

    activity = elements.getTypeElement(Activity.class.getName());
    baseActivityBuilder =
        elements.getTypeElement("info.dourok.esactivity.BaseActivityBuilder");
    baseResultConsumer =
        elements.getTypeElement("info.dourok.esactivity.BaseResultConsumer");
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
    private List<ParameterWriter> parameterList;
    private List<ResultWriter> resultList;

    public ActivityProcessor(TypeElement element) {
      easyActivity = element;
      packageElement = elements.getPackageOf(element);
      findAnnotations();
    }

    private void findAnnotations() {
      parameterList = new LinkedList<>();
      resultList = new LinkedList<>();
      for (Element element : easyActivity.getEnclosedElements()) {
        //find paramters
        if (element.getKind() == ElementKind.FIELD) {
          ActivityParameter activityParameter = element.getAnnotation(ActivityParameter.class);
          if (activityParameter != null) {
            parameterList.add(ParameterWriter.newWriter(activityParameter,
                (VariableElement) element));
          }
        }
        //Result 注解的方法
        if (element.getKind() == ElementKind.METHOD) {
          Result result = element.getAnnotation(Result.class);
          if (result != null) {
            resultList.add(ResultWriter.newWriter(result, (ExecutableElement) element));
          }
        }
      }
    }

    public void generate() {
      TypeSpec consumer = null;
      //需要自定义结果时，才需要子类化 BaseResultConsumer
      if (!resultList.isEmpty()) {
        consumer = generateConsumer().build();
      }

      TypeSpec.Builder builder = generateBuilder(consumer);
      TypeSpec.Builder helper = generateHelper();

      try {
        JavaFile.builder(packageElement.getQualifiedName().toString(), builder.build())
            .build()
            .writeTo(filer);
        JavaFile.builder(packageElement.getQualifiedName().toString(), helper.build())
            .build()
            .writeTo(filer);
        if (consumer != null) {
          JavaFile.builder(packageElement.getQualifiedName().toString(), consumer)
              .addStaticImport(ClassName.get(packageElement.getQualifiedName().toString(),
                  easyActivity.getSimpleName() + "Helper"), "*") //FIXME
              .build()
              .writeTo(filer);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * 创建 Builder 的基本骨架
     *
     * @throws IOException
     */
    private TypeSpec.Builder generateBuilder(TypeSpec consumer) {
      ClassName builderClass = ClassName.get(packageElement.getQualifiedName().toString(),
          easyActivity.getSimpleName() + "Builder");
      ParameterizedTypeName builderWithParameter =
          ParameterizedTypeName.get(builderClass, TypeVariableName.get("A"));

      MethodSpec constructor = MethodSpec.constructorBuilder()
          .addModifiers(Modifier.PRIVATE)
          .addParameter(TypeVariableName.get("A"), "activity")
          .addStatement("super($L)", "activity")
          .addStatement("setIntent(new $T($L, $T.class))", Intent.class, "activity", easyActivity)
          .build();

      MethodSpec create = MethodSpec.methodBuilder("create")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addTypeVariable(TypeVariableName.get("A", TypeName.get(activity.asType())))
          .returns(builderWithParameter)
          .addParameter(TypeVariableName.get("A"), "activity")
          .addStatement("return new $T(activity)", builderWithParameter)
          .build();

      MethodSpec self = MethodSpec.methodBuilder("self")
          .addAnnotation(Override.class)
          .returns(builderWithParameter)
          .addModifiers(Modifier.PROTECTED)
          .addStatement("return this")
          .build();

      TypeSpec.Builder builder = TypeSpec.classBuilder(builderClass)
          .addTypeVariable(TypeVariableName.get("A", TypeName.get(activity.asType())))
          .superclass(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder),
              builderWithParameter,
              TypeVariableName.get("A")))
          .addMethod(constructor)
          .addMethod(create)
          .addMethod(self);

      for (ParameterWriter parameterWriter : parameterList) {
        MethodSpec.Builder setter = MethodSpec.methodBuilder(parameterWriter.getName())
            .returns(builderWithParameter)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(parameterWriter.getType()),
                parameterWriter.getName());
        parameterWriter.writeSetter(setter);
        setter.addStatement("return this");
        builder.addMethod(setter.build());
      }

      return builder;
    }

    private TypeSpec.Builder generateConsumer() {
      TypeSpec.Builder consumer =
          TypeSpec.classBuilder(ClassName.get(packageElement.getQualifiedName().toString(),
              easyActivity.getSimpleName() + "Consumer"))
              .addTypeVariable(TypeVariableName.get("A", TypeName.get(activity.asType())))
              .superclass(ParameterizedTypeName.get(ClassName.get(baseResultConsumer),
                  TypeVariableName.get("A")));

      MethodSpec.Builder hasConsumer = MethodSpec.methodBuilder("hasConsumer")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .returns(boolean.class);

      MethodSpec.Builder handleResult = MethodSpec.methodBuilder("handleResult")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PROTECTED)
          .returns(boolean.class)
          .addParameter(TypeVariableName.get("A"), "activity")
          .addParameter(int.class, "result")
          .addParameter(Intent.class, "intent");

      handleResult.beginControlFlow("switch ($L)", "result");

      StringBuilder literal = new StringBuilder("return ");
      for (ResultWriter resultWriter : resultList) {
        resultWriter.writeHandleResult(handleResult);
        try {
          consumer.addField(resultWriter.buildField());
        } catch (IOException e) {
          //TODO 生成 consumer 接口失败
          e.printStackTrace();
        }
        consumer.addMethod(resultWriter.buildMethod());

        literal.append(resultWriter.getFieldName())
            .append(" != null ||");
      }
      literal.append("super.hasConsumer()");
      hasConsumer.addStatement(literal.toString());
      handleResult.addStatement("default:")
          .addStatement("return false").endControlFlow();

      consumer.addMethod(handleResult.build())
          .addMethod(hasConsumer.build());

      return consumer;
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

      for (ParameterWriter parameterWriter : parameterList) {
        parameterWriter.writeInjectActivity(helperInject, "activity");
        parameterWriter.writeRestore(helperRestore, "activity", "savedInstanceState");
        parameterWriter.writeSave(helperSave, "activity", "savedInstanceState");
      }
      for (int i = 0; i < resultList.size(); i++) {
        ResultWriter resultWriter = resultList.get(i);
        helper.addField(
            FieldSpec.builder(int.class, resultWriter.getResultConstant(), Modifier.PUBLIC,
                Modifier.FINAL, Modifier.STATIC)
                .initializer("$T.RESULT_FIRST_USER + $L", Activity.class, i + 1).build());
      }
      helper.addMethod(helperInject.build())
          .addMethod(helperRestore.build())
          .addMethod(helperSave.build());
      return helper;
    }
  }
}
