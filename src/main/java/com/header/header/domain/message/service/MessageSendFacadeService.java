package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageDTO;
import com.header.header.domain.message.dto.MessageRequest;
import com.header.header.domain.message.dto.MessageResponse;
import com.header.header.domain.visitors.service.VisitorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageSendFacadeService {

    private final MessageAsyncService messageAsyncService;
    private final VisitorsService visitorsService;

    /**
     * 즉시 발송 메세지 FacadeService 메서드
     * @param request Client 요청 DTO
     * @return MessageResponse
     */
    public MessageResponse sendImmediateMessage(MessageRequest request){

        /* 템플릿이든 직접 작성이든 messageContent로 포맷되서 들어온다. : 자동 메세지가 아닐  경우 */
        List<Integer> userCodeList = new ArrayList<>();

        /*1. UserCode를 ClientCode로 바꾼다.*/
        for(Integer clientCode : request.getClientCodes()){
            userCodeList.add(visitorsService.getUserCodeByClientCode(clientCode));
        }

        /*2. Service를 위한 MessageDTO를 만든다. */
        MessageDTO messageDTO = MessageDTO.builder()
                .to(userCodeList)
                .from(request.getShopCode())
                .sendType(userCodeList.size() > 1 ? "GROUP" :"INDIVIDUAL" ) // 2명 이상이면 그룹발송
                .subject(request.getSubject())
                .text(request.getMessageContent())
                .build();

        /*3. MessageAsyncService에서 메서드 호출*/
        return messageAsyncService.sendMessageAsync(messageDTO);
    }
}
