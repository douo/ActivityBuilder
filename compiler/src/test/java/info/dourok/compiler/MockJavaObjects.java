package info.dourok.compiler;

import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;

/** Created by tiaolins on 2017/9/23. */
public class MockJavaObjects {
  private static String[] resources =
      new String[] {
        "mock/BaseActivityBuilder.java",
        "mock/BaseBuilder.java",
        "mock/BaseResultConsumer.java",
        "mock/RefManager.java",
        "mock/function/BiConsumer.java",
        "mock/function/Consumer.java",
        "mock/function/TriConsumer.java"
      };

  /** mock library 的类，返回生成代码所要的依赖 */
  public static JavaFileObject[] full(JavaFileObject... objects) {
    return Stream.concat(
            Arrays.stream(objects), Arrays.stream(resources).map(JavaFileObjects::forResource))
        .toArray(JavaFileObject[]::new);
  }
}
