# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/gradle-plugin/packaging-oci-image.html)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Local LLM (price estimates)

New service requests can get a price estimate from a local [Ollama](https://ollama.com) model (`qwen2.5:3b`).
It is switched on with `drobnapomoc.price-estimate.enabled=true` (set in the `dev` profile, off everywhere else).
Run Ollama natively (Docker on macOS has no GPU access, so a container would be too slow):

```bash
brew install ollama
ollama serve            # or start the Ollama app
ollama pull qwen2.5:3b  # once
```

The first request after Ollama loads the model may come back without an estimate (loading takes longer than the
5 s timeout); the next ones get it. Without a running Ollama requests are simply saved as `NEW` without an estimate.
