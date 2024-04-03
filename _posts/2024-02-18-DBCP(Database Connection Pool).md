---
title: DBCP(Database Connection Pool)
date: 2024-02-18 18:00:00 +09:00
description: >-
    데이터베이스 커넥션 풀(DBCP)에 대해서 알아봅니다.
categories: [CS, 포스팅]
tags: [데이터베이스, DBCP]
---

## DBCP란?

### 문제점

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/605c5cd3-a17a-41d1-824d-915da3831061/d12ac9b6-a451-40f7-a730-5d114715df2d/Untitled.png)

API를 통해서 서버가 요청을 받아 처리를 하다가 DB에 접근해 데이터를 조회할 일이 생기면 쿼리요청을 DB서버로 보낸다. DB서버는 데이터를 찾아서 처리한다음, 쿼리 응답을 보내준다. 쿼리 응답을 받은 백엔드 서버는 필요한 처리를 한 다음 응답을 보내주는 것이다.

백엔드 서버와 DB서버사이에서 요청이 왔을 때 어떤일이 일어나는지를 시퀀스 다이어그램을 통해 간단하게 살펴보았다. 쿼리를 요청/응답 부분을 좀 더 자세하게 살펴보자. Backend Server와 DB Server는 보통 서로 다른 컴퓨터에서 동작하기 때문에 쿼리를 요청하고 응답을 받는 것은 `네트워크 통신`을 하는 과정이다. 

Backend Server와 DB Server에서는 TCP 기반으로 네트워크 통신을 하게 된다. TCP를 기반으로 통신하게 되면 장점 중 하나가 높은 신뢰성으로 데이터를 송수신 할 수 있다는 것이다. 그런데 여기서 주목해야할 점은 TCP가 어떤식으로 동작하는지 집중해보자. TCP는 연결 지향적이기 때문에 데이터를 주고받기 전에 커넥션을 맺는 과정이 필요하다. 마찬가지로 데이터 송수신이 끝난 후에 커넥션을 끊는 과정이 필요하다.

그런데 문제는 커넥션을 열고 닫는 것이 간단한 과정이 아니라 꽤 시간을 소모하는 과정이라는 것이다. 보통 TCP 커넥션을 맺을 때는 3 way handshake, 끊을 때는 4 way handshake 과정을 거치는데 이것이 시간을 잡아먹는다. 따라서 커넥션을 열고 닫는 과정에서 매번 시간적인 비용이 발생하게 된다.

Backend Server에는 Request가 계속해서 들어오고 API의 종류도 여러 가지가 있을 것이고, 각각의 API가 한번만 DB에 접근하는 것이 아니라 여러번 DB에 접근할 수 있기 때문에 매번 커넥션을 열고 닫는 과정을 수행해야 하므로 시간적인 비용이 발생한다. 이것이 결국 서비스의 성능에 좋지 않은 영향을 준다.

이 문제를 DBCP를 이용해서 해결할 수 있다.

### DBCP는 어떻게 동작하는가?

Backend Sever를 띄울때 DB Connection을 미리 만들어두고 연결된 커넥션들을 마지 pool 처럼 관리할 수 있도록 해준다. 그러면 API 요청을 받아서 DB Server에 접근할 일이 있으면 새롭게 커넥션을 맺는 것이 아니라 커넥션 풀 에서 사용되지 않는 커넥션을 가져와 쿼리를 요청하는데 사용한다. (get connection) 쿼리 응답을 받은 백엔드 서버는 close connection 을 하게 된다. 커넥션 자체를 종료하는 것이 아니라 빌려온 connection을 커넥션 풀에 다시 반환한다. 

그러면 DB 서버에 접근해야할 때마다 커넥션을 새롭게 만드는 것이 아니라 미리 만들어진 커넥션을 커넥션 풀로부터 가져와 동작하기 때문에 커넥션을 재사용할 수 있고, 열고 닫는 시간도 절약된다. 백엔드 서버에서 더 좋은 성능을 발휘 할 수 있게 된다.

그때 이 커넥션 풀을 Database Connection Pool 이라고 부르고, 줄여서 DBCP 라고 한다.

## DBCP 설정 방법

