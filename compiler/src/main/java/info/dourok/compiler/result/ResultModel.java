package info.dourok.compiler.result;

import info.dourok.compiler.parameter.ParameterModel;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.TransmitType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import static info.dourok.compiler.EasyUtils.capitalize;
import static info.dourok.compiler.EasyUtils.error;
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

  public String getFieldName() {
    return getName() + "Consumer";
  }
}
