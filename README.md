# A java native-lib-preparer. What is it?

Java native library preparer, get native library prepared for jna / jni loading

## Get started

### An example

With dynamic load lib files in resources folder as: 

```
javaprj/src/main/resources
├── libAbx0.dylib 
└── libawesome.dylib

```

Simply use: 

```java 
try {
    String[] entryPaths = new String[]{
            "libAbx0.dylib",
            "libawesome.dylib",
    };
    String libPath = makeAvailable(entryPaths);
    if (! libPath.equals(System.getProperty("user.dir"))) {
        // Does not need loadLibrary when used with JNA. 
        System.loadLibrary("Abx0");
        System.out.println("Library loaded...");
    }
} catch (URISyntaxException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```
