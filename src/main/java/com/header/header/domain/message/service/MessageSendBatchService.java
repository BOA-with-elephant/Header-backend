package com.header.header.domain.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSendBatchService {

    /* CREATE */
    /* todo. 사용자가 메세지 요청시 배치가 생성된다, [shopCode, tempateCode, sendType, subject, totalCount ]*/
    /* 배치는 처음에는 PENDING으로 저장된다 이후 발송 완료 후 상태 및 통계 업데이트*/

    /* READ */
    /* todo. 간단 조회 배치 목록 Interface Projection 사용 */ 

    /* todo. 간단 조회 -> 상세조회 (Created At) 제외 이므로 Entity 조회*/

    /* UPDATE */
    /* todo. 메세지 발송 완료 후 상태 및 통계 업데이트 */

    /* DELETE ❌*/


}
