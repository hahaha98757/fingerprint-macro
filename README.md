# Fingerprint Macro
[다운로드](https://github.com/hahaha98757/fingerprint-macro/releases)

GTA 온라인의 지문 매크로입니다.

지원 정보<br>
- 게임 정보: Grand Theft Auto 5 레거시 및 인핸스드
- 화면 형식: 경계 없는 창, 경계 없는 전체 화면, 또는 전체 화면

## 사용법
1. 프로그램을 실행합니다.
2. 해상도에 맞게 설정을 조정하고 설정을 다시 불러옵니다.
3. 적어도 지문 조각이 있는 부분은 가려지지 않아야 하며, GTA5 창이 디스플레이 전체를 채우고 있어야 합니다.
4. 단축키(기본값 'F7')를 눌러 테스트를 할 수 있습니다.
5. 단축키(기본값 'F6')를 눌러 매크로를 작동시킵니다.

## 설정
설정은 setting.ini 파일에 저장됩니다. 프로그램 실행 시 생성되며, exe 파일의 경우 app 폴더에 있습니다.

- doNotEdit: 프로그램에서 자동으로 설정하는 값입니다. 변경하지 마세요.
  - version: 설정 파일의 버전입니다.
- general: 일반 설정입니다.
  - display: 감지할 모니터입니다. 1부터 시작합니다.
  - pressingTimes: 키를 누르고 있는 시간입니다. 단위는 밀리초(1 밀리초 = 0.001 초)입니다. 오차가 발생할 수 있으며 0 이하는 대기 코드를 건너 뜁니다.
  - inputDelays: 키를 때고, 다음 키를 누르기 까지 대기하는 시간입니다.
- layout: 해상도 설정입니다.
  - width: 디스플레이의 너비입니다.
  - height: 디스플레이의 높이입니다.
- hotkey: 단축키 설정입니다. 키의 이름은 [여기](https://javadoc.io/static/com.1stleg/jnativehook/2.1.0/org/jnativehook/keyboard/NativeKeyEvent.html) 에서 'VC_' 뒤의 이름을 "있는 그대로" 사용합니다.
  - exit: 매크로를 종료하는 단축키입니다.
  - reload: 설정을 다시 불러오는 단축키입니다.
  - start: 매크로를 실행시키는 단축키입니다.
  - test: 키 입력을 테스트하는 단축키입니다.
- debug: 디버그 모드입니다.
  - debug: 디버그 모드를 활성화합니다.
  - saveImage: 감지한 이미지를 저장합니다.
  - similarity: 각 지문 조각의 유사도를 출력합니다.


## 라이선스
이 프로젝트는 [LICENSE](LICENSE) 파일의 전문에 따라 MIT 라이선스가 적용됩니다.
<br>라이선스 및 저작권 고지 하에 개인적 이용, 수정, 배포, 상업적 이용이 가능하며 보증 및 책임을 지지 않습니다.

## 크레딧
- kwhat의 [JNativeHook](https://github.com/kwhat/jnativehook/tree/2.2.2) ([GNU 약소 일반 공중 사용 라이선스 v3.0](licenses/JNativeHook-LICENSE))
- JetBrains의 [Kotlin](https://github.com/JetBrains/kotlin) ([아파치 라이선스 2.0](https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt))

----

## 업데이트 로그

### 1.2.0
- 코드 수정
  - gradle 버전 업: 8.8 -> 9.6.1
  - 자바 버전 업: 17 -> 25
  - 코틀린 버전 업: 2.1.20 -> 2.3.21
- 설정 변경
  - pressingTimes와 inputDelays의 기본값을 20에서 10으로 변경.
  - 감지할 모니터 옵션 추가.
  - 해상도 설정 추가.
  - 허용 오차 및 유사도 임계값 옵션 추가.
  - 디버그 모드 세분화.
  - 설정 파일 형식 변경 시 설정 파일을 재생성.
  - 설정을 불러오는 중 매크로 비활성화.
- 디버그 모드
  - 각각의 설정을 따로 변경할 수 있음.
  - 유사도 출력 옵션 추가.
- 최적화
  - 이미지를 흑백으로 비교.
  - 프로그램 시작 시 지문 패턴을 미리 변환.
  - 프로그램 시작 시 모든 경우에 대한 최단 경로의 키 입력 순서를 생성.
  - pressingTimes, inputDelays가 0 이하일 때, Thread.sleep()을 호출하지 않음.
- Windows 의존성 제거.
- 테스트 시 입력 없이 매크로를 시작함.
- 감지한 지문과 입력한 키를 항상 출력.
- 버그 수정
  - 매크로 동작이 종료되지 않음.
  - 입력하는 키 이름이 제대로 표시되지 않음.

### 1.1.3
- 매크로가 여러번 작동하지 않도록 수정.

### 1.1.2
- 테스트 소리 감소.

### 1.1.1
- 코드 최적화.
- pressingTimes와 inputDelays의 기본값을 8에서 20으로 변경.

### 1.1.0
- 버그 수정.

### 1.0.0
- 매크로 개발.