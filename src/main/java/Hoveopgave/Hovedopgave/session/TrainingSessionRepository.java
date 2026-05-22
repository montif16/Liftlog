package Hoveopgave.Hovedopgave.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

	@Modifying
	@Transactional
	@Query("update TrainingSession session set session.template = null where session.template.id = :templateId")
	void clearTemplateReferences(@Param("templateId") Long templateId);
}
