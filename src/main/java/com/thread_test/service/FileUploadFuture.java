package com.thread_test.service;
import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;
import org.eclipse.tags.shaded.org.apache.xpath.operations.Mult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
public class FileUploadFuture {
    private Long start;
    private InputStream inputStream;
    private String filename;

    public FileUploadFuture(InputStream inputStream, String filename){
        this.inputStream = inputStream;
        this.filename = filename;
    }

    public InputStream getInputStream() {return this.inputStream;}
    public String getFilename() {return filename;}

    public void setStart(long time) {start = time;}

    public Long getStartTime() {return start;}

}
