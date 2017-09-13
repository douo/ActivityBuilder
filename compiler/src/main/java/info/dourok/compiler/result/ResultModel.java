package info.dourok.compiler.result;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.ConsumerHelper;
import info.dourok.compiler.parameter.ParameterModel;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.TransmitType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import static info.dourok.compiler.EasyUtils.capitalize;
import static info.dourok.compiler.EasyUtils.error;
import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.EasyUtils.log;

/**
 * Created by tiaolins on 2017/9/4.
 */

public class ResultModel {
  private String name;
  private List<ParameterModel> parameters;
  private static final Pattern pattern = Pattern.compile("result(.+)");

  /**
   * 将 void result[Name](Parameters){} 解析为 ResultWriter
   */
  public ResultModel(Result annotation, ExecutableElement element) {
    Matcher matcher = pattern.matcher(element.getSimpleName().toString());
    if (matcher.find()) {
      name = matcher.group(1).toLowerCase();
    } else {
      String msg = String.format(
          "Result annotated method must name as result[Name] %s is illegal result method name",
          element.getSimpleName());
      error(msg, element);
      throw new IllegalStateException(msg);
    }
    List<? extends VariableElement> variableElements = element.getParameters();
    parameters = new ArrayList<>(variableElements.size());
    for (VariableElement variableElement : variableElements) {
      parameters.add(
          new ParameterModel(variableElement, TransmitType.Auto));
    }
  }

  public ResultModel(AnnotationMirror result) {
    //FIXME 优化，可提取为全局变量
    TypeElement element = getElements().getTypeElement(Result.class.getName());
    List<? extends Element> elements = element.getEnclosedElements();
    ExecutableElement name = null;
    ExecutableElement parameters = null;
    for (Element e : elements) {
      if ("name".equals(e.getSimpleName().toString())) {
        name = (ExecutableElement) e;
      }
      if ("parameters".equals(e.getSimpleName().toString())) {
        parameters = (ExecutableElement) e;
      }
    }
    // 见  com.sun.tools.javac.code.Attribute
    // 常量就直接返回常量
    // 类 返回 TypeMirror
    // 枚举 返回 VariableElement
    // 注解 返回 AnnotationMirror
    //
    this.name = (String) result.getElementValues().get(name).getValue();
    AnnotationValue ps = result.getElementValues().get(parameters);
    List list = (List) ps.getValue();
    this.parameters = new ArrayList<>(list.size());
    for (Object o : list) {
      this.parameters.add(new ParameterModel((AnnotationMirror) o));
    }
  }

  public List<ParameterModel> getParameters() {
    return parameters;
  }

  public String getName() {
    return name;
  }

  public String getCapitalizeName() {
    return capitalize(getName());
  }

  public String getResultConstant() {
    return "RESULT_" + getName().toUpperCase();
  }

  public String getConsumerName() {
    return getName() + "Consumer";
  }

  public TypeName getConsumerType() throws IOException {
    int count = getParameters().size();
    if (count > 0) {
      TypeName types[] = new TypeName[count];
      for (int i = 0; i < count; i++) {
        types[i] = TypeName.get(getParameters().get(i).getType());
      }
      return ParameterizedTypeName
          .get(ConsumerHelper.get(count), types);
    } else {
      return ConsumerHelper.get(0);
    }
  }

  public TypeName getConsumerTypeWithContext() throws IOException {
    int count = getParameters().size() + 1;
    TypeName types[] = new TypeName[count];
    types[0] = TypeVariableName.get("A");
    for (int i = 1; i < count; i++) {
      types[i] = TypeName.get(getParameters().get(i - 1).getType());
    }
    return ParameterizedTypeName
        .get(ConsumerHelper.get(count), types);
  }
}
