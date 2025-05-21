package dev.tfkls.hikkei.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Transformers {
  public static class Compose implements Transformer {
    List<Transformer> transformList;

    public Compose() {
      transformList = new ArrayList<>();
    }

    public Compose(Transformer... transformers) {
      this(Arrays.asList(transformers));
    }

    public Compose(Collection<Transformer> transformers) {
      transformList = List.copyOf(transformers);
    }

    Compose compose(Transformer transformer) {
      transformList.add(transformer);
      return this;
    }

    Compose compose(Transformer... transformers) {
      transformList.addAll(Arrays.asList(transformers));
      return this;
    }

    @Override
    public CharSequence transform(CharSequence input) {
      CharSequence current = input;

      for (Transformer transformer : transformList) {
        current = transformer.transform(current);
      }

      return current;
    }
  }
}
