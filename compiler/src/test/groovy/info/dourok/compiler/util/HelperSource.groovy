package info.dourok.compiler.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Helpter to write source
 * Created by tiaolins on 2017/9/26.*/

class HelperSource extends Source {
  def injects = []
  def restores = []
  def saves = []
  def resultCodes = []

  HelperSource() {
    className = "com.example.EmptyActivityHelper"
    addImport(Intent, Bundle)
  }

  HelperSource injectStatement(name, getter, imports = []) {
    addImport(*imports)
    injects << "activity.${name} = ${getter};"
    return this
  }

  HelperSource restoreStatement(name, prefix, key, ignore = false) {
    if (!ignore) {
      restores << "activity.${name} = savedInstanceState.get${prefix}(\"${key}\");"
    }
    return this
  }

  HelperSource saveStatement(name, prefix, key, ignore = false) {
    if (!ignore) {
      saves << "savedInstanceState.put${prefix}(\"${key}\",activity.${name});"
    }
    return this
  }

  HelperSource resultCode(name, count = 1) {
    addImport(Activity)
    resultCodes << "public static final int RESULT_${name} = Activity.RESULT_FIRST_USER + ${count};"
    return this
  }

  @Override
  String toString() {
    """package com.example;
${getImports()}               
public class EmptyActivityHelper {
  ${write(resultCodes)}

  private final EmptyActivity activity;

  public EmptyActivityHelper(EmptyActivity activity) {
    this(activity, false)
  }

  public EmptyActivityHelper(EmptyActivity activity, boolean autoInject) {
    this.activity = activity;
    if (autoInject) {
      inject();
    }
  }
   ${write(methods)}
  void inject() {
    Intent intent = activity.getIntent();
    ${write(injects)}
  }

  void restore(Bundle savedInstanceState) {
    ${write(restores)}
  }

  void save(Bundle savedInstanceState) {
    ${write(saves)}
  }


}"""
  }
}