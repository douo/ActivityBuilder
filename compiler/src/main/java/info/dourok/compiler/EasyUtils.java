package info.dourok.compiler;

import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by tiaolins on 2017/8/30.
 */

public class EasyUtils {
  private Filer filer;
  private Messager messager;
  private Types types;
  private Elements elements;
  private TypeMirror string;
  private TypeMirror charSequence;
  private TypeMirror arrayList;
  private TypeMirror parcelable;
  private TypeMirror serializable;

  private static final EasyUtils instance = new EasyUtils();

  public static EasyUtils getInstance() {
    return instance;
  }

  public void init(ProcessingEnvironment processingEnvironment) {
    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    types = processingEnvironment.getTypeUtils();
    elements = processingEnvironment.getElementUtils();

    string = elements.getTypeElement(String.class.getName()).asType();
    charSequence = elements.getTypeElement(CharSequence.class.getName()).asType();
    arrayList = elements.getTypeElement(ArrayList.class.getName()).asType();
    parcelable = elements.getTypeElement(Parcelable.class.getName()).asType();
    serializable = elements.getTypeElement(Serializable.class.getName()).asType();
  }

  public static Types getTypes() {
    return instance.types;
  }

  public static Elements getElements() {
    return instance.elements;
  }

  public static boolean isString(TypeMirror mirror) {
    return instance.types.isSameType(mirror, instance.string);
  }

  public static boolean isCharSequence(TypeMirror mirror) {
    return instance.types.isSameType(mirror, instance.charSequence);
  }

  public static boolean isArrayList(TypeMirror mirror) {
    //XXX 类型擦除后的 ArrayList<T> 类不能等价于 ArrayList，所以不能用 isSame 判断
    return instance.types.isAssignable(getTypes().erasure(mirror), instance.arrayList);
  }

  public static boolean isParcelable(TypeMirror mirror, boolean same) {
    if (same) {
      return instance.types.isSameType(mirror, instance.parcelable);
    } else {
      return instance.types.isAssignable(mirror, instance.parcelable);
    }
  }

  public static boolean isSerializable(TypeMirror mirror, boolean same) {
    if (same) {
      return instance.types.isSameType(mirror, instance.serializable);
    } else {
      return instance.types.isAssignable(mirror, instance.serializable);
    }
  }

  public static void log(String msg) {
    instance.messager.printMessage(Diagnostic.Kind.NOTE, msg);
  }

  public static void log(String msg, Element element) {
    instance.messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
  }

  public static void warn(String msg) {
    instance.messager.printMessage(Diagnostic.Kind.WARNING, msg);
  }

  public static void warn(String msg, Element element) {
    instance.messager.printMessage(Diagnostic.Kind.WARNING, msg, element);
  }

  public static void error(String msg) {
    instance.messager.printMessage(Diagnostic.Kind.ERROR, msg);
  }

  public static void error(String msg, Element element) {
    instance.messager.printMessage(Diagnostic.Kind.ERROR, msg, element);
  }

  public static String capitalize(String s) {
    if (s.length() == 0) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }
}
