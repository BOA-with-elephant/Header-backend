package com.header.header.domain.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopMessageHistoryService {

    /* Read */
    /* todo. batch에 해당하는 history에서 수신자 목록 조회 (아마 History로 빼는게 나을 듯 )*/

    /* todo. 수신자에게 발송한 상세 메세지 내용 조회 단일 필드 조회이기 때문에 Projection 사용. */

    /* Creat */
    /* todo. 배치 Insert 이후 수신자 별로 history가 생성 됨. 메세지 발송과 비동기적으로 일어나야하는지 확인해야함! 예)번호가 없는 번호일 경우? 수신 차단된 경우? */
    /* CREATE 할 때 STATUS = PENDING 으로 초기화 */

    /* Update */
    /* todo. 예약 메세지일 경우에는 reserved 상태에서 send로 변경됨. pending이 필요할 수도 있음! */

    /* todo. API 호출이 처리 된 후에 Update 함( STATUS나 ErrorMessage ) */

    /* Delete : ❌*/
}
