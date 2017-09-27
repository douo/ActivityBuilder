package info.dourok.compiler.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Helpter to write source
 * Created by tiaolins on 2017/9/26.*/

class ConsumerSource extends Source {

  ConsumerSource() {
    className = "com.example.EmptyActivityConsumer"
    addImport(Activity, Intent, Override, "info.dourok.esactivity.BaseResultConsumer")
  }

  @Override
  String toString() {
    """package com.example;
import static com.example.EmptyActivityHelper.*;
${getImports()}

class EmptyActivityConsumer<A extends Activity> extends BaseResultConsumer<A> {
  ${write(fields)}
  ${write(methods)}
}
"""
  }
}