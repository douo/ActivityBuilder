package info.dourok.compiler.generator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import info.dourok.compiler.parameter.ParameterModel;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultModel;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by tiaolins on 2017/9/5.
 */

public class HelperGenerator extends BaseActivityGenerator {
  public static final String TARGET_ACTIVITY_VARIABLE_NAME = "activity";
  private List<ParameterWriter> parameterList;
  private List<ResultModel> resultList;

  public HelperGenerator(TypeElement activity,
      TypeElement targetActivity,
      PackageElement targetPackage, List<ParameterWriter> parameterList,
      List<ResultModel> resultList) {
    super(activity, targetActivity, targetPackage);
    this.parameterList = parameterList;
    this.resultList = resultList;
  }

  @Override protected TypeSpec generate() {
    TypeSpec.Builder helper =
        TypeSpec.classBuilder(ClassName.get(targetPackage.getQualifiedName().toString(),
            targetActivity.getSimpleName() + "Helper"))
            .addModifiers(Modifier.PUBLIC) //PUBLIC is not safe, but BuilderUtils need it.
            .addField(FieldSpec.builder(ClassName.get(targetActivity),
                TARGET_ACTIVITY_VARIABLE_NAME,
                Modifier.PRIVATE, Modifier.FINAL).build())
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(targetActivity), TARGET_ACTIVITY_VARIABLE_NAME)
                .addStatement("this($L, false)", TARGET_ACTIVITY_VARIABLE_NAME).build())
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(targetActivity), TARGET_ACTIVITY_VARIABLE_NAME)
                .addParameter(TypeName.BOOLEAN, "autoInject")
                .addStatement("this.$L = $L", TARGET_ACTIVITY_VARIABLE_NAME,
                    TARGET_ACTIVITY_VARIABLE_NAME)
                .beginControlFlow("if(autoInject)")
                .addStatement("inject()")
                .endControlFlow()
                .build());

    MethodSpec.Builder helperInject = MethodSpec.methodBuilder("inject")
        .addStatement("$T intent = $L.getIntent()", Intent.class, TARGET_ACTIVITY_VARIABLE_NAME);

    MethodSpec.Builder helperRestore = MethodSpec.methodBuilder("restore")
        .addParameter(Bundle.class, "savedInstanceState");

    MethodSpec.Builder helperSave = MethodSpec.methodBuilder("save")
        .addParameter(Bundle.class, "savedInstanceState");

    for (ParameterWriter parameterWriter : parameterList) {
      parameterWriter.writeInjectActivity(helperInject, TARGET_ACTIVITY_VARIABLE_NAME);
      parameterWriter.writeRestore(helperRestore, TARGET_ACTIVITY_VARIABLE_NAME,
          "savedInstanceState");
      parameterWriter.writeSave(helperSave, TARGET_ACTIVITY_VARIABLE_NAME, "savedInstanceState");
    }

    for (int i = 0; i < resultList.size(); i++) {
      ResultModel result = resultList.get(i);
      // 为每个 Result 生成一个常量，用于表示 Result code
      helper.addField(
          FieldSpec.builder(int.class, result.getResultConstant(), Modifier.PUBLIC,
              Modifier.FINAL, Modifier.STATIC)
              .initializer("$T.RESULT_FIRST_USER + $L", Activity.class, i + 1).build());
      MethodSpec resultSetter = buildResultSetter(result);
      helper.addMethod(resultSetter);
      helper.addMethod(buildFinishWithResult(result, resultSetter));
    }

    helper.addMethod(helperInject.build())
        .addMethod(helperRestore.build())
        .addMethod(helperSave.build());

    return helper.build();
  }

  private MethodSpec buildFinishWithResult(ResultModel result, MethodSpec resultSetter) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("finish" + result.getCapitalizeName());
    StringBuilder literal = new StringBuilder(resultSetter.name).append("(");
    String[] names = new String[result.getParameters().size()];
    if (!result.getParameters().isEmpty()) {
      for (int i = 0; i < result.getParameters().size(); i++) {
        ParameterModel parameter = result.getParameters().get(i);
        builder.addParameter(TypeName.get(parameter.getType()), parameter.getName());
        //FIXME 重构 parameter writer
        names[i] = parameter.getName();
        if (i == 0) {
          literal.append("$L");
        } else {
          literal.append(", $L");
        }
      }
    }
    literal.append(")");
    builder.addStatement(literal.toString(), (Object[]) names);
    builder.addStatement("$L.finish()", "activity");
    return builder.build();
  }

  private MethodSpec buildResultSetter(ResultModel result) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("result" + result.getCapitalizeName());
    if (!result.getParameters().isEmpty()) {
      builder.addStatement("$T intent = new $T()", Intent.class, Intent.class);
      for (int i = 0; i < result.getParameters().size(); i++) {
        ParameterModel parameter = result.getParameters().get(i);
        builder.addParameter(TypeName.get(parameter.getType()), parameter.getName());
        //FIXME 重构 parameter writer
        ParameterWriter writer = ParameterWriter.newWriter(parameter);
        writer.writeConsumerSetter(builder);
      }
      builder.addStatement("$L.setResult($L,intent)", "activity", result.getResultConstant());
    } else {
      builder.addStatement("$L.setResult($L)", "activity", result.getResultConstant());
    }
    return builder.build();
  }
}
