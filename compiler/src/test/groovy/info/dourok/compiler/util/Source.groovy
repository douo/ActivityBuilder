package info.dourok.compiler.util

import com.google.testing.compile.JavaFileObjects

/**
 * Helpter to write source
 * Created by tiaolins on 2017/9/26.*/

class Source {
  final static def _ = null
  def imports = []
  def methods = []
  def fields = []
  def className

  def addImport(def ... classes) {
    imports.addAll(classes)
  }

  Source method(method, imports = []) {
    addImport(*imports)
    methods << method
    return this
  }

  Source field(field, imports = []) {
    addImport(*imports)
    fields << field
    return this
  }

  def getImports() {
    writeImports(*imports)
  }

  def source() {
    JavaFileObjects.forSourceString(className, toString())
  }

  static def write(codes) {
    codes.join('\n')
  }

  /**
   * 生成简单注解的文法
   * @param annotation 注解类
   * @param elements 注解参数的键值对
   * @return
   */
  static String writeAnnotation(Class annotation, Map<String, String> elements) {
    String eleStr = elements
        .grep { it.value }
        .collect { "${it.key} = ${it.value}" }
        .join(', ')
    if (eleStr) {
      "@${annotation.simpleName}(${eleStr})"
    } else {
      "@${annotation.simpleName}"
    }
  }

  /**
   * 生成 import
   * 因为 javapoet 会生成 java.lang.* 的 import
   * compile-testing 要求 import 的顺序一致（字符串顺序）
   * 所以需要一个 helper 方法来处理这个问题
   * @param classes 支持 {@link Class} 或 {@link String} 表示类名
   * @return
   */
  static def writeImports(def ... classes) {
    classes
        .collect({ it instanceof String ? it : it.name })
        .unique(false)
        .sort()
        .collect({ "import ${it};" })
        .join("\n")
  }

  static Source source(name) {
    return new Source(className: name)
  }

  static ActivitySource activity() {
    return new ActivitySource()
  }

  static BuilderSource builder() {
    return new BuilderSource()
  }

  static HelperSource helper() {
    return new HelperSource()
  }

  static ConsumerSource consumer() {
    return new ConsumerSource()
  }
}