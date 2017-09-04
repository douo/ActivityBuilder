package info.dourok.compiler;

import android.util.SparseArray;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.util.HashMap;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by tiaolins on 2017/9/5.
 */

public class ConsumerHelper {
  public HashMap<Integer, ClassName> consumers;
  private static ConsumerHelper sInstance = new ConsumerHelper();

  private ConsumerHelper() {
    consumers = new HashMap<>();
    consumers.put(0, ClassName.get(Runnable.class));
    consumers.put(1, ClassName.get("info.dourok.esactivity.function", "Consumer"));
    consumers.put(2, ClassName.get("info.dourok.esactivity.function", "BiConsumer"));
    consumers.put(3, ClassName.get("info.dourok.esactivity.function", "TriConsumer"));
  }

  public static ClassName get(int count) throws IOException {
    ClassName type = sInstance.consumers.get(count);
    if (type == null) {
      type = writeConsumer(count);
      sInstance.consumers.put(count, type);
    }
    return type;
  }

  private static ClassName writeConsumer(int count) throws IOException {
    String packageName = "info.dourok.esactivity.function";
    MethodSpec.Builder method = MethodSpec.methodBuilder("accept");

    TypeSpec.Builder type = TypeSpec.interfaceBuilder("Consumer" + count)
        .addModifiers(Modifier.PUBLIC);
    for (int i = 0; i < count; i++) {
      type.addTypeVariable(TypeVariableName.get("T" + count));
      method.addParameter(
          TypeVariableName.get("T" + count), "t" + count);
    }
    type.addMethod(method.build());
    JavaFile.builder(packageName, type.build())
        .build()
        .writeTo(EasyUtils.getFiler());
    return ClassName.get(packageName, ".Consumer" + count);
  }
}
