package Hoveopgave.Hovedopgave.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "training_sessions")
@Getter
@Setter
@NoArgsConstructor
public class TrainingSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_id", nullable = false)
	private WorkoutTemplate template;

	@Column(nullable = false, length = 120)
	private String templateName;

	@Column(nullable = false)
	private Instant startedAt;

	@Column(nullable = false)
	private Instant endedAt;

	@Column(nullable = false)
	private Long durationSeconds;

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TrainingSessionExercise> exercises = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
