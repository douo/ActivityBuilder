package info.dourok.compiler;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

/**
 * Created by tiaolins on 2017/9/11.
 */
public class ActivityBuilderTest {
  @Test
  public void emptyActivity() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivity.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation).generatedSourceFile("test/EmptyActivityBuilder")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivityBuilder.java"));
    assertThat(compilation).generatedSourceFile("test/EmptyActivityHelper")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/EmptyActivityHelper.java"));
    assertThat(compilation).generatedSourceFile("info/dourok/esactivity/BuilderUtils")
        .hasSourceEquivalentTo(
            JavaFileObjects.forResource("ActivityBuilder/emptyActivity/BuilderUtils.java"));
  }
}