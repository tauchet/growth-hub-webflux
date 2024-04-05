package com.pragmaticos.transactions.infrastructure.configuration.beans;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.List;
import java.util.regex.Pattern;

import static com.pragmaticos.transactions.infrastructure.application.TransactionsApplication.EXCLUDE_ADAPTERS_ROUTES;

public class ScannerClasses {

    public static String[] scannerClasses(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
        for (Pattern pattern: EXCLUDE_ADAPTERS_ROUTES) {
            scanner.addExcludeFilter(new RegexPatternTypeFilter(pattern));
        }
        List<String> classNames = scanner.findCandidateComponents(basePackage)
                .stream()
                .map(BeanDefinition::getBeanClassName)
                .toList();
        return classNames.toArray(new String[0]);
    }

}