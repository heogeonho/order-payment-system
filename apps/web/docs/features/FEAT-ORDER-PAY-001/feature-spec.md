
(기능 명세 템플릿: 기능 정의 · 규칙 · API · UI 흐름까지 포함)

⸻

[Feature Spec] 홈쇼핑 단일 상품 주문 및 결제 승인

0. 메타 정보
- Feature ID: FEAT-ORDER-PAY-001
- Version: v1.0.0
- 작성일: 2025-12-04
- 담당자: TBD
- 관련 화면 ID:
  - SCR-HP-001: 상품 목록 화면
  - SCR-HP-002: 상품 상세/주문 화면
  - SCR-HP-003: PG 결제 화면
  - SCR-HP-004: 주문 완료 화면
- 관련 API:
  - API-PRD-001: GET /api/products
  - API-ORD-001: POST /api/orders
  - API-PAY-001: POST /api/payments/approve

⸻

1. 기능 개요

1.1 기능 목적

- 홈쇼핑/이커머스 환경에서 고객이 **단일 상품을 선택 → 수량 지정 → 주문 생성 → PG 결제 승인 → 주문 확정**까지의 전체 플로우를 제공한다.
- 초기 버전에서는 **단일 상품 기반 즉시구매 흐름**만 지원하고, 장바구니/다중 상품/옵션 선택 등은 추후 확장을 전제로 한다.
- 추후 매출/전환율/결제 실패율 등의 통계를 계산할 수 있도록 **주문/결제 이벤트 히스토리를 저장**한다.

1.2 정상 흐름

1. 고객이 상품 목록 화면(SCR-HP-001)에서 원하는 상품을 선택한다.
2. 상품 상세/주문 화면(SCR-HP-002)에서 수량을 입력하고 “주문하기” 버튼을 클릭한다.
3. 프론트엔드는 `POST /api/orders`(API-ORD-001)를 호출하여 주문을 생성한다.
4. 백엔드는 상품/수량을 검증하고 **Order(PENDING_PAYMENT)**를 생성한 뒤 `ORDER_CREATED` 히스토리를 기록하고 응답한다.
5. 프론트엔드는 응답으로 받은 `orderId`, `totalAmount`를 기반으로 PG 결제 화면(SCR-HP-003)을 호출한다.
6. 고객이 PG 화면에서 결제를 성공적으로 완료하면 PG로부터 `paymentKey`, 결제 금액(`amount`)을 프론트가 수신한다.
7. 프론트엔드는 `POST /api/payments/approve`(API-PAY-001)를 호출하여 결제 승인을 요청한다.
8. 백엔드는 주문/금액/상태를 검증하고 단일 PG에 승인 요청을 수행한다.
9. 승인 성공 시:
   - Payment = APPROVED
   - Order = PAID
   - `PAYMENT_APPROVED` 히스토리를 기록한다.
10. 프론트엔드는 주문 완료 화면(SCR-HP-004)으로 이동시키고 주문완료 정보를 표시한다.

1.3 예외 흐름

- EX-01: 상품이 존재하지 않거나 판매 불가 상태
  - 주문 요청 시 `PRODUCT_NOT_FOUND` 또는 `PRODUCT_NOT_AVAILABLE` 에러 응답.
  - 프론트는 에러 메시지를 표시하고 주문을 중단한다.
- EX-02: 재고 부족 또는 수량 오류
  - 주문 요청 시 `OUT_OF_STOCK` 또는 `QUANTITY_INVALID` 응답.
- EX-03: 결제 금액 불일치
  - 결제 승인 요청 시 `AMOUNT_MISMATCH` 응답, PG 승인 호출은 수행하지 않는다.
- EX-04: 주문 상태가 결제 가능 상태가 아님
  - 이미 PAID 또는 PAYMENT_FAILED인 주문에 대해 결제 승인 호출 시 `ORDER_NOT_PAYABLE`.
- EX-05: PG 승인 실패
  - PG 측에서 실패하는 경우 `PG_APPROVAL_FAILED` 응답.
  - Order = PAYMENT_FAILED, Payment = DECLINED, 히스토리 `PAYMENT_FAILED` 기록.
- EX-06: 이미 승인된 주문에 대한 중복 결제 시도
  - 동일 orderId에 대해 결제 승인 재요청 시 `PAYMENT_ALREADY_APPROVED` (멱등 처리 정책에 따라 응답).

