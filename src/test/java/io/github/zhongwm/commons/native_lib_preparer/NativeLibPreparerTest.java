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


import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.github.zhongwm.commons.native_lib_preparer.NativeLibPreparer.makeAvailable;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class NativeLibPreparerTest {

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
}
