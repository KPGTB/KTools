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

import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.util.time.KTime;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Timer class that handles making timers
 */
public class KTimer {
    private final BukkitAudiences adventure;
    private final JavaPlugin plugin;

    private final TimerSendType sendType;
    private final Set<Integer> timeType;
    private final int time;
    private final List<Player> viewers;

    private String timeFormat;
    private boolean timeFormatHideZero;
    private String timeFormatSplitSeq;
    private String timeReplaceSplitSeq;
    private String timeEmptyReplace;

    private String tickMessage;
    private Component cancelMessage;
    private Component endMessage;
    private TimerAction startAction;
    private TimerAction tickAction;
    private TimerAction cancelAction;
    private TimerAction endAction;

    private int timeLeft;
    private BukkitTask timer;
    private boolean started;
    private boolean ended;

    /**
     * Constructor of timer
     * @param adventure Instance of AdventureAPI
     * @param plugin Instance of Plugin
     * @param languageManager Instance of Language Manager
     * @param sendType Type of timer
     * @param timeType Seconds when timer should send message {@link com.github.kpgtb.ktools.util.time.timer.TimerTime}
     * @param time Time of timer
     */
    public KTimer(BukkitAudiences adventure, JavaPlugin plugin, LanguageManager languageManager,
                  TimerSendType sendType, Set<Integer> timeType, int time) {
        this.adventure = adventure;
        this.plugin = plugin;
        this.sendType = sendType;
        this.timeType = timeType;
        this.time = time;

        this.viewers = new ArrayList<>();

        setTickMessage(languageManager.getSingleComponent(LanguageLevel.GLOBAL, "defaultTimer"));
        setEndMessage(languageManager.getSingleComponent(LanguageLevel.GLOBAL, "defaultEndTimer"));
        setEndMessage(languageManager.getSingleComponent(LanguageLevel.GLOBAL, "defaultCancelTimer"));

        end();
    }

    /**
     * Constructor of timer
     * @param wrapper Instance of ToolsObjectWrapper
     * @param sendType Type of timer
     * @param timeType Seconds when timer should send message {@link com.github.kpgtb.ktools.util.time.timer.TimerTime}
     * @param time Time of timer
     */
    public KTimer(ToolsObjectWrapper wrapper,
                  TimerSendType sendType, Set<Integer> timeType, int time) {
        this.adventure = wrapper.getAdventure();
        this.plugin = wrapper.getPlugin();
        this.sendType = sendType;
        this.timeType = timeType;
        this.time = time;

        this.viewers = new ArrayList<>();

        setTickMessage(wrapper.getLanguageManager().getSingleComponent(LanguageLevel.GLOBAL, "defaultTimer"));
        setEndMessage(wrapper.getLanguageManager().getSingleComponent(LanguageLevel.GLOBAL, "defaultEndTimer"));
        setEndMessage(wrapper.getLanguageManager().getSingleComponent(LanguageLevel.GLOBAL, "defaultCancelTimer"));

        end();
    }

    /**
     * Set format of timer
     * @param timeFormat {@link KTime}#format()
     * @param timeFormatHideZero {@link KTime}#format()
     * @param timeFormatSplitSeq {@link KTime}#format()
     * @param timeReplaceSplitSeq {@link KTime}#format()
     * @param timeEmptyReplace {@link KTime}#format()
     * @return This timer
     */
    public KTimer setTimeFormat(@NotNull String timeFormat, boolean timeFormatHideZero, @NotNull String timeFormatSplitSeq, @NotNull String timeReplaceSplitSeq, String timeEmptyReplace) {
        this.timeFormat = timeFormat;
        this.timeFormatHideZero = timeFormatHideZero;
        this.timeFormatSplitSeq = timeFormatSplitSeq;
        this.timeReplaceSplitSeq = timeReplaceSplitSeq;
        this.timeEmptyReplace = timeEmptyReplace;
        return this;
    }

    /**
     * Set message that should be sent to players when timer ticks
     * @param message Message that should be sent. Add <time> placeholder to see time
     * @return This timer
     */
    public KTimer setTickMessage(@NotNull String message) {
        this.tickMessage = message;
        return this;
    }
    /**
     * Set message that should be sent to players when timer ticks
     * @param component Component that should be sent. Add <time> placeholder to see time
     * @return This timer
     */
    public KTimer setTickMessage(@NotNull Component component) {
        return setTickMessage(MiniMessage.miniMessage().serialize(component).replace("\\<time>", "<time>"));
    }

