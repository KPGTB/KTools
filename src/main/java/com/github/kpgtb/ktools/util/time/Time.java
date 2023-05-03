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

/**
 * Object that handles time (days, hours, minutes, seconds)
 * @since 1.6.0
 */
public class Time {
    private final long millis;
    private final String text;

    /**
     * Get object of Time from millis
     * @param millis Milliseconds
     */
    public Time(long millis) {
        this.millis = millis;
        this.text = fromMillis(millis);
    }

    /**
     * Get object of Time from string
     * @param text String in format XdXhXmXs (d=days, h=hours, m=minutes, s=seconds, X=integer)
     */
    public Time(String text) {
        this.text = text;
        this.millis = toMillis(text);
    }

    /**
     * Get time in millis
     * @return milliseconds
     */
    public long getMillis() {
        return millis;
    }

    /**
     * Get time as text
     * @return Text in format Xd Xh Xm Xs
     */
    public String getText() {
        return text;
    }

    /**
     * Get time as text in specified format
     * @param format format of text where %d is next number (order: days, hours, minutes, seconds)
     * @return Text in specified format
     */
    public String format(String format) {
        return fromMillis(this.millis, format);
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

    private String fromMillis(long millis, String format) {

        int seconds = (int) Math.floorDiv(millis,1000);
        int minutes = Math.floorDiv(seconds, 60);
        seconds -= minutes * 60;
        int hours = Math.floorDiv(minutes, 60);
        minutes -= hours * 60;
        int days = Math.floorDiv(hours, 24);
        hours -= days * 24;

        return String.format(format, days,hours,minutes, seconds);
    }

    private String fromMillis(long millis) {
        return fromMillis(millis,"%dd %dh %dm %ds");
    }
}
