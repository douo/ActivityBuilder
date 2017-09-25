package info.dourok.compiler

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.google.common.collect.ImmutableList
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import info.dourok.esactivity.Builder
import info.dourok.esactivity.BuilderParameter
import info.dourok.esactivity.TransmitType
import spock.genesis.Gen
import spock.lang.Specification
import spock.lang.Unroll

import java.security.Key

import static com.google.common.truth.Truth.assert_
import static info.dourok.compiler.MockJavaObjects.full

final class BuilderParameterSpec extends Specification {

  /**
   * 生成 import
   * 因为 javapoet 会生成 java.lang.* 的 import
   * compile-testing 要求 import 的顺序一致（字符串顺序）
   * 所以需要一个 helper 方法来处理这个问题
   * @param classes 支持 {@link Class} 或 {@link String} 表示类名
   * @return
   */
  def buildImports(def ... classes) {
    classes
        .collect({ it instanceof String ? it : it.name })
        .sort()
        .collect({ "import ${it};" })
        .join("\n")
  }

  def inputSource(paramType, imports = [], annotation = "@BuilderParameter") {
    JavaFileObjects.forSourceString("com.example.EmptyActivity",
        """
            package com.example;
            ${buildImports(Activity, Builder, BuilderParameter, *imports)}
            @Builder
            public class EmptyActivity extends Activity {
            ${annotation} ${paramType} val;
            }
            """)
  }

  def builderSource(paramType, imports = [], setter = "getIntent().putExtra(\"val\",val);") {

    JavaFileObjects.forSourceString("com.example.EmptyActivityBuilder",
        """
            package com.example;
            ${
          buildImports("info.dourok.esactivity.BaseActivityBuilder", Activity, Intent, Override,
              *imports)
        }
            public class EmptyActivityBuilder<A extends Activity> extends BaseActivityBuilder<EmptyActivityBuilder<A>, A> {
                 
                 private EmptyActivityBuilder(A activity) {
                    super(activity);
                    setIntent(new Intent(activity, EmptyActivity.class));
                 }

                 public static <A extends Activity> EmptyActivityBuilder<A> create(A activity) {
                   return new EmptyActivityBuilder<A>(activity);
                 }
               
                 @Override
                 protected EmptyActivityBuilder<A> self() {
                   return this;
                 }
                 public EmptyActivityBuilder<A> val(${paramType} val) {
                   ${setter}
                   return this;
                 }
            }
            """)
  }

  def helperSource(getter, imports = [], restore = "", save = "") {
    JavaFileObjects.forSourceString("com.example.EmptyActivityHelper",
        """
               package com.example;
${buildImports(Intent, Bundle, *imports)}               
               public class EmptyActivityHelper {
                 private final EmptyActivity activity;
               
                 public EmptyActivityHelper(EmptyActivity activity) {
                   this.activity = activity;
                 }
               
                 void inject() {
                   Intent intent = activity.getIntent();
                   activity.val = ${getter};
                 }
               
                 void restore(Bundle savedInstanceState) {
                   ${restore}
                 }
               
                 void save(Bundle savedInstanceState) {
                   ${save}
                 }
               }
               """)
  }

  def getDefaultValue(typeName) {
    switch (typeName.toLowerCase()) {
      case "boolean":
        return "false"
      case "byte":
        return "(byte)0"
      case "short":
        return "(short)0"
      case "char":
      case "character":
        return "(char)0"
      case "int":
      case "integer":
      case "long":
        return "0"
      case "float":
        return ".0f"
      case "double":
        return ".0"
    }
  }

  @Unroll
  def "activity with 1 #category builder parameter: #paramType"() {
    given:
    // import order isn't matter, but compile-testing comparing each line
    def input = inputSource(paramType, imports)
    def builder = builderSource(paramType, imports)
    def getter
    switch (category) {
      case "primitive":
        getter = "intent.get${paramType.capitalize()}Extra(\"val\",${getDefaultValue(paramType)})"
        break
      case "boxed":
        getter =
            "intent.get${paramType.with({ it in "Character" ? "Char" : it in "Integer" ? "Int" : it.toLowerCase().capitalize() })}Extra(\"val\",${getDefaultValue(paramType)})"
        break
      case "array":
        getter = "intent.get${paramType[0..-3].capitalize()}ArrayExtra(\"val\")"
        break
      case "list":
      case "normal":
      default:
        getter = "intent.get${imports.simpleName.reverse().join("")}Extra(\"val\")"
    }

    def helper = helperSource(getter)

    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(ImmutableList.copyOf(full(input)))
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)


