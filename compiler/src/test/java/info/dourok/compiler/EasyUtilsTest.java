package info.dourok.compiler;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

/**
 * Created by tiaolins on 2017/9/11.
 */
public class EasyUtilsTest {
  @Test
  public void isArrayList() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new ActivityBuilderProcessor())
            .compile(JavaFileObjects.forResource("ActivityBuilder/EmptyActivity.java"));
    assertThat(compilation).succeeded();
  }
}