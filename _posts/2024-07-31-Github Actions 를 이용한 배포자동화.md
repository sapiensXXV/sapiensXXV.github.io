---
title: Github Actions를 이용한 배포 자동화
date: 2024-07-31 23:00:00 +09:00
description: >-
  Github Actions를 사용한 배포자동화 과정을 설명합니다.
categories: [인프라]
tags: [Github Actions, 배포]
---

## 백엔드 CI
`main`, `dev` 브랜치로 backend 디렉토리 하위에 커밋, 풀 리퀘스트가 발생했을 때 동작하는 워크플로우이다.
- 애플리케이션이 테스트를 통과하는지 검사한다.

## 백엔드 CD
`main` 브랜치로 backend 디렉토리 하위의 파일이 커밋, 풀 리퀘스트가 발생했을 때 동작하는 워크플로우이다.
- 빌드
  - Gradle로 애플리케이션을 빌드한다.
  - 빌드된 결과(`.jar`)를 아티팩트 파일로 업로드한다.
- 도커 이미지 빌드, 도커허브에 푸시
  - 빌드 단계에서 저장해둔 .jar 아티팩트 파일을 다운로드한다.
  - 도커 이미지를 빌드한다.
  - 도커 이미지를 도커 허브에 푸시한다.
- 배포
  - 배포할 서버에 SSH로 접속한다.
  - 도커 컴포즈 설정정보에 사용되는 .env 파일을 가상머신에 생성한다. 
  - 도커 컴포즈 설정파일을 사용해 도커 허브로부터 이미지를 내려받고 실행한다.
- 결과 안내
  - 배포 결과를 슬랙 메세지로 전달한다.

### 환경 변수 관리


## 부딪힌 문제들

### 로컬에서 환경변수 활용
스프링 부트 애플리케이션의 설정정보는 `application.properties`나 `application.yml`에 작성된다. 문제는 보안과 관련된 정보를 직접적으로 입력해서는 안된다는 것이다. 데이터 소스 URL, 사용자 이름, 패스워드, 개인키와 같은 보안과 관련된 정보가 레포지토리에 노출되기 때문이다. 

배포자동화를 하기 이전에 민감한 정보가 담긴 설정파일은 `.gitignore` 에 포함시켜 레포지토리에 올리는 것을 금지하였다. 하지만 Github Actions를 사용해 애플리케이션을 빌드하기 위해서는 설정파일도 레포지토리에 올라와 있어야한다. 체크아웃 액션을 사용해서 레포지토리의 파일, 디렉토리들을 CI 서버로 불러온 환경에서 빌드를 진행하는데, 설정파일이 존재하지 않는다면 어떻게 빌드가 수행될 수 있겠는가?

그래서 설정파일의 민감한 정보들을 환경변수로 대체하고, 실제 값은 Github Actions의 `secrets`에 저장해두고 사용한다. 그리고 워크플로우 파일에서 환경변수로 등록할 수 있다.

아래의 설정 파일은 스프링 부트 프로젝트의 데이터소스, 사용자 이름, 패스워드를 설정하는 부분이다. 각각을 환경변수로 대체해서 사용하였다. `.yml`에서 환경변수는 `${}`로 감싸면 사용할 수 있다.
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

Github Actions에 
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

세가지 `secrets`를 등록하고, 아래와 같이 워크플로우 설정파일에서 환경변수로 등록해 사용하면, 워크플로우에서 Gradle 빌드시 사용할 수 있다.

```yaml
env:
  DATABASE_URL: ${{ secrets.DATABASE_URL }}
  DATABASE_USERNAME: ${{ secrets.DATABASE_USERNAME }}
  DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
```

그런데 문제가 남아있다. 로컬에서 실행시킬 때는 환경변수가 등록되어 있지 않기 때문에 에러가 발생하기 때문이다. 인텔리제이 IDE를 사용한다면 `Edit Configuration...` 속성에 환경변수를 추가해서 실행시키면 환경변수를 등록한 채로 실행할 수 있다. 하지만 커맨드라인으로 환경변수와함께 gradle 빌드를 하고 싶어씩 때문에 다른방법을 찾아야 했다.

