package info.dourok.compiler.generator;

import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.EasyUtils;
import info.dourok.compiler.parameter.ParameterModel;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultModel;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by tiaolins on 2017/9/5.
 */

public class ConsumerGenerator extends BaseActivityGenerator {

  private TypeElement baseResultConsumer;

  private List<ResultModel> resultList;

  private TypeSpec helper;

  public ConsumerGenerator(TypeElement activity, TypeElement targetActivity,
      PackageElement targetPackage, TypeElement baseResultConsumer,
      TypeSpec helper,
      List<ResultModel> resultList) {
    super(activity, targetActivity, targetPackage);
    this.baseResultConsumer = baseResultConsumer;
    this.resultList = resultList;
    this.helper = helper;
  }

  @Override
  public void write() throws IOException {
    JavaFile.builder(targetPackage.getQualifiedName().toString(), getTypeSpec())
        .addStaticImport(ClassName.get(targetPackage.getQualifiedName().toString(),
            helper.name), "*")
        .build()
        .writeTo(EasyUtils.getFiler());
  }

  @Override
  protected TypeSpec generate() {
    TypeSpec.Builder consumer =
        TypeSpec.classBuilder(ClassName.get(targetPackage.getQualifiedName().toString(),
            targetActivity.getSimpleName() + "Consumer"))
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
    for (ResultModel result : resultList) {
      handleResult.addCode("case $L:\n", result.getResultConstant());
      handleResult.addStatement("return process$L($L,$L)", result.getCapitalizeName(), "activity",
          "intent");
      try {
        consumer.addField(buildField(result));
      } catch (IOException e) {
        //TODO 生成 consumer 接口失败
        e.printStackTrace();
      }
      consumer.addMethod(buildResultProcessor(result));

      literal.append(result.getConsumerName())
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

  private FieldSpec buildField(ResultModel result) throws IOException {
    return FieldSpec.builder(result.getConsumerTypeWithContext(), result.getConsumerName())
        .build();
  }

  private MethodSpec buildResultProcessor(ResultModel result) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("process" + result.getCapitalizeName())
        .addModifiers(Modifier.PRIVATE)
        .returns(boolean.class)
        .addParameter(TypeVariableName.get("A"), "activity")
        .addParameter(Intent.class, "intent")
        .beginControlFlow("if($L != null)", result.getConsumerName());
    if (!result.getParameters().isEmpty()) {
      StringBuilder literal = new StringBuilder(result.getConsumerName()).append(".accept(activity");
      String[] names = new String[result.getParameters().size()];
      for (int i = 0; i < result.getParameters().size(); i++) {
        ParameterModel parameter = result.getParameters().get(i);
        //FIXME 重构 parameter writer
        ParameterWriter writer = ParameterWriter.newWriter(parameter);
        writer.writeConsumerGetter(builder);
        names[i] = parameter.getName();
        literal.append(", $L");
      }
      literal.append(")");
      builder.addStatement(literal.toString(), (Object[]) names);
    } else {
      builder.addStatement("$L.run()", result.getConsumerName());
    }
    builder.addStatement("return true");
    builder.endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("return false")
        .endControlFlow();
    return builder.build();
  }
}
