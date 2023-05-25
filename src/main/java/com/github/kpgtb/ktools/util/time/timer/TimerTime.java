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

package com.github.kpgtb.ktools.util.time.timer;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class that handles when timer should send message.
 * Integers in set are seconds when timer should send message
 * -1 means that message should be sent everytime
 */
public class TimerTime {
    public final static Set<Integer> INVISIBLE = new HashSet<>();

    public final static Set<Integer> EVERY = new HashSet<>(Arrays.asList(-1));
    public final static Set<Integer> TIMER_10_EVERY = new HashSet<>(Arrays.asList(10,9,8,7,6,5,4,3,2,1));

    public final static Set<Integer> TIMER_30_EVERY = new HashSet<>(Arrays.asList(30,29,28,27,26,25,24,23,22,21,
                                                                                    20,19,18,17,16,15,14,13,12,11,
                                                                                    10,9,8,7,6,5,4,3,2,1));
    public final static Set<Integer> TIMER_30_20_10_EVERY = new HashSet<>(Arrays.asList(30,20,10,9,8,7,6,5,4,3,2,1));
    public final static Set<Integer> TIMER_30_10_EVERY = new HashSet<>(Arrays.asList(30,10,9,8,7,6,5,4,3,2,1));

    public final static Set<Integer> TIMER_60_50_40_30_20_10_EVERY = new HashSet<>(Arrays.asList(60,50,40,30,20,10,9,8,7,6,5,4,3,2,1));
    public final static Set<Integer> TIMER_60_30_10_EVERY = new HashSet<>(Arrays.asList(60,30,10,9,8,7,6,5,4,3,2,1));

    private final static Map<String, Set<Integer>> byKey = new HashMap<>();

    static  {
        byKey.put("INVISIBLE", INVISIBLE);

        byKey.put("EVERY", EVERY);
        byKey.put("TIMER_10_EVERY", TIMER_10_EVERY);

        byKey.put("TIMER_30_EVERY", TIMER_30_EVERY);
        byKey.put("TIMER_30_20_10_EVERY", TIMER_30_20_10_EVERY);
        byKey.put("TIMER_30_10_EVERY", TIMER_30_10_EVERY);

        byKey.put("TIMER_60_50_40_30_20_10_EVERY", TIMER_60_50_40_30_20_10_EVERY);
        byKey.put("TIMER_60_30_10_EVERY", TIMER_60_30_10_EVERY);
    }

    /**
     * Easily change seconds to Set
     * @param times Seconds when timer should send message
     * @return Set of seconds
     */
    public static Set<Integer> toTimer(int... times) {
        Set<Integer> result = new HashSet<>();
        for (int i : times) {
            result.add(i);
        }
        return result;
    }

    /**
     * Combine multiple sets into one
     * @param sets Sets that should be combined
     * @return One set that contains all values
     */
    @SafeVarargs
    public static Set<Integer> combineTimer(Set<Integer>... sets) {
        Set<Integer> result = new HashSet<>();
        for (Set<Integer> set : sets) {
            result.addAll(set);
        }
        return result;
    }


    /**
     * Get timer time from name
     * @param value Name of timer time
     * @return Timer time or null if not exists
     */
    @Nullable
    public static Set<Integer> valueOf(String value) {
        return byKey.get(value);
    }

    /**
     * Get all defined values of timer type
     * @return Values of timer type
     */
    public static Collection<Set<Integer>> values() {
        return byKey.values();
    }
}
