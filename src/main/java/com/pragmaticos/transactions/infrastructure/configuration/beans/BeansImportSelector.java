package com.pragmaticos.transactions.infrastructure.configuration.beans;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.logging.Logger;

import static com.pragmaticos.transactions.infrastructure.application.TransactionsApplication.ADAPTERS_ROUTES;
import static com.pragmaticos.transactions.infrastructure.application.TransactionsApplication.USECASES_ROUTE;

public class BeansImportSelector implements ImportSelector {


    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        String[] useCaseClasses = ScannerClasses.scannerClasses(USECASES_ROUTE);
        String[] adapterClasses = ScannerClasses.scannerClasses(ADAPTERS_ROUTES);
        String[] totalScanner = new String[useCaseClasses.length + adapterClasses.length];
        System.arraycopy(useCaseClasses, 0, totalScanner, 0, useCaseClasses.length);
        System.arraycopy(adapterClasses, 0, totalScanner, useCaseClasses.length, adapterClasses.length);
        Logger.getLogger(BeansImportSelector.class.getName()).info("Imported Beans: " + String.join("\n", totalScanner));
        return totalScanner;
    }


}
