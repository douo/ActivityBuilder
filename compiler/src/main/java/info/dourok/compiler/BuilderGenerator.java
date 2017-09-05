package info.dourok.compiler;

import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultWriter;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by tiaolins on 2017/9/5.
 */

public class BuilderGenerator extends Generator {
  private List<ParameterWriter> parameterList;
  private List<ResultWriter> resultList;
  private final TypeElement baseActivityBuilder;

  public BuilderGenerator(TypeElement activity, TypeElement easyActivity,
      PackageElement activityPackage,
      List<ParameterWriter> parameterList,
      List<ResultWriter> resultList, TypeElement baseActivityBuilder) {
    super(activity, easyActivity, activityPackage);
    this.parameterList = parameterList;
    this.resultList = resultList;
    this.baseActivityBuilder = baseActivityBuilder;
  }

  @Override protected TypeSpec generate() {
    ClassName builderClass = ClassName.get(activityPackage.getQualifiedName().toString(),
        easyActivity.getSimpleName() + "Builder");
    ParameterizedTypeName builderWithParameter =
        ParameterizedTypeName.get(builderClass, TypeVariableName.get("A"));

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(TypeVariableName.get("A"), "activity")
        .addStatement("super($L)", "activity")
        .addStatement("setIntent(new $T($L, $T.class))", Intent.class, "activity", easyActivity)
        .build();

    MethodSpec create = MethodSpec.methodBuilder("create")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addTypeVariable(TypeVariableName.get("A", TypeName.get(activity.asType())))
        .returns(builderWithParameter)
        .addParameter(TypeVariableName.get("A"), "activity")
        .addStatement("return new $T(activity)", builderWithParameter)
        .build();

    MethodSpec self = MethodSpec.methodBuilder("self")
        .addAnnotation(Override.class)
        .returns(builderWithParameter)
        .addModifiers(Modifier.PROTECTED)
        .addStatement("return this")
        .build();

    TypeSpec.Builder builder = TypeSpec.classBuilder(builderClass)
        .addTypeVariable(TypeVariableName.get("A", TypeName.get(activity.asType())))
        .superclass(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder),
            builderWithParameter,
            TypeVariableName.get("A")))
        .addMethod(constructor)
        .addMethod(create)
        .addMethod(self);

    for (ParameterWriter parameterWriter : parameterList) {
      MethodSpec.Builder setter = MethodSpec.methodBuilder(parameterWriter.getName())
          .returns(builderWithParameter)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ClassName.get(parameterWriter.getType()),
              parameterWriter.getName());
      parameterWriter.writeSetter(setter);
      setter.addStatement("return this");
      builder.addMethod(setter.build());
    }

    return builder.build();
  }
}
