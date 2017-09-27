package info.dourok.compiler

import com.google.common.collect.ImmutableList
import com.google.testing.compile.JavaSourcesSubjectFactory
import info.dourok.compiler.util.Source
import info.dourok.esactivity.ResultParameter
import info.dourok.esactivity.ResultSet
import spock.lang.Specification

import javax.tools.JavaFileObject

import static com.google.common.truth.Truth.assert_
import static com.google.testing.compile.Compiler.javac
import static com.google.testing.compile.JavaFileObjectSubject.assertThat

class ResultSetSpec extends Specification {
  def "multi result using ResultSet annotation"() {
    given:
    def input = Source.activity()
        .result("""
                @ResultSet(results = { 
                  @Result(name = "date",parameters = {@ResultParameter(name = "date", type = Long.class)}),
                  @Result(name = "text",parameters = {
                                         @ResultParameter(name = "ids", type = ArrayList.class),
                                         @ResultParameter(name = "name", type = Character.class)})                  
                })""", [ResultSet, ResultParameter, ArrayList, Character, Long]).source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forDate(Consumer<Long> dateConsumer) {
                  getConsumer().dateConsumer = (activity, date) -> dateConsumer.accept(date);
                  return this;
                }""", ["info.dourok.esactivity.function.Consumer", Long])
        .method("""
                public EmptyActivityBuilder<A> forDate(BiConsumer<A, Long> dateConsumer) {
                  getConsumer().dateConsumer = dateConsumer;
                  return this;
                }""", ["info.dourok.esactivity.function.BiConsumer"])
        .method(""" 
                public EmptyActivityBuilder<A> forText(BiConsumer<ArrayList, Character> textConsumer) {
                  getConsumer().textConsumer = (activity, ids, name) -> textConsumer.accept(ids, name);
                  return this;
                }""", ["info.dourok.esactivity.function.BiConsumer", ArrayList, Character])
        .method("""
                public EmptyActivityBuilder<A> forText(TriConsumer<A, ArrayList, Character> textConsumer) {
                  getConsumer().textConsumer = textConsumer;
                  return this;
                }""", ["info.dourok.esactivity.function.TriConsumer"])
        .source()
    def helper = Source.helper()
        .resultCode("DATE")
        .method("""
                void resultDate(Long date) {
                  Intent intent = new Intent();
                  intent.putExtra("date",date);
                  activity.setResult(RESULT_DATE,intent);
                }""")
        .method("""
               void finishDate(Long date) {
                 resultDate(date);
                 activity.finish();
               }""")
        .resultCode("TEXT", 2)
        .method("""
                void resultText(ArrayList ids, Character name) {
                  Intent intent = new Intent();
                  RefManager.getInstance().put(intent,"ids",ids);
                  intent.putExtra("name",name);
                  activity.setResult(RESULT_TEXT,intent);
                }""", ["info.dourok.esactivity.RefManager", ArrayList, Character, Long])
        .method("""
               void finishText(ArrayList ids, Character name) {
                 resultText(ids, name);
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("BiConsumer<A,Long> dateConsumer;",
        ["info.dourok.esactivity.function.BiConsumer", Long])
        .method("""
               private boolean processDate(A activity,Intent intent) {
                 if(dateConsumer != null) {
                   Long date = intent.getLongExtra("date",0);
                   dateConsumer.accept(activity,date);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """)
        .field("TriConsumer<A,ArrayList,Character> textConsumer;",
        ["info.dourok.esactivity.function.TriConsumer", ArrayList, Character])
        .method("""
               private boolean processText(A activity,Intent intent) {
                 if(textConsumer != null) {
                   ArrayList ids = RefManager.getInstance().get(intent,"ids");
                   Character name = intent.getCharExtra("name",(char)0);
                   textConsumer.accept(activity,ids,name);
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
                   case RESULT_DATE:
                   return processDate(activity,intent);
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
                 return dateConsumer != null ||textConsumer != null ||super.hasConsumer();
               }
               """)
        .source()
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder, helper, consumer)
  }

  def "ResultSet can be replaced with multi result method"() {
    given:
    def inputUsingResultSet = Source.activity()
        .result("""
                @ResultSet(results = { 
                  @Result(name = "date",parameters = {@ResultParameter(name = "date", type = Long.class)}),
                  @Result(name = "text",parameters = {
                                         @ResultParameter(name = "ids", type = ArrayList.class),
                                         @ResultParameter(name = "name", type = Character.class)})                  
                })""", [ResultSet, ResultParameter, ArrayList, Character, Long]).source()
    def inputUsingResultMethod = Source.activity()
        .resultMethods("""
                      @Result public void resultDate(Long date){}
                      """, [Long])
        .resultMethods("""
                      @Result public void resultText(ArrayList ids, Character name){}""",
        [ResultParameter, ArrayList, Character])
        .source()
    when:

    ImmutableList<JavaFileObject> sourcesWithResultSet =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(inputUsingResultSet).generatedSourceFiles()
    ImmutableList<JavaFileObject> sourcesResultMethod =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(inputUsingResultMethod).generatedSourceFiles()
    then:
    sourcesWithResultSet.eachWithIndex {
      source, idx -> assertThat(source).hasSourceEquivalentTo(sourcesResultMethod.get(idx))
    }
  }
}