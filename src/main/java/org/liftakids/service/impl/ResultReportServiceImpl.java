package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.liftakids.dto.resultReport.ResultReportRequestDto;
import org.liftakids.dto.resultReport.ResultReportResponseDto;
import org.liftakids.dto.resultReport.SubjectMarkDto;
import org.liftakids.entity.ResultReport;
import org.liftakids.entity.Student;
import org.liftakids.entity.SubjectMark;
import org.liftakids.repositories.ResultReportRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.repositories.SubjectMarkRepository;
import org.liftakids.service.ResultReportService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultReportServiceImpl implements ResultReportService {

    private final ResultReportRepository resultReportRepository;
    private final SubjectMarkRepository subjectMarkRepository;
    private final StudentRepository studentRepository;

    @Override
    public ResultReportResponseDto uploadResult(ResultReportRequestDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String text = extractTextFromImage(dto.getResultImage());

        // Parse extracted text
        String exam = extractValue(text, "Exam:");
        String term = extractValue(text, "Term:");
        LocalDate date = LocalDate.parse(extractValue(text, "Date:"));
        String studentClass = extractValue(text, "Class:");

        List<SubjectMark> subjectMarks = extractSubjectMarks(text);

        ResultReport report = new ResultReport();
        report.setExam(exam);
        report.setTerminal(term);
        report.setExamDate(date);
        report.setStudentClass(studentClass);
        report.setStudent(student);
        report.setUploadDate(LocalDate.now());

        for (SubjectMark sm : subjectMarks) {
            sm.setResultReport(report);
        }

        report.setSubjectMarks(subjectMarks);
        ResultReport saved = resultReportRepository.save(report);

        return buildResponseDto(saved);
    }

    @Override
    public List<ResultReportResponseDto> getResultsByStudentId(Long studentId) {
        List<ResultReport> reports = resultReportRepository.findByStudent_StudentId(studentId);
        List<ResultReportResponseDto> dtos = new ArrayList<>();

        for (ResultReport report : reports) {
            dtos.add(buildResponseDto(report));
        }

        return dtos;
    }

    private String extractTextFromImage(org.springframework.web.multipart.MultipartFile file) {
        try {
            File tempFile = File.createTempFile("ocr_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata");
            return tesseract.doOCR(tempFile);
        } catch (IOException | TesseractException e) {
            throw new RuntimeException("Failed to extract text", e);
        }
    }

    private String extractValue(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.startsWith(key)) {
                return line.replace(key, "").trim();
            }
        }
        throw new RuntimeException("Key not found: " + key);
    }

    private List<SubjectMark> extractSubjectMarks(String text) {
        List<SubjectMark> list = new ArrayList<>();
        boolean start = false;

        for (String line : text.split("\n")) {
            if (line.toLowerCase().contains("subject") && line.toLowerCase().contains("obtained")) {
                start = true;
                continue;
            }
            if (start && !line.trim().isEmpty()) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String subject = parts[0];
                    int mark = Integer.parseInt(parts[1]);
                    list.add(new SubjectMark(null, subject, mark, null));
                }
            }
        }

        return list;
    }

    private ResultReportResponseDto buildResponseDto(ResultReport report) {
        ResultReportResponseDto dto = new ResultReportResponseDto();
        dto.setExamName(report.getExam());
        dto.setTerm(report.getTerminal());
        dto.setExamDate(report.getExamDate());
        dto.setStudentClass(report.getStudentClass());
        dto.setStudentId(report.getStudent().getStudentId());
        dto.setStudentName(report.getStudent().getStudentName());

        List<SubjectMarkDto> marks = new ArrayList<>();
        for (SubjectMark sm : report.getSubjectMarks()) {
            SubjectMarkDto smDto = new SubjectMarkDto();
            smDto.setSubjectName(sm.getSubjectName());
            smDto.setObtainedMark(sm.getObtainedMark());
            marks.add(smDto);
        }

        dto.setSubjectMarks(marks);
        return dto;
    }
}

