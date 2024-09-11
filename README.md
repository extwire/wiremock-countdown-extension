# CountDown Extension for WireMock

Add support for a countdown in the wiremock stubs. Wiremock will respond in the exact order they are added. 

For example, if a stub **A** is added with the metadata `times: 3`, and then a stub B is added with the metadata `times: 2` with the same request matcher, WireMock will respond in the following order: **A, A, A, B, B**.

## Java/JVM usage

### Step 1: Add to your build file

For Maven users:

```xml
<dependency>
    <groupId>com.github.extwire</groupId>
    <artifactId>wiremock-countdown-extension</artifactId>
    <version>0.1.0</version>
</dependency>
```

For Gradle users:

```groovy
dependencies {
    implementation 'com.github.extwire:wiremock-countdown-extension:0.1.0'
}
```

### Step 2: Register the extension with your server

```java
new WireMockServer(wireMockConfig().extensions(CountdownExtensionFactory.class));
```

### Step 3: Create stubs with `times` metadata.

```java
wm.stubFor(get(urlEqualTo("/some/thing"))  // first stub
    .withMetadata(Metadata.metadata()
                    .attr("times", 3)
                    .build())
    .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Hello world!")));

wm.stubFor(get(urlEqualTo("/some/thing"))  // second stub
    .withMetadata(Metadata.metadata()
                    .attr("times", 2)
                    .build())
    .willReturn(aResponse()
                    .withStatus(409)));
```
