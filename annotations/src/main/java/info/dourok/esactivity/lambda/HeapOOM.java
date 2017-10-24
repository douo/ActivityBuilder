package info.dourok.esactivity.lambda;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiaolins on 2017/9/30.
 */

public class HeapOOM {
  static class OOMObject{}

  public static void main(String[] args) {
    List<OOMObject> list = new ArrayList<>();
    while (true){
      list.add(new OOMObject());
    }
  }
}
