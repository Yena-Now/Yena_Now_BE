package com.example.yenanow.s3.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedUrlRequest {

    @NotBlank
    private String type;
    @NotBlank
    private String fileName;
    @NotBlank
    private String contentType;
    
    private String relayUuid;
    private String ncutUuid;
}
