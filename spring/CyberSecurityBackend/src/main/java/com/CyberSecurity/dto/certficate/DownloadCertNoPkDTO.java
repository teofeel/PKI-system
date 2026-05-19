package com.CyberSecurity.dto.certficate;

import lombok.*;
import org.springframework.http.MediaType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadCertNoPkDTO {
    byte[] cert;
    String filename;
    MediaType mediaType;
}
