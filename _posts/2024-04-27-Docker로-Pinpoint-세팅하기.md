---
title: Docker로 Pinpoint 세팅하기
date: 2024-04-27 18:00:00 +09:00
description: >-
    애플리케이션 문제 원인에 대해 파악하는 방법을 알아봅니다.
categories: [성능 테스트, 환경설정]
tags: [성능 테스트, 스트레스 테스트, 테스트 도구]
---

## PINPOINT

### 핀포인트를 사용하는 이유
- 그라파나도 좋은 툴이지만 애플리케이션의 병목지점을 확인할 수 없다.
- 핀포인트는 기본적인 프로파일 정보뿐만 아니라 
    - 애플리케이션의 요청 흐름을 topology 도식으로 보여준다.
    - 요청이 몇번 호출되었는지
    - 어떤 메서드가 몇번 호출되었는지
    - 시간을 얼마나 걸렸는지
    - 호출 스택도 볼 수 있다.

이러한 장점 덕분에 문제가 발생한 지점을 좀더 빠르고 정확하게 파악할 수 있다는 장점이 잇다.

### 구축 흐름
핀포인트를 사용하기 위해서는 Controller와 Agent를 설치해야합니다. HBase등 과거이력을 보기 위해서 추출한 정보를 저장해두는 데이터베이스도 설치가 필요하지만 Docker Compose를 사용해서 설치한다면 크게 신경쓸 필요는 없습니다.

![핀포인트 구조도](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/6f04c7d3-d722-4c62-8bc8-9000e3dc8fee)

핀포인트 서버에 컨트롤러를 설치하고 운영 서버에서 웹 애플리케이션과 핀포인트 에이전트를 함께 실행시키면 됩니다.

> 핀포인트 서버는 메모리 용량이 `16GB` 이상인 환경에서 구축할 것을 권장하고 있습니다. 핀포인트가 띄워질 때 사용되는 메모리가 `13~15GB`정도이기 때문입니다. 필자는 처음에 `1코어, 2GB 메모리 환경`에서 핀포인트 서버를 구축했다가 머신이 완전히 멈추어버리는 문제를 겪었습니다..
{: .prompt-warning }

핀포인트 서버는 따로 VM을 구성하지 않고 로컬에서 실행하기로 하였다.

