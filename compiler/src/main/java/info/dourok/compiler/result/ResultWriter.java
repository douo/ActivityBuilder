package info.dourok.compiler.result;

import android.content.Intent;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.ConsumerHelper;
import info.dourok.compiler.parameter.ParameterModel;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.esactivity.Result;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import static info.dourok.compiler.EasyUtils.capitalize;

/**
 * Created by tiaolins on 2017/9/4.
 */

public class ResultWriter {
  private ResultModel result;

  public ResultWriter(ResultModel result) {
    this.result = result;
  }

  public String getCapitalizeName() {
    return capitalize(result.getName());
  }

  public void writeHandleResult(MethodSpec.Builder handleResult) {
    handleResult.addCode("case $L:\n", getResultConstant());
    handleResult.addStatement("return handle$L($L,$L)", getCapitalizeName(), "activity", "intent");
  }

  public FieldSpec buildField() throws IOException {
    int count = result.getParameters().size() + 1;
    TypeName typeName;
    if (count > 0) {
      TypeName types[] = new TypeName[count];
      types[0] = TypeVariableName.get("A");
      for (int i = 1; i < count; i++) {
        types[i] = TypeName.get(result.getParameters().get(i - 1).getType());
      }
      typeName = ParameterizedTypeName
          .get(ConsumerHelper.get(count), types);
    } else {
      typeName = ConsumerHelper.get(0);
    }
    return FieldSpec.builder(typeName, getFieldName())
        .build();
  }

  public String getResultConstant() {
    return "RESULT_" + result.getName().toUpperCase();
  }

  public String getFieldName() {
    return result.getName() + "Consumer";
  }

  public MethodSpec buildMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("handle" + getCapitalizeName())
        .addModifiers(Modifier.PRIVATE)
        .returns(boolean.class)
        .addParameter(TypeVariableName.get("A"), "activity")
        .addParameter(Intent.class, "intent")
        .beginControlFlow("if($L != null)", getFieldName());
    if (!result.getParameters().isEmpty()) {
      StringBuilder literal = new StringBuilder(getFieldName()).append(".accept(activity");
      String[] names = new String[result.getParameters().size()];
      for (int i = 0; i < result.getParameters().size(); i++) {
        ParameterModel parameter = result.getParameters().get(i);
        ParameterWriter writer = ParameterWriter.newWriter(parameter);
        writer.writeConsumerGetter(builder);
        names[i] = parameter.getName();
        literal.append(", $L");
      }
      literal.append(")");
      builder.addStatement(literal.toString(), (Object[]) names);
    } else {
      builder.addStatement("$L.run()", getFieldName());
    }
    builder.addStatement("return true");
    builder.endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("return false")
        .endControlFlow();
    return builder.build();
  }

  /**
   * 将 void result[Name](Parameters){} 解析为 ResultWriter
   */
  public static ResultWriter newWriter(Result annotation, ExecutableElement element) {
    return new ResultWriter(new ResultModel(annotation, element));
  }
}
