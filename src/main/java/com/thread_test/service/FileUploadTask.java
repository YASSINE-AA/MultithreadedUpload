package com.thread_test.service;

import com.thread_test.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.thread_test.entity.User;

import java.io.*;
import java.util.concurrent.Callable;
import org.springframework.context.annotation.Scope;

@Service
@Scope("prototype")
public class FileUploadTask implements Callable<Boolean> {

    private InputStream inputStream;

    @Autowired
    private CrudService crudService;

    public FileUploadTask() {}

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Boolean call() throws Exception {
        System.out.println("Uploading and processing CSV started");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.readLine(); // Skip the header
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] data = line.split(";");
                User user_tmp = new User(Long.parseLong(data[1]), data[0], data[2], data[3]);
                crudService.insert(user_tmp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Task completed.");
        return true;
    }
}
