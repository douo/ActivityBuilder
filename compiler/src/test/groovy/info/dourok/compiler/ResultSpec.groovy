package info.dourok.compiler

import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import info.dourok.compiler.util.Source
import info.dourok.esactivity.ResultParameter
import spock.lang.Specification

import static com.google.common.truth.Truth.assert_

class ResultSpec extends Specification {

  def "result annotate activity without params"() {
    given:
    def input = Source.activity().result("@Result(name = \"text\")")
        .source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forText(Runnable textConsumer) {
                  getConsumer().setTextConsumer(textConsumer);
                  return this;
                }""", [Runnable])
        .source()
    def helper = Source.helper()
        .resultCode("TEXT")
        .method("""
               void resultText() {
                 activity.setResult(RESULT_TEXT);
               }""")
        .method("""
               void finishText() {
                 resultText();
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private Runnable textConsumer;", [Runnable])
        .method("""
               void setTextConsumer(Runnable consumer) {
                 beforeAdd(consumer);
                 this.textConsumer = consumer;
               }
               """)
        .method("""
               private boolean processText(A activity, Intent intent) {
                 if(textConsumer != null) {
                   beforeExecute(activity, textConsumer);
                   textConsumer.run();
                   afterExecute(activity, textConsumer);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """)
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
        .generatesSources(builder, helper, consumer)
  }

  def "result annotate method without params"() {
    given:
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultText(){}
                      """)
        .source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forText(Runnable textConsumer) {
                  getConsumer().setTextConsumer(textConsumer);
                  return this;
                }""", [Runnable])
        .source()
    def helper = Source.helper()
        .resultCode("TEXT")
        .method("""
               void resultText() {
                 activity.setResult(RESULT_TEXT);
               }""")
        .method("""
               void finishText() {
                 resultText();
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private Runnable textConsumer;", [Runnable])
        .method("""
               void setTextConsumer(Runnable consumer) {
                 beforeAdd(consumer);
                 this.textConsumer = consumer;
               }
               """)
        .method("""
               private boolean processText(A activity, Intent intent) {
                 if(textConsumer != null) {
                   beforeExecute(activity, textConsumer);
                   textConsumer.run();
                   afterExecute(activity, textConsumer);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """)
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
        .generatesSources(builder, helper, consumer)
  }

  def "result annotate activity with some params and has boxed type"() {
    given:
    def input = Source.activity().result("""@Result(name = "text",parameters = {
                         @ResultParameter(name = "ids", type = ArrayList.class),
                         @ResultParameter(name = "name", type = Character.class)
                  })
               """, [ResultParameter, ArrayList, Character]).source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forText(BiConsumer<ArrayList, Character> textConsumer) {
                  getConsumer().setTextConsumer(textConsumer);
                  return this;
                }""", ["info.dourok.esactivity.function.BiConsumer", ArrayList, Character])
        .source()
    def helper = Source.helper()
        .resultCode("TEXT")
        .method("""
                void resultText(ArrayList ids, Character name) {
                  Intent intent = new Intent();
                  RefManager.getInstance().put(intent,"ids",ids);
                  intent.putExtra("name",name);
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
                   Character name = intent.getCharExtra("name",(char)0);
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
        .generatesSources(builder, helper, consumer)
  }

  def "result annotate method with a primitive param"() {
    given:
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultDate(long date){}
                      """)
        .source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forDate(Consumer<Long> dateConsumer) {
                  getConsumer().setDateConsumer(dateConsumer);
                  return this;
                }""", ["info.dourok.esactivity.function.Consumer", Long])
        .source()
    def helper = Source.helper()
        .resultCode("DATE")
        .method("""
                void resultDate(long date) {
                  Intent intent = new Intent();
                  intent.putExtra("date",date);
                  activity.setResult(RESULT_DATE,intent);
                }""")
        .method("""
               void finishDate(long date) {
                 resultDate(date);
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private Consumer<Long> dateConsumer;",
        ["info.dourok.esactivity.function.Consumer", Long])
        .method("""
               void setDateConsumer(Consumer<Long> consumer) {
                 beforeAdd(consumer);
                 this.dateConsumer = consumer;
               }
               """)
        .method("""
               private boolean processDate(A activity,Intent intent) {
                 if(dateConsumer != null) {
                   beforeExecute(activity, dateConsumer);
                   long date = intent.getLongExtra("date",0);
                   dateConsumer.accept(date);
                   afterExecute(activity, dateConsumer);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """)
        .method("""
               @Override
               protected boolean handleResult(A activity, int result, Intent intent) {
                 switch (result) {
                   case RESULT_DATE:
                   return processDate(activity,intent);
                   default:
                   return false;
                 }
               }
               """)
        .method("""
               @Override
               public boolean hasConsumer() {
                 return dateConsumer != null ||super.hasConsumer();
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

  def "result annotate method with a nest generic type param"() {
    given:
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultMap(Map<String,ArrayList<Integer>> map){}
                      """, [Map, String, ArrayList, Integer])
        .source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forMap(Consumer<Map<String,ArrayList<Integer>>> mapConsumer) {
                  getConsumer().setMapConsumer(mapConsumer);
                  return this;
                }""", ["info.dourok.esactivity.function.Consumer", Map, String, ArrayList, Integer])

        .source()
    def helper = Source.helper()
        .resultCode("MAP")
        .method("""
                void resultMap(Map<String,ArrayList<Integer>> map) {
                  Intent intent = new Intent();
                  RefManager.getInstance().put(intent,"map",map);
                  activity.setResult(RESULT_MAP,intent);
                }""", ["info.dourok.esactivity.RefManager", Map, String, ArrayList, Integer])
        .method("""
               void finishMap(Map<String,ArrayList<Integer>> map) {
                 resultMap(map);
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private Consumer<Map<String,ArrayList<Integer>>> mapConsumer;",
        ["info.dourok.esactivity.function.Consumer", Map, String, ArrayList, Integer])
        .method("""
               void setMapConsumer(Consumer<Map<String,ArrayList<Integer>>> consumer) {
                 beforeAdd(consumer);
                 this.mapConsumer = consumer;
               }
               """)
        .method("""
               private boolean processMap(A activity,Intent intent) {
                 if(mapConsumer != null) {
                   beforeExecute(activity, mapConsumer);
                   Map<String,ArrayList<Integer>> map = RefManager.getInstance().get(intent,"map");
                   mapConsumer.accept(map);
                   afterExecute(activity, mapConsumer);
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
                   case RESULT_MAP:
                   return processMap(activity,intent);
                   default:
                   return false;
                 }
               }
               """)
        .method("""
               @Override
               public boolean hasConsumer() {
                 return mapConsumer != null ||super.hasConsumer();
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

  def "result annotation mix using in class and method"() {
    given:
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultDate(long date){}
                      """)
        .result("""@Result(name = "text",parameters = {
                         @ResultParameter(name = "ids", type = ArrayList.class),
                         @ResultParameter(name = "name", type = Character.class)
                  })
               """, [ResultParameter, ArrayList, Character])
        .source()
    expect:
    def builder = Source.builder()
        .hasConsumer()
        .method(""" 
                public EmptyActivityBuilder<A> forDate(Consumer<Long> dateConsumer) {
                  getConsumer().setDateConsumer(dateConsumer);
                  return this;
                }""", ["info.dourok.esactivity.function.Consumer", Long])
        .method(""" 
                public EmptyActivityBuilder<A> forText(BiConsumer<ArrayList, Character> textConsumer) {
                  getConsumer().setTextConsumer(textConsumer);
                  return this;
                }""", ["info.dourok.esactivity.function.BiConsumer", ArrayList, Character])
        .source()
    def helper = Source.helper()
        .resultCode("DATE")
        .method("""
                void resultDate(long date) {
                  Intent intent = new Intent();
                  intent.putExtra("date",date);
                  activity.setResult(RESULT_DATE,intent);
                }""")
        .method("""
               void finishDate(long date) {
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
                }""", ["info.dourok.esactivity.RefManager", ArrayList, Character])
        .method("""
               void finishText(ArrayList ids, Character name) {
                 resultText(ids, name);
                 activity.finish();
               }""")
        .source()
    def consumer = Source.consumer()
        .field("private Consumer<Long> dateConsumer;",
        ["info.dourok.esactivity.function.Consumer", Long])
        .method("""
               void setDateConsumer(Consumer<Long> consumer) {
                 beforeAdd(consumer);
                 this.dateConsumer = consumer;
               }
               """)
        .method("""
               private boolean processDate(A activity,Intent intent) {
                 if(dateConsumer != null) {
                   beforeExecute(activity, dateConsumer);
                   long date = intent.getLongExtra("date",0);
                   dateConsumer.accept(date);
                   afterExecute(activity, dateConsumer);
                   return true;
                 }
                 else {
                   return false;
                 }
               }
               """)
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
                   Character name = intent.getCharExtra("name",(char)0);
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

  def "result has more then 2 params should generate a new consumer interface"() {
    given:
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultDate(int year, int month, int day, int hour){}
                      """)
        .resultMethods("""
                      @Result public void resultMany(int i,short s,long l,double d,char c,float f,boolean b,byte _b){}
                      """)
        .source()
    expect:
    def consumer4 = JavaFileObjects.forSourceString("info.dourok.esactivity.function.Consumer4", """
     package info.dourok.esactivity.function;
     public interface Consumer4<T0, T1, T2, T3> {
       void accept(T0 t0, T1 t1, T2 t2, T3 t3);
     }""")
    def consumer9 = JavaFileObjects.forSourceString("info.dourok.esactivity.function.Consumer8", """
     package info.dourok.esactivity.function;
     public interface Consumer8<T0, T1, T2, T3,T4, T5, T6, T7> {
       void accept(T0 t0, T1 t1, T2 t2, T3 t3,T4 t4, T5 t5, T6 t6, T7 t7);
     }""")
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and().generatesSources(consumer4, consumer9)
  }

  def "result annotate method with a illegal name can't compile"() {
    def input = Source.activity()
        .resultMethods("""
                      @Result public void wtf(int year){}
                      """)
        .source()
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .failsToCompile()
        .withErrorContaining("wtf is illegal result method name")
  }

  def "result annotate class name can't be null"() {
    def input = Source.activity().result("@Result")
        .source()
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .failsToCompile()
        .withErrorContaining("Result annotated activity its name must specified")
  }

  def "result has 253 parameter"() {
    // java max method parameter 255 for static and 254 for non-static ( this will be the 255th in this case) methods
    // and we need a parameter leave for activity
    // so the max parameter of result is 254 - 1 = 253
    def max = 253
    def input = Source.activity()
        .resultMethods("""
                      @Result public void resultTest(${(1..max).collect { "int p${it}" }.join(', ')}){}
                      """)
        .source()
    expect:
    assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that(input)
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
  }
}