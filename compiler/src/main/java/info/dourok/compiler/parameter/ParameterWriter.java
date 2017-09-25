package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import info.dourok.esactivity.BuilderParameter;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by tiaolins on 2017/8/30.
 */

public abstract class ParameterWriter {

  protected final static int TYPE_UNKNOWN = 0;
  protected final static int TYPE_ARRAY = 1;
  protected final static int TYPE_ARRAY_LIST = 2;
  protected ParameterModel parameter;

  public ParameterWriter(ParameterModel parameter) {
    this.parameter = parameter;
  }

  public TypeMirror getType() {
    return parameter.getType();
  }

  public VariableElement getElement() {
    return parameter.getElement();
  }

  public String getName() {
    return parameter.getName();
  }

  public String getKey() {
    return parameter.getKey();
  }

  public boolean isKeep() {
    return parameter.isKeep();
  }

  /**
   * #{activityName}.name = xx 的形式
   */
  public abstract void writeInjectActivity(MethodSpec.Builder paper, String activityName);

  public abstract void writeConsumerGetter(MethodSpec.Builder paper);

  public abstract void writeConsumerSetter(MethodSpec.Builder paper);

  public final void writeRestore(MethodSpec.Builder paper, String activityName,
      String bundleName) {
    if (parameter.isKeep()) {
      doWriteRestore(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteRestore(MethodSpec.Builder paper, String activityName,
      String bundleName);

  public final void writeSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    if (parameter.isKeep()) {
      doWriteSave(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteSave(MethodSpec.Builder paper, String activityName,
      String bundleName);

  public abstract void writeSetter(MethodSpec.Builder paper);

  public static ParameterWriter newWriter(BuilderParameter annotation,
      VariableElement variable) {
    return newWriter(new ParameterModel(annotation, variable));
  }

  public static ParameterWriter newWriter(ParameterModel parameter) {
    switch (parameter.getTransmit()) {
      case REF:
        return new RefWriter(parameter);
      case AUTO:
        String prefix =
            BundleWriter.generatePrefix(parameter.getType(), ParameterWriter.TYPE_UNKNOWN);
        if (prefix != null) {
          return new BundleWriter(parameter, prefix);
        } else {
          return new RefWriter(parameter);
        }
      case UNSAFE:
        return null; //TODO
      default:
        return null;
    }
  }
}
