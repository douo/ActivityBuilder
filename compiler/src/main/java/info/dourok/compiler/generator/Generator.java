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

public abstract class Generator {
  protected TypeElement activity;
  protected TypeElement easyActivity;
  protected PackageElement activityPackage;
  protected TypeSpec typeSpec;

  public Generator(TypeElement activity, TypeElement easyActivity,
      PackageElement activityPackage) {
    this.activity = activity;
    this.easyActivity = easyActivity;
    this.activityPackage = activityPackage;
  }

  public TypeSpec getTypeSpec() {
    if (typeSpec == null) {
      typeSpec = generate();
    }
    return typeSpec;
  }

  protected abstract TypeSpec generate();

  public void write() throws IOException {
    JavaFile.builder(activityPackage.getQualifiedName().toString(), getTypeSpec())
        .build()
        .writeTo(EasyUtils.getFiler());
  }
}
