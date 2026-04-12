package otvosuzlet.javitasnyilntarto.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.springframework.web.multipart.MultipartFile;

public class Base64MultipartFile implements MultipartFile {

    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;

    public Base64MultipartFile(String fileContent, String fileName, String contentType) {
        this.fileContent = java.util.Base64.getDecoder().decode(fileContent);
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override public String getName() { return "file"; }
    @Override public String getOriginalFilename() { return fileName; }
    @Override public String getContentType() { return contentType; }
    @Override public boolean isEmpty() { return fileContent.length == 0; }
    @Override public long getSize() { return fileContent.length; }
    @Override public byte[] getBytes() { return fileContent; }
    
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(fileContent); }
    
    @Override public void transferTo(File dest) throws IOException {
        Files.write(dest.toPath(), fileContent);
    }

}
