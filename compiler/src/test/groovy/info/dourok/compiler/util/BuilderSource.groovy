package info.dourok.compiler.util

import android.app.Activity
import android.content.Intent

/**
 * Helpter to write source
 * Created by tiaolins on 2017/9/26.*/

class BuilderSource extends Source {
  def setters = []

  BuilderSource() {
    className = "com.example.EmptyActivityBuilder"
    addImport("info.dourok.esactivity.BaseActivityBuilder", Activity, Intent, Override)
  }

  BuilderSource setter(paramType, name, imports = [],
      setter = "getIntent().putExtra(\"${name}\",${name});") {
    addImport(*imports)
    setters << """public EmptyActivityBuilder<A> ${name}(${paramType} ${name}) {
          ${setter}
          return this;
        }"""
    return this
  }

  BuilderSource setterMethod(setterMethod, imports = []) {
    addImport(*imports)
    setters << setterMethod
    return this
  }

  BuilderSource hasConsumer() {
    method("""
                @Override
                protected EmptyActivityConsumer<A> getConsumer() {
                  if(consumer == null) {
                    consumer = new EmptyActivityConsumer<>();
                  }
                  return (EmptyActivityConsumer<A>) consumer;
                }""", [Override])
    return this
  }

  @Override
  String toString() {
    """package com.example;
${getImports()}
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
     ${write(setters)}
     ${write(methods)}
}"""
  }
}