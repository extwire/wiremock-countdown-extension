package com.github.extwire.wiremock.countdown;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.WireMockServices;

import java.util.List;

public class CountdownExtensionFactory implements ExtensionFactory {

    @Override
    public List<Extension> create(WireMockServices services) {
        return List.of(
                new CountdownStubListener(),
                new ContdownServeEventListener(services.getAdmin())
        );
    }
}
