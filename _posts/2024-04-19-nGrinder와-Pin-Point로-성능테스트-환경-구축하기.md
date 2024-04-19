---
title: 애플리케이션 성능 테스트로 서버 튜닝하기
date: 2024-04-19 18:00:00 +09:00
description: >-
    웹 서버 성능 테스트를 시행하고 튜닝과정에 대해서 설명합니다.
categories: [성능 테스트, 모니터링]
tags: [성능 테스트, 부하 테스트]
---

## nGrinder
nGrinder를 사용하기 위해서는 Controller와 Agent를 실행해야한다. 도커 이미지를 받아 실행시켜 보겠다.

### Controller
```sh
docker pull ngrinder/controller
docker run -d -v
```
### Agent


## 트러블 슈팅
### Error while fetching files from SVN for admin
[이슈](https://github.com/naver/ngrinder/discussions/968)
- 도커 홈 디렉토리를 삭제하고 다시 실행하면 해결된다? -> ❌
- jdk 11 까지만 지원하기 때문에 jdk 버전을 바꾸어 본다.

