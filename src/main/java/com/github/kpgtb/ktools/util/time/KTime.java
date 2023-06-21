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

package com.github.kpgtb.ktools.util.time;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

/**
 * Object that handles time (days, hours, minutes, seconds)
 * @since 1.6.0
 */
public class KTime {
    private final long millis;
    private final String text;

    /**
     * Get object of Time from millis
     * @param millis Milliseconds
     */
    public KTime(long millis) {
        this.millis = millis;
        this.text = fromMillis(millis);
    }

    /**
     * Get object of Time from string
     * @param text String in format XdXhXmXs (d=days, h=hours, m=minutes, s=seconds, X=integer)
     */
    public KTime(String text) {
        this.millis = toMillis(text);
        this.text = fromMillis(this.millis);
    }

    /**
     * Get time in millis
     * @return milliseconds
     */
    public long getMillis() {
        return millis;
    }

    /**
     * Get time in ticks
     * @return ticks
     */
    public long getTicks() {return (millis / 1000L * 20L);}

    /**
     * Get time as text
     * @return Text in format Xd Xh Xm Xs
     */
    public String getText() {
        return text;
    }

    /**
     * Get time as text in specified format
     * @param format format of text. Placeholders: <days> <hours> <minutes> <seconds>
     * @param hideZero Hide elements with "0" like 0 seconds
     * @param splitSeq String which is a seq of chars between elements
     * @param replaceSplitSeq string that should be replacement for splitSeq or null when it should be like splitSeq
     * @param emptyReplace string that should be returned if millis is 0
     * @return Text in specified format
     */
    public String format(String format, boolean hideZero, String splitSeq, @Nullable String replaceSplitSeq, String emptyReplace) {
        return fromMillis(this.millis, format, hideZero, splitSeq, replaceSplitSeq, emptyReplace);
    }

    private long toMillis(String time) {

        String temp = "";

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        for (String s : time.split("")) {
            int num = -1;

            try {
                num = Integer.parseInt(s);
            } catch (Exception e) {}

            if(num >= 0) {
                temp += num;
                continue;
            }

            if(temp.isEmpty()) {
                continue;
            }

            switch (s) {
                case "d":
                    days = Integer.parseInt(temp);
                    break;
                case "h":
                    hours = Integer.parseInt(temp);
                    break;
                case "m":
                    minutes = Integer.parseInt(temp);
                    break;
                case "s":
                    seconds = Integer.parseInt(temp);
                    break;
            }

            temp = "";
        }

        hours += days * 24;
        minutes += hours * 60;
        seconds += minutes * 60;

        return seconds * 1000L;
    }

    private String fromMillis(long millis, String format, boolean hideZero, String splitSeq, @Nullable String replaceSplitSeq, String emptyReplace) {
        String end = replaceSplitSeq == null ? splitSeq : replaceSplitSeq;

        int seconds = (int) Math.floorDiv(millis,1000);
        int minutes = Math.floorDiv(seconds, 60);
        seconds -= minutes * 60;
        int hours = Math.floorDiv(minutes, 60);
        minutes -= hours * 60;
        int days = Math.floorDiv(hours, 24);
        hours -= days * 24;

        String[] formatSplit = format.split(splitSeq);
        StringBuilder result = new StringBuilder();

        HashMap<String, Integer> types = new HashMap<>();
        types.put("<days>", days);
        types.put("<hours>", hours);
        types.put("<minutes>", minutes);
        types.put("<seconds>", seconds);

        for (int i = 0; i < formatSplit.length; i++) {
            String text = formatSplit[i];
            String type = "";
            int number = 0;

            for (String t : types.keySet()) {
                if(text.contains(t)) {
                    type = t;
                    number = types.get(t);
                    break;
                }
            }

            if(type.isEmpty()) {
                result.append(text);
                if((i+1) != formatSplit.length) {
                    result.append(end);
                }
                break;
            }

            if(!hideZero || number != 0) {
                result.append(text.replace(type, String.valueOf(number)));
                if((i+1) != formatSplit.length) {
                    result.append(end);
                }
            }
        }

        String resultStr = result.toString();
        if(resultStr.endsWith(end)) {
            resultStr = resultStr.substring(
                    0,
                    resultStr.length() - end.length()
            );
        }

        return resultStr.isEmpty() ? emptyReplace : resultStr;
    }

    private String fromMillis(long millis) {
        return fromMillis(millis,"<days>d <hours>h <minutes>m <seconds>s", true, " ", null, "now");
    }

    @Override
    public String toString() {
        return this.text.replace(" ", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KTime time = (KTime) o;
        return millis == time.millis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(millis);
    }
}
