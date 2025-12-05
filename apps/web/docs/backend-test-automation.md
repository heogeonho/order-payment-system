(백엔드 테스트 자동화 명세)

⸻

Backend Test Automation Spec
	1.	목적

이 문서는 LLM이 백엔드(도메인/서비스/컨트롤러/Repository) 테스트 코드를 자동 생성할 때 따라야 할 규칙을 정의한다.
특히, FEAT-ORDER-PAY-001 (홈쇼핑 단일 상품 주문 및 결제 승인) 기능에 대해 기능 명세(feature-spec.md)의 Rule/TC ID와 정합성을 유지하는 것을 목표로 한다.

⸻
	2.	테스트 범위 분류

2.1 Unit Test
	•	대상
	•	서비스 레이어(OrderService, PaymentService 등)
	•	도메인 객체(Order, Payment, Product)의 비즈니스 메서드
	•	공통 유틸(금액 계산, 상태 전이 등)
	•	특징
	•	외부 시스템(DB, MQ, PG, 외부 API 등)은 모두 Mock 처리
	•	예: ProductRepository, OrderRepository, PgClient 등

2.2 Integration Test
	•	대상
	•	Spring Context 전체 또는 주요 Bean 조합
	•	특징
	•	Repository는 H2 또는 Testcontainers 기반 실제 DB 연동
	•	API 테스트는 @SpringBootTest + MockMvc/WebTestClient 사용
	•	트랜잭션/영속성/엔티티 매핑 검증

2.3 Slice Test
	•	대상
	•	@WebMvcTest, @DataJpaTest 등 특정 계층에 집중
	•	특징
	•	WebMvcTest: Controller + ExceptionHandler 레벨 검증
	•	DataJpaTest: Repository 쿼리/엔티티 매핑 검증

⸻
	3.	테스트 설계 규칙

3.1 네이밍 규칙
	•	테스트 클래스 파일명: ClassNameTest
	•	예: OrderServiceTest, PaymentServiceTest, OrderControllerTest
	•	테스트 메서드명: 메서드명_상황_기대결과
	•	예: createOrder_유효한_요청이면_주문과_히스토리가_생성된다
	•	TC ID 포함 예시:
	•	@DisplayName("BE_UT_01 - 정상 주문 생성 시 Order와 History 생성")
	•	메서드명: createOrder_BE_UT_01_정상_주문이면_Order와_History가_생성된다

3.2 Given — When — Then 구조 필수
	•	given:
	•	입력 데이터/Command 객체 구성
	•	Mock Repository, Mock PG Client 행동 정의
	•	when:
	•	실제 테스트 대상 메서드 호출
	•	then:
	•	Assertion (상태/리턴값/예외/호출 여부 검증)

3.3 Assertion 규칙
	•	AssertJ 사용 권장
	•	assertThat(), assertThatThrownBy() 사용
	•	예외 검증:
	•	assertThatThrownBy(() -> service.approvePayment(cmd))
.isInstanceOf(AmountMismatchException.class);
	•	필수 검증 항목:
	•	상태 전이 (Order.status, Payment.status)
	•	금액/수량 계산값
	•	히스토리 엔티티 생성 여부

3.4 Mocking 규칙
	•	Mockito 또는 MockK 사용 (프로젝트 표준에 맞춤)
	•	외부 시스템 호출은 모두 Mock 처리:
	•	ProductRepository, OrderRepository, PaymentRepository
	•	PgClient (단일 PG)
	•	명세에 없는 행동은 임의로 추가하지 않는다.
	•	불명확한 메서드명/동작은 TODO: 실제 메서드 확인 필요 주석 추가

3.5 테스트 품질 기준
	1.	하나의 테스트는 하나의 규칙 또는 하나의 시나리오만 검증한다.
	2.	내부 구현 세부사항(로깅, private 메서드 등)에 직접 의존하지 않는다.
	3.	성공/실패/경계값 케이스를 모두 정의한다.
	4.	각 테스트는 feature-spec.md의 Rule/TC ID와 명시적으로 매핑된다.
	5.	히스토리/이벤트와 같이 후속 기능의 기반이 되는 데이터는 반드시 검증한다.

⸻
	4.	LLM 자동 생성 요구사항
	5.	feature-spec.md의 Rule ID, TC ID를 반드시 반영한다.
	•	예: Rule-03(금액 계산 규칙) → BE_UT_04(결제 금액 불일치 테스트)
	6.	명세에 존재하지 않는 필드/메서드/엔티티를 임의로 생성하지 않는다.
	7.	DTO/엔티티 구조는 명세에 정의된 필드만 사용한다.
	8.	Mock 동작은 명세에 정의된 시나리오만 활용한다.
	•	예: PG 성공/실패, 재고 부족, 주문 상태 오류 등
	9.	각 테스트 메서드는 Given/When/Then 구조를 주석 또는 코드 블록으로 명확히 나눈다.
	10.	테스트마다 “검증 목적”을 @DisplayName 또는 주석으로 한 줄로 기술한다.
	11.	예외/에러코드 테스트 시:
	•	HTTP status뿐 아니라 code 필드까지 검증하는 API 테스트 코드를 생성한다.

⸻