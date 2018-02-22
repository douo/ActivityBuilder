package info.dourok.compiler;

import com.google.auto.service.AutoService;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderUtilPackage;
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
import static info.dourok.compiler.EasyUtils.warn;

/** @author tiaolins */
@SupportedAnnotationTypes({
  "info.dourok.esactivity.Builder",
  "info.dourok.esactivity.BuilderUtilPackage"
})
@AutoService(Processor.class)
public class ActivityBuilderProcessor extends AbstractProcessor {

  private ActivityProcessorFactory mFactory;
  private boolean firstRound;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    EasyUtils.getInstance().init(processingEnvironment);
    mFactory = new ActivityProcessorFactory(processingEnvironment);
    firstRound = true;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    try {
      for (Element element : env.getElementsAnnotatedWith(Builder.class)) {
        if (mFactory.isEasyActivity(element)) {
          ActivityProcessorFactory.ActivityProcessor processor =
              mFactory.create((TypeElement) element);
          processor.generate();
        } else {
          warn(
              "annotate " + Builder.class.getName() + " to not Activity subclass make no sense!",
              element);
        }
      }

      // 保证 BuilderUtils 只创建一次
      // 如果在最后一个回合(env.processingOver)创建，编译器貌似不能正确找到新创建的类，会提示找不到符号
      if (firstRound) {
        Set<? extends Element> set = env.getElementsAnnotatedWith(BuilderUtilPackage.class);
        if (set.size() > 1) {
          set.forEach(o -> warn("element annotated with @BuilderUtilPackage:", o));
          error("there are more than one element annotated with @BuilderUtilPackage");
        }

        OptionalConsumer.of(set.stream().findFirst())
            .ifPresent(ele -> mFactory.generateBuilderUtil((PackageElement) ele))
            .ifNotPresent(
                () ->
                    mFactory.generateBuilderUtil(
                        getElements().getPackageElement("info.dourok.esactivity")));
      }
      firstRound = false;
    } catch (IllegalStateException e) {
      // 其他对象通过抛出异常，跳出流程
      // Processor 将其捕获避免 javac 异常
    }
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
