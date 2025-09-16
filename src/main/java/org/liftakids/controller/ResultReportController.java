package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.resultReport.ResultReportRequestDto;
import org.liftakids.dto.resultReport.ResultReportResponseDto;
import org.liftakids.service.ResultReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultReportController {

    private final ResultReportService resultService;

    @PostMapping("/upload")
    public ResponseEntity<ResultReportResponseDto> uploadResult(
            @RequestParam("studentId") Long studentId,
            @RequestParam("resultImage") MultipartFile resultImage) {

        ResultReportRequestDto dto = new ResultReportRequestDto();
        dto.setStudentId(studentId);
        dto.setResultImage(resultImage);

        return ResponseEntity.ok(resultService.uploadResult(dto));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ResultReportResponseDto>> getByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(resultService.getResultsByStudentId(studentId));
    }

}
