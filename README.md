# A java native lib preparer

Java native library preparer, get native library prepared for JNA / JNI loading.

## If you use JNA

If you use JNA, this library also helps, actually, what it does is a needed step.
You do not need loadLibrary when used with JNA, just be sure to "makeAvailable"
before JNA loading phase.


## Get started


Make some arbitrary native library in your classpath available to your java process, make it from
your java code before your first load of the native library or at the bootstrapping of your process.
usage:

```java
makeAvailable(...)  // 3 overloads for your need.
```

### An example

With dynamic load lib files in resources folder as:

```
src/main/resources
├── libfoo.dylib      // The library you are interested in.
└── libbar.dylib      // Some library that "libfoo.dylib" depends on.

``` 

```java 

...
import static io.github.zhongwm.commons.native_lib_preparer.NativeLibPreparer.makeAvailable;
...

try {
    String[] entryPaths = new String[]{
            "libfoo.dylib",
            "libbar.dylib",
    };

    ///////////// the Only step needed before your loading //////////
    makeAvailable(entryPaths);
    
    // Now we are ok to load the native lib.
    if (! libPath.equals(System.getProperty("user.dir"))) { 
        System.loadLibrary("foo");
        System.out.println("Library loaded...");
    }
} catch (URISyntaxException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```

## System compatibility

Currently supports Mac OS X, for windows, linux.    
