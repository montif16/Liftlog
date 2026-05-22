import { useEffect, useState } from 'react'
import { getWorkoutTemplates, saveTrainingSession } from '../services/api'

function SessionPage() {
  const [state, setState] = useState({
    loading: true,
    templates: [],
    error: null,
  })
  const [activeSession, setActiveSession] = useState(null)
  const [expandedExerciseIds, setExpandedExerciseIds] = useState([])
  const [noteExerciseIds, setNoteExerciseIds] = useState([])
  const [savingSession, setSavingSession] = useState(false)
  const [saveError, setSaveError] = useState(null)
  const [savedSessionId, setSavedSessionId] = useState(null)
  const [restDurationSeconds, setRestDurationSeconds] = useState(90)
  const [restRemainingSeconds, setRestRemainingSeconds] = useState(90)
  const [restTimerRunning, setRestTimerRunning] = useState(false)

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

  useEffect(() => {
    if (!restTimerRunning) {
      return undefined
    }

    const timerId = window.setInterval(() => {
      setRestRemainingSeconds((current) => {
        if (current <= 1) {
          setRestTimerRunning(false)
          return 0
        }

        return current - 1
      })
    }, 1000)

    return () => window.clearInterval(timerId)
  }, [restTimerRunning])

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
    setSaveError(null)
    setSavedSessionId(null)
    handleResetRestTimer()
  }

  function formatRestTime(seconds) {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60

    return `${minutes}:${String(remainingSeconds).padStart(2, '0')}`
  }

  function handleRestDurationChange(event) {
    const seconds = Number(event.target.value)
    const nextDuration = Number.isNaN(seconds) ? 0 : Math.max(0, seconds)

    setRestDurationSeconds(nextDuration)
    setRestRemainingSeconds(nextDuration)
    setRestTimerRunning(false)
  }

  function handleStartRestTimer() {
    if (restRemainingSeconds === 0) {
      setRestRemainingSeconds(restDurationSeconds)
    }

    setRestTimerRunning(true)
  }

  function handlePauseRestTimer() {
    setRestTimerRunning(false)
  }

  function handleResetRestTimer() {
    setRestTimerRunning(false)
    setRestRemainingSeconds(restDurationSeconds)
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

  async function handleSaveSession() {
    setSaveError(null)
    setSavingSession(true)

    try {
      const savedSession = await saveTrainingSession({
        templateId: activeSession.templateId,
        startedAt: activeSession.startedAt,
        exercises: activeSession.exercises.map((exercise, index) => {
          const useIndividualSets = expandedExerciseIds.includes(exercise.templateItemId)
          const firstSet = exercise.sets[0]

          return {
            exerciseId: exercise.exerciseId,
            orderIndex: index + 1,
            note: exercise.note || null,
            sets: exercise.sets.map((set) => {
              const sourceSet = useIndividualSets ? set : firstSet

              return {
                setNumber: set.setNumber,
                weight: sourceSet.weight === '' ? null : Number(sourceSet.weight.replace(',', '.')),
                reps: Number(sourceSet.reps),
              }
            }),
          }
        }),
      })

      setSavedSessionId(savedSession.id)
    } catch (error) {
      setSaveError(error.message)
    } finally {
      setSavingSession(false)
    }
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

                <div className="border rounded px-2 py-2 mb-3">
                  <div className="d-flex align-items-center justify-content-between gap-2 flex-wrap">
                    <div>
                      <p className="text-secondary small mb-1">Pausetimer</p>
                      <p className="h4 mb-0">{formatRestTime(restRemainingSeconds)}</p>
                    </div>
                    <div className="d-flex align-items-center gap-2 flex-wrap">
                      <input
                        className="form-control form-control-sm session-timer-input"
                        inputMode="numeric"
                        min={0}
                        onChange={handleRestDurationChange}
                        value={restDurationSeconds}
                      />
                      <span className="text-secondary small">sek</span>
                      {restTimerRunning ? (
                        <button
                          className="btn btn-sm btn-outline-secondary"
                          onClick={handlePauseRestTimer}
                          type="button"
                        >
                          Pause
                        </button>
                      ) : (
                        <button
                          className="btn btn-sm btn-dark"
                          disabled={restDurationSeconds === 0}
                          onClick={handleStartRestTimer}
                          type="button"
                        >
                          Start
                        </button>
                      )}
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        onClick={handleResetRestTimer}
                        type="button"
                      >
                        Nulstil
                      </button>
                    </div>
                  </div>
                </div>

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

                {saveError && <p className="text-danger mt-3 mb-0">{saveError}</p>}

                {savedSessionId && (
                  <p className="text-success mt-3 mb-0">Session gemt.</p>
                )}

                <button
                  className="btn btn-dark mt-3"
                  disabled={savingSession || Boolean(savedSessionId)}
                  onClick={handleSaveSession}
                  type="button"
                >
                  {savingSession ? 'Gemmer...' : 'Afslut og gem session'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

export default SessionPage
