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
  private boolean firstRound;

  @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    EasyUtils.getInstance().init(processingEnvironment);
    mFactory = new ActivityProcessorFactory(processingEnvironment);
    firstRound = true;
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

    // 保证 BuilderUtils 只创建一次
    // 如果在最后一个回合(env.processingOver)创建，编译器貌似不能正确找到新创建的类，会提示找不到符号
    if (firstRound) {
      Set<? extends Element> set = env.getElementsAnnotatedWith(BuilderUtilsPackage.class);
      if (set.size() > 1) {
        set.forEach(o -> warn("element annotated with @BuilderUtilsPackage:", o));
        error("there are more than one element annotated with @BuilderUtilsPackage");
      }

      OptionalConsumer.of(set.stream().findFirst())
          .ifPresent(ele -> mFactory.generateBuilderUtil((PackageElement) ele))
          .ifNotPresent(() -> mFactory.generateBuilderUtil(
              getElements().getPackageElement("info.dourok.esactivity")));
    }
    firstRound = false;
    return false;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