데이터베이스의 종류도 다양하고 DBCP의 종류도 다양하기 때문에 DB는 MySQL, DBCP는 HikariCP 커넥션 풀을 기준으로 설명하겠다.

DB Connection은 Backend Server와 DB 서버 사이의 연결을 의미하기 때문에 Backend Server와 DB 서버 각각에서의 설정(configuration)방법을 잘 알고 있어야한다.

### DB 서버 설정

MySQL에는 중요한 두가지 파라미터가 있다.

- max_connections
    - **client와 맺을 수 있는 최대 connection 수**
    - 만약 max_connection 수가 4, DBCP의 최대 connection 수가 4라면?
        - 기존의 서버에 트래픽이 너무 몰려 가지고 있던 커넥션을 모두 사용하게 되는 등 과부하가 일어나서 신규 서버를 열었다고 해보자. 그러면 신규서버도 커넥션을 4개 맺어야하는데, DB 서버의 최대 커넥션수는 4이고, 이미 기존의 백엔드 서버와 커넥션을 맺고있기 때문에 커넥션을 맺을 수 없는 상태가 된다. 이런 상황이 발생하지 않도록 max_connections 파라미터 값을 잘 선택해야한다.
- wait_timeout
    - DB 서버에서 커넥션이 inactive할 때 다시 요청이 오기까지 얼마의 시간을 기다린 뒤에 close할 것인지를 결정
    - 다음과 같은 이상현상이 발생할 수 있다.
        - 비정상적으로 커넥션이 종료
        - 커넥션을 다쓰고 반환이 되지 않음. 누군가 점유는 하고있지만 사용하지도 않는 상태
        - 네트워크가 단절됨
    - DB 서버 입장에서는 이상현상이 발생해도 정상적으로 열려있는 커넥션이다. 따라서 요청이 오기를 기다리고 있는 상태가 된다. 적절한 시점에서 기다리는 문제를 해결해주어야한다.
    - 이 문제를 해결해주는 것이 wait_timeout 파라미터이다.
    - wait_timeout 파라미터에 설정한 값의 시간동안 요청이 오지않으면 close한다.
    - wait_timeout 값만큼 시간이 끝나기 이전에 요청이 오면 응답을 해준후 wait_timeout 카운트는 다시 0이된다.

### DBCP 설정

- minimumIdle
    - 커넥션 풀에서 유지하는 최소한의 idle connection 수
    - 아무 일도 하지 않는 유효 커넥션의 갯수를 결정한다.
- maximumPoolSize
    - Pool이 가질 수 있는 최대 커넥션 수
    - idle과 active(in-use) 커넥션을 합쳐서 최대 수

<aside>
💡 idle connection 수가 `minimumIdle`보다 작고, 전체 connection 수도 `maximumPoolSize`보다 작다면 신속하게 추가로 connection을 만든다. maximumPoolSize가 minimumIdle보다 우선순위가 높다.

</aside>

<aside>
💡 minimumIdle의 기본값은 maximumPoolSize의 값과 동일하도록 하는 것이고, 이 값을 사용하도록 권장된다. 두 파라미터의 값이 같다는 것은 Pool Size가 고정이라는 것을 의미한다. minimumIdle의 값을 maximumPoolSize 보다 작게하면 커넥션을 새롭게 만드는 상황이 생기고 이 과정에 시간적인 비용이 들어가기 때문이다.

</aside>

- maxLifetime
    - Pool에서 Connection의 최대 수명을 의미한다.
    - maxLifetime을 넘기면 idle일 경우 Pool에서 바로 제거된다. active인 경우 Pool로 반환된 후 제거된다.
    - DB의 Connection Time Limit보다 몇초 짧게 설정해야한다.
        - 2~3초 정도 짧게 설정하라고 권장된다.
        - DB의 wait_time과 DBCP의 maxLifetime에 차이가 없으면 아슬아슬하게 커넥션이 폐기될 수 있기 때문

<aside>
💡 Pool 로 반환되지 않으면 maxLifetime은 동작하지 않는다.

</aside>

- connectionTimeout
    - Pool에서 커넥션을 받기 위한 대기시간
    - 이 시간을 지나면 더 이상 커넥션을 대기하지 않고 Exception을 던진다.
    - 무한정 기다리지 않고 어느 순간에는 끊어주기 위해서 사용하는 파라미터이다.
    - 적절하게 connectionTimeout을 잡아주는 것도 중요하다.

