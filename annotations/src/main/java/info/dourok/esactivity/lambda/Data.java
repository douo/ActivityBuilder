package info.dourok.esactivity.lambda;

/**
 * Created by tiaolins on 2017/9/29.
 */

public class Data {
  private int a;
  private int b;
  private int result;

  public Data(int a, int b) {
    this.a = a;
    this.b = b;
  }

  public int getResult() {
    return result;
  }

  public void calc(BiIntFunction calc) {
    result = calc.apply(a, b);
    System.out.println("result is " + result);
  }
}
