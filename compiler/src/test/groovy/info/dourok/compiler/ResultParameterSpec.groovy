package info.dourok.compiler

import com.google.testing.compile.JavaSourcesSubjectFactory
import info.dourok.compiler.util.Source
import info.dourok.esactivity.ResultParameter
import info.dourok.esactivity.TransmitType
import spock.lang.Specification

import static com.google.common.truth.Truth.assert_

class ResultParameterSpec extends Specification {

  def "result parameter can configure TransmitType"() {
    given:
    def input = Source.activity().result("""@Result(name = "text",parameters = {
                         @ResultParameter(name = "ids", type = ArrayList.class),
                         @ResultParameter(name = "name", type = Character.class, transmit = TransmitType.REF)
                  })
               """, [ResultParameter, ArrayList, Character, TransmitType]).source()
    expect:
    def helper = Source.helper()
        .resultCode("TEXT")
        .method("""
                void resultText(ArrayList ids, Character name) {
                  Intent intent = new Intent();
                  RefManager.getInstance().put(intent,"ids",ids);
                  RefManager.getInstance().put(intent,"name",name);
                  activity.setResult(RESULT_TEXT,intent);
                }""", ["info.dourok.esactivity.RefManager", ArrayList, Character])
        .method("""
               void finishText(ArrayList ids, Character name) {
                 resultText(ids, name);
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private BiConsumer<ArrayList,Character> textConsumer;",
        ["info.dourok.esactivity.function.BiConsumer", ArrayList, Character])
        .method("""
               void setTextConsumer(BiConsumer<ArrayList,Character> consumer) {
                 beforeAdd(consumer);
                 this.textConsumer = consumer;
               }
               """)
        .method("""
               private boolean processText(A activity,Intent intent) {
                 if(textConsumer != null) {
                   beforeExecute(activity, textConsumer);
                   ArrayList ids = RefManager.getInstance().get(intent,"ids");
                   Character name = RefManager.getInstance().get(intent,"name");
                   textConsumer.accept(ids,name);
                   afterExecute(activity, textConsumer);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """, ["info.dourok.esactivity.RefManager"])
        .method("""
               @Override
               protected boolean handleResult(A activity, int result, Intent intent) {
                 switch (result) {
                   case RESULT_TEXT:
                   return processText(activity,intent);
                   default:
                   return false;
                 }
               }
               """)
        .method("""
               @Override
               public boolean hasConsumer() {
                 return textConsumer != null ||super.hasConsumer();
               }
               """)
        .source()
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(helper, consumer)
  }

  def "result parameter name can't start with number"() {
    given:
    def input = Source.activity().result("""@Result(name = "text",parameters = {
                         @ResultParameter(name = "1invalid", type = ArrayList.class)
                  })
               """, [ResultParameter, ArrayList, Character, TransmitType]).source()
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .failsToCompile().withErrorContaining("not a valid name: 1invalid")
  }
}