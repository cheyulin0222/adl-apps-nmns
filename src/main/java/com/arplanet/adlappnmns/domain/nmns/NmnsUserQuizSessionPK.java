package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NmnsUserQuizSessionPK {

    private Long uid;

    @Column(name="session_id")
    private String sessionId;

    @Column(name="quiz_id")
    private String quizId;

    @Column(name="question_id")
    private String questionId;

    @Column(name="option_id")
    private String optionId;
}
