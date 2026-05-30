package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.ReportRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportAccessValidator {

    @Autowired
    private ReportRepository reportRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ReportAccessValidator.class
            );

    public Report getReport(
            Long reportId
    ) {

        logger.debug(
                "Fetching report | reportId: {}",
                reportId
        );

        return reportRepository.findWithReporterById(reportId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Report not found | reportId: {}",
                            reportId
                    );

                    return new ResourceNotFound(
                            "Report not found with id: "
                                    + reportId
                    );
                });

    }

}

