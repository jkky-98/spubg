# spubg

# 목차
- [개발 환경](#개발-환경)
- [사용 기술](#사용-기술)
    * [백엔드](#백엔드)
    * [데이터베이스](#데이터베이스)
    * [기타 라이브러리](#기타-라이브러리)
- [핵심 키워드](#핵심-키워드)
- [시스템 아키텍처](#시스템-아키텍처)
- [E-R Diagram](#e-r-diagram)
- [프로젝트 목적](#프로젝트-목적)
    * [기획 의도](#기획-의도)
- [핵심 기능](#핵심-기능)
    * [데이터 파싱 기능](#데이터-파싱-기능)
    * [디스코드 봇 제공 기능](#디스코드-봇-제공-기능)

## 개발 환경
- IntelliJ IDEA
- MAC OS

## 사용 기술
### 백엔드
### 주요 프레임워크 / 라이브러리
- Java 17
- SpringBoot 3.4
- Spring Data JPA
- MyBatis
- JDA 5.3

### 빌드 Tool
- Gradle

#### 데이터베이스
- H2

### 기타 라이브러리
- Lombok
- webflux - webclient

## 핵심 키워드
- 스프링 부트, 디스코드 봇, PUBG API를 사용하여 데이터 파싱 및 저장 어플리케이션, 디스코드 봇으로 메세지 기반 매치 데이터 통계 제공
- 스케줄러 기반 데이터 파싱 어플리케이션, JDA 기반 디스코드 봇 요청 및 응답 어플리케이션 분리
- 소규모 사용자 가정, 대규모 전적 통계 서비스에서 가용성 문제로 제공하지 못하는 세부 통계 데이터 제시

## 시스템 아키텍처
### 데이터 수집 및 처리
![data_parsing](https://github.com/user-attachments/assets/4b76e8e3-cf59-4c9b-8c62-6609fd117919)

### 디스코드 요청 및 응답 처리

## E-R Diagram

## 프로젝트 목적

### 기획 의도
친구들과 유일하게 즐기는 온라인 FPS 게임인 PUBG을 더 재밌게 즐기기 위해, 기존 전적 통계 사이트에서 제공하지 않는 신기하고 재밌는 통계를 디스코드 봇을 통해 제공하고 싶었습니다.

기존 전적 분석 사이트는 매치에 대한 개괄적인 통계를 제시합니다.(승, 패, 딜량, 킬, 데스)

PUBG API는 더욱 세부적인 지표를 제공하는데, 예를 들어 매치에서 개별 플레이어의 총알 발사에 대한 상세적인 정보를 제공합니다. 이를 통해 명중률과 같은 수치를 구할 수 있습니다.

단순히 한 매치에서 기록한 총 데미지량 뿐만 아니라 총 데미지 / 발사 횟수와 같이 발사대비 기록한 데미지와 같은 상세 정보를 제공할 수 있습니다.

더욱 세부적인 지표를 제공하고 한 시즌동안 치루어진 매치들을 모두 합산하여 하나의 매치 뿐만 아니라 한 시즌에 대한 통계, 팀원들끼리의 통계 비교를 통한 랭킹 제시 등을 수행하기 위해 이 프로젝트를 기획하였습니다.

## 핵심 기능

### 데이터 파싱 기능

### 디스코드 봇 제공 기능

디스코드 봇이 제공하는 통계는 Mybatis를 통해 DB의 데이터를 조회하여 제공합니다.

#### !도움

수행 가능 명령어를 나타냅니다. 간단한 명령어가 수행해주는 기능 설명이 들어가있습니다.

![image](https://github.com/user-attachments/assets/f356d367-c68c-482e-8c9d-4c9a0fab3a0b)

#### !등록

디스코드 닉네임과 DB에 등록된 PUBG username을 연동하는 기능입니다.

#### !멤버

현재 서버가 데이터를 관리, 파싱, 처리하고 있는 멤버들의 목록을 나타냅니다.

#### !웨폰마스터
이번 시즌 총기 및 투척 별 1등을 나타냅니다. 

(거리 및 클러치 상황은 가중 반영됩니다. 발사당 가한 데미지를 기본으로 판단합니다.)

(총알을 낭비하는 경우 이 지표는 좋지 않게 나타날 수 있습니다.)

![image](https://github.com/user-attachments/assets/a7c24bec-466b-4655-a0b3-139611f9258e)

#### !헤드슈터

이번시즌 헤드샷 비율이 높은 순으로 멤버들의 순위를 측정합니다.

헤드샷 비율은 기존 통계 사이트(kill시 헤드샷에 의한 것인지)와 다르게 헤드샷 이벤트 / 데미지 이벤트로 더 상세하게 헤드샷 비율을 제공합니다.

![image](https://github.com/user-attachments/assets/8115ea01-b155-4705-b5b2-5a2e2c72f682)

#### !감자왕

감자는 수류탄을 뜻하는 별명입니다. 수류탄으로 적에게 데미지를 많이 입히고 던진대비 평균 데미지가 높은 사람 순으로 멤버들의 순위를 측정합니다.

![image](https://github.com/user-attachments/assets/fe62f1df-71b8-4733-aed9-3a9e32897260)

#### !장거리러버

데미지 이벤트에 대해 공격자의 위치와 피격자의 위치로 하여금 거리를 구하여 DB에서 보관중입니다.

멤버별 이 거리를 평균을 내어 평균 거리가 높은 순으로 랭킹을 제공합니다.

순위가 높을수록 멀리있는 적을 잘 맞추는 플레이어입니다.
![image](https://github.com/user-attachments/assets/d9844e73-7686-4ef5-bb44-e1635a5bc95c)

#### !라이딩샷마스터

데미지 이벤트이면서 isVehicleIsIn 이 true일 경우 해당 공격자는 차를 탄 상태에서 총을 발사해서 적을 맞춘 것으로 해석됩니다.

이 데미지 량이 높은 순으로 랭킹을 제공합니다.

![image](https://github.com/user-attachments/assets/f9af3185-dc03-4347-b682-688ed0a26474)

#### !발사왕

매치당 총알 발사 이벤트의 SUM을 나타냅니다.

순위가 높을 수록 많은 총알을 사용하는 성향의 플레이어임을 뜻합니다.

![image](https://github.com/user-attachments/assets/116cfbf2-2ed2-4b59-8de7-2c2e82439cdd)

#### !후반딜러

데미지 이벤트에는 phase(게임의 초반,중반,후반)을 나타내는 값이 포함됩니다. 이는 1~8까지 소수로 이루어집니다.

자신의 데미지량 대비 후반 페이즈 데미지량 비율이 높은 순으로 랭킹을 제공합니다.

![image](https://github.com/user-attachments/assets/7b5f83ea-dea9-4bff-bdb6-9c80c08c26c1)

#### !클러치

CLUTCH VALUE는 자신이 낮은 체력임에도 불구하고 적에게 데미지를 주는 경향이 많다면 높게 측정됩니다.

자신의 위험을 무릎쓰고 적에게 데미지를 주는 성향이 높은 순으로 랭킹을 제공합니다.

![image](https://github.com/user-attachments/assets/a1bb479f-f976-4415-a6e4-2a7740a3b15f)

#### !기절왕

매치당 평균 기절 이벤트 발생을 플레이어 별로 나타냅니다.

매치당 평균 기절 이벤트가 높은 순으로 랭킹을 제공합니다.

![image](https://github.com/user-attachments/assets/0ece9327-6da8-4c3a-9a68-3f23bd03e1a0)




