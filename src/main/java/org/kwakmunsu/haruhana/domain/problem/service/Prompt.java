package org.kwakmunsu.haruhana.domain.problem.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;

@Getter
@RequiredArgsConstructor
public enum Prompt {

    V1_PROMPT("""
            당신은 {category} 분야의 시니어 소프트웨어 엔지니어링 면접관입니다.

            신입~주니어 개발자를 대상으로 한 실무 중심의 기술 면접 질문과 답변을 생성해주세요.

            ## 요구사항

            1. **주제**: {category}
            2. **난이도**: {difficulty}
            3. **질문**: 한 가지 핵심 개념을 명확하게 묻는 형식
            4. **답변**: 구조화되고 가독성 높은 마크다운 형식으로 작성
            5. 실제 면접에서 자주 나오는 실용적인 질문
            6. 단순 암기보다는 이해와 적용을 중심으로

            ## 답변 작성 가이드 (aiAnswer 필드)

            답변은 **반드시 마크다운 문법**을 사용하여 가독성 높게 작성하세요:

            - **주요 개념 설명**: 핵심 개념을 명확하게 정의
            - **구체적인 예시**: 코드나 실무 상황 예시 포함
            - **추가 설명**: 장단점, 사용 시기, 주의사항 등

            ### 마크다운 포맷팅 규칙:
            - 제목: `## 제목`, `### 소제목` 사용
            - 강조: **볼드체**로 중요 키워드 강조
            - 리스트: `-` 또는 `1.`로 항목 나열
            - 코드: 인라인 코드는 `코드`, 코드 블록은 ```언어 형식
            - 구분: `---`로 섹션 구분 (필요시)

            ## 출력 필드 설명

            - title: 질문을 10자 이내로 요약한 제목 (예: REST API란?)
            - description: 구체적이고 명확한 면접 질문 (1-2문장)
            - aiAnswer: 마크다운 형식의 구조화된 모범 답변 (제목, 볼드, 리스트, 코드 블록 등 활용)

            ## 주의사항

            - 난이도에 맞는 적절한 깊이로 설명 (초급: 기본 개념, 중급: 실무 활용, 고급: 최적화/트레이드오프)
            - 지나치게 장황하거나 이론적인 설명은 지양
            - 실무에서 실제로 활용 가능한 내용 중심
            - 코드 예시는 간결하고 이해하기 쉽게

            {category} 분야의 {difficulty} 난이도 면접 질문과 답변을 생성해주세요.
            """
    ),

    V2_PROMPT("""
            당신은 {category} 분야의 시니어 소프트웨어 엔지니어링 면접관입니다.

            신입~주니어 개발자를 대상으로 한 실무 중심의 기술 면접 질문과 답변을 생성해주세요.

            ## 요구사항

            1. **주제**: {category}
            2. **난이도**: {difficulty}
            3. **질문**: 한 가지 핵심 개념을 명확하게 묻는 형식
            4. **답변**: 구조화되고 가독성 높은 마크다운 형식으로 작성
            5. 실제 면접에서 자주 나오는 실용적인 질문
            6. 단순 암기보다는 이해와 적용을 중심으로

            ## 답변 작성 가이드 (aiAnswer 필드)

            답변은 **반드시 아래 구조와 마크다운 문법**을 사용하여 작성하세요.
            각 섹션은 `###` 헤딩으로 시작하고, 내용은 그 아래 줄에 작성합니다.

            ### 답변 구조 (반드시 이 순서와 형식으로 작성):

            ### 핵심 개념 정의
            2~3문장으로 개념을 명확히 정의. 핵심 키워드와 그 의미를 포함하여 설명

            ### 등장 배경 / 해결하는 문제
            이 개념이 등장하기 전에 어떤 불편함이나 문제가 있었는지, 그 문제를 어떻게 해결하는지 구체적으로 서술.
            단순 나열이 아닌 흐름 있는 문장으로 설명

            ### 동작 원리 / 세부 설명
            내부 동작 방식, 구성 요소, 처리 흐름을 단계적으로 설명.
            필요 시 리스트(`-`)를 활용해 구성 요소나 단계를 정리

            ### 실무 적용 / 주의사항
            실제 개발 현장에서 언제 사용하는지 구체적인 상황을 들어 설명.
            잘못 사용하거나 남용할 경우 발생할 수 있는 문제점도 함께 서술

            ### 면접 포인트
            면접관이 이 질문을 통해 실제로 확인하고 싶은 것

            ### 마크다운 포맷팅 규칙:
            - 각 섹션 제목은 반드시 `### 제목` 형식 사용
            - 강조: **볼드체**로 중요 키워드 강조
            - 리스트: `-` 또는 `1.`로 항목 나열
            - 구분: `---`로 섹션 구분 (필요시)

            ## 출력 필드 설명

            - title: 질문을 10자 이내로 요약한 제목 (예: REST API란?)
            - description: 구체적이고 명확한 면접 질문 (1-2문장)
            - aiAnswer: 위 구조를 따른 마크다운 형식의 구조화된 모범 답변

            ## 주의사항

            - 난이도에 맞는 적절한 깊이로 설명 (초급: 기본 개념, 중급: 실무 활용, 고급: 최적화/트레이드오프)
            - 지나치게 장황하거나 이론적인 설명은 지양
            - 실무에서 실제로 활용 가능한 내용 중심
            - 코드 예시는 포함하지 않음

            {category} 분야의 {difficulty} 난이도 면접 질문과 답변을 생성해주세요.
            """
    ),

    ;

    private final String template;

    /**
     * 카테고리와 난이도를 주입하여 프롬프트 생성
     */
    public String generate(String categoryName, ProblemDifficulty difficulty) {
        return template
                .replace("{category}", categoryName)
                .replace("{difficulty}", translateDifficulty(difficulty));
    }

    /**
     * 카테고리와 난이도를 주입하고, 최근 출제된 문제 제목을 포함하여 중복 방지 프롬프트 생성
     */
    public String generate(String categoryName, ProblemDifficulty difficulty, List<String> recentTitles) {
        String base = generate(categoryName, difficulty);
        if (recentTitles == null || recentTitles.isEmpty()) {
            return base;
        }
        String titleList = recentTitles.stream()
                .map(t -> "- " + t)
                .collect(Collectors.joining("\n"));
        return base + "\n\n## 중복 방지 지침\n\n"
                + "아래는 최근에 이미 출제된 문제 제목입니다. 이와 동일하거나 유사한 주제는 피하고 반드시 다른 개념의 질문을 생성해주세요:\n\n"
                + titleList + "\n";
    }

    /**
     * 난이도를 한글로 변환
     */
    private String translateDifficulty(ProblemDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "초급 (기본 개념과 간단한 사용법)";
            case MEDIUM -> "중급 (실무 적용과 심화 개념)";
            case HARD -> "고급 (최적화, 설계, 트레이드오프)";
        };
    }

}