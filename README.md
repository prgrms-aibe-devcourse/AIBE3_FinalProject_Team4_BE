# 🚀 TexTok  
## 짧은 글과 깊은 글이 자연스럽게 연결되는 AI 기반 텍스트 크리에이터 플랫폼

“짧은 글부터 깊이 있는 글까지  
한 공간에서 창작하고 확장하며 소통하는 새로운 콘텐츠 경험”

---

## 📝 프로젝트 소개

TexTok은 **짧은 숏로그와 깊이 있는 블로그 글이 자연스럽게 오가는 ‘연결 중심 플랫폼’**입니다.

사용자는 짧은 생각을 **숏로그로 빠르게 기록**하고,  
그 내용을 더 깊이 탐구하고 싶을 때 **블로그 글로 확장**할 수 있습니다.  

반대로, 긴 블로그 글은 핵심만 담아 **숏로그로 요약하여 가볍게 공유**할 수도 있습니다.  

즉, TexTok은:

- **짧은 생각을 깊게 키울 수 있고**,  
- **깊은 글을 다시 짧게 압축해 확산시킬 수 있는**,  

**양방향 텍스트 경험**을 제공하는 플랫폼입니다.

---

# 💡 이런 서비스를 목표로 했어요!

TexTok은 **“짧은 글과 깊은 글의 자연스러운 연결”**이라는 문제의식에서 출발했습니다.  
숏폼 시대의 빠른 소비와 블로그의 깊이 있는 탐구가 **하나의 흐름으로 이어지는 경험**을 만들고자 합니다.

사용자가 짧은 생각을 빠르게 기록하고,  
그 생각을 다시 깊은 글로 확장하거나  
반대로 긴 글을 짧게 요약해 확산할 수 있는 **양방향 텍스트 플랫폼**을 목표로 합니다.

또한 AI를 활용해 **작성·탐색·추천·요약** 전반의 편의성을 극대화하여  
창작자가 더 빠르고 효율적으로 콘텐츠를 만들고 공유할 수 있는 환경을 제공합니다.

---

# 🎯 서비스 대상

- **콘텐츠 크리에이터**: 다양한 형식(숏로그·블로그)으로 창작물을 공유하고 싶은 사용자  
- **독서/글쓰기 애호가**: 간결한 인사이트와 깊이 있는 글쓰기를 모두 즐기는 사용자  
- **소셜 미디어 사용자**: 새로운 형태의 텍스트 기반 소통 경험을 원하는 사용자  
- **오디오 콘텐츠 소비자**: 글을 들으며 소비하고 싶은 사용자  

---

## 🏗️ 시스템 아키텍처  
<img width="1171" height="645" alt="image" src="https://github.com/user-attachments/assets/a87a51d8-c578-406b-8e85-eec33bf14b18" />


---

## 🗄️ ERD  
| <img width="3390" height="1722" alt="Copy of NEXT 5 - 3차 프로젝트" src="https://github.com/user-attachments/assets/e45bde4a-7b19-44e8-939c-d4d5132de6ac" />|
|:---------:|
| https://www.erdcloud.com/d/xEpyYdG29HuG32Z7j |

---

## 🛠️ 기술 스택

🎨 Frontend
| 기술                 | 설명                                    |
| ------------------ | ------------------------------------- |
| **Next.js**        | React 기반 프레임워크(React + TypeScript 기반) |
| **React**          | UI 라이브러리                              |
| **TypeScript**     | 정적 타입 언어                              |
| **Tailwind CSS**   | 유틸리티 클래스 기반 스타일링                      |
| **Headless UI**    | 접근성 기반 Headless 컴포넌트                  |
| **Lucide React**   | 아이콘 라이브러리                             |
| **Zustand**        | React 상태 관리                           |
| **react-markdown** | Markdown 렌더링 및 에디터                    |
| **ESLint**         | 코드 린팅/품질 관리                           |
| **Prettier**       | 코드 포매터                                |

⚙️ Backend
| 기술                      | 설명                   |
| ----------------------- | -------------------- |
| **Java**                | 백엔드 런타임/언어           |
| **Spring Boot**         | 독립 실행형 서비스 및 빠른 설정   |
| **Gradle (Kotlin DSL)** | 빌드/의존성 관리            |
| **Spring Web**          | REST API 개발          |
| **Spring WebFlux**      | 외부 API 스트리밍 및 리액티브 연동        |
| **Spring Data JPA**     | ORM 기반 데이터 매핑        |
| **Hibernate**           | JPA 구현체              |
| **QueryDSL**            | 타입 세이프 동적 쿼리         |
| **Spring Security**     | 인증/인가 처리             |
| **JWT (jjwt)**          | 토큰 기반 인증             |
| **OAuth2 Client**       | 소셜 로그인               |
| **Spring Mail**         | 이메일 발송               |
| **SMTP 인증**             | 이메일 인증 플로우           |
| **Spring Validation**   | 요청 및 도메인 검증          |
| **Spring WebSocket**    | 실시간 양방향 통신           |
| **SSE**                 | 서버→클라이언트 실시간 단방향 이벤트 |
| **Spring Actuator**     | 헬스체크/메트릭 제공          |
| **Spring AOP**          | 로깅/트랜잭션 등 횡단 관심사 처리  |
| **Lombok**              | 보일러플레이트 코드 제거        |
| **Swagger UI**          | API 문서/테스트 UI        |
| **TSID Creator**        | 분산환경 고유 ID 생성        |

💾 Data & Storage
| 기술         | 설명          |
| ---------- | ----------- |
| **MySQL**  | 관계형 데이터베이스  |
| **Redis**  | 캐싱/세션 저장소   |
| **AWS S3** | 이미지/파일 스토리지 |

🤖 AI & External Services
| 기술                     | 설명          |
| ---------------------- | ----------- |
| **Spring AI (OpenAI)** | AI 모델 연동    |
| **Google TTS**         | 텍스트 → 음성 변환 |
| **Unsplash API**       | 이미지 소스      |
| **Pixabay API**        | 이미지 소스      |


---

## 📌 팀원 구성

|<img src="https://avatars.githubusercontent.com/u/217847761?v=4" width="125" />|<img src="https://avatars.githubusercontent.com/u/95841885?v=4" width="125" />|<img src="https://avatars.githubusercontent.com/u/103419261?v=4" width="125" />|<img src="https://avatars.githubusercontent.com/u/81467332?v=4" width="125" />|<img src="https://avatars.githubusercontent.com/u/132271194?v=4" width="125" />|
|:---------:|:---------:|:---------:|:---------:|:---------:|
|[박세웅 (팀장)](https://github.com/Park-seungg)|[주권영](https://github.com/jookyworld)|[이연서](https://github.com/weare0gimbab)|[이지연](https://github.com/get783)|[이해민](https://github.com/haemin4738)|
|FE, BE|FE, BE|FE, BE|FE, BE|FE, BE|

