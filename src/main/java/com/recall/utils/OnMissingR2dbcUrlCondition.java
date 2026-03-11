package com.recall.utils;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition to check if the R2DBC URL property is missing.
 * Used to conditionally enable embedded database configurations.
 */
public class OnMissingR2dbcUrlCondition implements Condition {
    /**
     * Checks if the condition matches (i.e., R2DBC URL is not set).
     * @param context The condition context
     * @param metadata The annotated type metadata
     * @return true if spring.r2dbc.url is not set, false otherwise
     */
    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        return !context.getEnvironment().containsProperty("spring.r2dbc.url");
    }
}