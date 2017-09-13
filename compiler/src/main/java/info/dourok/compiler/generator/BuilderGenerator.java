package info.dourok.compiler.generator;

import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
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

public class BuilderGenerator extends BaseActivityGenerator {
  private List<ParameterWriter> parameterList;
  private List<ResultModel> resultList;
  private final TypeElement baseActivityBuilder;

  private final TypeSpec consumer;
  private final ClassName builderClass;
  private final ParameterizedTypeName builderWithParameter;

  public BuilderGenerator(TypeElement activity, TypeElement targetActivity,
      PackageElement targetPackage,
      List<ParameterWriter> parameterList,
      List<ResultModel> resultList, TypeElement baseActivityBuilder,
      TypeSpec consumer) {
    super(activity, targetActivity, targetPackage);
    this.parameterList = parameterList;
    this.resultList = resultList;
    this.baseActivityBuilder = baseActivityBuilder;

    builderClass = ClassName.get(targetPackage.getQualifiedName().toString(),
        targetActivity.getSimpleName() + "Builder");
    builderWithParameter = ParameterizedTypeName.get(builderClass, TypeVariableName.get("A"));
    this.consumer = consumer;
  }

  @Override protected TypeSpec generate() {

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(TypeVariableName.get("A"), "activity")
        .addStatement("super($L)", "activity")
        .addStatement("setIntent(new $T($L, $T.class))", Intent.class, "activity", targetActivity)
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
        .addModifiers(Modifier.PUBLIC)
        .addTypeVariable(TypeVariableName.get("A", ClassName.get(activity)))
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

    if (consumer != null) {
      builder.addMethod(buildGetConsumer());
      resultList.forEach(resultModel -> {
        try {
          builder.addMethod(buildResultCallback(resultModel));
          builder.addMethod(buildResultCallbackWithContext(resultModel));
        } catch (IOException e) {
          e.printStackTrace();
          //FIXME
        }
      });
    }

    return builder.build();
  }

  private MethodSpec buildGetConsumer() {
    TypeName consumerType = ParameterizedTypeName.get(
        ClassName.get(targetPackage.getQualifiedName().toString(), consumer.name),
        TypeVariableName.get("A"));

    return MethodSpec.methodBuilder("getConsumer")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(consumerType)
        .beginControlFlow("if($L == null)", "consumer")
        .addStatement("$L = new $L<>()", "consumer", consumer.name)
        .endControlFlow()
        .addStatement("return ($T) $L", consumerType, "consumer").build();
  }

  private MethodSpec buildResultCallbackWithContext(ResultModel result) throws IOException {
    return MethodSpec.methodBuilder("for" + result.getCapitalizeName())
        .returns(builderWithParameter)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(result.getConsumerTypeWithContext(), result.getConsumerName())
        .addStatement("getConsumer().$L = $L", result.getConsumerName(), result.getConsumerName())
        .addStatement("return this")
        .build();
  }

  private MethodSpec buildResultCallback(ResultModel result) throws IOException {
    StringBuilder literal = new StringBuilder("getConsumer().").append(result.getConsumerName())
        .append(" = (activity");

    StringBuilder parameters = result.getParameters().stream().reduce(new StringBuilder()
        , (stringBuilder, parameterModel) -> stringBuilder.append(", ")
            .append(parameterModel.getName()),
        (stringBuilder, stringBuilder2) -> stringBuilder2);
    String accept;
    if (result.getParameters().isEmpty()) {
      accept = "$L.run()";
    } else {
      accept = "$L.accept(" +
          parameters.substring(2) + ")";
    }

    return MethodSpec.methodBuilder("for" + result.getCapitalizeName())
        .returns(builderWithParameter)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(result.getConsumerType(), result.getConsumerName())
        .addCode(literal.append(parameters).append(") -> ").toString())
        .addStatement(accept, result.getConsumerName())
        .addStatement("return this")
        .build();
  }
}
