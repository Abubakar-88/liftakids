package org.liftakids.controller;

import org.liftakids.service.impl.ResultReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/results")
public class ResultReportController {

    @Autowired
    private ResultReportService resultService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResult(@RequestParam Long studentId,
                                               @RequestParam String terminal,
                                               @RequestParam MultipartFile file) {
        resultService.uploadResult(studentId, terminal, file);
        return ResponseEntity.ok("Uploaded successfully");
    }

    @PostMapping("/publish/{id}")
    public ResponseEntity<String> publishResult(@PathVariable Long id) {
        resultService.publishResult(id);
        return ResponseEntity.ok("Published successfully");
    }
}
