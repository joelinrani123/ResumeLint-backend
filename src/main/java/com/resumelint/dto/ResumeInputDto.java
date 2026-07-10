package com.resumelint.dto;

import jakarta.validation.constraints.NotBlank;

/** Equivalent of the Node UploadResumeBody zod schema (ResumeInput). */
public record ResumeInputDto(
        @NotBlank(message = "fileName is required") String fileName,
        @NotBlank(message = "content is required") String content,
        String jobTitle
) {
}
