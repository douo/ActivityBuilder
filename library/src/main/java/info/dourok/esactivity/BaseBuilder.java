package info.dourok.esactivity;

/**
 * @author tiaolins
 * @date 2017/8/6
 */
public interface BaseBuilder {
  /**
   * 把 Builder 转换为 Intent 接口
   * @return 代理了 Intent 的方法，并新增 asBuilder 方法用于转换回 Builder
   */
  IntentWrapper asIntent();
}
