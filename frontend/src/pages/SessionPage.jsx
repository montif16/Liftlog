import { useEffect, useState } from 'react'
import { getWorkoutTemplates } from '../services/api'

function SessionPage() {
  const [state, setState] = useState({
    loading: true,
    templates: [],
    error: null,
  })
  const [activeSession, setActiveSession] = useState(null)
  const [expandedExerciseIds, setExpandedExerciseIds] = useState([])
  const [noteExerciseIds, setNoteExerciseIds] = useState([])

  useEffect(() => {
    const controller = new AbortController()

    async function loadTemplates() {
      try {
        const templates = await getWorkoutTemplates(controller.signal)
        setState({ loading: false, templates, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setState({ loading: false, templates: [], error: error.message })
        }
      }
    }

    loadTemplates()
    return () => controller.abort()
  }, [])

  function handleStartSession(template) {
    const exercises = [...template.items]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map((item) => ({
        templateItemId: item.id,
        exerciseId: item.exerciseId,
        exerciseName: item.exerciseName,
        muscleGroup: item.muscleGroup,
        targetSets: item.targetSets,
        targetReps: item.targetReps,
        note: '',
        sets: Array.from({ length: item.targetSets }, (_, index) => ({
          setNumber: index + 1,
          weight: '',
          reps: item.targetReps,
        })),
      }))

    setActiveSession({
      templateId: template.id,
      templateName: template.name,
      startedAt: new Date().toISOString(),
      exercises,
    })
    setExpandedExerciseIds([])
    setNoteExerciseIds([])
  }

  function handleSetChange(templateItemId, setNumber, field, value) {
    setActiveSession((current) => ({
      ...current,
      exercises: current.exercises.map((exercise) =>
        exercise.templateItemId === templateItemId
          ? {
              ...exercise,
              sets: exercise.sets.map((set) =>
                set.setNumber === setNumber ? { ...set, [field]: value } : set,
              ),
            }
          : exercise,
      ),
    }))
  }

  function handleToggleExerciseDetails(templateItemId) {
    setExpandedExerciseIds((current) =>
      current.includes(templateItemId)
        ? current.filter((id) => id !== templateItemId)
        : [...current, templateItemId],
    )
  }

  function handleToggleExerciseNote(templateItemId) {
    setNoteExerciseIds((current) =>
      current.includes(templateItemId)
        ? current.filter((id) => id !== templateItemId)
        : [...current, templateItemId],
    )
  }

  function handleExerciseNoteChange(templateItemId, event) {
    const { value } = event.target
    setActiveSession((current) => ({
      ...current,
      exercises: current.exercises.map((exercise) =>
        exercise.templateItemId === templateItemId ? { ...exercise, note: value } : exercise,
      ),
    }))
  }

  return (
    <section className="row g-3">
      <div className="col-12 col-lg-5">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Start session</h2>
            <p className="text-secondary mb-3">
              Vælg en workout template som udgangspunkt for dagens træning.
            </p>

            {state.loading && <p className="mb-0">Henter workouts...</p>}

            {state.error && (
              <>
                <p className="status-pill error mb-2">Offline</p>
                <p className="text-danger mb-0">{state.error}</p>
              </>
            )}

            {!state.loading && !state.error && state.templates.length === 0 && (
              <p className="mb-0">Opret en workout før du starter en session.</p>
            )}

            {!state.loading && !state.error && state.templates.length > 0 && (
              <div className="d-grid gap-2">
                {state.templates.map((template) => (
                  <article className="border rounded px-2 py-1" key={template.id}>
                    <div className="d-flex align-items-center justify-content-between gap-2">
                      <div className="d-flex align-items-center gap-2 min-width-0">
                        <h3 className="h6 mb-0">{template.name}</h3>
                        <span className="text-secondary small">{template.items.length} øvelser</span>
                      </div>
                      <button
                        className="btn btn-sm btn-dark"
                        disabled={template.items.length === 0}
                        onClick={() => handleStartSession(template)}
                        type="button"
                      >
                        Start
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="col-12 col-lg-7">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Aktiv session</h2>

            {!activeSession && (
              <p className="text-secondary mb-0">
                Vælg en workout for at starte en aktiv session.
              </p>
            )}

            {activeSession && (
              <>
                <p className="text-secondary mb-3">{activeSession.templateName}</p>

                <div className="d-grid gap-2">
                  {activeSession.exercises.map((exercise, index) => {
                    const firstSet = exercise.sets[0]
                    const extraSets = exercise.sets.slice(1)
                    const isExpanded = expandedExerciseIds.includes(exercise.templateItemId)
                    const hasNoteOpen = noteExerciseIds.includes(exercise.templateItemId)

                    return (
                      <div className="border rounded px-2 py-1" key={exercise.templateItemId}>
                        <div className="d-flex align-items-center justify-content-between gap-2 flex-wrap">
                          <div className="d-flex align-items-center gap-2 flex-wrap min-width-0">
                            <span className="text-secondary small">#{index + 1}</span>
                            <span className="fw-semibold">{exercise.exerciseName}</span>
                            <span className="text-secondary small">{exercise.muscleGroup}</span>
                            <span className="text-secondary small">{exercise.targetSets} sets</span>
                          </div>
                          <div className="d-flex align-items-center gap-2">
                            {!isExpanded && (
                              <>
                                <div className="session-weight-field">
                                  <input
                                    className="form-control form-control-sm session-log-input"
                                    inputMode="decimal"
                                    onChange={(event) =>
                                      handleSetChange(
                                        exercise.templateItemId,
                                        firstSet.setNumber,
                                        'weight',
                                        event.target.value,
                                      )
                                    }
                                    placeholder="0"
                                    value={firstSet.weight}
                                  />
                                  <span className="session-input-suffix">kg</span>
                                </div>
                                <input
                                  className="form-control form-control-sm session-log-input"
                                  inputMode="numeric"
                                  onChange={(event) =>
                                    handleSetChange(
                                      exercise.templateItemId,
                                      firstSet.setNumber,
                                      'reps',
                                      event.target.value,
                                    )
                                  }
                                  placeholder="reps"
                                  value={firstSet.reps}
                                />
                              </>
                            )}
                            <button
                              className={`btn btn-sm text-nowrap ${
                                hasNoteOpen ? 'btn-secondary' : 'btn-outline-secondary'
                              }`}
                              onClick={() => handleToggleExerciseNote(exercise.templateItemId)}
                              type="button"
                            >
                              Note
                            </button>
                            {extraSets.length > 0 && (
                              <button
                                className={`btn btn-sm text-nowrap ${
                                  isExpanded ? 'btn-secondary' : 'btn-outline-secondary'
                                }`}
                                onClick={() => handleToggleExerciseDetails(exercise.templateItemId)}
                                type="button"
                              >
                                Special workout
                              </button>
                            )}
                          </div>
                        </div>

                        {hasNoteOpen && (
                          <textarea
                            className="form-control form-control-sm mt-2"
                            maxLength={1000}
                            onChange={(event) =>
                              handleExerciseNoteChange(exercise.templateItemId, event)
                            }
                            placeholder="Note til øvelsen"
                            rows={2}
                            value={exercise.note}
                          />
                        )}

                        {isExpanded && (
                          <div className="d-grid gap-1 mt-1">
                            {exercise.sets.map((set) => (
                              <div
                                className="d-flex align-items-center gap-2"
                                key={`${exercise.templateItemId}-${set.setNumber}`}
                              >
                                <span className="text-secondary small session-set-label">
                                  Set {set.setNumber}
                                </span>
                                <div className="session-weight-field">
                                  <input
                                    className="form-control form-control-sm session-log-input"
                                    inputMode="decimal"
                                    onChange={(event) =>
                                      handleSetChange(
                                        exercise.templateItemId,
                                        set.setNumber,
                                        'weight',
                                        event.target.value,
                                      )
                                    }
                                    placeholder="0"
                                    value={set.weight}
                                  />
                                  <span className="session-input-suffix">kg</span>
                                </div>
                                <input
                                  className="form-control form-control-sm session-log-input"
                                  inputMode="numeric"
                                  onChange={(event) =>
                                    handleSetChange(
                                      exercise.templateItemId,
                                      set.setNumber,
                                      'reps',
                                      event.target.value,
                                    )
                                  }
                                  placeholder="reps"
                                  value={set.reps}
                                />
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )
                  })}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

export default SessionPage
