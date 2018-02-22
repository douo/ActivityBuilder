package info.dourok.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderUtilPackage;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static info.dourok.compiler.MockJavaObjects.full;

/** Created by tiaolins on 2017/9/13. */
public class BuilderUtilPackageTest {
  /** 使用注解 {@link BuilderUtilPackage} 声明了包， BuilderUtil 应该生成在这个包下面 */
  @Test
  public void hasAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                full(
                    JavaFileObjects.forResource(
                        "BuilderUtilsPackage/hasAnnotation/package-info.java")));

    assertThat(compilation)
        .generatedSourceFile("test/BuilderUtil")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("BuilderUtilsPackage/hasAnnotation/BuilderUtil.java"));
  }

  /** 没有注解 {@link Builder} 和 {@link BuilderUtilPackage} 注解处理不会工作，所以没有任何生成文件 */
  @Test
  public void withoutAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                full(
                    JavaFileObjects.forResource(
                        "BuilderUtilsPackage/withoutAnnotation/package-info.java")));
    assertThat(compilation).succeededWithoutWarnings();
    assert_()
        .that(
            compilation
                .generatedFiles()
                .stream()
                .filter(javaFileObject -> javaFileObject.getKind() == JavaFileObject.Kind.SOURCE)
                .count())
        .isEqualTo(0);
    ;
  }

  /** 有多个注释，编译器应该给出错误提示 */
  @Test
  public void multiAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                full(
                    JavaFileObjects.forResource(
                        "BuilderUtilsPackage/multiAnnotation/package-info.java"),
                    JavaFileObjects.forResource(
                        "BuilderUtilsPackage/multiAnnotation/other/package-info.java")));

    assertThat(compilation)
        .hadErrorContaining("there are more than one element annotated with @BuilderUtilPackage");
  }
}
