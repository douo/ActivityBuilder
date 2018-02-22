package info.dourok.esactivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author tiaolins
 * @date 2017/8/6
 */
public class IntentWrapper<T extends BaseBuilder> {
  private T baseBuilder;
  private final Intent intent;

  public IntentWrapper(T baseBuilder, Intent intent) {
    this.baseBuilder = baseBuilder;
    this.intent = intent;
  }

  public T asBuilder() {
    return baseBuilder;
  }

  public Intent getIntent() {
    return intent;
  }

  // generate

  public IntentWrapper<T> setAction(String action) {
    intent.setAction(action);
    return this;
  }

  public IntentWrapper<T> setData(Uri data) {
    intent.setData(data);
    return this;
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  public IntentWrapper<T> setDataAndNormalize(Uri data) {
    intent.setDataAndNormalize(data);
    return this;
  }

  public IntentWrapper<T> setType(String type) {
    intent.setType(type);
    return this;
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  public IntentWrapper<T> setTypeAndNormalize(String type) {
    intent.setTypeAndNormalize(type);
    return this;
  }

  public IntentWrapper<T> setDataAndType(Uri data, String type) {
    intent.setDataAndType(data, type);
    return this;
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  public IntentWrapper<T> setDataAndTypeAndNormalize(Uri data, String type) {
    intent.setDataAndTypeAndNormalize(data, type);
    return this;
  }

  public IntentWrapper<T> addCategory(String category) {
    intent.addCategory(category);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, boolean value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, byte value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, char value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, short value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, int value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, long value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, float value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, double value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, String value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, CharSequence value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, Parcelable value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, Parcelable[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putParcelableArrayListExtra(
      String name, ArrayList<? extends Parcelable> value) {
    intent.putParcelableArrayListExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putIntegerArrayListExtra(String name, ArrayList<Integer> value) {
    intent.putIntegerArrayListExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putStringArrayListExtra(String name, ArrayList<String> value) {
    intent.putStringArrayListExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putCharSequenceArrayListExtra(
      String name, ArrayList<CharSequence> value) {
    intent.putCharSequenceArrayListExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, Serializable value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, boolean[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, byte[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, short[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, char[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, int[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, long[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, float[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, double[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, String[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, CharSequence[] value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtra(String name, Bundle value) {
    intent.putExtra(name, value);
    return this;
  }

  public IntentWrapper<T> putExtras(Intent src) {
    intent.putExtras(src);
    return this;
  }

  public IntentWrapper<T> putExtras(Bundle extras) {
    intent.putExtras(extras);
    return this;
  }

  public IntentWrapper<T> replaceExtras(Intent src) {
    intent.replaceExtras(src);
    return this;
  }

  public IntentWrapper<T> replaceExtras(Bundle extras) {
    intent.replaceExtras(extras);
    return this;
  }

  public IntentWrapper<T> setFlags(int flags) {
    intent.setFlags(flags);
    return this;
  }

  public IntentWrapper<T> addFlags(int flags) {
    intent.addFlags(flags);
    return this;
  }

  public IntentWrapper<T> setPackage(String packageName) {
    intent.setPackage(packageName);
    return this;
  }

  public IntentWrapper<T> setComponent(ComponentName component) {
    intent.setComponent(component);
    return this;
  }

  public IntentWrapper<T> setClassName(Context packageContext, String className) {
    intent.setClassName(packageContext, className);
    return this;
  }

  public IntentWrapper<T> setClassName(String packageName, String className) {
    intent.setClassName(packageName, className);
    return this;
  }

  public IntentWrapper<T> setClass(Context packageContext, Class<?> cls) {
    intent.setClass(packageContext, cls);
    return this;
  }
}
