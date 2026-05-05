package com.genai.java.spring.rag.config.preprocessor;


import com.genai.java.spring.rag.config.data.RagConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DomainSynonymTransformer implements QueryTransformer {
    private final Map<Pattern, String> replacements;

    public DomainSynonymTransformer(RagConfigData ragConfigData) {
        Map<String, String> synonymMap = ragConfigData.getSynonyms();
        this.replacements = new LinkedHashMap<>();
        if (synonymMap != null) {
            synonymMap.forEach((k, v) ->
                    this.replacements.put(Pattern.compile("\\b" + Pattern.quote(k) + "\\b",
                            Pattern.CASE_INSENSITIVE), v));
        }
    }

    @Override
    public Query transform(Query query) {
        String queryText = query.text();
        if (queryText.isBlank()) {
            return query;
        }

        String cleanedText = getCleanedText(queryText);

        log.info("cleanedText after replacing (please|can you|could you): {}", cleanedText);


        cleanedText = applyDomainSynonyms(cleanedText);


        return Query.builder().text(cleanedText).build();
    }
// replaces question mark
    private String getCleanedText(String queryText) {
        return queryText.trim()
                .replaceAll("(?i)^(please|can you|could you)\\s+", "")
                .replaceAll("\\s*\\?$", ""); // ← change \s+ to \s*
    }

    private String applyDomainSynonyms(String cleanedText) {
        for (var entry : replacements.entrySet()) {
            cleanedText = entry.getKey().matcher(cleanedText).replaceAll(entry.getValue());
        }
        log.info("Transformed/cleanedText after Applying Domain Synonyms: {}", cleanedText);

        return cleanedText;
    }

}
