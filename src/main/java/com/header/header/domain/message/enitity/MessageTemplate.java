package com.header.header.domain.message.enitity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_message_template")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class MessageTemplate {
    /* comment. MessageTemplate은 Service단에서 Create될 때 templateType이 결정된다.*/
    /* comment.  따라서 User은 Type을 바꿀 수 없다. 이를 Service에서 핸들링 해줘야함! */
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer templeteCode;
    private Integer shopCode;
    private String templateContent;
    private String templateType;/* todo. ✏️ String type에서 Enum으로 변경하기 */

    public void modifyMessageTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }
}
