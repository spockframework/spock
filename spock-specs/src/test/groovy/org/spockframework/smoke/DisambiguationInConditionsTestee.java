package org.spockframework.smoke;

import java.util.regex.Pattern;

public class DisambiguationInConditionsTestee {
  interface Verifier {
    void verify(Class<?> clazz);
  }

  public static abstract class Java1 {
    public Java1(String s, Verifier verifier) {
      verifier.verify(String.class);
    }

    public Java1(Pattern p, Verifier verifier) {
      verifier.verify(Pattern.class);
    }
  }

  public static class Java2 extends Java1 {
    public Java2(String s, Verifier verifier) {
      super(s, verifier);
    }

    public Java2(Pattern p, Verifier verifier) {
      super(p, verifier);
    }
  }

  public static class Java3 extends Java1 {
    public Java3(String s, Verifier verifier) {
      super((String) s, verifier);
    }

    public Java3(Pattern p, Verifier verifier) {
      super((Pattern) p, verifier);
    }
  }

  public static class Java4 {
    public Java4(String s, Verifier verifier) {
      verifier.verify(String.class);
    }

    public Java4(Pattern p, Verifier verifier) {
      verifier.verify(Pattern.class);
    }
  }
}
