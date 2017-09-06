package info.dourok.compiler;

import android.app.Activity;
import info.dourok.compiler.generator.BuilderGenerator;
import info.dourok.compiler.generator.ConsumerGenerator;
import info.dourok.compiler.generator.HelperGenerator;
import info.dourok.compiler.parameter.ParameterWriter;
import info.dourok.compiler.result.ResultModel;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.EasyActivity;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.ResultSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.EasyUtils.log;

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
    private List<ResultModel> resultList;

    public ActivityProcessor(TypeElement element) {
      easyActivity = element;
      packageElement = elements.getPackageOf(element);
      findAnnotations();
    }

    private void findAnnotations() {
      parameterList = new LinkedList<>();
      resultList = new LinkedList<>();
      for (Element element : easyActivity.getEnclosedElements()) {
        //find parameters
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
            resultList.add(new ResultModel(result, (ExecutableElement) element));
          }
        }
      }


      //处理直接注解于 Activity 的 Result 注解
      TypeMirror result = getElements().getTypeElement(
          Result.class.getName()).asType();
      TypeMirror resultSet = getElements().getTypeElement(
          ResultSet.class.getName()).asType();

      for (AnnotationMirror annotationMirror : easyActivity.getAnnotationMirrors()) {
        if (annotationMirror.getAnnotationType().equals(result)) {
          resultList.add(new ResultModel(annotationMirror));
        }
        if (annotationMirror.getAnnotationType().equals(resultSet)) {
          annotationMirror.getElementValues().forEach((o, o2) -> {
            if (o.getSimpleName().toString().equals("results")) {
              List<AnnotationMirror> list = (List<AnnotationMirror>) o2.getValue();
              for (AnnotationMirror mirror : list) {
                resultList.add(new ResultModel(mirror));
              }
            }
          });
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
              resultList, baseActivityBuilder,
              needCustomConsumer ? consumerGenerator.getTypeSpec() : null);
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
