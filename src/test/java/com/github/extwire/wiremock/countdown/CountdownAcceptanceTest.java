package com.github.extwire.wiremock.countdown;

import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.github.extwire.wiremock.countdown.CountdownExtensionFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.Exceptions.uncheck;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

public class CountdownAcceptanceTest {

    HttpClient client;

    @RegisterExtension
    static WireMockExtension wm =
            WireMockExtension.newInstance()
                    .options(WireMockConfiguration.options().extensions(new CountdownExtensionFactory()))
                    .build();

    @BeforeEach
    void init() {
        client = HttpClient.newBuilder().build();
    }

    @Test
    public void exactUrlOneTimeStubbing() {
        wm.stubFor(get(urlEqualTo("/some/thing"))
                .withMetadata(Metadata.metadata()
                        .attr("times", 1)
                        .build())
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello world!")));

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(200);
        assertThat(exchange("/some/thing").statusCode()).isEqualTo(404); // second request (no match)
        assertThat(exchange("/some/thing/else").statusCode()).isEqualTo(404);
    }

    @Test
    public void exactUrlDoubleOneTimeStubbing() {
        wm.stubFor(get(urlEqualTo("/some/thing"))  // first stub
                .withMetadata(Metadata.metadata()
                        .attr("times", 1)
                        .build())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello world!")));

        wm.stubFor(get(urlEqualTo("/some/thing"))  // second stub
                .withMetadata(Metadata.metadata()
                        .attr("times", 1)
                        .build())
                .willReturn(aResponse()
                        .withStatus(409)));

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(200); // first request (matches first stub)
        assertThat(exchange("/some/thing").statusCode()).isEqualTo(409); // second request (matches second stub)
        assertThat(exchange("/some/thing").statusCode()).isEqualTo(404); // third request (no match)
        assertThat(exchange("/some/thing/else").statusCode()).isEqualTo(404);
    }

    @Test
    public void testSequenceTimeStub() {
        final StubMapping firstStub = wm.stubFor(get(urlEqualTo("/some/thing"))  // first stub
                .withMetadata(Metadata.metadata()
                        .attr("times", 3)
                        .build())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello world!")));

        final StubMapping secondStub = wm.stubFor(get(urlEqualTo("/some/thing"))  // second stub
                .withMetadata(Metadata.metadata()
                        .attr("times", 2)
                        .build())
                .willReturn(aResponse()
                        .withStatus(409)));

        final UUID firstStubId = firstStub.getId();
        final UUID secondStubId = secondStub.getId();

        assertRemainingTimes(firstStubId, 3);
        assertRemainingTimes(secondStubId, 2);


        assertThat(exchange("/some/thing").statusCode()).isEqualTo(200); // first request (matches first stub)
        assertRemainingTimes(firstStubId, 2);
        assertRemainingTimes(secondStubId, 2);

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(200); // second request (matches first stub)
        assertRemainingTimes(firstStubId, 1);
        assertRemainingTimes(secondStubId, 2);

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(200); // third request (matches first stub)
        assertRemainingTimes(firstStubId, 0);
        assertRemainingTimes(secondStubId, 2);

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(409); // fourth request (matches second stub)
        assertRemainingTimes(firstStubId, 0);
        assertRemainingTimes(secondStubId, 1);

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(409); // fifth request (matches second stub)
        assertRemainingTimes(firstStubId, 0);
        assertRemainingTimes(secondStubId, 0);

        assertThat(exchange("/some/thing").statusCode()).isEqualTo(404); // last request (no match)
    }

    private void assertRemainingTimes(UUID stubId, int expected) {
        final Integer remainingTimes = remainingTimes(stubId);
        assertThat(remainingTimes).isEqualTo(expected);
    }

    private int remainingTimes(UUID stubId) {
        final SingleStubMappingResult stubMapping = wm.getStubMapping(stubId);
        if (!stubMapping.isPresent()) return 0;
        return stubMapping.getItem().getMetadata().getInt("times");
    }

    private HttpResponse<String> exchange(String path) {
        URI fullUrl = URI.create(wm.baseUrl() + path);
        return uncheck(
                () ->
                        client.send(
                                HttpRequest.newBuilder(fullUrl).GET().build(),
                                HttpResponse.BodyHandlers.ofString()),
                HttpResponse.class);
    }


}
