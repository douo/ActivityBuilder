package info.dourok.compiler.parameter;

import android.app.Activity;
import info.dourok.esactivity.BuilderParameter;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.ResultParameter;
import info.dourok.esactivity.TransmitType;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static info.dourok.compiler.EasyUtils.error;
import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.EasyUtils.getTypes;

/**
 * @author tiaolins
 * @date 2017/9/4
 */
public class ParameterModel {
  private String key;
  private String name;
  private String displayName;
  private TypeMirror type;
  private boolean keep;
  private TransmitType transmit;
  protected VariableElement element;

  public ParameterModel(BuilderParameter annotation, VariableElement element) {
    this.element = element;
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      error("BuilderParameter can not be private", element);
      throw new IllegalStateException("BuilderParameter can not be private");
    }
    name = element.getSimpleName().toString();
    // 将 mVar 转换为 var
    if (name.matches("m[A-Z].*$")) {
      displayName =
          name.substring(1, 2).toLowerCase()
              + (name.length() > 2 ? name.substring(2, name.length()) : "");
    } else {
      displayName = name;
    }

    key =
        annotation.key().equals(BuilderParameter.USE_VARIABLE_NAME) ? getName() : annotation.key();
    keep = annotation.keep();
    type = element.asType();
    transmit = annotation.transmit();
  }

  /**
   * 用于 {@link Result} 注解方法
   *
   * @param element 方法的参数
   */
  public ParameterModel(VariableElement element, TransmitType transmit) {
    this.element = element;
    name = element.getSimpleName().toString();
    key = name;
    keep = false;
    type = element.asType();
    this.transmit = transmit;
  }

  /** 用于 {@link Result} 注解 {@link Activity} */
  public ParameterModel(AnnotationMirror annotationMirror) {
    // FIXME 优化，可提取为全局变量
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
    if (!SourceVersion.isName(this.name)) {
      error("not a valid name: " + this.name, name);
      throw new IllegalStateException("not a valid name: " + this.name);
    }
    this.key = this.name;
    this.type = (TypeMirror) map.get(type).getValue();
    AnnotationValue transmitValue = map.get(transmit);
    this.transmit =
        transmitValue == null
            ? TransmitType.AUTO
            : TransmitType.valueOf(transmitValue.getValue().toString());
  }

  public TypeMirror getType() {
    return type;
  }

  /** 如果是原生类型则返回装箱类型 */
  public TypeMirror getObjectType() {
    if (isPrimitive()) {
      return getTypes().boxedClass((PrimitiveType) getType()).asType();
    } else {
      return type;
    }
  }

  public VariableElement getElement() {
    return element;
  }

  public String getName() {
    return name;
  }

  /** 用于 Builder 方法名和方法参数 */
  public String getDisplayName() {
    return displayName;
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
