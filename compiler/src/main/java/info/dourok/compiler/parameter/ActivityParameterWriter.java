package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import info.dourok.esactivity.ActivityParameter;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static info.dourok.compiler.EasyUtils.getTypes;

/**
 * Created by tiaolins on 2017/8/30.
 */

public abstract class ActivityParameterWriter {
  protected ActivityParameter annotation;
  protected VariableElement parameter;

  protected final static int TYPE_UNKNOWN = 0;
  protected final static int TYPE_ARRAY = 1;
  protected final static int TYPE_ARRAY_LIST = 2;

  public ActivityParameterWriter(ActivityParameter annotation, VariableElement parameter) {
    this.annotation = annotation;
    this.parameter = parameter;
  }

  public TypeMirror getTypeMirror() {
    return parameter.asType();
  }

  public ActivityParameter getAnnotation() {
    return annotation;
  }

  public VariableElement getParameter() {
    return parameter;
  }

  public String getName() {
    return parameter.getSimpleName().toString();
  }

  public String getKey() {
    return annotation.key().equals(ActivityParameter.USE_VARIABLE_NAME) ?
        getName() : annotation.key();
  }

  public boolean isPrimitive() {
    return getTypeMirror() instanceof PrimitiveType;
  }

  public boolean isBoxed() {
    try {
      PrimitiveType primitiveType = getTypes().unboxedType(getTypeMirror());
      if (primitiveType != null) {
        return true;
      }
    } catch (IllegalArgumentException e) {
      // ignore;
    }
    return false;
  }

  public abstract void writeInject(MethodSpec.Builder paper, String activityName,
      String intentName);

  public final void writeRestore(MethodSpec.Builder paper, String activityName,
      String bundleName) {
    if (annotation.keep()) {
      doWriteRestore(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteRestore(MethodSpec.Builder paper, String activityName,
      String bundleName);

  public final void writeSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    if (annotation.keep()) {
      doWriteSave(paper, activityName, bundleName);
    }
  }

  protected abstract void doWriteSave(MethodSpec.Builder paper, String activityName,
      String bundleName);

  public abstract void writeSetter(MethodSpec.Builder paper);

  public static ActivityParameterWriter newWriter(ActivityParameter annotation,
      VariableElement parameter) {
    //TODO
    switch (annotation.transmit()) {
      case Ref:
        //TODO
        return null;
      case Auto:
        String prefix =
            BundleWriter.generatePrefix(parameter.asType(), ActivityParameterWriter.TYPE_UNKNOWN);
        if (prefix != null) {
          return new BundleWriter(annotation, parameter, prefix);
        } else {
          return null;//TODO
        }
      case Bundle:
        return new BundleWriter(annotation, parameter);
      case UnsafeCopy:
        return null; //TODO
      default:
        return null;
    }
  }
}
