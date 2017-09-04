package info.dourok.compiler.parameter;

import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.TransmitType;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static info.dourok.compiler.EasyUtils.getTypes;

/**
 * Created by tiaolins on 2017/9/4.
 */

public class ParameterModel {
  private String key;
  private String name;
  private TypeMirror type;
  private boolean keep;
  private TransmitType transmit;
  protected VariableElement element;

  public ParameterModel(ActivityParameter annotation, VariableElement element) {
    this.element = element;
    name = element.getSimpleName().toString();
    key = annotation.key().equals(ActivityParameter.USE_VARIABLE_NAME) ?
        getName() : annotation.key();
    keep = annotation.keep();
    type = element.asType();
    transmit = annotation.transmit();
  }

  public ParameterModel(VariableElement element, TransmitType transmit) {
    this.element = element;
    name = element.getSimpleName().toString();
    key = name;
    keep = false;
    type = element.asType();
    this.transmit = transmit;
  }

  public TypeMirror getType() {
    return type;
  }

  public VariableElement getElement() {
    return element;
  }

  public String getName() {
    return name;
  }

  public String getKey() {
    return key;
  }

  public boolean isKeep() {
    return keep;
  }

  public TransmitType getTransmit() {
    return transmit;
  }

  public boolean isPrimitive() {
    return getType() instanceof PrimitiveType;
  }

  public boolean isBoxed() {
    try {
      PrimitiveType primitiveType = getTypes().unboxedType(getType());
      if (primitiveType != null) {
        return true;
      }
    } catch (IllegalArgumentException e) {
      // ignore;
    }
    return false;
  }
}
