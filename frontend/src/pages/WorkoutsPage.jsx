import { useEffect, useState } from 'react'
import { getWorkoutTemplates } from '../services/api'

function WorkoutsPage() {
  const [state, setState] = useState({
    loading: true,
    templates: [],
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    async function loadWorkoutTemplates() {
      try {
        const templates = await getWorkoutTemplates(controller.signal)
        setState({ loading: false, templates, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setState({ loading: false, templates: [], error: error.message })
        }
      }
    }

    loadWorkoutTemplates()
    return () => controller.abort()
  }, [])

  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">Workouts</h2>
        <p className="text-secondary mb-3">
          Workouts er færdige templates, for eksempel Upper Body Day, som senere kan
          bruges til at starte en aktiv session.
        </p>

        {state.loading && <p className="mb-0">Henter workouts...</p>}

        {state.error && (
          <>
            <p className="status-pill error mb-2">Offline</p>
            <p className="text-danger mb-0">{state.error}</p>
          </>
        )}

        {!state.loading && !state.error && state.templates.length === 0 && (
          <p className="mb-0">Ingen workouts endnu.</p>
        )}

        {!state.loading && !state.error && state.templates.length > 0 && (
          <div className="d-grid gap-3">
            {state.templates.map((template) => (
              <article className="border rounded p-3" key={template.id}>
                <div className="d-flex flex-column flex-md-row justify-content-md-between gap-2">
                  <div>
                    <h3 className="h5 mb-1">{template.name}</h3>
                    {template.description && (
                      <p className="text-secondary mb-0">{template.description}</p>
                    )}
                  </div>
                  <span className="text-secondary small">
                    {template.items.length} øvelser
                  </span>
                </div>

                {template.items.length > 0 && (
                  <div className="table-responsive mt-3">
                    <table className="table table-sm align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Øvelse</th>
                          <th>Muskelgruppe</th>
                          <th>Sets</th>
                          <th>Reps</th>
                        </tr>
                      </thead>
                      <tbody>
                        {template.items.map((item) => (
                          <tr key={item.id}>
                            <td>{item.exerciseName}</td>
                            <td>{item.muscleGroup}</td>
                            <td>{item.targetSets}</td>
                            <td>{item.targetReps}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

export default WorkoutsPage
