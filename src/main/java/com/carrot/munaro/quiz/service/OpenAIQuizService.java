package com.carrot.munaro.quiz.service;

import com.carrot.munaro.quiz.dto.response.QuizGenerationResponse;
import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIQuizService {

    private static final String OPENAI_RESPONSES_URL =
            "https://api.openai.com/v1/responses";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public QuizGenerationResponse generateQuiz(TouristSpot touristSpot) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured."
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        buildRequestBody(buildPrompt(touristSpot)),
                        headers
                );

        String responseBody =
                restTemplate.exchange(
                        OPENAI_RESPONSES_URL,
                        HttpMethod.POST,
                        request,
                        String.class
                ).getBody();

        return parseResponse(responseBody);
    }

    private Map<String, Object> buildRequestBody(String prompt) {

        return Map.of(
                "model", model,
                "input", prompt,
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "tourist_spot_quiz",
                                "strict", true,
                                "schema", buildJsonSchema()
                        )
                )
        );
    }

    private Map<String, Object> buildJsonSchema() {

        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("title", "difficulty", "questions"),
                "properties", Map.of(
                        "title", Map.of("type", "string"),
                        "difficulty", Map.of(
                                "type", "string",
                                "enum", List.of("EASY", "NORMAL", "HARD")
                        ),
                        "questions", Map.of(
                                "type", "array",
                                "minItems", 3,
                                "maxItems", 3,
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "required", List.of(
                                                "question",
                                                "answer",
                                                "choices"
                                        ),
                                        "properties", Map.of(
                                                "question", Map.of(
                                                        "type",
                                                        "string"
                                                ),
                                                "answer", Map.of(
                                                        "type",
                                                        "string"
                                                ),
                                                "choices", Map.of(
                                                        "type",
                                                        "array",
                                                        "minItems",
                                                        4,
                                                        "maxItems",
                                                        4,
                                                        "items",
                                                        Map.of(
                                                                "type",
                                                                "string"
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private QuizGenerationResponse parseResponse(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("OpenAI response is empty.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String outputText = root.path("output")
                    .path(0)
                    .path("content")
                    .path(0)
                    .path("text")
                    .asText();

            if (outputText == null || outputText.isBlank()) {
                throw new IllegalStateException(
                        "OpenAI response text is empty."
                );
            }

            return objectMapper.readValue(
                    outputText,
                    QuizGenerationResponse.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse OpenAI quiz response.",
                    e
            );
        }
    }

    private String buildPrompt(TouristSpot touristSpot) {

        return """
                다음 관광지 정보를 기반으로 한국어 객관식 퀴즈 3개를 생성해줘.

                규칙:
                - 관광지 설명에 근거한 문제만 만든다.
                - 각 문제는 선택지 4개를 가진다.
                - 정답은 choices 안의 값과 정확히 같은 문자열이어야 한다.
                - 너무 지엽적인 숫자 암기 문제는 피한다.
                - 출력은 JSON만 반환한다.

                관광지명: %s
                설명: %s
                카테고리: %s
                주소: %s
                """.formatted(
                nullToBlank(touristSpot.getName()),
                nullToBlank(touristSpot.getDescription()),
                nullToBlank(touristSpot.getCategory()),
                nullToBlank(touristSpot.getAddress())
        );
    }

    private String nullToBlank(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }
}
