package info.dourok.compiler.util

import android.app.Activity
import com.google.common.collect.ImmutableList
import info.dourok.compiler.MockJavaObjects
import info.dourok.esactivity.Builder
import info.dourok.esactivity.BuilderParameter
import info.dourok.esactivity.Result
import info.dourok.esactivity.ResultParameter

import javax.tools.JavaFileObject

/**
 * Created by tiaolins on 2017/9/26.*/

class ActivitySource extends Source {

  def params = []
  def results = []
  def resultMethods = []

  ActivitySource() {
    className = "com.example.EmptyActivity"
    addImport(Activity, Builder)
  }

  ActivitySource param(paramType, name, imports = [], key = Source._, transmit = Source._,
      keep = Source._) {
    addImport(BuilderParameter, *imports)
    def annotation = writeAnnotation(BuilderParameter,
        [key: key ? "\"${key}\"" : Source._, transmit: transmit, keep: keep])
    params << "${annotation} ${paramType} ${name};"
    return this
  }

  ActivitySource paramStatment(paramStatement, imports = []) {
    addImport(BuilderParameter, *imports)
    params << paramStatement
    return this
  }

  ActivitySource result(resultStatement, imports = []) {
    addImport(Result, ResultParameter, *imports)
    results << resultStatement
    return this
  }

  ActivitySource resultMethods(resultMethod, imports = []) {
    addImport(Result, *imports)
    resultMethods << resultMethod
    return this
  }

  ImmutableList<JavaFileObject> source() {
    ImmutableList.copyOf(MockJavaObjects.full(super.source()))
  }

  @Override
  String toString() {
    """package com.example;
${getImports()}
@Builder
${write(results)}
public class EmptyActivity extends Activity {
${write(params)}
${write(resultMethods)}
}
"""
  }
}