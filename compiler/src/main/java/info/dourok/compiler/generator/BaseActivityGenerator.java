package info.dourok.compiler.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import info.dourok.compiler.EasyUtils;
import java.io.IOException;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/** @author tiou */
public abstract class BaseActivityGenerator extends Generator {
  protected TypeElement activity;
  protected TypeElement targetActivity;

  public BaseActivityGenerator(
      TypeElement activity, TypeElement targetActivity, PackageElement targetPackage) {
    super(targetPackage);
    this.activity = activity;
    this.targetActivity = targetActivity;
    this.targetPackage = targetPackage;
  }

  @Override public TypeSpec getTypeSpec() {
    if (typeSpec == null) {
      typeSpec = generate();
    }
    return typeSpec;
  }

  @Override protected abstract TypeSpec generate();

  @Override public void write() throws IOException {
    JavaFile.builder(targetPackage.getQualifiedName().toString(), getTypeSpec())
        .build()
        .writeTo(EasyUtils.getFiler());
  }
}
