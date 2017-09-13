package info.dourok.esactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指明生成 BuilderUtils 的包
 * 默认情况是位于 {@link info.dourok.esactivity}
 * 但是如果依赖的库也使用了 ActivityBuilder
 * 那就会造成冲突
 * 使用对于库来说这个注解是建议用着库的根包
 * 只允许使用一次
 * Created by tiaolins on 2017/9/13.
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.PACKAGE })
public @interface BuilderUtilsPackage {
}
