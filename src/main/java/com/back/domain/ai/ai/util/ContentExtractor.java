package com.back.domain.ai.ai.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentExtractor {

    private ContentExtractor() {
    }

    public static String extractContent(String content) {
        final int CONTENT_MAX_CHARS = 6000;
        final int TITLE_MAX_CHARS = 80;
        final int HEAD_OR_TAIL_MAX_CHARS = 2500;
        final int MAX_HEADINGS = 12;
        final int MIN_HEADINGS_FOR_MIDDLE_SKIP = 2;  // 헤딩이 너무 적으면 중간도 뽑아주기 위한 기준/길이
        final int MIDDLE_MAX_CHARS = 1000;

        if (content == null) return "";

        int contentLength = content.length();
        if (contentLength <= CONTENT_MAX_CHARS) return content;

        // 1) 헤딩 추출
        LinkedHashMap<String, String> headingMap = new LinkedHashMap<>();

        Pattern h1 = Pattern.compile("(?m)^#\\s+(.+)$");
        Pattern h2 = Pattern.compile("(?m)^##\\s+(.+)$");

        collectHeadingsWithPrefix(content, h1, "#", headingMap, MAX_HEADINGS, TITLE_MAX_CHARS);
        if (headingMap.size() < MAX_HEADINGS) {
            collectHeadingsWithPrefix(content, h2, "##", headingMap, MAX_HEADINGS, TITLE_MAX_CHARS);
        }

        List<String> headings = new ArrayList<>(headingMap.values());
        String headingBlock = headings.isEmpty() ? "" : String.join("\n", headings);

        // 2) 앞/뒤 자르기
        int slice = HEAD_OR_TAIL_MAX_CHARS;

        int headLen = slice;
        int tailLen = Math.min(slice, content.length() - headLen);

        String head = content.substring(0, headLen);
        String tail = content.substring(contentLength - tailLen);

        // 3) 중간 핵심 문단 추가 (헤딩이 적을 때만)
        String middle = "";
        if (headings.size() < MIN_HEADINGS_FOR_MIDDLE_SKIP) {
            middle = extractMiddle(content, headLen, tailLen, MIDDLE_MAX_CHARS);
        }

        // 4) 합치기
        StringBuilder sb = new StringBuilder();

        if (!headingBlock.isBlank()) {
            sb.append("(헤딩 목록)\n")
                    .append(headingBlock)
                    .append("\n\n---\n\n");
        }

        sb.append("(본문 일부)\n")
                .append(head);

        if (!middle.isBlank()) {
            sb.append("\n\n...\n\n")
                    .append(middle);
        }

        sb.append("\n\n...\n\n")
                .append(tail);

        return sb.toString();
    }

    private static void collectHeadingsWithPrefix(
            String content,
            Pattern pattern,
            String prefix,
            LinkedHashMap<String, String> out,
            int maxHeadings,
            int titleMaxChars
    ) {
        Matcher m = pattern.matcher(content);
        while (m.find() && out.size() < maxHeadings) {
            String rawTitle = m.group(1).trim();
            if (rawTitle.isEmpty()) continue;

            String title = rawTitle;
            if (title.length() > titleMaxChars) {
                title = title.substring(0, titleMaxChars) + "...";
            }

            // 중복 제거는 "제목 텍스트" 기준
            // 이미 같은 제목이 있으면 스킵(첫 등장 레벨 유지)
            out.putIfAbsent(rawTitle, prefix + " " + title);
        }
    }

    private static String extractMiddle(String content, int headLen, int tailLen, int middleMax) {
        int n = content.length();

        int midStartLimit = headLen;
        int midEndLimit = n - tailLen;

        if (midEndLimit <= midStartLimit) return "";

        int midAvailable = midEndLimit - midStartLimit;
        int take = Math.min(middleMax, midAvailable);

        int center = (midStartLimit + midEndLimit) / 2;
        int start = Math.max(midStartLimit, center - take / 2);
        int end = Math.min(midEndLimit, start + take);

        start = Math.max(midStartLimit, end - take);

        return content.substring(start, end);
    }
}
