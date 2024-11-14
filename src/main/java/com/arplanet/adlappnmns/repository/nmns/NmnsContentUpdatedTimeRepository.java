package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsContentUpdatedTime;
import com.arplanet.adlappnmns.dto.ItemInfoDTO;
import com.arplanet.adlappnmns.dto.MaterialInfoDTO;
import com.arplanet.adlappnmns.dto.PaperDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NmnsContentUpdatedTimeRepository extends JpaRepository<NmnsContentUpdatedTime, Long> {


    @Query(nativeQuery = true, value =
            "SELECT " +
                    "CONVERT(ncuc.unit_content_id, CHAR) as materialSn, " +
                    "nc.content_id as materialId, " +
                    "CONVERT(ncuc.unit_exp_type, CHAR) as materialExpType, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT ncug.group_key), '') as materialGroups, " +
                    "CONVERT(ncuc.unit_content_type, CHAR) as materialContentType, " +
                    "CONVERT(ncuc.player_type, CHAR) as type, " +
                    "CONVERT(nci_title.info_val, CHAR) as name, " +
                    "CONVERT(ncuc.player_provider, CHAR) as publisher, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_subject' THEN ncm.metrics_val END), '') as subject, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_code' THEN ncm.metrics_val END), '') as node, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'grade_stage' THEN ncm.metrics_val END), '') as gradeStage, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_competence' THEN ncm.metrics_val END), '') as coreCompetence, " +
                    "CONVERT(ncuc.player_url, CHAR) as path, " +
                    "nc.created_at as creationTimestamp, " +
                    "ncut.updated_at as updateTimestamp " +
                    "FROM nmns_content_updated_time ncut " +
                    "INNER JOIN nmns_content nc ON ncut.content_id = nc.content_id " +
                    "INNER JOIN nmns_course_units_content ncuc ON ncut.content_id = ncuc.content_id " +
                    "INNER JOIN nmns_course_units ncu ON ncuc.unit_id = ncu.unit_id " +
                    "INNER JOIN nmns_course_units_groups ncug ON ncu.group_id = ncug.group_id " +
                    "LEFT JOIN nmns_content_metrics ncm ON ncu.content_id = ncm.content_id " +
                    "LEFT JOIN nmns_content_info nci_title ON nci_title.content_id = ncu.content_id AND nci_title.info_key = 'title' " +
                    "WHERE ncut.updated_date = :date " +
                    "GROUP BY ncuc.unit_content_id")
    List<MaterialInfoDTO> findMaterialInfo(
            @Param("date") String date
    );


    @Query(nativeQuery = true, value =
            "SELECT " +
                    "CONVERT(nqq.question_id, CHAR) as itemSn, " +
                    "CONVERT(ncuc.unit_content_id, CHAR) as materialSn, " +
                    "CONVERT(nqq.question_type, CHAR) as itemType, " +
                    "CONVERT(nqo.option_id, CHAR) as correctAnswer, " +
                    "'easy' as level, " +
                    "CONVERT(ncuc.player_provider, CHAR) as publisher, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_subject' THEN ncm.metrics_val END), '') as subject, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_code' THEN ncm.metrics_val END), '') as node, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'grade_stage' THEN ncm.metrics_val END), '') as gradeStage, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_competence' THEN ncm.metrics_val END), '') as coreCompetence, " +
                    "nc.created_at as creationTimestamp, " +
                    "ncut.updated_at as updateTimestamp " +
                    "FROM nmns_content_updated_time ncut " +
                    "INNER JOIN nmns_content nc ON ncut.content_id = nc.content_id " +
                    "INNER JOIN nmns_quiz_questions nqq ON ncut.content_id = nqq.content_id " +
                    "INNER JOIN nmns_course_units_content ncuc ON nqq.quiz_id = ncuc.quiz_id " +
                    "INNER JOIN nmns_course_units ncu ON ncuc.unit_id = ncu.unit_id " +
                    "LEFT JOIN nmns_content_metrics ncm ON ncu.content_id = ncm.content_id " +
                    "LEFT JOIN nmns_quiz_options nqo ON nqo.question_id = nqq.question_id AND nqo.is_correct = true " +
                    "WHERE ncut.updated_date = :date " +
                    "GROUP BY nqq.question_id")
    List<ItemInfoDTO> findItemInfo(
            @Param("date") String date
    );

    @Query(nativeQuery = true, value =
            "SELECT " +
                    "CONVERT(nq.quiz_id, CHAR) as paperSn, " +
                    "CONVERT(qq.question_id, CHAR) as itemSn, " +
                    "CONVERT(ncuc.unit_content_id, CHAR) as materialSn, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_subject' THEN ncm.metrics_val END), '') as subject, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_code' THEN ncm.metrics_val END), '') as node, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'grade_stage' THEN ncm.metrics_val END), '') as gradeStage, " +
                    "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_competence' THEN ncm.metrics_val END), '') as coreCompetence, " +
                    "100 as fullScore, " +
                    "nc.created_at as creationTimestamp, " +
                    "ncut.updated_at as updateTimestamp " +
                    "FROM nmns_content_updated_time ncut " +
                    "INNER JOIN nmns_content nc ON ncut.content_id = nc.content_id " +
                    "INNER JOIN nmns_quiz nq ON ncut.content_id = nq.content_id " +
                    "LEFT JOIN nmns_quiz_questions qq ON nq.quiz_id = qq.quiz_id " +
                    "INNER JOIN nmns_course_units_content ncuc ON nq.quiz_id = ncuc.quiz_id " +
                    "INNER JOIN nmns_course_units ncu ON ncuc.unit_id = ncu.unit_id " +
                    "LEFT JOIN nmns_content_metrics ncm ON ncu.content_id = ncm.content_id " +
                    "WHERE ncut.updated_date = :date " +
                    "GROUP BY qq.question_id")
    List<PaperDTO> findPaper(
            @Param("date") String date
    );
}
