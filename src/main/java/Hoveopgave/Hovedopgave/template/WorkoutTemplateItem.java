package Hoveopgave.Hovedopgave.template;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workout_template_items")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutTemplateItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_id", nullable = false)
	private WorkoutTemplate template;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	@Column(nullable = false)
	private Integer orderIndex;

	@Column(nullable = false)
	private Integer targetSets;

	@Column(nullable = false)
	private Integer targetReps;
}
