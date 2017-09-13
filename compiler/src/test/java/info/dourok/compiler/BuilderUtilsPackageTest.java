package info.dourok.compiler;

import com.google.testing.compile.Compilation;

import static com.google.common.truth.Truth.*;

import com.google.testing.compile.JavaFileObjects;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderUtilsPackage;
import java.io.IOException;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Created by tiaolins on 2017/9/13.
 */

public class BuilderUtilsPackageTest {
  /**
   * 使用注解 {@link BuilderUtilsPackage} 声明了包，
   * BuilderUtils 应该生成在这个包下面
   */
  @Test
  public void hasAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                JavaFileObjects.forResource("BuilderUtilsPackage/hasAnnotation/package-info.java"));

    //compilation.generatedFiles()
    //    .stream()
    //    .filter(javaFileObject -> javaFileObject.getKind() == JavaFileObject.Kind.SOURCE)
    //    .forEach(javaFileObject -> {
    //      try {
    //        System.out.println(javaFileObject.getCharContent(true));
    //      } catch (IOException e) {
    //        e.printStackTrace();
    //      }
    //    });
    assertThat(compilation).generatedSourceFile("test/BuilderUtils")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("BuilderUtilsPackage/hasAnnotation/BuilderUtils.java"));
  }

  /**
   * 没有注解 {@link Builder} 和 {@link BuilderUtilsPackage}
   * 注解处理不会工作，所以没有任何生成文件
   */
  @Test
  public void withoutAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                JavaFileObjects.forResource(
                    "BuilderUtilsPackage/withoutAnnotation/package-info.java"));
    assertThat(compilation).succeededWithoutWarnings();
    assert_().that(compilation.generatedFiles().asList())
        .isEmpty();
  }

  /**
   * 有多个注释，编译器应该给出错误提示
   */
  @Test
  public void multiAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(
                JavaFileObjects.forResource(
                    "BuilderUtilsPackage/multiAnnotation/package-info.java"),
                JavaFileObjects.forResource(
                    "BuilderUtilsPackage/multiAnnotation/other/package-info.java"));

    assertThat(compilation).hadErrorContaining(
        "there are more than one element annotated with @BuilderUtilsPackage");
  }
}
