package codes.shubham.mvtscli.helpers;

import java.util.Objects;

public class Tuple2<A,B> {
    public final A first;
    public final B second;

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Tuple2<?, ?> tuple2)) return false;

    return Objects.equals(first, tuple2.first) && Objects.equals(second, tuple2.second);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(first);
    result = 31 * result + Objects.hashCode(second);
    return result;
  }
}
