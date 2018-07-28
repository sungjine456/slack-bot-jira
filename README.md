### 프로젝트 설명
슬랙 봇과 지라, 빗버켓을 연결하여 슬랙에서 관련 정보들을 볼 수 있도록 만들고 있는 프로젝트이다. 
지라 : 이슈의 키값을 입력하여 해당 이슈의 정보를 볼 수 있다.
빗버켓 : 간단한 명령어를 통해 여러 정보를 볼 수 있다. 명령어는 ``bit-help``을 입력하면 볼 수 있다.

### 슬랙 봇의 토큰 관리
typesafe config 에 맞춰 key 파일을 생성한다.(key.conf에 맞춰 예제를 작성한다.)

src/main/resource 디렉토리에 key.conf 파일 생성
생성한 파일에 다음과 같이 추가한다. (토큰명에 실제 토큰을 작성)
```
slack {
  token = "토큰명"
}
```

### 지라의 이슈키를 입력하여 이슈의 URI을 받는 방법
슬랙 봇의 토큰 관리에서 생성한 key.conf 에 아래와 같이 값을 넣어 준다.

baseUri의 마지막에는 `/` 를 반드시 넣어 줘야한다.
```
jira {
  user = "유저명"
  pass = "비밀번호"
  issueKey = "이슈키"
  baseUri = "jira의 경로"  (ex) http://localhost:8089/
}
```

### 빗버켓의 설정 값을 추가하는 방법
슬랙 봇의 토큰 관리에서 생성한 key.conf 에 아래와 같이 값을 넣어 준다.

baseUri의 마지막에는 `/` 를 반드시 넣어 줘야한다.
```
bitbucket {
  user = "유저명"
  pass = "비밀번호"
  baseUri = "빗버켓의 경로"  (ex) http://localhost:8089/
  projectName = "프로젝트 명"
  repositoryName = "저장소 명"
}
```

### conf 파일 변경 방법
/scr/main/resources/scala/utils/ConfigurationReader.scala 파일에 있는 
private val config = ConfigFactory.load("key") 코드에서 "key" 문자열을 원하는 파일 명으로 변경해준다.

### 실행방법
sbt run

최초 1회 실행 시 URI가 나오는게 느리다.
