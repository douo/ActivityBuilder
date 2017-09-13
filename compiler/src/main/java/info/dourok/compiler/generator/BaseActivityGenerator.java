package info.dourok.compiler.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import info.dourok.compiler.EasyUtils;
import java.io.IOException;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by tiaolins on 2017/9/5.
 */

public abstract class BaseActivityGenerator extends Generator {
  protected TypeElement activity;
  protected TypeElement targetActivity;

  public BaseActivityGenerator(TypeElement activity, TypeElement targetActivity,
      PackageElement targetPackage) {
    super(targetPackage);
    this.activity = activity;
    this.targetActivity = targetActivity;
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
