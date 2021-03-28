# A Java Native Lib Preparer

Java native library preparer, get native library prepared for JNA / JNI loading.

#### Installation

sbt

```scala
libraryDependencies += "io.github.zhongwm.commons" % "native-lib-preparer" % "0.2.1"
```
Maven

```xml
<dependency>
    <groupId>io.github.zhongwm.commons</groupId>
    <artifactId>native-lib-preparer</artifactId>
    <version>0.2.1</version>
</dependency>
```

## System compatibility

Supports macOS, windows, Linux

## If you use JNA

If you use JNA, this library also helps, actually, what it does is a needed step.
You do not need loadLibrary when used with JNA, just be sure to "makeAvailable"
before JNA loading phase.

## Get started

Make some arbitrary native library in your classpath available to your java process, make it from
your java code before your first load of the native library or at the bootstrapping of your process.
usage:

```java
makeAvailable(new String[]{"libfoo.dll"});  // 3 overloads for your need.
// Now your library ready to load your library.
```

We can use other overloaded mehods:

```java
Map<String, InputStream> m = new HashMap();
m.put("libfoo.dll", classLoader.getResourceAsStream("libfoo.dll"));
m.put("native/libbar.dll", classLoader.getResourceAsStream("native/libbar.dll"));
makeAvailable(m);
```

### A detailed example

Now we have a native dynamic load library named `foo` to load into your java process, and our
resources folder like this.

```
src/main/resources
            ├── libfoo.dylib      // The library you are interested in.
            └── libbar.dylib      // Some library that "libfoo.dylib" depends on.
```

Here is how to make things work.

```java 
import static io.github.zhongwm.commons.native_lib_preparer.NativeLibPreparer.makeAvailable;

try {
    String[] entryPaths = new String[]{
        "libfoo.dylib",
        "libbar.dylib",
    };

    // The only step needed before your loading
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

### Can load libraries into a different layout than classpath.

```java
    @Test
    public void testLoadMyLibToDifferentDir() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        Map<String, InputStream> m = new HashMap<>();
        m.put("libfoo.dll", classLoader.getResourceAsStream("libfoo.dll"));
        m.put("libbar.dll", classLoader.getResourceAsStream("native/libbar.dll"));
        makeAvailable(m);

        File toCheckFoo = Paths.get("libfoo.dll").toFile();
        System.out.println(toCheckFoo.getAbsolutePath());
        assertTrue(toCheckFoo.exists());
        assertTrue(toCheckFoo.isFile());

        File toCheckBar = Paths.get("libbar.dll").toFile();
        System.out.println(toCheckBar.getAbsolutePath());
        assertTrue(toCheckBar.exists());
        assertTrue(toCheckBar.isFile());
    }
```

### Can work on different level of native libraries

```java
    @Test
    public void testLoadMyLib() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        Map<String, InputStream> m = new HashMap<>();
        m.put("libfoo.dll", classLoader.getResourceAsStream("libfoo.dll"));
        m.put("native/libbar.dll", classLoader.getResourceAsStream("native/libbar.dll"));
        makeAvailable(m);

        File toCheckFoo = Paths.get("libfoo.dll").toFile();
        System.out.println(toCheckFoo.getAbsolutePath());
        assertTrue(toCheckFoo.exists());
        assertTrue(toCheckFoo.isFile());

        File toCheckBar = Paths.get("native", "libbar.dll").toFile();
        System.out.println(toCheckBar.getAbsolutePath());
        assertTrue(toCheckBar.exists());
        assertTrue(toCheckBar.isFile());
    }
```