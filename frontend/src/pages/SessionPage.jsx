import { useEffect, useState } from 'react'
import { getWorkoutTemplates } from '../services/api'

function SessionPage() {
  const [state, setState] = useState({
    loading: true,
    templates: [],
    error: null,
  })
  const [activeSession, setActiveSession] = useState(null)

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
      }))

    setActiveSession({
      templateId: template.id,
      templateName: template.name,
      startedAt: new Date().toISOString(),
      exercises,
    })
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
                  {activeSession.exercises.map((exercise, index) => (
                    <div className="border rounded px-2 py-1" key={exercise.templateItemId}>
                      <div className="d-flex align-items-center justify-content-between gap-2">
                        <div className="d-flex align-items-center gap-2 min-width-0">
                          <span className="text-secondary small">#{index + 1}</span>
                          <span className="fw-semibold">{exercise.exerciseName}</span>
                          <span className="text-secondary small">{exercise.muscleGroup}</span>
                        </div>
                        <div className="d-flex gap-3 small text-nowrap">
                          <span>
                            <span className="text-secondary">Sets </span>
                            <span className="fw-semibold">{exercise.targetSets}</span>
                          </span>
                          <span>
                            <span className="text-secondary">Reps </span>
                            <span className="fw-semibold">{exercise.targetReps}</span>
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
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