    /**
     * Set message that should be sent to players when timer ends
     * @param component Component that should be sent.
     * @return This timer
     */
    public KTimer setEndMessage(@NotNull Component component) {
        this.endMessage = component;
        return this;
    }
    /**
     * Set message that should be sent to players when timer ends
     * @param message Message that should be sent.
     * @return This timer
     */
    public KTimer setEndMessage(@NotNull String message) {
        return setEndMessage(MiniMessage.miniMessage().deserialize(message));
    }

    /**
     * Set message that should be sent to players when timer is cancelled
     * @param component Component that should be sent.
     * @return This timer
     */
    public KTimer setCancelMessage(@NotNull Component component) {
        this.cancelMessage = component;
        return this;
    }
    /**
     * Set message that should be sent to players when timer is cancelled
     * @param message Message that should be sent.
     * @return This timer
     */
    public KTimer setCancelMessage(@NotNull String message) {
        return setCancelMessage(MiniMessage.miniMessage().deserialize(message));
    }

    /**
     * Set action that should happen when timer starts
     * @param startAction action
     * @return This timer
     */
    public KTimer setStartAction(TimerAction startAction) {
        this.startAction = startAction;
        return this;
    }
    /**
     * Set action that should happen when timer tick
     * @param tickAction action
     * @return This timer
     */
    public KTimer setTickAction(TimerAction tickAction) {
        this.tickAction = tickAction;
        return this;
    }
    /**
     * Set action that should happen when timer is canelled
     * @param cancelAction action
     * @return This timer
     */
    public KTimer setCancelAction(TimerAction cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }
    /**
     * Set action that should happen when timer ends
     * @param endAction action
     * @return This timer
     */
    public KTimer setEndAction(TimerAction endAction) {
        this.endAction = endAction;
        return this;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
    public boolean isStarted() {
        return started;
    }
    public boolean isEnded() {
        return ended;
    }
    public int getTime() {
        return time;
    }

    /**
     * Add viewer to timer
     * @param player viewer
     * @return This timer
     */
    public KTimer addViewer(Player player) {
        if(!hasViewer(player)) {
            viewers.add(player);
        }
        return this;
    }
    /**
     * Remove viewer from timer
     * @param player viewer
     * @return This timer
     */
    public KTimer removeViewer(Player player) {
        viewers.remove(player);
        return this;
    }
    /**
     * Clear viewers
     * @return This timer
     */
    public KTimer clearViewers() {
        viewers.clear();
        return this;
    }
    /**
     * Check if timer contains this viewer
     * @param player viewer
     * @return true if this player is a viewer of this timer
     */
    public boolean hasViewer(Player player) {
        return viewers.contains(player);
    }

    /**
     * Get all viewers of timer
     * @return all viewers
     */
    public Player[] getViewers() {
        return viewers.toArray(new Player[]{});
    }

    /**
     * Start the timer
     * @return This timer
     */
    public KTimer start() {
        if(started) {
            return this;
        }

        started = true;
        if(startAction != null) {
            startAction.run(this);
        }

        KTimer timerObj = this;
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if(timeLeft <= 0) {
                    if(endAction != null) {
                        endAction.run(timerObj);
                    }
                    sendMessageToViewers(endMessage);
                    end();
                    return;
                }
                tick();
            }
        }.runTaskTimer(plugin, 0,20);
        return this;
    }

    /**
     * Cancel the timer
     * @return This timer
     */
    public KTimer cancel() {
        if(!started || ended) {
            return this;
        }

        end();
        if(cancelAction != null) {
            cancelAction.run(this);
        }
        sendMessageToViewers(cancelMessage);
        return this;
    }

    private void tick() {
        if(tickAction != null) {
            tickAction.run(this);
        }

        if(timeType.contains(timeLeft) || timeType.contains(-1)) {
            KTime time = new KTime(timeLeft * 1000L);
            String timeStr = timeFormat == null || timeFormat.isEmpty() ? time.getText() : time.format(timeFormat, timeFormatHideZero, timeFormatSplitSeq, timeReplaceSplitSeq,timeEmptyReplace);

            sendMessageToViewers(
                    MiniMessage.miniMessage().deserialize(tickMessage, Placeholder.unparsed("time", timeStr))
            );
        }

        timeLeft--;
    }

    private void end() {
        started = false;
        ended = true;
        timeLeft = time;
        if(timer != null && !timer.isCancelled()) {
            timer.cancel();
        }
    }

    private void sendMessageToViewers(Component component) {
        viewers.forEach(viewer -> {
            if(!viewer.isOnline()) {
                return;
            }
            Audience audience = adventure.player(viewer);
            switch (sendType) {
                case MESSAGE:
                    audience.sendMessage(component);
                    break;
                case ACTIONBAR:
                    audience.sendActionBar(component);
                    break;
            }
        });
    }
}
