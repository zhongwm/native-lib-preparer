/*
 * Copyright 2020, Wenming Zhong
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/* Written by Wenming Zhong */

package io.github.zhongwm.commons.native_lib_preparer;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Make some arbitrary native library in your classpath available to your java process, make it from
 * your code before your first load of the native library or at the bootstrapping of your process.
 * usage:
 *
 * <pre><code>makeAvailable(...)  // 3 overloads for your need. </code></pre>
 */
public class NativeLibPreparer {
    private static final String NAME_JAVA_LIBRARY_PATH = "java.library.path";
    public static boolean debug = false;

    /**
     * Make the libraries specified by entries from your name to InputStream map available to java
     * native lib loader, 1 of 3 overloads.
     * 
     * Names can be different from the class fqdn path of your classpath, for say, "native/liba.a":
     * getResourceAsStream("lib/liba.a")
     *
     * @param entries
     * @return
     * @throws IOException
     */
    public static String makeAvailable(Map<String, InputStream> entries) throws IOException {
        Path jn = getPathToLink(true);
        for (Map.Entry<String, InputStream> entry: entries.entrySet())
            extractTo(jn, entry.getKey(), entry.getValue());

        return jn.toFile().getAbsolutePath();
    }

    /**
     * Make the libraries specified by entries from your name to InputStream map available to java
     * native lib loader, 2 of 3 overloads.
     *
     * Names can be different from the class fqdn path of your classpath, for say, "native/liba.a":
     * getResourceAsStream("lib/liba.a")
     *
     * @param entries
     * @return
     * @throws IOException
     */
    public static String makeAvailable(List<Map.Entry<String, InputStream>> entries) throws IOException {
        Path jn = getPathToLink(true);
        for (Map.Entry<String, InputStream> entry: entries)
            extractTo(jn, entry.getKey(), entry.getValue());
        
        return jn.toFile().getAbsolutePath();
    }

    /**
     * Make the native libraries from your classpath available to the java native lib loader, 3 of 3
     * overloads.
     *
     * @param entryPaths
     * @return
     * @throws IOException
     */
    public static String makeAvailable(String[] entryPaths) throws URISyntaxException, IOException {
        File jarFile = new File(NativeLibPreparer.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        String jarFilePath = jarFile.getPath();
        Path jn = getPathToLink(true);
        extractTo(jarFile, jn, entryPaths);
        if (System.getProperty("os.name").startsWith("Linux")) appendSystemLibraryPath();
        return jn.toFile().getAbsolutePath();
    }

    static void extractTo(File jarFile, Path contentRoot, String[] subPaths) throws IOException {
        if (jarFile.isDirectory()) {
            for (String p: subPaths) {
                mkLinkToCwd(contentRoot, p);
            }
        } else {
            ZipFile j = new ZipFile(jarFile);
            for (String p : subPaths) {
                ZipEntry entry = j.getEntry(p);
                InputStream inputStream = j.getInputStream(entry);
                Path outputPath = contentRoot.resolve(p);
                boolean mkdirsResultIgnored = outputPath.getParent().toFile().mkdirs();
                Files.copy(inputStream, outputPath);
                outputPath.toFile().deleteOnExit();
                mkLinkToCwd(contentRoot, p);
            }
            j.close();
        }
    }

    static Path getPathToLink(boolean isTmp) throws IOException {
        if (!isTmp) {
            return Paths.get(System.getProperty("user.dir"));
        } else {
            Path jn = Files.createTempDirectory("jn");
            if (debug) {
                System.out.println(jn);
            }
            jn.toFile().deleteOnExit();
            return jn;
        }
    }

    /**
     * mklink to that file.
     *
     * @param dir
     * @param subPath
     * @return
     * @throws IOException
     */
    static Path mkLinkToCwd(Path dir, String subPath) throws IOException {
        Path finalPath = dir.resolve(subPath);
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path linkPath = cwd.resolve(subPath);
        File linkPathFile = linkPath.toFile();
        if (cwd.equals(dir)) {
            return linkPath;
        }
        if (!linkPathFile.exists()) {
            File linkParentDir = linkPathFile.getParentFile();
            if (! linkParentDir.exists()) {
                linkParentDir.mkdirs();
                linkParentDir.deleteOnExit();
            }
        }
        try {
            Files.createSymbolicLink(linkPath, finalPath);
            linkPathFile.deleteOnExit();
        } catch (java.nio.file.FileAlreadyExistsException e) {
            System.out.println("file already exist " + linkPath);
        }
        return linkPath;
    }

    static void extractTo(Path contentRoot, String filename, InputStream inputStream) throws IOException {
        Path outputPath = contentRoot.resolve(filename);
        boolean mkdirsResultIgnored = outputPath.getParent().toFile().mkdirs();
        Files.copy(inputStream, outputPath);
        outputPath.toFile().deleteOnExit();
        mkLinkToCwd(contentRoot, filename);
    }

    static void appendSystemLibraryPath() {
        String currentSysLibPath = System.getProperty(NAME_JAVA_LIBRARY_PATH);
        String[] paths = currentSysLibPath.split(File.pathSeparator);
        for (String p: paths) {
            if (p.equals(".")) return;
        }
        StringBuilder sb = new StringBuilder(".");
        sb.append(File.pathSeparator);
        sb.append(currentSysLibPath);
        System.setProperty(NAME_JAVA_LIBRARY_PATH, sb.toString());
    }

    static void appendLoadDir(String libPath, String key) throws IOException {
        StringBuilder sb = new StringBuilder(libPath);
        sb.append(File.pathSeparator);
        String origJnaLibPath = System.getProperty(key, null);
        if (origJnaLibPath != null) {
            sb.append(origJnaLibPath);
            sb.append(File.pathSeparator);
        }
        sb.deleteCharAt(sb.length() - 1);
        System.setProperty(key,
                sb.toString()
        );
        System.out.println("P:" + System.getProperty(key));
    }

}
