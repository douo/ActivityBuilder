package info.dourok.compiler.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import info.dourok.compiler.EasyUtils;
import java.io.IOException;
import javax.lang.model.element.PackageElement;

/**
 * @author tiaolins
 * @date 2017/9/13
 */
public abstract class Generator {
  protected PackageElement targetPackage;
  protected TypeSpec typeSpec;

  public Generator(PackageElement targetPackage) {
    this.targetPackage = targetPackage;
  }

  public TypeSpec getTypeSpec() {
    if (typeSpec == null) {
      typeSpec = generate();
    }
    return typeSpec;
  }

  protected abstract TypeSpec generate();

  public void write() throws IOException {
    JavaFile.builder(targetPackage.getQualifiedName().toString(), getTypeSpec())
        .build()
        .writeTo(EasyUtils.getFiler());
  }
}
