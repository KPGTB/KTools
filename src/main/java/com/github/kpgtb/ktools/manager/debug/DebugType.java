package com.github.kpgtb.ktools.manager.debug;

public enum DebugType {
    START("debug.start"),
    STOP("debug.stop");

    private final String configStr;

    DebugType(String configStr) {
        this.configStr = configStr;
    }

    public String getConfigStr() {
        return configStr;
    }
}
