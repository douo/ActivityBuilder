package info.dourok.compiler;

import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.result.ResultWriter;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by tiaolins on 2017/9/5.
 */

public class ConsumerGenerator extends Generator {

  private TypeElement baseResultConsumer;

  private List<ResultWriter> resultList;

  private TypeSpec helper;

  public ConsumerGenerator(TypeElement activity, TypeElement easyActivity,
      PackageElement activityPackage, TypeElement baseResultConsumer,
      TypeSpec helper,
      List<ResultWriter> resultList) {
    super(activity, easyActivity, activityPackage);
    this.baseResultConsumer = baseResultConsumer;
    this.resultList = resultList;
    this.helper = helper;
  }

  @Override
  public void write() throws IOException {
    JavaFile.builder(activityPackage.getQualifiedName().toString(), getTypeSpec())
        .addStaticImport(ClassName.get(activityPackage.getQualifiedName().toString(),
            helper.name), "*")
        .build()
        .writeTo(EasyUtils.getFiler());
  }

  @Override
  protected TypeSpec generate() {
    TypeSpec.Builder consumer =
        TypeSpec.classBuilder(ClassName.get(activityPackage.getQualifiedName().toString(),
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

    return consumer.build();
  }
}
