<div align="center">
  <img src="https://github.com/user-attachments/assets/8761517f-b6b2-45a8-8c2d-fcfd4c95f1b7" alt="logo" width="300" height="200"/>
</div>

<p align="center">
  <strong><font size="30">Header Project</font></strong><br>
  <font size="20">소상공인들을 위한 CRM 프로젝트</font>
</p>

## 목차
> ### [1. S.O.M (Solving Our Meals)](#1-som-solving-our-meals-1) 
> ### [2. Team Members Information](#2-team-members-information-1)
> ### [3. Project Outline](#3-project-outline-1) 
> ### [4. Project Architecture](#4-project-architecture-1) 
> ### [5. Development Environment](#5-development-environment-1) 
> ### [6. Features](#6-features-1) 
> ### [7. Directory Structure](#7-directory-structure-1) 
> ### [8. Comparative Program Analysis](#8-comparative-program-analysis-1)
> ### [9. Work Flow Chart](#9-work-flow-chart-1) 
> ### [10. ERD](#10-erd-1)   
> ### [11. Persona Analysis](#11-persona-analysis-1)
> ### [12. WBS](#12-wbs-1)
> ### [13. Software Requirement Specification](#13-software-requirement-specification-1)  
> ### [14. RESTful API design](#14-restful-api-design-1)
> ### [15. Figma Planning](#15-figma-planning-1)
> ### [16. Questionnaire](#16-questionnaire-1)

<br>

## 1. BOA
>- **BOA - 이유...?**

<br>

## 2. Team Members Information

