package com.mlm.backend.Service;

import com.mlm.backend.Model.MigrationError;
import com.mlm.backend.Repository.MigrationErrorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ROCmAdvisorService {

    private final MigrationErrorRepository errorRepository;

    public ROCmAdvisorService(MigrationErrorRepository errorRepository) {
        this.errorRepository = errorRepository;
    }

    public MigrationError createErrorEntry(MigrationError error) {
        return errorRepository.save(error);
    }

    public List<MigrationError> getAllErrors() {
        return errorRepository.findAll();
    }

    /**
     * Parses raw compiler output and tries to find a matching explanation
     * in the knowledge base.
     */
    public MigrationError analyzeCompilerOutput(String rawCompilerLog) {
        List<MigrationError> allKnownErrors = errorRepository.findAll();

        for (MigrationError knownError : allKnownErrors) {
            if (rawCompilerLog.toLowerCase().contains(knownError.getErrorSnippet().toLowerCase())) {
                return knownError;
            }
        }

        // Return a generic fallback if the error is unknown
        MigrationError unknown = new MigrationError();
        unknown.setErrorCode("UNKNOWN_HIP_ERR");
        unknown.setHumanReadableExplanation("The ROCm Advisor could not recognize this specific compiler error.");
        unknown.setSuggestedFix("Please check the official AMD ROCm documentation or ask in the Developer Discord.");
        return unknown;
    }

    public void deleteErrorEntry(Long id) {
        errorRepository.deleteById(id);
    }
}