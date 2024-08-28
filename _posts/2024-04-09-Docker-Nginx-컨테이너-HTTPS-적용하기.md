---
title: Docker Nginx 컨테이너로 HTTPS 적용하기
date: 2024-04-09 18:00:00 +09:00
description: >-
    Docker Nginx 컨테이너로 SSL 적용하는 방법에 대해서 알아봅니다.
categories: [인프라, 도커, 프로젝트]
tags: [백엔드, 메이플 주문서 시뮬레이터]
---

[지난 포스팅](https://mynameisjaehoon.github.io/posts/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-Docker%EB%A1%9C-%EB%B0%B0%ED%8F%AC%ED%95%98%EA%B8%B0/)에서 프로젝트를 도커로 배포하는 부분에 대해서 살펴보았습니다. Nginx와 React를 하나의 컨테이너에 담아 배포하였습니다. 도커 환경에서 Nginx 웹서버의 SSL을 적용하는 방법에 대해서 살펴보겠습니다.

## 과정 설명

사실 몇가지 도커 관련작업을 해주는 것 외에는 별로 다를 것이 없습니다.
1. SSL을 제공하는 서비스를 사용해서 공개키와 개인키를 확보한다.
    - 기존에 도메인을 등록해두지 않았다면 `Let's Encrypt`와 같은 서비스를 사용해서 
        - 도메인을 등록하고
        - 공개키와 개인키를 확보한다.
    - 등록해둔 도메인과 키, 그리고 설정정보를 미리가지고 있다면 그 정보를 컨테이너에 복사할 수 있도록 `Dockerfile`에 명령어를 추가한다.
2. Nginx가 `443(https)`포트로 접속할 수 있도록 설정해준다.
    - 컨테이너로 `80`, `443` 포트로 접속할 수 있도록 도커 컴포즈 파일을 수정한다.
    - `80(http)`번 포트로 접속하면 `443(https)`로 리다이렉션한다.
    - `443(https)`포트에 접속할 수 있는 스크립트를 작성한다.
    - 사용하는 공개키, 개인키, ssl 설정정보등을 포함시킨다.
3. Nginx가 포함되어 있는 도커 이미지를 다시 빌드한다. 

## HTTPS 적용
### SSL 키, 설정정보 복사
나의 경우에는 현재 서비스에 HTTPS를 사용하는 것이 처음이 아니기 때문에 공개키와 비밀키, 그리고 설정정보 파일을 미리 가지고 있다.
[다른 서버로 프로젝트 이전하기](https://velog.io/@januaryone/%EB%8B%A4%EB%A5%B8-%EC%84%9C%EB%B2%84%EB%A1%9C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%9D%B4%EC%A0%84%ED%95%98%EA%B8%B0#6-https-%ED%82%A4-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0)라는 글에서도 작성해두었지만 Let's Encrypt를 사용한 경우 인증을 위한 파일을 수동으로 새로운 서버에 복사해놓으면 사용할 수 있다. 
따라서 다음 세가지 파일을 Dockerfile이 위치하는 폴더에 secret이라는 폴더를 새롭게 만들고 저장해두었다.
- `fullchain.pem`
- `privkey.pem`
- `options-ssl-nginx.conf`

그런 다음 Dockerfile에 다음 내용을 추가한다.
```docker
FROM nginx:1.18.0-alpine
...

RUN mkdir /etc/secret
RUN chmod 755 /etc/secret

COPY ./secret/fullchain.pem /etc/secret/
COPY ./secret/options-ssl-nginx.conf /etc/secret/
COPY ./secret/privkey.pem /etc/secret/
```

> 위 스크립트처럼 도커 파일에 직접 인증 키를 포함하는 것은 심각한 보안 이슈가 된다. 공부할 당시에는 몰랐지만 이제는 절대 저런방식으로 인증키를 전달하지 않는다. 보안과 관련된 정보를 직접적으로 담지 말자.
{: .prompt-danger }

`RUN mkdir /etc/secret`
- https를 적용하는데 필요한 인증파일이 위치할 폴더를 만든다.

`COPY ./secret/fullchain.pem /etc/secret/`
`COPY ./secret/options-ssl-nginx.conf /etc/secret/`
`COPY ./secret/privkey.pem /etc/secret/`
- 필요한 인증파일을 특정 폴더에 복사한다. 
- 굳이 해당경로의 폴더가 아니더라도 본인이 두고싶은 곳에 두면된다.

### Nginx 설정정보 수정
다음으로 Nginx 설정파일을 수정한다. 
```
server {
    listen 80;
    server_name gongnomok.site;

    location / {
        return 301 https://gongnomok.site$request_uri;
    }

}

server {
    listen 443 ssl;
    server_name gongnomok.site;

    ssl_certificate /etc/secret/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/secret/privkey.pem; # managed by Certbot
    include /etc/secret/options-ssl-nginx.conf; # managed by Certbot

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

	location /api {
        proxy_pass http://gongnomok.site:8080;
    }
}
```

80 포트로 오는 요청은 https로 리다이렉션하였고, 443 요청으로 올 때는 이전에 복사해둔 인증파일을 사용해서 SSL을 수행하게 된다. 
`/api` 경로로 온 요청은 WAS로 넘기고 이외의 경로에는 리액트를 빌드한 결과로 나온 index.html을 반환한다.

### 도커 컴포즈 수정
이전에는 80포트만 사용했기 때문에 도커 컴포즈 파일을 수정해야한다.

```yml
version: "1.1.2"
services:
  back:
    platform: linux/amd64
    build:
      context: ./backend
      dockerfile: Dockerfile
    image: sjhn/gongnomok-backend:1.1.2
    restart: always
    ports:
      - 8080:8080
  front:
    platform: linux/amd64
    build:
      context: ./frontend
      dockerfile: Dockerfile
    image: sjhn/gongnomok-frontend:1.1.2
    restart: always
    ports:
      - 80:80
      - 443:443
```
달라진 부분으로는 front 서비스의 포트 설정 부분이다.
- 80포트로 온 요청을 https로 리다이렉션 해야하기 때문에 결과적으로 80포트도 허용해야한다.
- https 프로토콜의 포트번호인 443 포트를 허용해야한다.
- 도커 컴포즈 파일로 여러개의 포트를 허용하고 싶을 때 위와같이 작성할 수 있다.

## 트러블 슈팅
### 인증파일이 컨테이너 내부에 존재하지 않는 문제

node, nginx가 함께 담겨있는 컨테이너를 실행시켰더니 인증파일을 찾을 수 없다는 에러를 마주했다.

해당 컨테이너의 Dockerfile은 다음과 같이 작성되어 있었다.
```docker
FROM node:20.11.0-bullseye as builder
WORKDIR /frontend
COPY . .
RUN npm install
RUN npm run build

FROM nginx:1.18.0-alpine
RUN rm /etc/nginx/conf.d/default.conf
RUN rm -rf /etc/nginx/conf.d/*
COPY ./default.conf /etc/nginx/conf.d/

COPY --from=builder frontend/dist /usr/share/nginx/html
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
```

이 상황에서 https를 적용하기 위해서 필요한 인증파일을 복사하기 위해 다음과 같은 명령어를 수행해야한다.

```
RUN mkdir /etc/secret
RUN chmod 755 /etc/secret

COPY ./secret/fullchain.pem /etc/secret/
COPY ./secret/options-ssl-nginx.conf /etc/secret/
COPY ./secret/privkey.pem /etc/secret/
```

위 명령어들을 nginx 이미지가 올라오기 전 `FROM` 명령어 앞에 작성한 것이 원인이 되었다.
이미지가 올라오지도 않았는데 디렉토리를 만들고, 파일을 복사한다는 명령을 수행했으니 당연히 해당 디렉토리나 파일은 찾을 수가 없었다.


### 443 포트로 연결되지 않음
- 운영서버의 도커 컴포즈 파일을 갱신하지 않고 docker compose 명령으로 컨테이너를 실행시켜 발생한 문제였다.
- 갱신되기 이전의 도커 컴포즈 파일에는 컨테이너가 443포트를 노출한다는 내용이 없었으므로 당연히 443 포트로 접속할 수 없었다.
### 이미지 이름을 잘못적은 어이없는 실수
- 도커 컴포즈 파일에서 서비스 이름과 도커 이미지 이름을 잘못 매칭시킨 어이없는 실수를 함.


