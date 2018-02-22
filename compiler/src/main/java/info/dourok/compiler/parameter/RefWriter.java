package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.TypeElement;

import static info.dourok.compiler.EasyUtils.capitalize;
import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.parameter.BundleWriter.getDefaultValue;

/**
 * @author tiaolins
 * @date 2017/8/31
 */
public class RefWriter extends ParameterWriter {
  private final TypeElement refManager;

  public RefWriter(ParameterModel parameter) {
    super(parameter);
    refManager = getElements().getTypeElement("info.dourok.esactivity.RefManager");
  }

  /**
   * FIXME 蕴含了 target 等同于 Activity 的假设
   *
   * @param activityName 可以为空，变成 name = xx;
   */
  @Override
  public void writeInjectActivity(MethodSpec.Builder paper, String activityName) {
    if (parameter.isPrimitive()) {
      paper.addStatement(
          "$L.$L = $T.getInstance().get$L($L,$S,$L)",
          activityName,
          getName(),
          refManager,
          capitalize(parameter.getType().getKind().name()),
          activityName,
          getKey(),
          getDefaultValue(parameter.getType()));
    } else {
      paper.addStatement(
          "$L.$L = $T.getInstance().get($L,$S)",
          activityName,
          getName(),
          refManager,
          activityName,
          getKey());
    }
  }

  @Override
  public void writeConsumerGetter(MethodSpec.Builder paper) {
    if (parameter.isPrimitive()) {
      paper.addStatement(
          "$T $L = $T.getInstance().get$L($L,$S,$L)",
          getType(),
          getName(),
          refManager,
          capitalize(parameter.getType().getKind().name()),
          "intent",
          getKey(),
          getDefaultValue(parameter.getType()));
    } else {
      paper.addStatement(
          "$T $L = $T.getInstance().get($L,$S)",
          getType(),
          getName(),
          refManager,
          "intent",
          getKey());
    }
  }

  @Override
  public void writeConsumerSetter(MethodSpec.Builder paper) {
    paper.addStatement("$T.getInstance().put($L,$S,$L)", refManager, "intent", getKey(), getName());
  }

  @Override
  protected void doWriteRestore(MethodSpec.Builder paper, String activityName, String bundleName) {
    // DO NOT SUPPORT KEEP member
  }

  @Override
  protected void doWriteSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    // DO NOT SUPPORT KEEP member
  }

  @Override
  public void writeSetter(MethodSpec.Builder paper) {
    paper.addStatement("getRefMap().put($S,$L)", getKey(), getDisplayName());
  }
}