환경변수 정보를 담은 `.env` 파일을 프로젝트의 루트에 두고(멀티 모듈 프로젝트라면 사용하는 모듈의 루트에 놓아야한다.) 애플리케이션 설정파일에 spring.config.import 옵션을 사용해서 환경변수를 추가할 수 있다.

```properties
# .env
DATABASE_URL=데이터베이스 주소
DATABASE_USERNAME=데이터베이스 사용자 이름
DATABASE_PASSWORD=데이터베이스 비밀번호
```

```yml
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```
optional은 `.env` 파일이 존재할 때만 가져온다는 것을 의미하고, `[.properties]`는 `.env`를 `.env.properties`로 해석하여 가져오겠다는 것을 의미한다.

### Dockerfile 컨텍스트

다음 워크플로우 설정은 도커이미지를 빌드하는 부분이다.

```yaml
name: 백엔드 빌드-배포
# ...

jobs:
  debug:
    # ...
  build:  
    # ...
  docker-image:
    needs: build
    runs-on: ubuntu-latest
    name: 도커 이미지 생성, 푸시
    steps:
      - uses: actions/checkout@v4
      - name: 도커 로그인
        uses: docker/login-action@v3
        with: 
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
      - name: 빌드 jar 아티팩트 다운로드
        uses: actions/download-artifact@v4
        with:
          name: ${{ env.ARTIFACT_NAME }}
      - name: 도커 이미지 빌드
        run: |
          docker compose -f ./backend/docker/docker-compose.yml build --no-cache

      - name: 도커 이미지 푸시
        run: |
          docker compose -f ./backend/docker/docker-compose.yml push

  deploy:
    #...
```

도커 이미지 빌드 단계(step)을 보면 도커 컴포즈를 사용해서 빌드 파일을 만들고 있다. 도커 컴포즈를 통해 실행한 Dockerfile은 다음과 같은데,

```dockerfile
FROM openjdk:17-oracle as builder
WORKDIR /backend

ARG JAR_PATH=gongnomok-api.jar
COPY ${JAR_PATH} app.jar
ENTRYPOINT [ "java", "-jar", "app.jar" ]
```

> ERROR: failed to calculate checksum of ref 68fa3e36-3e48-46b1-9ff3-248c6f3575df::ys6agcbweudsif983yfwzb3pc: "/gongnomok-api.jar": not found

`빌드 jar 아티팩트 다운로드` 단계(step)에서 받아온 gongnomok-api.jar 파일을 찾을 수 없다는 에러가 발생했다.

문제의 원인은 docker-compose 설정파일에서 컨텍스트를 잘못 설정했기 때문이였다. 
```yaml
services:
  back:
    platform: linux/amd64
    build:
      context: ../..
      dockerfile: backend/docker/Dockerfile
    image: sjhn/gongnomok-backend:prod-1.2.3
    restart: always
    ports:
      - 8080:8080
```

`docker-compose.yml` 의 `build.context` 속성에 설정한 위치를 기준으로 dockerfile을 인식하고, `Dockerfile` 내부에서의 경로표현도 인식하는데, 그런 사항들을 고려하지 않고 막연하게 루트 경로를 기준으로 인식되겠거니 넘겨짚었기 때문에 발생한 문제였다.

따라서 도커 컴포즈 파일에서 설정한 context 정보가 Dockerfile의 컨텍스트가 된다는 점을 꼭 명심하고 작성하도록 하자.




## 참조
- [블로그 - GithubActions](https://www.daleseo.com/?tag=GitHubActions)
- [TISTORY - [Spring] 스프링 부트 프로젝트에서 dotenv 환경변수 파일 사용하기](https://gengminy.tistory.com/24)
