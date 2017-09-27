package info.dourok.compiler.generator;

import android.app.Activity;
import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import static info.dourok.compiler.EasyUtils.getElements;

/**
 * Created by tiaolins on 2017/9/13.
 */

public class BuilderUtilGenerator extends Generator {
  private List<TypeElement> activityList;
  private final TypeElement baseActivityBuilder;
  private TypeElement activity;

  public BuilderUtilGenerator(List<TypeElement> activityList,
      PackageElement targetPackage, TypeElement activity,
      TypeElement baseActivityBuilder) {
    super(targetPackage);
    this.activityList = activityList;
    this.activity = activity;
    this.baseActivityBuilder = baseActivityBuilder;
  }

  @Override protected TypeSpec generate() {
    TypeSpec.Builder builder = TypeSpec.classBuilder("BuilderUtil")
        .addModifiers(Modifier.PUBLIC)
        .addField(FieldSpec.builder(
            ParameterizedTypeName.get(ClassName.get(HashMap.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(Activity.class)),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(ClassName.get(baseActivityBuilder)))
            ), "sBuilderMap")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T<>()", HashMap.class)
            .build());

    MethodSpec createBuilder = buildCreateBuilder();
    builder.addMethod(createBuilder)
        .addMethod(buildCreateBuilderWithIntent())
        .addMethod(buildSmallCreate(createBuilder));

    CodeBlock.Builder mapIniter = CodeBlock.builder();

    for (TypeElement element : activityList) {
      addBuilderToMap(mapIniter, element);
      builder.addMethod(buildCreateHelper(element));
    }
    builder.addStaticBlock(mapIniter.build());
    return builder.build();
  }

  private MethodSpec buildCreateBuilder() {
    return MethodSpec.methodBuilder("createBuilder")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addTypeVariable(TypeVariableName.get("A", ClassName.get(activity)))
        .returns(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder),
            WildcardTypeName.subtypeOf(ClassName.get(baseActivityBuilder)),
            TypeVariableName.get("A")))
        .addParameter(TypeVariableName.get("A"), "activity")
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class),
            WildcardTypeName.subtypeOf(ClassName.get(activity))), "clazz")
        .addStatement("return $T.create($L, $L)", ClassName.get(baseActivityBuilder), "activity",
            "clazz")
        .build();
  }

  private MethodSpec buildCreateBuilderWithIntent() {
    return MethodSpec.methodBuilder("createBuilder")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addTypeVariable(TypeVariableName.get("A", ClassName.get(activity)))
        .returns(ParameterizedTypeName.get(ClassName.get(baseActivityBuilder),
            WildcardTypeName.subtypeOf(ClassName.get(baseActivityBuilder)),
            TypeVariableName.get("A")))
        .addParameter(TypeVariableName.get("A"), "activity")
        .addParameter(ClassName.get(Intent.class), "intent")
        .addStatement("return $T.create($L, $L)", ClassName.get(baseActivityBuilder), "activity",
            "intent")
        .build();
  }

  private MethodSpec buildSmallCreate(MethodSpec createBuilder) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("smallCreate")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addTypeVariable(TypeVariableName.get("A", ClassName.get(activity)))
        .addTypeVariable(TypeVariableName.get("T",
            ParameterizedTypeName.get(ClassName.get(baseActivityBuilder), TypeVariableName.get("T"),
                TypeVariableName.get("A"))))
        .returns(TypeVariableName.get("T"))
        .addParameter(TypeVariableName.get("A"), "activity")
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class),
            WildcardTypeName.subtypeOf(ClassName.get(activity))), "clazz");

    builder.beginControlFlow("if ($L.containsKey($L))", "sBuilderMap", "clazz")
        .beginControlFlow("try")
        .addStatement("return (T) $L.get($L).getMethod($S, $T.class).invoke(null,$L)",
            "sBuilderMap", "clazz", "create", ClassName.get(activity), "activity")
        .endControlFlow() // try
        .beginControlFlow("catch ($T e)", ClassName.get(NoSuchMethodException.class))
        .addStatement("e.printStackTrace()")
        .endControlFlow()
        .beginControlFlow("catch ($T e)", ClassName.get(IllegalAccessException.class))
        .addStatement("e.printStackTrace()")
        .endControlFlow()
        .beginControlFlow("catch ($T e)", ClassName.get(InvocationTargetException.class))
        .addStatement("e.printStackTrace()")
        .endControlFlow()
        .endControlFlow() // if
        .addStatement("return (T) $N($L,$L)", createBuilder, "activity", "clazz");

    return builder.build();
  }

  private MethodSpec buildCreateHelper(TypeElement activity) {
    PackageElement pack = getElements().getPackageOf(activity);
    ClassName helper =
        ClassName.get(pack.getQualifiedName().toString(), activity.getSimpleName() + "Helper");
    ClassName activityName = ClassName.get(activity);
    return MethodSpec.methodBuilder("createHelper")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(helper)
        .addParameter(activityName, "activity")
        .addStatement("return new $T($L, true)", helper, "activity")
        .build();
  }

  private void addBuilderToMap(CodeBlock.Builder block, TypeElement activity) {
    PackageElement pack = getElements().getPackageOf(activity);
    ClassName builder =
        ClassName.get(pack.getQualifiedName().toString(), activity.getSimpleName() + "Builder");
    ClassName activityName = ClassName.get(activity);
    block.addStatement("sBuilderMap.put($T.class,$T.class)", activityName, builder);
  }
}
