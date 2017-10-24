package info.dourok.esactivity.lambda;

/**
 * Created by tiaolins on 2017/9/29.
 */
@Deprecated
public class Calc {
  public Data data = new Data(1, 2);

  public int doCalc(int a, int b) {
    return a + b;
  }

  public void calc() {
    //data.calc(this::doCalc);
    data.calc((a, b) -> {
      System.out.println(a + b);
      return a + b;
    });
  }

  public static void main(String[] args) {
    new Calc().calc();
  }
}
