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

public class HelperGenerator extends Generator {
  private List<ParameterWriter> parameterList;
  private List<ResultModel> resultList;

  public HelperGenerator(TypeElement activity,
      TypeElement easyActivity,
      PackageElement activityPackage, List<ParameterWriter> parameterList,
      List<ResultModel> resultList) {
    super(activity, easyActivity, activityPackage);
    this.parameterList = parameterList;
    this.resultList = resultList;
  }

  @Override protected TypeSpec generate() {
    TypeSpec.Builder helper =
        TypeSpec.classBuilder(ClassName.get(activityPackage.getQualifiedName().toString(),
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
    MethodSpec.Builder builder = MethodSpec.methodBuilder("finish" + result.getCapitalizeName())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(easyActivity), "activity");
    StringBuilder literal = new StringBuilder(resultSetter.name).append("(activity");
    String[] names = new String[result.getParameters().size()];
    if (!result.getParameters().isEmpty()) {
      for (int i = 0; i < result.getParameters().size(); i++) {
        ParameterModel parameter = result.getParameters().get(i);
        builder.addParameter(TypeName.get(parameter.getType()), parameter.getName());
        //FIXME 重构 parameter writer
        names[i] = parameter.getName();
        //if (i == 0) {
        //  literal.append("$L");
        //} else {
        literal.append(", $L");
        //}
      }
    }
    literal.append(")");
    builder.addStatement(literal.toString(), (Object[]) names);
    builder.addStatement("$L.finish()", "activity");
    return builder.build();
  }

  private MethodSpec buildResultSetter(ResultModel result) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("result" + result.getCapitalizeName())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(easyActivity), "activity");
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
