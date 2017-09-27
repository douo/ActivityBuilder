package info.dourok.compiler

import com.google.common.collect.ImmutableList
import info.dourok.compiler.util.Source
import info.dourok.esactivity.ResultParameter
import info.dourok.esactivity.Transmit
import info.dourok.esactivity.TransmitType
import spock.lang.Specification

import javax.tools.JavaFileObject;
import static com.google.common.truth.Truth.assert_
import static com.google.testing.compile.Compiler.javac
import static com.google.testing.compile.JavaFileObjectSubject.assertThat

/**
 * See {@link ResultParameterSpec}
 * Created by tiaolins on 2017/9/27.*/

class TransmitSpec extends Specification{
  def "Transmit can configure TransmitType in result annotated method parameter"() {
    given:
    def usingTransmit = Source.activity()
        .resultMethods("""
               @Result public void resultText(@Transmit(TransmitType.REF) Character name){}
               """, [Transmit, Character, TransmitType]).source()
    def usingResultParameter = Source.activity().result("""@Result(name = "text",parameters = {
                         @ResultParameter(name = "name", type = Character.class, transmit = TransmitType.REF)
                  })
               """, [ResultParameter, Character, TransmitType]).source()
    when:
    ImmutableList<JavaFileObject> sourcesTransmit =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(usingTransmit).generatedSourceFiles()
    ImmutableList<JavaFileObject> sourcesResultParameter =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(usingResultParameter).generatedSourceFiles()

    then: "ResultParameter 指定 TransmitType 已经测试通过，Transmit 注解方法参数应该达到同样效果"

    sourcesTransmit.eachWithIndex {
      source, idx -> assertThat(source).hasSourceEquivalentTo(sourcesResultParameter.get(idx))
    }
  }
}
