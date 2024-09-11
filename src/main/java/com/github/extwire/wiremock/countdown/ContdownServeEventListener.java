package com.github.extwire.wiremock.countdown;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class ContdownServeEventListener implements ServeEventListener {

    public static final String TIMES = "times";

    private final Admin admin;

    public ContdownServeEventListener(Admin admin) {
        this.admin = admin;
    }

    @Override
    public String getName() {
        return "countdown-serve-event-listener";
    }

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        final boolean hasTimes = serveEvent.getStubMapping().getMetadata().containsKey(TIMES);
        if (!hasTimes) return;
        final Integer remainingTimes = serveEvent.getStubMapping().getMetadata().getInt(TIMES, -1);
        if (remainingTimes == -1) return;
        final int nextCount = remainingTimes - 1;
        serveEvent.getStubMapping().getMetadata().put(TIMES, nextCount);
        if (nextCount == 0) {
            admin.removeStubMapping(serveEvent.getStubMapping());
        }
    }
}
