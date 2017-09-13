package info.dourok.compiler.parameter;

import info.dourok.esactivity.BuilderParameter;
import info.dourok.esactivity.ResultParameter;
import info.dourok.esactivity.TransmitType;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.EasyUtils.getTypes;
import static info.dourok.compiler.EasyUtils.log;

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

  public ParameterModel(BuilderParameter annotation, VariableElement element) {
    this.element = element;
    name = element.getSimpleName().toString();
    key = annotation.key().equals(BuilderParameter.USE_VARIABLE_NAME) ?
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

  public ParameterModel(AnnotationMirror annotationMirror) {
    //FIXME 优化，可提取为全局变量
    TypeElement element = getElements().getTypeElement(ResultParameter.class.getName());
    List<? extends Element> elements = element.getEnclosedElements();
    ExecutableElement name = null;
    ExecutableElement type = null;
    ExecutableElement transmit = null;
    for (Element e : elements) {
      if ("name".equals(e.getSimpleName().toString())) {
        name = (ExecutableElement) e;
      }
      if ("type".equals(e.getSimpleName().toString())) {
        type = (ExecutableElement) e;
      }
      if ("transmit".equals(e.getSimpleName().toString())) {
        transmit = (ExecutableElement) e;
      }
    }
    Map<? extends ExecutableElement, ? extends AnnotationValue> map =
        annotationMirror.getElementValues();
    this.name = (String) map.get(name).getValue();
    this.type = (TypeMirror) map.get(type).getValue();
    AnnotationValue transmitValue =
        ((AnnotationValue) map.get(transmit));
    this.transmit = transmitValue == null ? TransmitType.Auto
        : TransmitType.valueOf(transmitValue.getValue().toString());
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
