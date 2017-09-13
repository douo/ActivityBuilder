package info.dourok.compiler;

import com.google.auto.service.AutoService;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderUtilsPackage;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import static info.dourok.compiler.EasyUtils.error;
import static info.dourok.compiler.EasyUtils.getElements;
import static info.dourok.compiler.EasyUtils.log;
import static info.dourok.compiler.EasyUtils.warn;

@SupportedAnnotationTypes({
    "info.dourok.esactivity.Builder", "info.dourok.esactivity.BuilderUtilsPackage"
})
@AutoService(Processor.class)
public class ActivityBuilderProcessor extends AbstractProcessor {

  private ActivityProcessorFactory mFactory;
  private Optional<PackageElement> builderUtilPackage = Optional.empty();

  @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    EasyUtils.getInstance().init(processingEnvironment);
    mFactory = new ActivityProcessorFactory(processingEnvironment);
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations,
      RoundEnvironment env) {

    for (Element element : env.getElementsAnnotatedWith(Builder.class)) {
      if (mFactory.isEasyActivity(element)) {
        ActivityProcessorFactory.ActivityProcessor processor = mFactory.create(
            (TypeElement) element);
        processor.generate();
      } else {
        warn("annotate "
            + Builder.class.getName()
            + " to not Activity subclass make no sense!", element);
      }
    }

    Set<? extends Element> set = env.getElementsAnnotatedWith(BuilderUtilsPackage.class);
    if (set.size() > 1) {
      set.forEach(o -> warn("element annotated with @BuilderUtilsPackage:", o));
      error("there are more than one element annotated with @BuilderUtilsPackage");
    }

    set.stream().findFirst().ifPresent(
        ele -> {
          builderUtilPackage.ifPresent(packageElement -> warn(
              "there are more than one element annotated with @BuilderUtilsPackage",
              packageElement));
          builderUtilPackage = Optional.of((PackageElement) ele);
        }
    );
    // 在最后一次循环才创建 BuilderUtils
    if (env.processingOver()) {
      OptionalConsumer.of(builderUtilPackage)
          .ifPresent(ele -> mFactory.generateBuilderUtil(ele))
          .ifNotPresent(() -> mFactory.generateBuilderUtil(
              getElements().getPackageElement("info.dourok.esactivity")));
    }
    return false;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
