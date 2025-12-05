(프론트엔드 테스트 자동화 명세)

⸻

Frontend Test Automation Spec (Vue3 / Vitest)
	1.	목적

Vue 3 기반 컴포넌트 및 Pinia/Vuex 스토어에 대해 테스트를 자동 생성할 때,
LLM이 따라야 할 규칙을 정의한다.
특히 FEAT-ORDER-PAY-001 기능의 FE-SC 시나리오와 UI 요소(data-testid)를 기준으로 테스트를 구성한다.

⸻
	2.	테스트 범위

2.1 컴포넌트 테스트
	•	대상
	•	상품 목록 컴포넌트 (예: ProductListView.vue)
	•	상품 상세/주문 컴포넌트 (예: ProductOrderView.vue)
	•	주문 완료 컴포넌트 (예: OrderCompleteView.vue)
	•	검증 항목
	•	초기 렌더링
	•	props 처리
	•	emit 이벤트 발생 여부
	•	사용자 입력 처리(수량, 버튼 클릭 등)
	•	API 호출 후 UI 상태 변화
	•	에러 응답 시 메시지 및 재시도 버튼 노출

2.2 스토어 테스트 (Pinia/Vuex)
	•	대상
	•	주문/결제 관련 store 모듈 (예: useOrderStore, usePaymentStore)
	•	검증 항목
	•	state 초기화
	•	actions/mutations 동작
	•	비동기 API 호출 로직 (axios/fetch mock)
	•	실패 케이스/에러 상태 처리

⸻
	3.	테스트 설계 규칙

3.1 파일/테스트명
	•	파일명:
	•	ComponentName.spec.ts (예: ProductOrderView.spec.ts)
	•	storeName.spec.ts (예: orderStore.spec.ts)
	•	테스트명:
	•	"상황 - 기대 결과" 형식의 한글 설명
	•	TC ID를 명시적으로 포함
	•	예: it("FE_UT_01 - 정상 입력 시 주문 생성 API가 호출된다", () => { ... })

3.2 Given — When — Then 구조
	•	given:
	•	컴포넌트 마운트, 스토어 초기화, 기본 props 설정
	•	when:
	•	사용자 입력/클릭/이벤트 발생
	•	then:
	•	DOM 변화, emit 발생, store state 변화 등을 검증

⸻
	4.	DOM 접근 규칙

	•	data-testid 기반 접근만 허용한다.
	•	CSS 클래스명, 텍스트 내용, DOM 구조 깊이에 의존한 선택자는 사용 금지.
	•	예시:
	•	getByTestId("order-quantity-input")
	•	getByTestId("order-submit-button")
	•	getByTestId("payment-error-message")

⸻
	5.	API Mock 규칙

	•	axios 또는 별도 api 모듈은 전부 Mock 처리한다.
	•	성공/실패 응답은 feature-spec.md에 정의된 시나리오/에러코드에 한해서만 사용한다.
	•	예: PRODUCT_NOT_FOUND, OUT_OF_STOCK, AMOUNT_MISMATCH 등
	•	명세에 없는 에러 타입/코드는 새로 만들지 않는다.
	•	실제 문구/UI 텍스트가 명확하지 않은 경우:
	•	"TODO: 실제 에러 메시지 문구 확인 필요" 주석을 남긴다.
	•	PG 결제 성공/실패 콜백 또한 mock 함수로 주입하고,
onPaymentSuccess, onPaymentFail 핸들러를 중심으로 검증한다.

⸻
	6.	LLM 자동 생성 요구사항
	7.	각 테스트는 Feature Spec의 FE-SC 시나리오(FE-SC-01~03 등)를 기반으로 도출한다.
	8.	DOM 접근 시 data-testid만 사용하며, Element ID는 feature-spec.md 4.1을 따른다.
	9.	DOM 구조나 스타일링(CSS) 세부 구현에 의존하지 않는다.
	10.	각 테스트는 Given/When/Then 주석을 포함하여 구조를 명확히 표현한다.
	11.	실제 텍스트(문구)가 명확하지 않을 때는 추측하지 않고 TODO 주석으로 대체한다.
	12.	프론트 테스트는 비즈니스 규칙이 UI에 어떻게 반영되는지 검증해야 한다.
	•	예: 재고 부족 시 주문 버튼 동작, 금액 불일치 시 에러 메시지 표시 등.
	13.	최소한 다음 유형의 테스트를 자동 생성해야 한다.
	•	정상 플로우 (FE-SC-01)
	•	재고 부족 등 서버 에러 플로우 (FE-SC-02)
	•	결제 금액 불일치 등 결제 에러 플로우 (FE-SC-03)

⸻