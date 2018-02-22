package info.dourok.esactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** marked a activity, ActivityBuilder will generate the builder and the helper class for it */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Builder {}
