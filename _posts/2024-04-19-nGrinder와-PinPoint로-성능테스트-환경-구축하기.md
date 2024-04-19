---
title: 성능테스트 환경(nGrinder, PinPoint) 구축
date: 2024-04-19 18:00:00 +09:00
description: >-
    성능테스트 환경을 구축하는 과정을 설치합니다. 성능 테스트 툴로는 nGrinder, 모니터링 툴로는 PINPOINT를 사용합니다.
categories: [환경설정, 성능 테스트, 모니터링]
tags: [성능 테스트, 부하 테스트]
---

성능 테스트 환경을 구축하기 위해서 로컬에 nGrinder를 설치하고 애플리케이션 서버나 새로운 서버를 구축해서 PINPOINT를 활성화 시키는 방법에 대해서 설명합니다. 환경설정에 대한 글이기 때문에 각 도구의 사용방법에 대해서는 자세히 설명하지 않습니다.

> 여러 시행착오를 거친 후 결국 Docker가 짱이라는 생각이 박히게 되었다.

## nGrinder
nGrinder를 설치할 수 있는 방법은 다양한다.
- nGrinder Controller, Agent 파일을 다운 받아서 각각 실행하기
- Docker를 통해 설치하기
    - Docker 명령어를 직접 입력하기
    - Docker Compose 로 설치

나는 여기서 Docker Compose를 통할 설치를 선택했다. docker-compose.yml 파일만 잘 작성해놓는다면 다음에 다른 환경에서 필요할 때 파일만 복사해 설치/실행 명령어 하나만 입력하면 되기 때문이다.

```yml
version: '3.8'
services:
  controller:
    image: ngrinder/controller
    restart: always
    ports:
      - "9000:80"
      - "16001:16001"
      - "12000-12009:12000-12009"
    volumes:
      - ./ngrinder-controller:/opt/ngrinder-controller
  agent:
    image: ngrinder/agent
    restart: always
    links:
      - controller
```

- 실행을 마친 후 [localhost:9000](localhost:9000)으로 접속하면 웹 UI에 접속할 수 있다.
- 우상단의 메뉴에서 Agent Management 를 클릭해서 Agent도 잘 감지되는지 확인하자. 

## PINPOINT
원래는 핀포인트가 아닌 그라파나(Grafana)를 사용했으나 

## 트러블 슈팅
### Error while fetching files from SVN for admin
[이슈](https://github.com/naver/ngrinder/discussions/968)
- 도커 홈 디렉토리를 삭제하고 다시 실행하면 해결된다? -> ❌
- jdk 11 까지만 지원하기 때문에 jdk 버전을 바꾸어 본다 -> ❌
- 직접설치하는 방식을 포기하고 docker compose를 사용해서 컨트롤러와 에이전트를 돌리는 방식으로 방향을 회피했다. 해결못함...

### 토폴로지 형식으로 흐름이 보이지 않음
그라파나가 아니라 핀포인트를 사용하기로 마음먹은 이유가 토폴로지 형식으로 요청의 흐름을 보여주고, 요청의 수, 어디에 부하가 걸리는지 자세히 UI로 보여주기 때문인데... 이게 안되면 곤란하다.

### Unsupported class file major version 61
- PINPOINT에 있는 gradle과 JDK의 버전이 맞지 않아 생기는 에러.
- 에러 메세지 뒤의 `61`이라는 숫자가 JDK 버전에 따라 다르다고 한다. 이 숫자는 JDK 17과는 호환되지 않아 발생한 에러였다.
- JDK 버전을 11로 다운그레이드 시켜 해결하였다.

PINPOINT jar파일을 다운받고 실행하는 과정에서 발생한 에러였다. 하지만 결국에는 도커를 사용해서 PINPOINT를 설치했기에 고민할 필요가 없어졌다.