    where:
    paramType                 | imports                   | category
    // primitive
    "boolean"                 | []                        | "primitive"
    "byte"                    | []                        | "primitive"
    "short"                   | []                        | "primitive"
    "char"                    | []                        | "primitive"
    "int"                     | []                        | "primitive"
    "long"                    | []                        | "primitive"
    "float"                   | []                        | "primitive"
    "double"                  | []                        | "primitive"
    // boxed type
    "Boolean"                 | [Boolean]                 | "boxed"
    "Byte"                    | [Byte]                    | "boxed"
    "Short"                   | [Short]                   | "boxed"
    "Character"               | [Character]               | "boxed"
    "Integer"                 | [Integer]                 | "boxed"
    "Long"                    | [Long]                    | "boxed"
    "Float"                   | [Float]                   | "boxed"
    "Double"                  | [Double]                  | "boxed"
    // array
    "boolean[]"               | []                        | "array"
    "byte[]"                  | []                        | "array"
    "short[]"                 | []                        | "array"
    "char[]"                  | []                        | "array"
    "int[]"                   | []                        | "array"
    "long[]"                  | []                        | "array"
    "float[]"                 | []                        | "array"
    "double[]"                | []                        | "array"
    "String[]"                | [String]                  | "array"
    "CharSequence[]"          | [CharSequence]            | "array"
    "Parcelable[]"            | [Parcelable]              | "array"
    // normal
    "String"                  | [String]                  | "normal"
    "CharSequence"            | [CharSequence]            | "normal"
    "Parcelable"              | [Parcelable]              | "normal"
    "Serializable"            | [Serializable]            | "normal"
    "Bundle"                  | [Bundle]                  | "normal"
    // ArrayList
    "ArrayList<String>"       | [ArrayList, String]       | "list"
    "ArrayList<CharSequence>" | [ArrayList, CharSequence] | "list"
    "ArrayList<Parcelable>"   | [ArrayList, Parcelable]   | "list"
    "ArrayList<Integer>"      | [ArrayList, Integer]      | "list"
  }

  @Unroll
  def "activity with 1 builder parameter #paramType implementation of #itf"() {
    given:
    def input = inputSource(paramType, imports)
    def builder = builderSource(paramType, imports)
    def helper = helperSource("(${paramType}) intent.get${itf}Extra(\"val\")", imports)

    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(ImmutableList.copyOf(full(input)))
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)


    where:
    paramType | imports  | itf
    "Bitmap"  | [Bitmap] | "Parcelable"
    "Rect"    | [Rect]   | "Parcelable"
    "Uri"     | [Uri]    | "Parcelable"
    "Key"     | [Key]    | "Serializable"
  }

  def "activity with 1 builder parameter with custom key"() {
    given:
    def paramType = "int"
    def input = inputSource(paramType, [], "@BuilderParameter(key = \"${key}\")")
    def builder = builderSource(paramType, [], "getIntent().putExtra(\"${key}\",val);")
    def helper = helperSource(
        "intent.get${paramType.capitalize()}Extra(\"${key}\",${getDefaultValue(paramType)})",
        [])
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(ImmutableList.copyOf(full(input)))
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)

    where:
    //生成一些非 " 和 \ 的字符
    //本来应该生成些 unicode 字符 https://github.com/mifmif/Generex 有 bug [^\\x00-\\x19] 这样的形式不能支持
    key << Gen.string(~'[#-\\[^-~]*').take(5)
  }

  @Unroll
  def "#paramType with keep"() {
    given:
    def input = inputSource(paramType, [TransmitType, *imports],
        "@BuilderParameter(keep = true)")
    def builder = builderSource(paramType, imports, setter)
    def helper = helperSource(getter, helperImports, restore, save)
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(ImmutableList.copyOf(full(input)))
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)
    where:
    paramType | imports  | helperImports                         | getter                                           | setter                               | restore                                                 | save
    "String"  | [String] | []                                    | "intent.getStringExtra(\"val\")"                 | "getIntent().putExtra(\"val\",val);" | "activity.val = savedInstanceState.getString(\"val\");" | "savedInstanceState.putString(\"val\",activity.val);"
    "Object"  | [Object] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | ""                                                      | ""
  }

  @Unroll
  def "#paramType with #transmit should using #setter"() {
    given:
    def input = inputSource(paramType, [TransmitType, *imports],
        "@BuilderParameter(transmit = ${transmit})")
    def builder = builderSource(paramType, imports, setter)
    def helper = helperSource(getter, helperImports)
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(ImmutableList.copyOf(full(input)))
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)

    where:
    paramType | imports  | helperImports                         | getter                                           | setter                               | transmit
    "String"  | [String] | []                                    | "intent.getStringExtra(\"val\")"                 | "getIntent().putExtra(\"val\",val);" | "TransmitType.AUTO"
    "Object"  | [Object] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | "TransmitType.AUTO"
    "String"  | [String] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | "TransmitType.REF"
  }

  def "activity with multi builder parameter"(){
     //TODO
  }
}