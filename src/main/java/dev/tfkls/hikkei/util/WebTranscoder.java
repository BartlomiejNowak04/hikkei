package dev.tfkls.hikkei.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.mikael.urlbuilder.util.Decoder;
import io.mikael.urlbuilder.util.Encoder;
import java.util.Base64;

public class WebTranscoder {
  private static final Encoder urlEncoder = new Encoder(UTF_8);
  private static final Decoder urlDecoder = new Decoder(UTF_8);

  private static final Base64.Encoder base64Encoder = Base64.getEncoder();
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();

  public static String urlEncode(String raw) {
    return urlEncoder.encodeQueryElement(raw);
  }

  public static String urlDecode(String armor) {
    return urlDecoder.urlDecode(armor, true);
  }

  public static String base64Encode(String raw) {
    return new String(base64Encoder.encode(raw.getBytes(UTF_8)), UTF_8);
  }

  public static String base64Decode(String armor) {
    return new String(base64Decoder.decode(armor.getBytes(UTF_8)), UTF_8);
  }
}
