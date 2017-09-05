package info.dourok.compiler;

import android.app.Activity;
import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultWriter;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.EasyActivity;
import info.dourok.esactivity.Result;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by tiaolins on 2017/8/30.
 */

public class ActivityProcessorFactory {
  private Filer filer;
  private Messager messager;
  private Types types;
  private Elements elements;
  private final TypeElement baseActivityBuilder;
  private final TypeElement baseResultConsumer;
  private final TypeElement activity;

  public ActivityProcessorFactory(ProcessingEnvironment processingEnvironment) {

    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    types = processingEnvironment.getTypeUtils();
    elements = processingEnvironment.getElementUtils();

    activity = elements.getTypeElement(Activity.class.getName());
    baseActivityBuilder =
        elements.getTypeElement("info.dourok.esactivity.BaseActivityBuilder");
    baseResultConsumer =
        elements.getTypeElement("info.dourok.esactivity.BaseResultConsumer");
  }

  public boolean isEasyActivity(Element element) {
    return element.getAnnotation(EasyActivity.class) != null
        && element instanceof TypeElement
        && types.isSubtype(element.asType(), activity.asType());
  }

  public ActivityProcessor create(TypeElement element) {
    return new ActivityProcessor(element);
  }

  public class ActivityProcessor {
    private TypeElement easyActivity;
    private PackageElement packageElement;
    private List<ParameterWriter> parameterList;
    private List<ResultWriter> resultList;

    public ActivityProcessor(TypeElement element) {
      easyActivity = element;
      packageElement = elements.getPackageOf(element);
      findAnnotations();
    }

    private void findAnnotations() {
      parameterList = new LinkedList<>();
      resultList = new LinkedList<>();
      for (Element element : easyActivity.getEnclosedElements()) {
        //find paramters
        if (element.getKind() == ElementKind.FIELD) {
          ActivityParameter activityParameter = element.getAnnotation(ActivityParameter.class);
          if (activityParameter != null) {
            parameterList.add(ParameterWriter.newWriter(activityParameter,
                (VariableElement) element));
          }
        }
        //Result 注解的方法
        if (element.getKind() == ElementKind.METHOD) {
          Result result = element.getAnnotation(Result.class);
          if (result != null) {
            resultList.add(ResultWriter.newWriter(result, (ExecutableElement) element));
          }
        }
      }
    }

    public void generate() {
      boolean needCustomConsumer = !resultList.isEmpty();
      ConsumerGenerator consumerGenerator = null;
      HelperGenerator helperGenerator = new HelperGenerator(activity, easyActivity, packageElement,
          parameterList,
          resultList);
      //需要自定义结果时，才需要子类化 BaseResultConsumer
      if (needCustomConsumer) {
        consumerGenerator =
            new ConsumerGenerator(activity, easyActivity, packageElement, baseResultConsumer,
                helperGenerator.getTypeSpec(),
                resultList);
      }
      BuilderGenerator builderGenerator =
          new BuilderGenerator(activity, easyActivity, packageElement,
              parameterList,
              resultList, baseActivityBuilder);
      try {
        builderGenerator.write();
        helperGenerator.write();
        if (needCustomConsumer) {
          consumerGenerator.write();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
