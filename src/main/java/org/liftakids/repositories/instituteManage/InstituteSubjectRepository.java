package org.liftakids.repositories.instituteManage;

import org.liftakids.entity.InstituteManage.InstituteSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteSubjectRepository extends JpaRepository<InstituteSubject, Long> {

    List<InstituteSubject> findByInstitution_InstitutionsIdOrderBySubjectNameAsc(Long institutionId);

    List<InstituteSubject> findByInstitution_InstitutionsIdAndIsActiveTrueOrderBySubjectNameAsc(Long institutionId);

    Optional<InstituteSubject> findByInstitution_InstitutionsIdAndSubjectName(Long institutionId, String subjectName);

    boolean existsByInstitution_InstitutionsIdAndSubjectName(Long institutionId, String subjectName);
}