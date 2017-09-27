package info.dourok.compiler;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static info.dourok.compiler.MockJavaObjects.full;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

/**
 * Created by tiaolins on 2017/9/11.
 */
public class BuilderTest {
  /**
   * 使用 Builder 注解了 Activity
   * 应该生成 [Activity]Builder 和 [Activity]Helper 两个类
   *
   * @throws Exception
   */
  @Test
  public void emptyActivity() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(full(
                JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivity.java")));
    assertThat(compilation).succeeded();
    assertThat(compilation).generatedSourceFile("test/EmptyActivityBuilder")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivityBuilder.java"));
    assertThat(compilation).generatedSourceFile("test/EmptyActivityHelper")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivityHelper.java"));
    assertThat(compilation).generatedSourceFile("info/dourok/esactivity/BuilderUtil")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/BuilderUtil.java"));
  }
}