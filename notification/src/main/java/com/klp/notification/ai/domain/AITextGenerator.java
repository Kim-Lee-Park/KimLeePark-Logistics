package com.klp.notification.ai.domain;

public interface AITextGenerator {

    /**
     * 주어진 프롬프트를 기반으로 텍스트를 생성합니다.
     */
    String generate(String prompt);
}
