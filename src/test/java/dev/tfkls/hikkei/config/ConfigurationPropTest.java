package dev.tfkls.hikkei.config;

import java.util.Objects;
import java.util.function.Function;
import net.jqwik.api.*;

/// TODO: Add more tests!
class ConfigurationPropTest {
  @Provide
  Arbitrary<Tuple.Tuple3<String, Function<ConfigView, Object>, Arbitrary<?>>> configMapping() {
    return Arbitraries.of(
      Tuple.of("database", ConfigView::databaseUrl, Arbitraries.strings().ofMaxLength(256)),
      Tuple.of("host", ConfigView::siteHost, Arbitraries.strings().ofMaxLength(256)),
      Tuple.of("port", ConfigView::sitePort, Arbitraries.integers().between(0, 1<<16-1)));
  }

  @Provide
  Arbitrary<Function<ConfigView, Object>> configAccessors() {
    return configMapping().map(Tuple.Tuple3::get2);
  }

  @Provide
  Arbitrary<String> configKeys() { return Arbitraries.of(Configuration.configKeys); }

  @Provide
  Arbitrary<ConfigView> standardConfigViews() {
    return Arbitraries.of(
      ConfigViewFactory.standardConfigView(), ConfigViewFactory.defaultConfigView());
  }
  @Provide
  Arbitrary<ConfigView> allConfigViews() {
    return standardConfigViews();
  }

  @Property(generation = GenerationMode.EXHAUSTIVE)
  boolean allKeysAreTested(@ForAll("configKeys") String key){
    var maybeStream = configMapping().allValues();
    return maybeStream.isPresent() && maybeStream.get().filter(
      tuple -> Objects.equals(tuple.get1(), key)
    ).toArray().length >= 1;
  }

  @Property
  boolean defaultsAreDefined(
      @ForAll("allConfigViews") ConfigView view,
      @ForAll("configAccessors") Function<ConfigView, Object> accessor) {
    return accessor.apply(view) != null;
  }

  @Property(generation = GenerationMode.RANDOMIZED)
  boolean configurationIsConstant(
      @ForAll("allConfigViews") ConfigView view,
      @ForAll("configAccessors") Function<ConfigView, Object> accessor) {
    return Objects.equals(accessor.apply(view), accessor.apply(view));
  }
}