⸻

2. 도메인 규칙

2.1 엔티티 / VO 정의

(요약: 실제 코드/DDL은 별도 문서에서 정의하되, LLM이 테스트 생성 시 참고할 필수 필드와 규칙만 명시)

[Entity] Product
- productId: Long (PK)
- name: String (상품명, 1~100자)
- basePrice: Long (정가, 0 < basePrice)
- discountPrice: Long (할인가, 0 < discountPrice ≤ basePrice)
- availableStock: Integer (재고 수량, 0 이상)
- available: Boolean (판매 가능 여부)
- createdAt: DateTime

[Entity] Order
- orderId: String (예: ORD-YYYYMMDD-NNNN)
- userId: Long (주문자 식별자)
- productId: Long
- quantity: Integer (1 이상)
- totalAmount: Long (discountPrice * quantity)
- status: Enum(OrderStatus)
  - PENDING_PAYMENT
  - PAID
  - PAYMENT_FAILED
- createdAt: DateTime
- updatedAt: DateTime

[Entity] Payment
- id: Long (PK)
- orderId: String
- paymentKey: String (PG에서 내려주는 결제 키)
- amount: Long
- status: Enum(PaymentStatus)
  - REQUESTED
  - APPROVED
  - DECLINED
- pgResultCode: String (PG 응답 코드, nullable)
- pgResultMessage: String (PG 응답 메시지, nullable)
- createdAt: DateTime

[Entity] OrderHistory
- id: Long (PK)
- orderId: String
- eventType: Enum(OrderEventType)
  - ORDER_CREATED
  - PAYMENT_APPROVED
  - PAYMENT_FAILED
- payloadJson: Text (관련 필드 JSON, 예: productId, amount, pgResultCode 등)
- createdAt: DateTime

2.2 비즈니스 규칙 목록

- Rule-01 (단일 상품 주문 제한)
  - 하나의 Order는 하나의 Product에 대해서만 생성된다.
  - 동일 주문에서 productId는 단일 값이어야 하며, 다중 상품/옵션은 허용하지 않는다.

- Rule-02 (수량 및 재고 규칙)
  - quantity는 1 이상이어야 한다.
  - quantity > availableStock인 경우 주문 생성은 거부된다 (`OUT_OF_STOCK`).
  - 수량은 정수만 허용한다.

- Rule-03 (금액 계산 규칙)
  - totalAmount = Product.discountPrice × quantity
  - 결제 승인 요청 시 전달되는 amount값은 Order.totalAmount와 정확히 일치해야 한다.
  - 불일치 시 `AMOUNT_MISMATCH` 에러로 처리하고 PG 승인 호출을 수행하지 않는다.

- Rule-04 (상태 전이 규칙)
  - Order 초기 상태는 `PENDING_PAYMENT`이다.
  - 결제 승인 성공 시:
    - Order: PENDING_PAYMENT → PAID
    - Payment: REQUESTED → APPROVED
  - 결제 승인 실패 시:
    - Order: PENDING_PAYMENT → PAYMENT_FAILED
    - Payment: REQUESTED → DECLINED

- Rule-05 (히스토리 기록 규칙)
  - 주문 생성 성공 시 항상 `ORDER_CREATED` 이벤트를 기록해야 한다.
  - 결제 승인 성공 시 항상 `PAYMENT_APPROVED` 이벤트를 기록해야 한다.
  - 결제 승인 실패 시 항상 `PAYMENT_FAILED` 이벤트를 기록해야 한다.
  - 히스토리 payloadJson은 최소한 다음 정보를 포함해야 한다:
    - ORDER_CREATED: productId, quantity, totalAmount
    - PAYMENT_APPROVED / PAYMENT_FAILED: paymentKey, amount, pgResultCode

⸻

3. API 스펙

3.1 Request / Response — 주문 생성

- API ID: API-ORD-001
- Method: POST
- Path: `/api/orders`

[Request Body]

