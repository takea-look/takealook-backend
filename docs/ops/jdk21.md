# 로컬 개발: JDK 21 세팅 가이드

이 프로젝트는 Gradle toolchain으로 **Java 21**을 요구합니다.

로컬에서 아래와 같은 에러가 나면 JDK21이 설치/인식되지 않은 상태입니다.

- `Cannot find a Java installation ... matching: {languageVersion=21 ...}`

## 1) JDK 21 설치

### macOS (권장: Temurin)

```bash
brew install --cask temurin@21
```

설치 확인:

```bash
/usr/libexec/java_home -V
java -version
```

> 참고: 여러 JDK가 공존할 수 있으니, `java -version`이 21을 가리키는지 확인하세요.

## 2) JAVA_HOME 설정(필요 시)

터미널 세션에서만 임시로 설정:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

영구 설정(zsh): `~/.zshrc`에 추가 후 새 터미널을 여세요.

## 3) Gradle 실행 확인

```bash
./gradlew test
```

## (선택) Gradle toolchain auto-provisioning

현재 레포 설정에서는 Gradle이 자동으로 JDK21을 다운로드(provision)하지 않습니다.
팀에서 auto-provisioning을 원하면 `toolchainManagement`(settings.gradle) 및 toolchain repository 설정을 추가하는 방향으로 별도 이슈로 다루는 것을 권장합니다.
