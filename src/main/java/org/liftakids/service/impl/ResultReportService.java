package org.liftakids.service.impl;

import org.liftakids.entity.ResultReport;
import org.liftakids.entity.Student;
import org.liftakids.exception.StudentNotFoundException;
import org.liftakids.repositories.ResultReportRepository;
import org.liftakids.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class ResultReportService {

    @Autowired
    private ResultReportRepository resultRepo;

    @Autowired
    private StudentRepository studentRepo;

    public ResultReport uploadResult(Long studentId, String terminal, MultipartFile file) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        String fileUrl = saveFile(file); // Implement file saving logic

        ResultReport report = new ResultReport();
        report.setStudent(student);
        report.setTerminal(terminal);
        report.setResultFileUrl(fileUrl);
        report.setPublished(false);

        return resultRepo.save(report);
    }

    public ResultReport publishResult(Long id) {
        ResultReport report = resultRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setPublished(true);
        return resultRepo.save(report);
    }

    private String saveFile(MultipartFile file) {
        try {
            // Define a local directory path to save
            String uploadDir = "uploads/results/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            File dest = new File(filePath);
            file.transferTo(dest);

            return filePath; // or return a public URL if hosted
        } catch (Exception e) {
            throw new RuntimeException("File saving failed: " + e.getMessage());
        }
    }

}
