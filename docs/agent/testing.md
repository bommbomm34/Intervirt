# Testing Intervirt Agent

Intervirt provides the test `GuestManagerTest` to test any Intervirt Agent and ensuring it's compatible with the
protocol.
Please keep in mind that this test is also experimental and it can also be the test's fault.

## Requirements

- Git
- JDK 21 or later
- (optional) Kotlin IDE (e.g. IntelliJ IDEA)

The commands may be easier to run on WSL if you're on Windows.

## Run test

#### Clone repository

```bash
git clone https://github.com/bommbomm34/Intervirt.git
cd Intervirt
```

#### Run tests

```bash
export INTERVIRT_TEST_VIRTUAL_AGENT_MODE=false
./gradlew :core:jvmTest --tests GuestManagerTest
```

Setting the environment variable is important to prevent the test from using a mock implementation of the agent. 