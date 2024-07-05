package com.thread_test.controller;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.thread_test.service.FileUploadFuture;
import com.thread_test.service.FileUploadTaskFactory;

@Controller
public class HomeController {

    @Autowired
    private FileUploadTaskFactory fileUploadTaskFactory;

    private final List<FileUploadFuture> files = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final AtomicBoolean showProgress = new AtomicBoolean(false);

    @PostMapping("/load")
    public String loadCSVS(@RequestParam("files") MultipartFile[] files, Model model) throws IOException {
        this.files.clear();

        for (MultipartFile file : files) {
            this.files.add(new FileUploadFuture(file.getInputStream(), file.getOriginalFilename(), false));
        }

        model.addAttribute("filenames", this.files);
        return "index";
    }

    private int findFileIndexByName(String filename) {
        for (int i = 0; i < files.size(); i++) {
            if (Objects.equals(files.get(i).getFilename(), filename)) {
                return i;
            }
        }
        return -1;
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam String convertFile, Model model) {
        model.addAttribute("showProgress", true);
        model.addAttribute("filenames", files);
        int fileIndex = findFileIndexByName(convertFile);

        if (fileIndex >= 0) {
            FileUploadFuture file = files.get(fileIndex);
            if (!file.getIsUploaded()) {
                try {
                    Callable<Boolean> callable = fileUploadTaskFactory.create(file.getInputStream());
                    Future<Boolean> future = executorService.submit(callable);

                    file.setStart(System.nanoTime());

                    if (future.get()) {
                        file.setUploadTime(TimeUnit.MILLISECONDS.convert(System.nanoTime() - file.getStartTime(), TimeUnit.NANOSECONDS));
                        file.setIsUploaded(true);
                        model.addAttribute("uploadSuccess", true); // Add success attribute
                    } else {
                        System.out.println("File not uploaded");
                    }
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof NoSuchFileException) {
                        System.err.println("No such file: " + cause.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }

        return "index";
    }

    @PostMapping("/uploadAll")
    public String uploadAll(Model model) throws InterruptedException, ExecutionException {
        boolean anyUploaded = false;
        if (!files.isEmpty()) {
            model.addAttribute("showProgress", true);
            model.addAttribute("filenames", files);

            for (FileUploadFuture file : files) {
                if (!file.getIsUploaded()) {
                    Callable<Boolean> callable = fileUploadTaskFactory.create(file.getInputStream());
                    file.setStart(System.nanoTime());
                    Future<Boolean> future = executorService.submit(callable);

                    if (future.get()) {
                        file.setUploadTime(TimeUnit.MILLISECONDS.convert(System.nanoTime() - file.getStartTime(), TimeUnit.NANOSECONDS));
                        file.setIsUploaded(true);
                        anyUploaded = true;
                    } else {
                        System.out.println("File not uploaded");
                    }
                }
            }
        } else {
            System.out.println("No files to upload");
        }
        model.addAttribute("uploadSuccess", anyUploaded); // Add success attribute
        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        files.clear();
        model.addAttribute("filenames", files);
        model.addAttribute("filename_index", 0);
        return "index";
    }

    @RequestMapping(value = "/checkProgress", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkProgress() {
        Map<String, Boolean> response = new ConcurrentHashMap<>();
        response.put("showProgress", showProgress.get());
        return ResponseEntity.ok(response);
    }
}
