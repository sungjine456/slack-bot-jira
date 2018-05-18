### 프로젝트 설명
슬랙 봇과 지라를 연결하여 슬랙에서 이슈의 키값을 입력했을 때 슬랙 봇이 반응하여 
해당 이슈의 URL을 반환하도록 하여 쉽게 이슈의 내용을 보기 위한 프로젝트이다.

### 슬랙 봇의 토큰 관리
src/main/resource 디렉토리에 key.properties 파일 생성
생성한 파일에 `token=토큰명` 추가 (토큰명에 실제 토큰을 작성)

### jira의 이슈키를 입력하여 이슈의 URL을 받는 방법
슬랙 봇의 토큰 관리에서 생성한 key.properties에 아래와 같이 값을 넣어 준다.
```
jira.user=유저명
jira.pass=비밀번호
jira.issueKey=이슈키
baseUri=jira의 경로 (http://localhost:8089/)
```

최초 1회 실행 시 URL이 나오는게 느리다.

### 실행방법
sbt run
