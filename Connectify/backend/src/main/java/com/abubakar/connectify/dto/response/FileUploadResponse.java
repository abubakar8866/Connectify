package com.abubakar.connectify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class FileUploadResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> uploadedFiles;

    private List<String> failedFiles;

}