| Project Manager | Configuration Manager | Database Manager | Database Manager | Database Manager |
|-------------------|-------------------|-------------------|-------------------|-------------------|
| ![윤정아](https://github.com/user-attachments/assets/58e319a6-9798-4e18-a09c-baa8917bc9de)  | ![서열익](https://github.com/user-attachments/assets/24678505-c91f-4e09-9501-39076ac953d9)  | ![김주혜](https://github.com/user-attachments/assets/acbe013b-33dd-439a-af41-60fe5fc20e43)  | ![배은아](https://github.com/user-attachments/assets/ad754943-7a38-4645-9c32-9d3be8c7e50e)  |![김예람](https://github.com/user-attachments/assets/ad754943-7a38-4645-9c32-9d3be8c7e50e)  |
|  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![Static Badge](https://img.shields.io/badge/https%3A%2F%2Fimg.shields.io%2Fbadge%2Fany_text-%EC%A1%B0%ED%98%95%EC%84%9D-blue?style=for-the-badge&logo=react&logoColor=0099e5&logoSize=auto&label=PM&labelColor=ff4c4c&color=34bf49) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![Static Badge](https://img.shields.io/badge/https%3A%2F%2Fimg.shields.io%2Fbadge%2Fany_text-%EC%9D%B4%ED%98%B8%EC%A4%80-blue?style=for-the-badge&logo=github&logoColor=black&logoSize=auto&label=CM&labelColor=0abf53&color=00112c) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![Static Badge](https://img.shields.io/badge/https%3A%2F%2Fimg.shields.io%2Fbadge%2Fany_text-%EA%B9%80%EC%A3%BC%ED%98%9C-blue?style=for-the-badge&logo=mysql&logoColor=b3dcff&logoSize=auto&label=DB&labelColor=003b64&color=ce1126) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![Static Badge](https://img.shields.io/badge/https%3A%2F%2Fimg.shields.io%2Fbadge%2Fany_text-%EC%9D%B4%EC%9D%80%EC%84%9C-blue?style=for-the-badge&logo=files&logoColor=ff7800&logoSize=auto&label=DB&labelColor=2d364c&color=6534ac) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  [**GitHub**](https://github.com/Passbob) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  [**GitHub**](https://github.com/Pear1yCode) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  [**GitHub**](https://github.com/JUHYE0925) | &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  [**GitHub**](https://github.com/manbeao) |

<br>

## 3. Project Outline
### 배경
> - 요리에 대한 여러 프로그램의 성행으로 맛있는 한 끼에 대한 관심도가 증가한 반면 혼밥을 하는 사람들에게 적합한 사이트가 존재하지 않아
혼밥을 위한 사이트를 기획하게 되었습니다.
> - 식사공간, 메뉴 선택의 제약, 최소주문인원 및 금액, 타인의 시선, 가게 주인의 눈치 등의 혼밥에 대한 불편한 인식을 개선하는 것을 목표로 삼았습니다.
### 방향성
> - 혼밥에 대해 초점을 맞춘, 혼밥에 최적화된 정보들을 제공하고자 합니다.
> - 가게 측에서 실시간 예약인원 조정을 할 수 있어 가게 상황에 맞는 최대의 인원을 수용하고 사용자는 예약 가능 여부를 실시간으로 확인 가능하도록 합니다.
> - QR코드를 통해 예약 정보를 확인하여 노쇼 회원을 파악 및 관리하도록 합니다.
> - 언제 어디서든 편하게 혼자 식사를 원할 때 혼밥을 할 수 있는 식당 정보를 쉽게 파악하고 예약하며 빠르게 자리를 잡을 수 있는 공간을 제공하여
    혼밥에 대한 긍정적인 인식을 확산시킴과 동시에 개인주의형 라이프스타일을 충족시키며 혼밥을 더욱 더 자유롭고 편리하게 즐길 수 있는 환경을 제공할 수 있을거라 기대하고 있습니다.
### 개발 기간
> 2024.11.20 ~ 2025.02.04

<br>

## 4. Project Architecture 
![호준 - 아키텍처 (1)](https://github.com/user-attachments/assets/04123cab-3aab-4fee-b0c5-02b66d05d1f6)

<br>

## 5. Development Environment 

#### Version Management
> ![git](https://img.shields.io/badge/GIT-E44C30?style=for-the-badge&logo=git&logoColor=white)
> ![github](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)

#### Cooperation
> ![github](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)
> ![figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
> ![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
> ![KakaoTalk](https://img.shields.io/badge/kakaotalk-ffcd00.svg?style=for-the-badge&logo=kakaotalk&logoColor=000000)

#### Front-End
> ![REACT](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
> ![JavaScript](https://img.shields.io/badge/JavaScript-323330?style=for-the-badge&logo=javascript&logoColor=F7DF1E)
> ![HTML](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
> ![CSS](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
> ![SASS](https://img.shields.io/badge/Sass-CC6699?style=for-the-badge&logo=sass&logoColor=white)

#### Back-End
> ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
> ![SpringBoot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
> ![SpringSecurity](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
> ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

#### DataBase
> ![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
> ![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)

#### Server & Distribution
> ![DOCKER](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
> ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

#### Development Tools
> ![intelliJ](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
> ![Vscode](https://img.shields.io/badge/VSCode-0078D4?style=for-the-badge&logo=visual%20studio%20code&logoColor=white)

#### Font
> __[Sandol Web Font Cloud](https://www.sandollcloud.com/?lang=ko_KR)__ (Payment & No copyright issues) <br>
> __[BobaesumJindoTTF](https://www.jindo.go.kr/home/sub.cs?m=870)__ (No copyright issues) <br>
> __[Pretendard](https://cactus.tistory.com/306)__ (No copyright issues) <br>
> __[Mungyeong-Gamhong-Apple](https://www.gbmg.go.kr/portal/contents.do?mId=0501110000)__ (No copyright issues) <br>

#### Image
> __[Freepik Premium](https://www.freepik.com/)__ (Payment & No copyright issues)

<br>

## 6. Features
### 공통 기능
#### 메인 페이지
<img src="https://github.com/user-attachments/assets/2e67414b-4943-41ca-87b6-99359ac9dfed" alt="메인페이지gif" width="600"/>

<hr>

#### 식당 검색
![검색](https://github.com/user-attachments/assets/1eaf232d-f2a2-48ff-9208-9f57847600cc)

<hr>

### 회원 관련 기능
#### 로그인
![로그인](https://github.com/user-attachments/assets/62c5f454-2998-4386-a8fa-9024ffa6d301)

<hr>

#### 식당 정보 상세 보기
![image (8)](https://github.com/user-attachments/assets/ba95924b-5b6e-4864-9872-f648c2a8510f)

<hr>

#### 식당 예약
![예약](https://github.com/user-attachments/assets/06a125e8-ac25-474e-8505-0567ae684390)

<hr>

### 식당 사장님 관련 기능
#### 예약 가능 인원 조정
![예약가능인원조정](https://github.com/user-attachments/assets/bcbcefc8-b2cb-48e8-a560-6dad77552dfd)

<hr>

### 관리자 관련 기능
![관리자](https://github.com/user-attachments/assets/84f60477-d456-494d-b6e5-a7826f9cb20d)

<br>

## 7. Directory Structure
<h3> 
<details>
<summary><a></a>Frontend</summary>
<div markdown="1">
  
```
📦src
├─📂admin
│  └─📂components
│     └─📂css
│  └─📂layouts
│     └─📂user
│  └─📂pages
│     ├─📂dashboard
│     ├─📂inquiry
│     ├─📂notice
│     ├─📂reservation
│     ├─📂review
│     ├─📂store
│     └─📂user
│  └─📂route
├─📂common
│  ├─📂authContext
│  ├─📂default
│  ├─📂error
│  ├─📂floatingBar
│  ├─📂footer
│  ├─📂header
│  ├─📂inquiry
│  ├─📂logoutButton
│  └─📂reset
├─📂config
├─📂store
│  ├─📂layouts
│  └─📂pages
│     ├─📂bossInquiry
│     ├─📂bossNotice
│     ├─📂bossReview
│     ├─📂bossStore
│     ├─📂qrCheck
│     └─📂storeInfo
├─📂user
│  ├─📂css
│  ├─📂layouts
│  └─📂pages
│     ├─📂announcement
│     ├─📂find
│     ├─📂info
│     ├─📂login
│     ├─📂main
│     ├─📂mypage
│     ├─📂reservation
│     ├─📂search
│     ├─📂signup
│     └─📂storedetail
├─ app.js
└─ index.js
```

</div>
</details>
</h3>

<a href="https://github.com/Solving-Our-Meals/ukki-front" target="_blank">우끼 Frontend 깃허브 보러가기</a>

<h3>
<details>
<summary>Backend</summary>
<div markdown="1">

```shell
 
📦ukki-back
  └─ src
     └─ main.java.com.ohgiraffers.ukki
        │           ├─ 📂admin
        │           │  ├─ 📂inquiry
        │           │  │  ├─ 📂controller
        │           │  │  └─ 📂model
        │           │  │     ├─ 📂dao
        │           │  │     ├─ 📂dto
        │           │  │     └─ 📂service
        │           │  ├─ 📂notice
        │           │  │  ├─ 📂controller
        │           │  │  └─ 📂model
        │           │  │     ├─ 📂dao
        │           │  │     ├─ 📂dto
        │           │  │     └─ 📂service
        │           │  ├─ 📂reservation
        │           │  │  ├─ 📂controller
        │           │  │  └─ 📂model
        │           │  │     ├─ 📂dao
        │           │  │     ├─ 📂dto
        │           │  │     └─ 📂service
        │           │  ├─ 📂review
        │           │  │  ├─ 📂controller
        │           │  │  └─ 📂model
        │           │  │     ├─ 📂dao
        │           │  │     ├─ 📂dto
        │           │  │     └─ 📂service
        │           │  ├─ 📂store
        │           │  │  ├─ 📂controller
        │           │  │  └─ 📂model
        │           │  │     ├─ 📂dao
        │           │  │     ├─ 📂dto
        │           │  │     └─ 📂service
        │           │  └─ 📂user
        │           │     ├─ 📂controller
        │           │     └─ 📂model
        │           │        ├─ 📂dao
        │           │        ├─ 📂dto
        │           │        └─ 📂service
        │           ├─ 📂auth
        │           │  ├─ 📂controller
        │           │  ├─ 📂filter
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           ├─ 📂common
        │           │  ├─ 📂controller
        │           │  ├─ 📂handler
        │           │  └─ 📂service
        │           ├─ 📂config
        │           ├─ 📂exception
        │           ├─ 📂inquiry
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           ├─ 📂notice
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           ├─ 📂qr
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     └─ 📂service
        │           ├─ 📂report
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           ├─ 📂reservation
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           ├─ 📂store
        │           │  ├─ 📂controller
        │           │  └─ 📂model
        │           │     ├─ 📂dao
        │           │     ├─ 📂dto
        │           │     └─ 📂service
        │           └─ 📂user
        │              ├─ 📂controller
        │              └─ 📂model
        │                 ├─ 📂dao
        │                 ├─ 📂dto
        │                 └─ 📂service
        └─ 📂resources
           ├─ 📂images
           ├─ 📂mappers
           │  ├─ 📂admin
           │  │  ├─ 📂inquiry
           │  │  ├─ 📂notice
           │  │  ├─ 📂reservation
           │  │  ├─ 📂view
           │  │  ├─ 📂store
           │  │  └─ 📂user
           │  ├─ 📂inquiry
           │  ├─ 📂mypage
           │  ├─ 📂notice
           │  ├─ 📂qr
           │  └─ 📂report
           ├─ application.yml
           ├─ application-prod.yml
           └─ credentials.json
```

</div>
</details>
</h3>

<a href="https://github.com/Solving-Our-Meals/ukki-back" target="_blank">우끼 Backend 깃허브 보러가기</a>

<br>

## 8. Comparative Program Analysis
<details>
<summary>1. 캐치테이블</summary>
  <img src="https://github.com/user-attachments/assets/ad796546-46c6-4f05-bc91-8550757927ee" alt="캐치테이블" width="600"/>
</details>
<details>
<summary>2. 포잉</summary>
  <img src="https://github.com/user-attachments/assets/5d8ad40b-70c1-4001-8d51-9cde193dde35" alt="포잉" width="600"/>
</details>
<details>
<summary>3. AutoReserve</summary>
  <img src="https://github.com/user-attachments/assets/062d4d13-6a2d-4ee6-8c12-979803faa07f" alt="AutoReserve" width="600"/>
</details>
<details>
<summary>4. 테이블링</summary>
  <img src="https://github.com/user-attachments/assets/951c275e-b500-4a16-9513-d12dd0b7a8bf" alt="테이블링" width="600"/>
</details>

<br>

## 9. Work Flow Chart
![업무흐름도](https://github.com/user-attachments/assets/3c26f01e-ea77-46fd-b5d5-5ccb264d50af)

<br>

## 10. ERD
### 논리 데이터베이스 모델
![image (2)](https://github.com/user-attachments/assets/b51f74e6-2ae2-4e52-88bd-908c789f53d5)
### 물리 데이터베이스 모델
![image (3)](https://github.com/user-attachments/assets/3f7a30f1-2632-4e5f-81cf-921817baf337)

<br>

## 11. Persona Analysis
<details>
<summary>페르소나 분석</summary>
  <img src="https://github.com/user-attachments/assets/82c0c042-eb38-4b1c-8755-9c54464fc812" alt="이명호_사용자여정지도" width="500"/>
  <img src="https://github.com/user-attachments/assets/7d331668-c937-4eed-b163-4d1c50d753ca" alt="이명호_페르소나분석" width="500"/>
  <img src="https://github.com/user-attachments/assets/23006004-6c23-481e-b4c1-24b241b49fc1" alt="한수희_사용자여정지도" width="500"/>
  <img src="https://github.com/user-attachments/assets/89e1e912-df00-4913-89a6-e4af83846dc4" alt="한수희_페르소나분석" width="500"/>
</details>

<br>

## 12. WBS
<details>
<summary>WBS</summary>
  <img src="https://github.com/user-attachments/assets/486b598a-194e-4afc-b096-6f2d87b589ed" alt="WBS" width="900"/>
</details>

<br>

## 13. Software Requirement Specification
<details>
<summary>Software Requirement Specification</summary>
  <img src="https://github.com/user-attachments/assets/7717b823-2485-4706-b24e-4a6bc681ca65" alt="Software Requirement Specification" width="500"/>
  <img src="https://github.com/user-attachments/assets/cb983fe4-e23e-4a05-9dab-64ee2f250ba3" alt="Software Requirement Specification" width="500"/>
  <img src="https://github.com/user-attachments/assets/d5ac65f8-dbe3-43c5-a82c-ac856c2cfa55" alt="Software Requirement Specification" width="500"/>
  <img src="https://github.com/user-attachments/assets/f3443697-4bfd-4595-834a-9b7d34b1226d" alt="Software Requirement Specification" width="500"/>
</details>

<br>

## 14. RESTful API design
<details>
<summary>RESTful API design</summary>
  <img src="https://github.com/user-attachments/assets/4cfc6c29-1810-4927-b757-c7b9366d91e6" alt="RESTful API design" width="500"/>
  <img src="https://github.com/user-attachments/assets/9f74ecc5-b938-418b-97d1-5c637375fcd0" alt="RESTful API design" width="500"/>
  <img src="https://github.com/user-attachments/assets/9d18575c-da2c-467f-85eb-2b0d8234e389" alt="RESTful API design" width="500"/>
  <img src="https://github.com/user-attachments/assets/8bc9ac40-81ea-4ca1-b78d-0dbd76757945" alt="RESTful API design" width="500"/>
</details>

<br>

## 15. Figma Planning
<details>
<summary>Figma Planning</summary>
  <img src="https://github.com/user-attachments/assets/4ca1e6c3-73ee-44ff-b44e-9db08804f9fb" alt="피그마 기획" width="900"/>
</details>

<br>

## 16. Questionnaire
<details>
<summary>Questionnaire</summary>
  <img src="https://github.com/user-attachments/assets/f06eb560-038b-45ad-9435-af749936deb0" alt="혼밥장소" width="500"/>
  <img src="https://github.com/user-attachments/assets/1414ca67-8ed1-4288-bbc4-99981e96e1cc" alt="혼밥식당참고사이트" width="500"/>
  <img src="https://github.com/user-attachments/assets/66589c2a-e22d-4d79-b707-d2f75ee03352" alt="혼밥만족도" width="500"/>
  <img src="https://github.com/user-attachments/assets/1b364d50-d371-4f83-b1dd-f19f40a310af" alt="혼밥개선점" width="500"/>
</details>
