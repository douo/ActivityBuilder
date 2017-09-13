package info.dourok.compiler;

import com.google.auto.service.AutoService;
import info.dourok.esactivity.Builder;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static info.dourok.compiler.EasyUtils.warn;

@SupportedAnnotationTypes("info.dourok.esactivity.Builder")
@AutoService(Processor.class)
public class EasyStartActivityProcessor extends AbstractProcessor {

  private ActivityProcessorFactory mFactory;

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

    return false;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
