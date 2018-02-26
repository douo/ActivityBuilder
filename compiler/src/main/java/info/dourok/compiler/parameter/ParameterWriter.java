package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import info.dourok.esactivity.BuilderParameter;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static info.dourok.compiler.EasyUtils.getTypes;

/**
 * @author tiaolins
 * @date 2017/8/30
 */
public abstract class ParameterWriter {

  protected static final int TYPE_UNKNOWN = 0;
  protected static final int TYPE_ARRAY = 1;
  protected static final int TYPE_ARRAY_LIST = 2;
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

  public String getDisplayName() {
    return parameter.getDisplayName();
  }

  public String getKey() {
    return parameter.getKey();
  }

  public boolean isKeep() {
    return parameter.isKeep();
  }

  /** #{activityName}.name = xx 的形式 */
  public abstract void writeInjectActivity(MethodSpec.Builder paper, String activityName);

  public abstract void writeConsumerGetter(MethodSpec.Builder paper);

  public abstract void writeConsumerSetter(MethodSpec.Builder paper);

  public final void writeRestore(MethodSpec.Builder paper, String activityName, String bundleName) {
    if (parameter.isKeep()) {
      doWriteRestore(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteRestore(
      MethodSpec.Builder paper, String activityName, String bundleName);

  public final void writeSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    if (parameter.isKeep()) {
      doWriteSave(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteSave(
      MethodSpec.Builder paper, String activityName, String bundleName);

  public abstract void writeSetter(MethodSpec.Builder paper);

  public static ParameterWriter newWriter(BuilderParameter annotation, VariableElement variable) {
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
        // TODO
      default:
        return null;
    }
  }

  /** @return 返回原生类型或者装箱类型的默认值，其他类型返回 null */
  static String getDefaultValue(TypeMirror typeMirror) {
    switch (typeMirror.getKind()) {
      case BOOLEAN:
        return "false";
      case BYTE:
        return "(byte)0";
      case SHORT:
        return "(short)0";
      case CHAR:
        return "(char)0";
      case INT:
      case LONG:
        return "0";
      case FLOAT:
        return ".0f";
      case DOUBLE:
        return ".0";
      default:
        try {
          PrimitiveType primitiveType = getTypes().unboxedType(typeMirror);
          return getDefaultValue(primitiveType);
        } catch (IllegalArgumentException e) {
          return null;
        }
    }
  }
}
