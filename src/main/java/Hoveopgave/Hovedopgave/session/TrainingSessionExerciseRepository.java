package Hoveopgave.Hovedopgave.session;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TrainingSessionExerciseRepository extends JpaRepository<TrainingSessionExercise, Long> {

	@Query("""
			select distinct sessionExercise from TrainingSessionExercise sessionExercise
			join fetch sessionExercise.session session
			left join fetch sessionExercise.sets
			where sessionExercise.exercise.id = :exerciseId
			order by session.startedAt asc, sessionExercise.orderIndex asc
			""")
	List<TrainingSessionExercise> findProgressionByExerciseId(@Param("exerciseId") Long exerciseId);

	@Modifying
	@Transactional
	@Query("update TrainingSessionExercise exercise set exercise.exercise = null where exercise.exercise.id = :exerciseId")
	void clearExerciseReferences(@Param("exerciseId") Long exerciseId);
}
