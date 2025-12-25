package com.recall.utils;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnMissingR2dbcUrlCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        return !context.getEnvironment().containsProperty("spring.r2dbc.url");
    }
}