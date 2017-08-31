package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import info.dourok.esactivity.ActivityParameter;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

import static info.dourok.compiler.EasyUtils.capitalize;
import static info.dourok.compiler.EasyUtils.error;
import static info.dourok.compiler.EasyUtils.getTypes;
import static info.dourok.compiler.EasyUtils.isArrayList;
import static info.dourok.compiler.EasyUtils.isCharSequence;
import static info.dourok.compiler.EasyUtils.isParcelable;
import static info.dourok.compiler.EasyUtils.isSerializable;
import static info.dourok.compiler.EasyUtils.isString;
import static info.dourok.compiler.EasyUtils.log;

/**
 * 生成 Bundle 支持类型的写入读取代码
 * Created by tiaolins on 2017/8/30.
 */
class BundleWriter extends ActivityParameterWriter {

  private String prefix;

  public BundleWriter(ActivityParameter annotation,
      VariableElement parameter, String prefix) {
    super(annotation, parameter);
    if (prefix == null) {
      prefix = generatePrefix(parameter.asType(), TYPE_UNKNOWN);
    }
    this.prefix = prefix;
    if (prefix == null) {
      String msg =
          String.format(
              "prefix can not be null cause by the type of %s is %s not supported by Bundle",
              parameter.getSimpleName().toString(), parameter.asType().toString());
      error(msg, parameter);
      throw new IllegalArgumentException(msg);
    }
  }

  public BundleWriter(ActivityParameter annotation, VariableElement parameter) {
    this(annotation, parameter, null);
  }

  static String generatePrefix(final TypeMirror mirror, int type) {
    TypeVisitor<String, Integer> visitor = new SimpleTypeVisitor8<String, Integer>() {
      @Override public String visitPrimitive(PrimitiveType primitiveType, Integer type) {
        String prefix = capitalize(primitiveType.getKind().name());
        switch (type) {
          case TYPE_ARRAY:
            return prefix + "Array";
          case TYPE_ARRAY_LIST:
            if (primitiveType.getKind() == TypeKind.INT) {
              return "IntegerArrayList";
            } else {
              return null;//列表不支持其他原生类型,泛型也不支持 boxing
            }
          default:
            return prefix;
        }
      }

      @Override public String visitArray(ArrayType arrayType, Integer type) {
        switch (type) {
          case TYPE_ARRAY:
            return null; //只支持一维数组
          case TYPE_ARRAY_LIST:
            return null;// Bundle 不支持泛型数组
          default:
            return generatePrefix(arrayType.getComponentType(), TYPE_ARRAY);
        }
      }

      @Override public String visitDeclared(DeclaredType declaredType, Integer type) {
        String prefix = null;
        if (isString(declaredType)) {
          prefix = "String";
        } else if (isCharSequence(declaredType)) {
          prefix = "CharSequence";
        } else if (isParcelable(declaredType, false)) {
          prefix = "Parcelable";
        }
        if (prefix != null) {
          switch (type) {
            case TYPE_ARRAY:
              return prefix + "Array";

            case TYPE_ARRAY_LIST:
              return prefix + "ArrayList";
            default:
              return prefix;
          }
        } else {

          if (isArrayList(declaredType)) {
            return generatePrefix(declaredType.getTypeArguments().get(0), TYPE_ARRAY_LIST);
          }
          //拆箱
          try {
            PrimitiveType primitiveType = getTypes().unboxedType(declaredType);
            if (primitiveType != null) {
              return generatePrefix(primitiveType, type);
            }
          } catch (IllegalArgumentException e) {
            // ignore;
          }

          if (isSerializable(declaredType, false)) {
            return "Serializable";
          } else {
            return null;
          }
        }
      }
    };
    return mirror.accept(visitor, type);
  }

  @Override
  public void writeInject(MethodSpec.Builder paper, String activityName, String intentName) {
    String defaultValue = getDefaultValue(getTypeMirror());
    if (defaultValue != null) {// isPrimitive() || isBoxed()
      paper.addStatement("$L.$L = $L.get$LExtra($S,$L)", activityName, getName(), intentName,
          prefix,
          getKey(), getDefaultValue(getTypeMirror()));
    } else {
      if (isSubTypeOfParcelableOrSerializable()) {
        paper.addStatement("$L.$L =($T) $L.get$LExtra($S)", activityName, getName(), parameter,
            intentName,
            prefix,
            getKey());
      } else {
        paper.addStatement("$L.$L = $L.get$LExtra($S)", activityName, getName(), intentName, prefix,
            getKey());
      }
    }
  }

  @Override
  public void doWriteRestore(MethodSpec.Builder paper, String activityName, String bundleName) {
    if (isSubTypeOfParcelableOrSerializable()) {
      paper.addStatement("$L.$L = ($T) $L.get$L($S)", activityName, getName(), parameter,
          bundleName, prefix,
          getKey());
    } else {
      paper.addStatement("$L.$L = $L.get$L($S)", activityName, getName(), bundleName, prefix,
          getKey());
    }
  }

  @Override
  public void doWriteSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    paper.addStatement("$L.put$L($S,$L.$L)", bundleName, prefix,
        getKey(), activityName, getName());
  }

  @Override public void writeSetter(MethodSpec.Builder paper) {
    paper.addStatement("getIntent().putExtra($S,$L)",
        getKey(),
        getName());
  }

  private boolean isSubTypeOfParcelableOrSerializable() {
    return (prefix.equals("Parcelable") && !isParcelable(getTypeMirror(), true))
        || (prefix.equals("Serializable")) && (!isSerializable(getTypeMirror(), true));
  }

  /**
   * @return 返回原生类型或者装箱类型的默认值，其他类型返回 null
   */
  private static String getDefaultValue(TypeMirror typeMirror) {
    switch (typeMirror.getKind()) {
      case BOOLEAN:
        return "false";
      case BYTE:
      case SHORT:
      case INT:
      case LONG:
      case CHAR:
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