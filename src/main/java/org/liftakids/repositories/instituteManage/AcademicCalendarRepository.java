package org.liftakids.repositories.instituteManage;

import org.liftakids.entity.AcademicCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AcademicCalendarRepository extends JpaRepository<AcademicCalendar, Long> {

    // Find by institution
    Page<AcademicCalendar> findByInstitution_InstitutionsId(Long institutionId, Pageable pageable);

    List<AcademicCalendar> findByInstitution_InstitutionsId(Long institutionId);

    // Find by institution and date range
    @Query("SELECT a FROM AcademicCalendar a WHERE a.institution.institutionsId = :institutionId " +
            "AND a.startDate <= :endDate AND a.endDate >= :startDate " +
            "AND a.isActive = true")
    List<AcademicCalendar> findByInstitutionAndDateRange(
            @Param("institutionId") Long institutionId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find by institution and event type
    List<AcademicCalendar> findByInstitution_InstitutionsIdAndEventType(
            Long institutionId, String eventType
    );

    // Find by institution and target audience
    List<AcademicCalendar> findByInstitution_InstitutionsIdAndTargetAudienceIn(
            Long institutionId, List<String> targetAudience
    );

    // Find upcoming events
    @Query("SELECT a FROM AcademicCalendar a WHERE a.institution.institutionsId = :institutionId " +
            "AND a.startDate >= :today AND a.isActive = true " +
            "ORDER BY a.startDate ASC")
    List<AcademicCalendar> findUpcomingEvents(
            @Param("institutionId") Long institutionId,
            @Param("today") LocalDate today
    );

    // Find ongoing events
    @Query("SELECT a FROM AcademicCalendar a WHERE a.institution.institutionsId = :institutionId " +
            "AND a.startDate <= :today AND a.endDate >= :today AND a.isActive = true")
    List<AcademicCalendar> findOngoingEvents(
            @Param("institutionId") Long institutionId,
            @Param("today") LocalDate today
    );

    // Count events by month
    @Query("SELECT MONTH(a.startDate), COUNT(a) FROM AcademicCalendar a " +
            "WHERE a.institution.institutionsId = :institutionId AND YEAR(a.startDate) = :year " +
            "GROUP BY MONTH(a.startDate)")
    List<Object[]> countEventsByMonth(
            @Param("institutionId") Long institutionId,
            @Param("year") int year
    );
}