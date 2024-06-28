package com.thread_test.controller;

import com.thread_test.service.FileUploadFuture;
import com.thread_test.service.FileUploadTask;
import com.thread_test.service.FileUploadTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class HomeController {

	@Autowired
	private FileUploadTaskFactory fileUploadTaskFactory;

	private final List<Map<String, String>> finishedFilenames = new CopyOnWriteArrayList<>();

	private final ExecutorService executorService = Executors.newFixedThreadPool(10);

	private List<FileUploadFuture> files = new CopyOnWriteArrayList<>();
	private final AtomicBoolean showProgress = new AtomicBoolean(false);

	@PostMapping("/load")
	public String loadCSVS(@RequestParam("files") MultipartFile[] files, Model model) throws IOException {
		finishedFilenames.clear();

		for (MultipartFile file : files) {
			Map<String, String> tmpHash = new ConcurrentHashMap<>();
			tmpHash.put("filename", file.getOriginalFilename());
			tmpHash.put("time", "Yet to be processed");
			this.files.add(new FileUploadFuture(file.getInputStream(), file.getOriginalFilename()));
			finishedFilenames.add(tmpHash);

		}
		model.addAttribute("filenames", finishedFilenames);
		return "index";
	}

	private int checkInFiles(String filename) {
		for (int i = 0; i < files.size(); i++) {
			if (Objects.equals(files.get(i).getFilename(), filename)) {
				return i;
			}
		}
		return -1;
	}

	private int checkInTable(String filename) {
		for (int i = 0; i < finishedFilenames.size(); i++) {
			if (Objects.equals(finishedFilenames.get(i).get("filename"), filename)) {
				return i;
			}
		}
		return -1;
	}

	@PostMapping("/uploadFile")
	public String uploadFile(@RequestParam String convertFile, Model model) {
		model.addAttribute("filenames", finishedFilenames);
		int fileIndex = checkInFiles(convertFile);
		System.out.println("this is the index: " + fileIndex);
		if (fileIndex >= 0) {
			// file found
			try {

				FileUploadFuture file = this.files.get(fileIndex);

                Callable<Boolean> callable = fileUploadTaskFactory.create(file.getInputStream());

				file.setStart(System.nanoTime());

				Future<Boolean> future = executorService.submit(callable);

				if (future.get()) {
					System.out.println("uploaded");
					int tableIndex = checkInTable(file.getFilename());
					Map<String, String> row = this.finishedFilenames.get(tableIndex);
					row.put("time", String.valueOf(TimeUnit.MILLISECONDS.convert(System.nanoTime() - file.getStartTime(), TimeUnit.NANOSECONDS)));
					this.finishedFilenames.set(tableIndex, row);
				} else {
					System.out.println("not uploaded");
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
		return "index";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		model.addAttribute("filenames", finishedFilenames);
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
