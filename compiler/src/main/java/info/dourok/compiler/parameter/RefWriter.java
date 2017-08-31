package info.dourok.compiler.parameter;

import com.squareup.javapoet.MethodSpec;
import info.dourok.esactivity.ActivityParameter;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static info.dourok.compiler.EasyUtils.getElements;

/**
 * Created by tiaolins on 2017/8/31.
 */

public class RefWriter extends ActivityParameterWriter {
  private final TypeElement refManager;

  public RefWriter(ActivityParameter annotation,
      VariableElement parameter) {
    super(annotation, parameter);
    refManager =
        getElements().getTypeElement("info.dourok.esactivity.RefManager");
  }

  @Override
  public void writeInject(MethodSpec.Builder paper, String activityName, String intentName) {
    paper.addStatement("$L.$L = $T.getInstance().get($L,$S)", activityName, getName(), refManager,
        activityName, getKey());
  }

  @Override
  protected void doWriteRestore(MethodSpec.Builder paper, String activityName, String bundleName) {
    //DO NOT SUPPORT KEEP member
  }

  @Override
  protected void doWriteSave(MethodSpec.Builder paper, String activityName, String bundleName) {
    //DO NOT SUPPORT KEEP member
  }

  @Override public void writeSetter(MethodSpec.Builder paper) {
    paper.addStatement("getRefMap().put($S,$L)", getKey(), getName());
  }
}