```json
{
  "userId": 1,
  "productId": 101,
  "quantity": 2
}

	•	필드 설명
	•	userId: 로그인 사용자 ID
	•	productId: 주문할 상품 ID
	•	quantity: 주문 수량 (Rule-02 적용)

[Response Body — 성공 (201 Created)]

{
  "orderId": "ORD-20251204-0001",
  "userId": 1,
  "productId": 101,
  "quantity": 2,
  "totalAmount": 258000,
  "status": "PENDING_PAYMENT"
}

3.2 Request / Response — 결제 승인
	•	API ID: API-PAY-001
	•	Method: POST
	•	Path: /api/payments/approve

[Request Body]

{
  "orderId": "ORD-20251204-0001",
  "paymentKey": "pay_abc123",
  "amount": 258000
}

	•	필드 설명
	•	orderId: 결제 승인 대상 주문 ID
	•	paymentKey: PG에서 전달된 결제 키
	•	amount: PG 결제 금액 (Rule-03 적용)

[Response Body — 성공 (200 OK)]

{
  "orderId": "ORD-20251204-0001",
  "paymentId": 10,
  "paymentKey": "pay_abc123",
  "amount": 258000,
  "paymentStatus": "APPROVED",
  "orderStatus": "PAID",
  "approvedAt": "2025-12-04T12:34:56"
}

3.3 에러 응답

모든 에러 응답은 다음 공통 구조를 따른다.

{
  "code": "에러코드",
  "message": "사용자/로그용 메시지",
  "detail": "선택: 추가 정보 또는 PG 코드"
}

대표 케이스:
	•	Case-API-01 (주문 생성 관련)
	•	PRODUCT_NOT_FOUND
	•	PRODUCT_NOT_AVAILABLE
	•	OUT_OF_STOCK
	•	QUANTITY_INVALID
	•	Case-API-02 (결제 승인 관련)
	•	ORDER_NOT_FOUND
	•	ORDER_NOT_PAYABLE
	•	AMOUNT_MISMATCH
	•	PG_APPROVAL_FAILED
	•	PAYMENT_ALREADY_APPROVED

⸻
	4.	UI 명세 (프론트 기준)

4.1 UI 요소 목록

(주요 data-testid 기준, 테스트 자동화를 위한 식별자 명시)

Element ID (data-testid)	설명	타입	표시 텍스트 예시
product-card	상품 카드	Container	상품명, 가격, 버튼 포함
product-name	상품명 텍스트	Text	“무선 청소기 프리미엄”
product-price	할인가 텍스트	Text	“129,000원”
product-select-button	상품 상세로 이동 버튼	Button	“자세히 보기”
order-quantity-input	주문 수량 입력 필드	Input	기본값 1
order-submit-button	주문 생성 요청 버튼	Button	“주문하기”
payment-loading-indicator	결제 진행 중 표시	Indicator	“결제 처리 중입니다.”
payment-error-message	결제 오류 메시지 영역	Text	
payment-retry-button	결제 재시도 버튼	Button	“다시 결제하기”
order-complete-message	주문 완료 메시지	Text	“주문이 정상적으로 완료되었습니다.”
order-complete-order-id	주문번호 표시 영역	Text	“주문번호: ORD-20251204-0001”

4.2 화면 내 이벤트 흐름
	•	상품 목록 화면(SCR-HP-001)
	•	사용자: product-select-button 클릭
	•	→ 상품 상세 화면(SCR-HP-002)로 이동
	•	상품 상세/주문 화면(SCR-HP-002)
	•	사용자: order-quantity-input에 수량 입력
	•	사용자: order-submit-button 클릭
	•	프론트:
	•	수량 유효성 검사 (Rule-02)
	•	유효하면 POST /api/orders 호출
	•	응답 성공:
	•	orderId, totalAmount 저장
	•	PG 결제 화면(SCR-HP-003) 오픈
	•	응답 실패:
	•	payment-error-message에 에러 메시지 표시
	•	PG 결제 화면(SCR-HP-003)
	•	PG UI에서 결제 성공 시:
	•	프론트가 { paymentKey, amount } 콜백 수신
	•	POST /api/payments/approve 호출
	•	PG UI에서 결제 실패 시:
	•	payment-error-message 표시
	•	payment-retry-button 노출
	•	주문 완료 화면(SCR-HP-004)
	•	결제 승인 성공 응답 수신 후:
	•	order-complete-message, order-complete-order-id 표시

4.3 주요 시나리오
	•	FE-SC-01: 정상 주문 및 결제 승인 시나리오
	•	단일 상품/수량 1 이상 → 주문 생성 → PG 결제 성공 → 결제 승인 → 주문 완료 화면 도달.
	•	FE-SC-02: 재고 부족으로 인한 주문 실패 시나리오
	•	quantity > availableStock → 주문 생성 API에서 OUT_OF_STOCK 응답 → 에러 메시지 표시.
	•	FE-SC-03: 결제 금액 불일치로 인한 승인 실패 시나리오
	•	PG에서 콜백된 amount ≠ Order.totalAmount → AMOUNT_MISMATCH → 결제 오류 메시지 및 재시도 가능.

⸻
	5.	테스트 케이스 매트릭스

5.1 Backend Unit Tests

TC ID	설명	조건	기대 결과	관련 Rule
BE_UT_01	정상 주문 생성 시 Order와 History 생성	유효한 productId, quantity, 재고 충분	Order=PENDING_PAYMENT, totalAmount 계산, ORDER_CREATED 기록	Rule-01, 02, 03, 05
BE_UT_02	재고 부족 시 주문 생성 실패	quantity > availableStock	OUT_OF_STOCK 예외/에러 반환	Rule-02
BE_UT_03	결제 승인 성공 시 상태 전이 검증	PENDING_PAYMENT 주문, amount=totalAmount, PG 성공	Payment=APPROVED, Order=PAID, PAYMENT_APPROVED 기록	Rule-03, 04, 05
BE_UT_04	결제 금액 불일치 시 승인 거부	amount ≠ totalAmount	AMOUNT_MISMATCH, PG 호출 없음, Payment 미생성	Rule-03
BE_UT_05	결제 실패 시 상태 전이 및 히스토리 검증	PG 실패 응답	Payment=DECLINED, Order=PAYMENT_FAILED, PAYMENT_FAILED 기록	Rule-04, 05

5.2 Backend API Tests

TC ID	설명	기대 결과
BE_API_01	주문 생성 API 정상 호출	201, JSON 내 orderId, totalAmount, status 반환
BE_API_02	주문 생성 시 PRODUCT_NOT_FOUND 발생	400, code=PRODUCT_NOT_FOUND
BE_API_03	결제 승인 API 정상 호출	200, paymentStatus=APPROVED, orderStatus=PAID
BE_API_04	결제 승인 시 ORDER_NOT_PAYABLE 발생	409, code=ORDER_NOT_PAYABLE
BE_API_05	결제 승인 시 AMOUNT_MISMATCH 발생	400, code=AMOUNT_MISMATCH

5.3 Frontend Component Tests

TC ID	설명	기대 결과
FE_UT_01	수량 입력 후 주문하기 클릭 시 API 호출	order-quantity-input 값 반영, 주문 API 호출 여부 검증
FE_UT_02	주문 생성 성공 시 PG 결제 플로우로 이동	주문 응답 수신 후 PG 호출 핸들러가 트리거되는지 검증
FE_UT_03	결제 승인 성공 시 주문 완료 화면 렌더링	payment 승인 응답 후 order-complete-message 표시
FE_UT_04	결제 실패 시 오류 메시지 및 재시도 버튼 노출	payment-error-message 표시, payment-retry-button 노출
FE_UT_05	재고 부족/수량 오류 시 에러 메시지 표시	OUT_OF_STOCK, QUANTITY_INVALID 응답 시 UI 에러 메시지 검증

⸻
	6.	LLM 참고용 요약

	•	이 기능은 단일 상품 주문 + 단일 PG 결제 승인에 한정된다. 장바구니 또는 다중 상품은 고려하지 않는다.
	•	핵심 비즈니스 규칙(Rule-01~05)과 상태 전이(주문/결제/히스토리)는 테스트 설계와 코드 생성의 기준이 된다.
	•	모든 API 에러 응답은 { code, message, detail? } 구조를 따르며, 테스트에서 code 값 검증이 필수다.
	•	프론트 테스트는 data-testid 기준으로 DOM 요소에 접근해야 하며, Feature Spec의 FE-SC 시나리오를 기반으로 케이스를 도출해야 한다.
	•	히스토리(OrderHistory)는 통계/추적의 핵심 데이터이므로, 백엔드 테스트에서는 해당 이벤트가 생성되는지 반드시 검증해야 한다.

⸻