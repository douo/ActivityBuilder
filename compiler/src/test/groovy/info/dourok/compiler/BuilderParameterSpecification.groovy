package info.dourok.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import spock.lang.Specification
import spock.lang.Unroll

final class BuilderParameterSpecification extends Specification {
  @Unroll
  def "activity with 1 #paramType builder parameter"() {
    given:
    def input = JavaFileObjects.forSourceString("com.example.EmptyActivity",
        """
            package com.example;
            import android.app.Activity;
            import info.dourok.esactivity.Builder;
            import info.dourok.esactivity.BuilderParameter;
            @Builder
            public class EmptyActivity extends Activity {
            @BuilderParameter ${paramType} val;
            }
            """)
    def builder = JavaFileObjects.forSourceString("com.example.EmptyActivityBuilder",
        """
            package com.example;
            import android.app.Activity;
            import android.content.Intent;
            import info.dourok.esactivity.BaseActivityBuilder;
            import java.lang.Override;
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
                   getIntent().putExtra("val",val);
                   return this;
                 }
            }
            """)

    def helper = JavaFileObjects.forSourceString("com.example.EmptyActivityHelper",
        """
               package com.example;
               import android.content.Intent;
               import android.os.Bundle;
               
               public class EmptyActivityHelper {
                 private final EmptyActivity activity;
               
                 public EmptyActivityHelper(EmptyActivity activity) {
                   this.activity = activity;
                 }
               
                 void inject() {
                   Intent intent = activity.getIntent();
                   activity.val = intent.get${paramType.capitalize()}Extra("val",${getDefaultValue(paramType)});
                 }
               
                 void restore(Bundle savedInstanceState) {
                 }
               
                 void save(Bundle savedInstanceState) {
                 }
               }
               """)

    expect:
    Truth.assert_()
        .about(JavaSourcesSubjectFactory.javaSources())
        .that([input])
        .processedWith(new ActivityBuilderProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(builder)
        .and()
        .generatesSources(helper)


    where:

    paramType << ["boolean", "byte", "short", "char", "int", "long", "float", "double"]
  }

  def getDefaultValue(typeName) {
    switch (typeName) {
      case "boolean":
        return "false"
      case "byte":
        return "(byte)0"
      case "short":
        return "(short)0"
      case "char":
        return "(char)0"
      case "int":
      case "long":
        return "0"
      case "float":
        return ".0f"
      case "double":
        return ".0"
    }
  }
}