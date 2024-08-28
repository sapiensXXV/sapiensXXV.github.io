---
title: Let's Encrypt 인증서 발급/갱신
date: 2024-08-29 02:00:00 +09:00
description: >-
  매번 까먹는 Let's Encrypt 인증서를 갱신하는 방법에 대해서 정리합니다. 갱신하는 것 정리하는 김에 발급받는 법도 정리했습니다.
categories: [인증]
tags: [보안]
---

## 인증서 발급

let's encrypt를 사용해서 인증서를 발급받는 여러 방식이 있지만 저는 DNS 방식으로 발급받을 것입니다. 도메인을 이미 구입해놨다면 DNS TXT를 등록하는 것만으로 쉽게 인증이 되고, 나중에 갱신하기도 간단하기 때문입니다.

```sh
certbot certonly --manual --preferred-challenges dns -d "<<도메인>>" -d "*.<<도메인>>"
```
certbot을 실행할 때 `--manual` 옵션과 `--preferred-challenges` 옵션을 추가해줍니다. `-d` 옵션을 사용해서 인증서를 적용할 도메인을 적어줍니다. 

여기서 저는 두개의 도메인을 등록했는데, 하나는 루트 도메인이고, 하나는 와일드 카드가 적용된 하위의 도메인입니다. 와일드 카드 도메인으로 인증서를 적용하면 `dev.<도메인>`과 같은 하위 도메인에도 인증서가 적용됩니다. `*.<도메인>` 만 적어주면 루트 도메인에는 적용되지 않으니 꼭 두개를 모두 적용 해주어야 합니다.

명령어를 입력하면 다음과 같은 안내가 나옵니다.

![발급 명령이후 알려주는 DNS TXT](https://github.com/user-attachments/assets/64e8ded5-c58b-4da1-8a0f-25e15c4d0628)

`_acme-challenge` 이라는 하위 도메인에 지정된 `DNS TXT`를 입력하라는 의미입니다. 

도메인을 구입한 곳에서 설정을 하고, 10분정도 기다리고 `ENTER`를 입력하면 인증이 완료됩니다. 너무 빨리 인증 확인 요청을 하면 아직 적용되지 않는 경우가 많아서 충분한 시간을 기다린 후 확인 요청을 하는 것이 좋습니다.

![DNS TXT 인증 이후](https://github.com/user-attachments/assets/91a3ce2f-1419-4e5a-9a39-f5dd6e1139ad)

인증이 완료되면 `fullchain.pem`, `privkey.pem` 파일의 위치를 알려줍니다. 이 파일의 키값들을 사용해서 HTTPS 인증에 사용하면 됩니다.


## 인증서 갱신

DNS 방식을 이용해 등록했다면 갱신하는 방법은 간단합니다.

dns방식으로 특정 도메인에 대해 재인증을 하겠다는 명령을 입력하면 됩니다.

```sh
certbot certonly --manual -d "*.<도메인>" -d "<도메인>" --preferred-challenges dns-01 --server https://acme-v02.api.letsencrypt.org/directory 
```

또는 [인증정보 조회](#인증정보-조회)를 통해서 알아낸 인증서 이름으로 renew 명령어를 사용할 수도 있습니다.
```sh
certbot renew <인증서 이름>
```
certbot의 renew명령어를 사용하여 인증서를 갱신하는 방법은 인증서가 처음 발급될 때 사용했던 인증 방법을 따라갑니다. DNS 방식으로 인증서를 발급받았다면, Certbot은 기본적으로 DNS 방식으로 인증서를 갱신하려고 시도합니다.

명령을 입력하면 새로운 DNS TXT를 제공해 줍니다. 제공된 TXT를 새롭게 등록하고 확인받으면 인증서 갱신이 완료됩니다.

## 인증정보 조회

```sh
certbot certificates
```

위의 명령으로 어떤 인증서가 어떤 도메인에 대해서 발급되어 있고, 기한은 언제까지인지 조회할 수 있다.

![인증서 정보조회](https://github.com/user-attachments/assets/e8e479ac-6b54-40c2-a0c0-c6ae0d1974ca)

인증서의 이름은 무엇이고, 어떤 도메인에 대해 인증서가 등록되어 있는지, 그리고 만료기한은 언제까지인지 정보를 확인할 수 있다.
