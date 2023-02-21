/*
 *    Copyright 2023 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import com.github.kpgtb.ktools.manager.command.parser.java.ShortParser;
import org.junit.Assert;
import org.junit.Test;

public class T {
    @Test
    public void a() {
        Assert.assertTrue(new ShortParser().canConvert("10"));
    }

    @Test
    public void b() {
        Assert.assertFalse(new ShortParser().canConvert("abcv"));
    }

    @Test
    public void c() {
        String s = "ala";
        Object o = s;
        System.out.println(o.getClass());
    }
}
