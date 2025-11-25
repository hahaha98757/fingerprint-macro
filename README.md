# Fingerprint Macro
[다운로드](https://github.com/hahaha98757/fingerprint-macro/releases)

GTA 온라인의 지문 매크로입니다.

지원 정보<br>
- 운영체제: Windows 10 64비트
- 게임 정보: Grand Theft Auto 5 레거시 및 인핸스드
- 지원 해상도: 1920 x 1080 (FHD)
- 화면 형식: 경계 없는 창, 경계 없는 전체 화면, 또는 전체 화면

## 사용법
1. 게임이 켜진 상태에서 프로그램을 실행합니다.
2. 화면이 가려지지 않도록 합니다.
3. 단축키(기본값 'F7')를 눌러 테스트를 할 수 있습니다.
4. 단축키(기본값 'F6')를 눌러 매크로를 작동시킵니다.

## 설정
설정은 app 폴더의 setting.ini 파일에 저장됩니다.

- legacyMode: GTA 온라인 레거시에서 매크로를 사용합니다. 변경이 매크로를 재시작해야 합니다.
- pressingTimes: 키를 누르고 있는 시간입니다. 단위는 밀리초(1 밀리초 = 0.001 초)입니다. 컴퓨터 성능에 따라 오차가 발생할 수 있으며, 너무 짧으면 인식되지 않을 수 있습니다. 이는 아래 inputDelays 또한 동일합니다.
- inputDelays: 키를 때고, 다음 키를 누르기 까지 대기하는 시간입니다.
- exit: 매크로를 종료하는 단축키입니다. 키의 이름은 [여기](https://javadoc.io/static/com.1stleg/jnativehook/2.1.0/org/jnativehook/keyboard/NativeKeyEvent.html) 에서 'VC_' 뒤의 이름을 "있는 그대로" 사용합니다. 이는 아래 단축키 3개 또한 동일합니다.
- reload: 설정을 다시 불러오는 단축키입니다.
- start: 매크로를 실행시키는 단축키입니다.
- test: 키 입력을 테스트하는 단축키입니다.

## 라이선스
이 프로젝트는 [LICENSE](LICENSE) 파일의 전문에 따라 MIT 라이선스가 적용됩니다.
<br>라이선스 및 저작권 고지 하에 개인적 이용, 수정, 배포, 상업적 이용이 가능하며 보증 및 책임을 지지 않습니다.

## 크레딧
- java-native-access의 [JNA](https://github.com/java-native-access/jna/tree/5.17.0) ([아파치 라이선스 2.0](https://github.com/java-native-access/jna/blob/5.17.0/AL2.0))
- kwhat의 [JNativeHook](https://github.com/kwhat/jnativehook/tree/2.2.2) ([GNU 약소 일반 공중 사용 라이선스 v3.0](licenses/JNativeHook-LICENSE))
- JetBrains의 [Kotlin](https://github.com/JetBrains/kotlin) ([아파치 라이선스 2.0](https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt))

----

## 업데이트 로그

### 1.1.2
- 테스트 소리 감소.

### 1.1.1
- 코드 최적화.
- pressingTimes와 inputDelays의 기본값을 8에서 20으로 변경.

### 1.1.0
- 버그 수정.

### 1.0.0
- 매크로 개발.