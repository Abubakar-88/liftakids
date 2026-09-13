package org.liftakids.service.instituteManage.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;
import org.liftakids.entity.InstituteManage.InstituteSubject;
import org.liftakids.entity.Institutions;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.instituteManage.InstituteSubjectRepository;
import org.liftakids.service.instituteManage.InstituteSubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstituteSubjectServiceImpl implements InstituteSubjectService {

    private final InstituteSubjectRepository subjectRepository;
    private final InstitutionRepository institutionRepository;

    private static final List<String> DEFAULT_SUBJECTS = Arrays.asList(
            "Arabic", "Bangla", "English", "Mathematics",
            "Science", "Islamic Studies", "General Knowledge"
    );

    @Override
    @Transactional
    public InstituteSubjectDto createSubject(InstituteSubjectDto requestDto, Long institutionId) {
        log.info("Creating subject for institution: {}", institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        if (subjectRepository.existsByInstitution_InstitutionsIdAndSubjectName(institutionId, requestDto.getSubjectName())) {
            throw new RuntimeException("Subject already exists");
        }

        InstituteSubject subject = InstituteSubject.builder()
                .subjectName(requestDto.getSubjectName())
                .subjectCode(requestDto.getSubjectCode())
                .description(requestDto.getDescription())
                .institution(institution)
                .isActive(true)
                .build();

        InstituteSubject saved = subjectRepository.save(subject);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public List<InstituteSubjectDto> getAllSubjects(Long institutionId) {
        return subjectRepository.findByInstitution_InstitutionsIdOrderBySubjectNameAsc(institutionId)
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<InstituteSubjectDto> getActiveSubjects(Long institutionId) {
        return subjectRepository.findByInstitution_InstitutionsIdAndIsActiveTrueOrderBySubjectNameAsc(institutionId)
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InstituteSubjectDto getSubjectById(Long subjectId) {
        InstituteSubject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        return convertToDto(subject);
    }

    @Override
    @Transactional
    public InstituteSubjectDto updateSubject(Long subjectId, InstituteSubjectDto requestDto, Long institutionId) {
        log.info("Updating subject: {} for institution: {}", subjectId, institutionId);

        InstituteSubject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (!subject.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to update this subject");
        }

        if (requestDto.getSubjectName() != null) subject.setSubjectName(requestDto.getSubjectName());
        if (requestDto.getSubjectCode() != null) subject.setSubjectCode(requestDto.getSubjectCode());
        if (requestDto.getDescription() != null) subject.setDescription(requestDto.getDescription());
        if (requestDto.getIsActive() != null) subject.setActive(requestDto.getIsActive());

        InstituteSubject updated = subjectRepository.save(subject);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteSubject(Long subjectId, Long institutionId) {
        log.info("Deleting subject: {} for institution: {}", subjectId, institutionId);

        InstituteSubject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (!subject.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to delete this subject");
        }

        subjectRepository.delete(subject);
    }

    @Override
    @Transactional
    public List<InstituteSubjectDto> createMultipleSubjects(List<InstituteSubjectDto> subjectDtos, Long institutionId) {
        List<InstituteSubjectDto> created = new ArrayList<>();
        for (InstituteSubjectDto dto : subjectDtos) {
            created.add(createSubject(dto, institutionId));
        }
        return created;
    }

    @Override
    @Transactional
    public void setupDefaultSubjectsForInstitution(Long institutionId) {
        log.info("Setting up default subjects for institution: {}", institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        for (String subjectName : DEFAULT_SUBJECTS) {
            if (!subjectRepository.existsByInstitution_InstitutionsIdAndSubjectName(institutionId, subjectName)) {
                InstituteSubject subject = InstituteSubject.builder()
                        .subjectName(subjectName)
                        .institution(institution)
                        .isActive(true)
                        .build();
                subjectRepository.save(subject);
            }
        }
    }

    private InstituteSubjectDto convertToDto(InstituteSubject subject) {
        return InstituteSubjectDto.builder()
                .id(subject.getId())
                .subjectName(subject.getSubjectName())
                .subjectCode(subject.getSubjectCode())
                .description(subject.getDescription())
                .institutionId(subject.getInstitution().getInstitutionsId())
                .institutionName(subject.getInstitution().getInstitutionName())
                .isActive(subject.isActive())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}
