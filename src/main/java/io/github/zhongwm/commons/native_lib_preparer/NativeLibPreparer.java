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
 *
 * by Zhongwenming<br>
 */

package io.github.zhongwm.commons.native_lib_preparer;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * todo: os specific treatment
 */
public class NativeLibPreparer {
    public static boolean debug = false;

    public static Path getPathToLink(boolean isTmp) throws IOException {
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
    public static Path mkLinkToCwd(Path dir, String subPath) throws IOException {
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
        Files.createSymbolicLink(linkPath, finalPath);
        linkPathFile.deleteOnExit();
        return linkPath;
    }

    public static String makeAvailable(String[] entryPaths) throws URISyntaxException, IOException {
        File jarFile = new File(NativeLibPreparer.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        String jarFilePath = jarFile.getPath();
//        String jnaLibPath = String.format("file://%s!/darwin", jarFilePath);
//        System.out.println("jnaLibPath: " + jnaLibPath);
//        System.setProperty("jna.library.path",
//                jnaLibPath
//        );


        Path jn = getPathToLink(true);
        extractTo(jarFile, jn, entryPaths);


//        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-l", "-c", String.format("find \"%s\"", jn.toFile().getAbsolutePath())});
        /*Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-l", "-c", String.format("find \"%s\"", System.getProperty("user.dir"))});
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            System.out.println(line);
        }
        bufferedReader.close();
        BufferedReader errbufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        for (String line = errbufferedReader.readLine(); line != null; line = errbufferedReader.readLine()) {
            System.out.println(line);
        }
        errbufferedReader.close();*/
        return jn.toFile().getAbsolutePath();
    }

    static void extractTo(File jarFile, Path contentRoot, String[] subPaths) throws IOException {
        if (jarFile.isDirectory()) {
            for (String p: subPaths) {
                mkLinkToCwd(contentRoot, p);
            }
        } else {
            
            Path darwinP = Files.createDirectory(contentRoot.resolve("darwin"));
            darwinP.toFile().deleteOnExit();
            ZipFile j = new ZipFile(jarFile);
            for (String p : subPaths) {
                ZipEntry entry = j.getEntry(p);
                InputStream inputStream = j.getInputStream(entry);
                Files.copy(inputStream, contentRoot.resolve(p));
                contentRoot.resolve(p).toFile().deleteOnExit();
                mkLinkToCwd(contentRoot, p);
            }
            j.close();
        }
    }

    public static void appendLoadDir(String libPath, String key) throws IOException {
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

//        AbxClient.ClassPathHacker.addFile(new File(libPath));
    }

}
