package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于通过 Map 在 Activity 之间直接传递引用
 * 每个 RefMap 都会和 Intent 绑定在一起
 * Intent 存放着 RefMap 在 {@link RefManager} 中的 Key
 * Created by tiaolins on 2017/8/31.
 */

public class RefManager {
  public final static String KEY_OF_MAP = "info.dourok.esactivity#KeyOfMap(DON_T_MODIFY)";
  private SparseArray<Map<String, Object>> sGlobalRefMap = new SparseArray<>();
  private static RefManager sInstance = new RefManager();

  public static RefManager getInstance() {
    return sInstance;
  }

  public <T> T get(Activity context, String key) {
    return get(context.getIntent(), key);
  }

  public <T> T get(Intent intent, String key) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      return (T) refMap.get(key);
    } else {
      return null;
    }
  }

  public <T> void put(Intent intent, String key, T value) {
    Map<String, Object> refMap = getOrCreateRefMap(intent);
    refMap.put(key, value);
  }

  public <T> void put(BaseBuilder builder, String key, T value) {
    Map<String, Object> refMap = getOrCreateRefMap(builder);
    refMap.put(key, value);
  }

  /**
   * 每个 buidler 实例都有独立的 ReFMap
   */
  public @NonNull Map<String, Object> getOrCreateRefMap(BaseBuilder builder) {
    return getOrCreateRefMap(builder.asIntent().getIntent());
  }

  /**
   * @return 返回 RefMap 的 key，如果 Intent 未绑定 RefMap，则返回 0
   */
  public static int getKeyOfMap(Intent intent) {
    return intent.getIntExtra(KEY_OF_MAP, 0);
  }

  public static boolean hasRefMap(Intent intent) {
    return intent.hasExtra(KEY_OF_MAP);
  }

  /**
   * 如果 oldIntent 绑定了 RefMap，那么将其解绑并重新绑定到 newIntent
   */
  public void rebindRefMap(Intent oldIntent, Intent newIntent) {
    if (hasRefMap(oldIntent)) {
      Map<String, Object> refMap = getRefMap(oldIntent);

      sGlobalRefMap.remove(getKeyOfMap(oldIntent));
      oldIntent.removeExtra(KEY_OF_MAP);

      int keyOfMap = newIntent.hashCode();
      newIntent.putExtra(KEY_OF_MAP, keyOfMap);
      sGlobalRefMap.put(keyOfMap, refMap);
    }
  }

  /**
   * 用于 setResult 的 Intent
   */
  public @NonNull Map<String, Object> getOrCreateRefMap(Intent intent) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap == null) {
      refMap = new HashMap<>();
      int keyOfMap = intent.hashCode();
      intent.putExtra(KEY_OF_MAP, keyOfMap);
      sGlobalRefMap.put(keyOfMap, refMap);
    }
    return refMap;
  }

  public @Nullable Map<String, Object> getRefMap(Intent intent) {
    if (intent.hasExtra(KEY_OF_MAP)) {
      int keyOfMap = intent.getIntExtra(KEY_OF_MAP, 0);
      return sGlobalRefMap.get(keyOfMap);
    } else {
      return null;
    }
  }

  public @Nullable Map<String, Object> getRefMap(Activity context) {
    return getRefMap(context.getIntent());
  }

  public void clearRefs(int keyOfMap) {
    sGlobalRefMap.remove(keyOfMap);
  }

  public void clearRefs(@Nullable Intent intent) {
    if (intent != null && hasRefMap(intent)) {
      sGlobalRefMap.remove(getKeyOfMap(intent));
    }
  }

  //XXX 拆箱 nullpointer

  public int getInt(Intent intent, String key, int defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Integer) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public char getChar(Intent intent, String key, char defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Character) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public short getShort(Intent intent, String key, short defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Short) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public byte getByte(Intent intent, String key, byte defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Byte) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public long getLong(Intent intent, String key, long defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Long) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public double getDouble(Intent intent, String key, double defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Double) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public float getFloat(Intent intent, String key, float defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Float) obj;
      }
    } else {
      return defaultValue;
    }
  }

  public boolean getBoolean(Intent intent, String key, boolean defaultValue) {
    Map<String, Object> refMap = getRefMap(intent);
    if (refMap != null) {
      Object obj = refMap.get(key);
      if (obj == null) {
        return defaultValue;
      } else {
        return (Boolean) obj;
      }
    } else {
      return defaultValue;
    }
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
