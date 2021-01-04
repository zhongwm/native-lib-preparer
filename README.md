# A java native-lib-preparer. What is it?

Java native library preparer, get native library prepared for jna / jni loading

## Get started

### An example

With dynamic load lib files in resources folder as: 

```
src/main/resources
├── libAbx0.dylib 
└── libawesome.dylib

```

Simply use: 

```java 

...
import static io.github.zhongwm.commons.native_lib_preparer.NativeLibPreparer.makeAvailable;
...

try {
    String[] entryPaths = new String[]{
            "libAbx0.dylib",
            "libawesome.dylib",
    };
    String libPath = makeAvailable(entryPaths);
    if (! libPath.equals(System.getProperty("user.dir"))) {
        // Does not need loadLibrary when used with JNA.
        // but just be sure to `makeAvaiable` before JNA loading phase.  
        System.loadLibrary("Abx0");
        System.out.println("Library loaded...");
    }
} catch (URISyntaxException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```

## System compatibility

Currently supports Mac OS X, windows, linux 

#### On linux

On linux we need to set System properties "java.library.path" = "." + File.pathSeparator + "${java.library.path}"