이렇게 파라미터를 적절하게 설정해주는 것이 중요하다.

## 적절한 connection 수 찾기

이 부분에 대해서는 사람마다 의견이 다를 수 있다.

일반적으로 백엔드를 구축할 때는 HA를 보장하기 위해서 레플리케이션으로 구성한다. 백엔드서버에서 여러 요청이 있을 텐데, 크게 데이터베이스에 `쓰는 요청`과 `읽는 요청`으로 나눌 수 있다.

설정해준 파라미터들이 트래픽이 몰려도 잘 활용할 수 있는 파라미터 인것인지 궁금해진다. 그러면 이때 어떻게 적절한 커넥션 수를 찾을 수 있을까?

- 모니터링 환경 구축 (서버 리소스, 서버 스레드 수, DBCP 등등)
- 백엔드 시스템 부하 테스트
    - nGrinder
    - 부하 테스트는 요청을 저음 보냈다가 요청 수를 늘려가면서 어떻게 동작하는지 관찰하는 것을 말한다.
    - request per second와 avg reponse time을 확인한다.
        - request per second는 초당 몇개의 요청을 처리할 수 있는지 `전체적인 처리량`을 의미
        - avg response time은 `request가 처리되는 평균적인 시간`을 나타낸다. `API의 성능`을 나타내는 지표
- 부하(traffic)을 늘려주게 되면 어느순간 RPS는 오르다가 변하지 않게 되고, ART는 일정하다가 응답시간이 늘어나는 시점이 온다.
- 백엔드 서버, DB 서버의 CPU, 메모리 등등 리소스 사용률을 확인한다.
    - 만약 백엔드 서버의 CPU나 메모리 리소스 사용률이 너무 높다면 준비한 서버만으로는 감당하지 못한다는 의미이므로 서버를 더 추가해줘야한다. 백엔드 서버를 더 추가해서 부하를 줄일 수 있다.
    - 그것이 아니라 백엔드 서버는 괜찮은데 DB 서버의 CPU, 메모리 등 리소스 사용률이 올라간다면 DB 서버에 문제가 있다는 것을 의미한다.
        - SELECT 쿼리가 많아서 그런 것이라면 Secondary Storage 를 추가할 수 있다.
        - 백엔드 서버와 DB 서버 사이에 Cache Layer를 두어서 DB 서버의 부하를 낮춘다.
        - Sharding을 해준다
        - 등등 여러 방법을 사용해서 DB 서버가 받는 부하를 낮춰줄 수 있다.
    - 트래픽을 올려줬는데도, 백엔드서버와 DB 서버의 리소스 사용률이 위험할 정도로 올라오지 않지만 RPS, ART가 급격하게 변하는 지점이 있다면 그 포인트에서
        - thread per reqeust 모델이라면 active thread 수 확인
            - thread per request 모델은 request마다 thread를 할당해서 처리하는 모델이다. 어쩌면 thread pool에 있는 thread의 수가 병목현상의 원인이 될 수도 있다.
            - 전체 thread의 수가 5개 인데, active thread의 수도 5개라면 thread가 부족하구나
            - 전체 thread의 수가 100개 인데, active thread의 수가 50개라면 thread의 수에 여유가 있는데 어째서 처리량에 문제가 생기는지 다른각도에서 생각해 보아야한다.
        - DBCP의 Active Connection수 확인
            - Pool 에 있는 커넥션을 모두 사용하고 있다면 커넥션 수가 적어서 발생하는 문제일 수 있기 때문에 maximumPoolSize를 늘려서 테스트
        - DB의 max_connections 를 넘지 않도록 maximumPoolSize를 설정했는데도 여유가 있다면 이제는 max_connections를 올린다. 이러한 과정을 반복하며 적절한 커넥션 수를 찾아간다.
            - 사용할 백엔드 서버 수를 고려하여 DBCP의 max pool size를 결정해야한다.

<aside>
💡 이런식으로 종합적으로 고려하면서 적절한 커넥션 수를 설정할 수 있다.

</aside>