package com.github.extwire.wiremock.countdown;

import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.extwire.wiremock.countdown.ContdownServeEventListener.TIMES;

public class CountdownStubListener implements StubLifecycleListener {
    
    @Override
    public String getName() {
        return "countdown-stub-listener";
    }

    @Override
    public void afterStubCreated(StubMapping stub) {
        if (!stub.getMetadata().containsKey(TIMES)) return;
        final long insertionIndex = stub.getInsertionIndex();
        stub.setPriority(Math.toIntExact(insertionIndex));
    }
}