### 핀포인트 Controller 설치
[핀포인트 Quick Start 가이드](https://pinpoint-apm.github.io/pinpoint/quickstart.html)를 보면 여러가지 설치법이 소개되어 있다. 나는 이 중에서 가장 편리한 도커를 이용한 설치를 진행하려고한다. [공식 핀포인트 Docker 레포지토리](https://github.com/pinpoint-apm/pinpoint-docker)에 도커를 활용한 설치법이 자세히 설명되어 있다.

```sh
git clone https://github.com/pinpoint-apm/pinpoint-docker.git
cd pinpoint-docker
docker-compose pull && docker-compose up -d
```
위와 같은 세개의 명령만으로 도커 이미지 파일을 다운받고 서버를 실행할 수 있다.

Pinpoint v2.5.0부터 URI 메트릭과 인프라 메트릭이 추가되었는데 이 메트릭을 사용하기 위해서는 `docker-compose-metric.yml` 파일을 함께 docker compose 설정파일에 추가해야한다. 아래의 명령으로 수행하면 된다. 

```sh
git clone https://github.com/pinpoint-apm/pinpoint-docker.git
cd pinpoint-docker
docker-compose pull
docker-compose -f docker-compose.yml -f docker-compose-metric.yml up -d
```

이후 `http://{핀포인트 서버 도메인/IP}:8080`로 접속하면 핀포인트에 접속할 수 있다. 나는 로컬에서 구축했기 때문에 [http://localhost:8080](http://localhost:8080)로 접속했다. 

도메인에 접속해보면 에이전트 테스트를 위한 QuickStart앱으로 에이전트 테스트를 해볼 수 있다.
![핀포인트 localhost:8080](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/1b62d353-13c5-46c0-bf81-5c1ebac6ee8d)


`http://{핀포인트 서버 도메인/IP}:8085`로 접속해서 quickapp에 대한 요청을 보내 테스트를 진행해볼 수 있다.
![핀포인트 localhost:8085](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/a4403ac3-2977-4d82-8a28-d37e4855f1bf)

QuickStart앱은 에이전트 테스트를 위한 앱이기 때문에 운영환경에서는 제외해야한다. <br>
`docker-compose.yml` 파일에서 services 목록 중 

- `pinpoint-quickstart`
- `pinpoint-agent` 

두 가지를 삭제하거나 주석처리 해주면 된다.

### 핀포인트 Agent 설치
핀포인트 컨트롤러는 로컬이나 별도의 서버에. 애플리케이션에 붙이는 것은 에이전트 뿐이다.

애플리케이션을 빌드하면서 핀포인트가 띄워진 서버와 연동을 시도해야한다. 나는 프로젝트 빌드를 `Dockerfile`로 진행하고 있기 때문에 일단 작성된 `Dockerfile`을 보면서 설명하겠다.

```dockerfile
FROM openjdk:17-oracle as builder
WORKDIR /backend

ADD https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.4/pinpoint-agent-2.5.4.tar.gz /backend/
RUN tar -zxvf ./pinpoint-agent-2.5.4.tar.gz -C /backend/

# 핀포인트 설정정보 업데이트
RUN sed -i 's/profiler.transport.grpc.collector.ip=127.0.0.1/profiler.transport.grpc.collector.ip=<PINPOINT IP주소>/g' pinpoint-agent-2.5.4/pinpoint-root.config
RUN sed -i 's/profiler.collector.ip=127.0.0.1/profiler.collector.ip=<PINPOINT IP주소>/g' pinpoint-agent-2.5.4/pinpoint-root.config

ARG JAR_PATH=build/libs/gongnomok-app.jar
COPY ${JAR_PATH} app.jar

ENTRYPOINT java -jar \
-javaagent:/backend/pinpoint-agent-2.5.4/pinpoint-bootstrap-2.5.4.jar \
-Dpinpoint.applicationName=gongnomok-dev-2 \
-Dpinpoint.config=/backend/pinpoint-agent-2.5.4/pinpoint-root.config \
-Dspring.profiles.active=dev \
app.jar
```

- [핀포인트 에이전트 Github 릴리스](https://github.com/pinpoint-apm/pinpoint-docker/releases)에서 압축파일을 가져와서 압축해제한다. 나는 `2024-04-27` 기준 가장 최근버전인 `2.5.4` 버전으로 설치하였다.
- 그리고 에이전트가 추출한 데이터를 컨트롤러에 보낼 수 있도록 설정정보의 수정이 필요하다.
    - pinpoint-root.config의 기본 localhost IP(`127.0.0.1`) 에서 핀포인트 서버 IP로 변경해준다.
- 빌드 시 agent 내용을 함께 실행한다.

### 핀포인트 접속 확인
서버에서 핀포인트에 프로파일 데이터를 보내고, pinpoint-web에서 이를 시각화 해서 보여준다.

![pinpoint 실행결과 1](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/bd41c91b-3997-46c7-87dd-2c2c337fe97a)
![pinpoint 실행결과 2](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/b4ed3209-af0a-4398-902b-8d38c6a07ea5)

## 트러블 슈팅
### Unable to find jarfile
보통 이 에러는 `.jar` 파일이 없거나 경로가 없을 때 발생하는 에러이다. 하지만 분명히 `pinpoint-bootstrap-2.5.4.jar`는 지정해준 경로에 분명히 위치하는 파일이라는 것을 확인했다. 원인은 어이없게도 javaagent 옵션 앞에 대시(`-`)를 생략해주었기 때문에 발생한 에러였다.

에러 메세지가 계속해서 다음과같이 나왔었는데,
![jarfile error](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/51002fe5-4c58-49f5-a4fe-e24b287bb3ad)

jarfile 경로를 찾을 수 없다며 `javaagent:` 부분을 함께 보여줄 때 눈치챘어야 했다.

### connection timed out /<IP주소>:<포트>
메세지 그대로 collector와 통신할 수 없기 때문에 발생하는 에러이다. 

다음 두가지 문제 때문에 발생한 에러였다.

- 공유기의 외부 IP가 아니라 내부 IP를 핀포인트 IP로 지정했다
- 공유기의 포트포워딩 설정을 해주지 않았다.

![](https://github.com/mynameisjaehoon/mynameisjaehoon.github.io/assets/76734067/25972732-f782-4374-ae0c-5a4bbee3187f)

에이전트가 수집한 프로파일 데이터를 보낼 collector의 주소를 게이트웨이의 IP주소가 아니라 내부 IP주소로 설정했기 때문에 연결할 수 없다는 에러가 발생했다.

나는 핀포인트 서버를 아마존 EC2 인스턴스나 GCP VM인스턴스에 설치하지 않고 로컬에 설치했기 때문에 외부에서 게이트웨이 주소로 요청이 오면 핀포인트 서버가 설치된 로컬 컴퓨터의 IP주소로 보내주는 포트포워딩 설정이 필요했다. 

KT 공유기를 사용하고 있기 때문에 KT 공유기 설정으로 들어가서 포트포워딩 설정을 해주어 해결했다. [KT 공유기 포트포워딩 설정 방법]()은 다른 글에서 다루도록 하겠다.

