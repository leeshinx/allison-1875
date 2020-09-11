package com.spldeolin.allison1875.docanalyzer.strategy;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;

/**
 * @author Deolin 2020-06-18
 */
public class DefaultAnalyzeCustomValidationStrategy implements AnalyzeCustomValidationStrategy {

    @Override
    public Collection<ValidatorDto> analyzeCustomValidation(AnnotatedElement annotatedElement) {
        return Lists.newArrayList();
    }

}
