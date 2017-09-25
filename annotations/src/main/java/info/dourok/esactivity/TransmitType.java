package info.dourok.esactivity;

/**
 *
 * Created by tiaolins on 2017/8/25.
 */
public enum TransmitType {
  // 如果能用 Bundle 传递则用 Bundle，不然用 REF
  AUTO,
  // 用 RefManager 直接传递引用
  REF,
  // TODO
  UNSAFE
}
