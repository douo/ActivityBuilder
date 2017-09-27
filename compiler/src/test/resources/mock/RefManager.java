package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;

public class RefManager {
  public static RefManager getInstance() {
    return null;
  }

  public <T> T get(Activity context, String key) {
    return null;
  }

  public <T> T get(Intent intent, String key) {
    return null;
  }

  /**
   * 用于 setResult 的 Intent
   */
  public <T> void put(Intent intent, String key, T value) {

  }

  public <T> void put(BaseBuilder builder, String key, T value) {

  }

  public int getInt(Intent intent, String key, int defaultValue) {
      return defaultValue;
  }

  public char getChar(Intent intent, String key, char defaultValue) {
      return defaultValue;
  }

  public short getShort(Intent intent, String key, short defaultValue) {
      return defaultValue;
  }

  public byte getByte(Intent intent, String key, byte defaultValue) {
      return defaultValue;
  }

  public long getLong(Intent intent, String key, long defaultValue) {
      return defaultValue;
  }

  public double getDouble(Intent intent, String key, double defaultValue) {
      return defaultValue;
  }

  public float getFloat(Intent intent, String key, float defaultValue) {
      return defaultValue;
  }

  public boolean getBoolean(Intent intent, String key, boolean defaultValue) {
      return defaultValue;
  }

  public int getInt(Activity context, String key, int defaultValue) {
    return getInt(context.getIntent(), key, defaultValue);
  }

  public char getChar(Activity context, String key, char defaultValue) {
    return getChar(context.getIntent(), key, defaultValue);
  }

  public short getShort(Activity context, String key, short defaultValue) {
    return getShort(context.getIntent(), key, defaultValue);
  }

  public byte getByte(Activity context, String key, byte defaultValue) {
    return getByte(context.getIntent(), key, defaultValue);
  }

  public long getLong(Activity context, String key, long defaultValue) {
    return getLong(context.getIntent(), key, defaultValue);
  }

  public double getDouble(Activity context, String key, double defaultValue) {
    return getDouble(context.getIntent(), key, defaultValue);
  }

  public float getFloat(Activity context, String key, float defaultValue) {
    return getFloat(context.getIntent(), key, defaultValue);
  }

  public boolean getBoolean(Activity context, String key, boolean defaultValue) {
    return getBoolean(context.getIntent(), key, defaultValue);
  }
}
