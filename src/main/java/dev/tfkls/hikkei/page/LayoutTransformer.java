package dev.tfkls.hikkei.page;

import dev.tfkls.hikkei.util.Either;
import dev.tfkls.hikkei.util.Left;
import dev.tfkls.hikkei.util.Right;
import java.util.ArrayList;
import java.util.List;

public class LayoutTransformer implements Transformer {
  List<Either<String, Transformer>> linearLayout;

  public LayoutTransformer() {
    linearLayout = new ArrayList<>();
  }

  @Override
  public CharSequence transform(CharSequence input) {
    StringBuilder result = new StringBuilder();
    for (var entry : linearLayout) {
      result.append(
          switch (entry) {
            case Left<String, Transformer>(String string) -> string;
            case Right<String, Transformer>(Transformer transformer) ->
                transformer.transform(input);
          });
    }

    return result.toString();
  }

  public class Builder {
    Builder() {}

    Builder register(String name, Transformer transformer) {
      return this;
    }

    Builder transformer(Transformer transformer) {
      linearLayout.add(new Right<>(transformer));
      return this;
    }

    Builder string(String value) {
      linearLayout.add(new Left<>(value));
      return this;
    }

    LayoutTransformer build() {
      return LayoutTransformer.this;
    }
  }

  public Builder builder() {
    return new LayoutTransformer().new Builder();
  }
}
