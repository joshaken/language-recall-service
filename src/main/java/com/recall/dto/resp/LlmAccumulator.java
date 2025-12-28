package com.recall.dto.resp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
public class LlmAccumulator {
    private final StringBuilder content = new StringBuilder();
    private ChatResponse finalMetadata = null;

    public void appendContent(String text) {
        if (text != null) content.append(text);
    }


    public EvaluationResult toEvaluationResult() {
        EvaluationResult result = new EvaluationResult();
        String fullText = content.toString();
        result.setFinalMetadata(finalMetadata); // 或提取需要的字段

        boolean correct =
//                fullText.toLowerCase().contains("\"correct\": \"true\"") ||
//                fullText.toLowerCase().contains("'correct':'true'") ||
//                fullText.toLowerCase().contains("correct:true") ||
                (fullText.contains("correct") && fullText.contains("true"));
        log.info("correct: {}", correct);
        result.setCorrect(correct);
        String modifiedContent = fullText.replace("{\"correct\": true}", "")
                .replace("{\"correct\": false}", "");
        result.setModelAnswer(modifiedContent);
        return result;
    }
}
