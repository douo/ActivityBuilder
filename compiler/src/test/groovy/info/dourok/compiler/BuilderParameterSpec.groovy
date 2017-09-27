package info.dourok.compiler

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.google.testing.compile.JavaSourcesSubjectFactory
import info.dourok.esactivity.TransmitType
import spock.genesis.Gen
import spock.lang.Specification
import spock.lang.Unroll
import info.dourok.compiler.util.Source
import java.security.Key

import static com.google.common.truth.Truth.assert_

final class BuilderParameterSpec extends Specification {

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
    def input = Source.activity().
        param(paramType, "val", imports).
        source()
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


    expect:
    def builder = Source.builder().setter(paramType, "val", imports).source()
    def helper = Source.helper().injectStatement("val", getter).source()

    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper)


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
    def input = Source.activity().
        param(paramType, "val", imports).
        source()


    expect:

    def builder = Source.builder().
        setter(paramType, "val", imports).
        source()

    def helper = Source.helper().injectStatement("val",
        "(${paramType}) intent.get${itf}Extra(\"val\")", imports)
        .source()

    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper)

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
    def input = Source.activity().
        param(paramType, "val", [], key).
        source()
    expect:
    def builder = Source.builder()
        .setter(paramType, "val", [],
        "getIntent().putExtra(\"${key}\",val);").source()
    def helper = Source.helper().injectStatement("val",
        "intent.get${paramType.capitalize()}Extra(\"${key}\",${getDefaultValue(paramType)}")
        .source()
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper)

    where:
    //生成一些非 " 和 \ 的字符
    //本来应该生成些 unicode 字符 https://github.com/mifmif/Generex 有 bug [^\\x00-\\x19] 这样的形式不能支持
    key << Gen.string(~'[#-\\[^-~]*').take(5)
  }

  @Unroll
  def "#paramType with keep"() {
    given:
    def input = Source.activity()
        .param(paramType, "val", [TransmitType, *imports], null, null, "true")
        .source()

    expect:
    def builder = Source.builder()
        .setter(paramType, "val", imports, setter).source()
    def helper = Source.helper()
        .injectStatement("val", getter, helperImports)
        .restoreStatement("val", paramType, "val", ignoreKeep)
        .saveStatement("val", paramType, "val", ignoreKeep).source()
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper)

    where:
    paramType | imports  | helperImports                         | getter                                           | setter                               | ignoreKeep
    "String"  | [String] | []                                    | "intent.getStringExtra(\"val\")"                 | "getIntent().putExtra(\"val\",val);" | false
    "Object"  | [Object] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | true
  }

  @Unroll
  def "#paramType with #transmit should using #setter"() {
    given:
    def input = Source.activity()
        .param(paramType, "val", [TransmitType, *imports], null, transmit)
        .source()

    expect:

    def builder = Source.builder()
        .setter(paramType, "val", imports, setter).source()

    def helper = Source.helper()
        .injectStatement("val",getter,helperImports).source()

    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper)

    where:
    paramType | imports  | helperImports                         | getter                                           | setter                               | transmit
    "String"  | [String] | []                                    | "intent.getStringExtra(\"val\")"                 | "getIntent().putExtra(\"val\",val);" | "TransmitType.AUTO"
    "Object"  | [Object] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | "TransmitType.AUTO"
    "String"  | [String] | ["info.dourok.esactivity.RefManager"] | "RefManager.getInstance().get(activity,\"val\")" | "getRefMap().put(\"val\",val);"      | "TransmitType.REF"
  }

  def "activity with 1 nest generic parameter"(){
      given:
      def paramType = "Map<String,ArrayList<Integer>>"
      def input = Source.activity()
          .param(paramType, "val", [String,Map,ArrayList,Integer])
          .source()

      expect:

      def builder = Source.builder()
          .setter(paramType, "val",  [String,Map,ArrayList,Integer],  "getRefMap().put(\"val\",val);").source()

      def helper = Source.helper()
          .injectStatement("val", "RefManager.getInstance().get(activity,\"val\")",["info.dourok.esactivity.RefManager"]).source()

      assert_()
          .about(JavaSourcesSubjectFactory.javaSources())
          .that(input)
          .processedWith(new ActivityBuilderProcessor())
          .compilesWithoutError()
          .and()
          .generatesSources(builder, helper)

  }

  def "activity with multi builder parameter"() {
    given:
    def params = [[paramType:"String",name:"val0",imports:[String]],
                  [paramType:"String",name:"val1",imports:[String]]]

    def input = Source.activity()
        .with({ params.each{param(it.paramType,it.name,it.imports)};return delegate})
        .source()
    expect:
    def builder = Source.builder()
        .with({params.each{setter(it.paramType,it.name,it.imports)};return delegate})
        .source()

    def helper = Source.helper()
        .with({params.each{
                 injectStatement(it.name,"intent.get${it.paramType}Extra(\"${it.name}\")")}
               return delegate})
        .source()

    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder,helper)
  }
}