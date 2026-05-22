import { useEffect, useState } from 'react'
import { Trash2 } from 'lucide-react'
import { deleteTrainingSession, getTrainingSessions } from '../services/api'

function HistoryPage() {
  const [state, setState] = useState({
    loading: true,
    sessions: [],
    error: null,
  })
  const [confirmingDeleteId, setConfirmingDeleteId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [deleteError, setDeleteError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    async function loadSessions() {
      try {
        const sessions = await getTrainingSessions(controller.signal)
        setState({ loading: false, sessions, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setState({ loading: false, sessions: [], error: error.message })
        }
      }
    }

    loadSessions()
    return () => controller.abort()
  }, [])

  function formatDate(value) {
    return new Intl.DateTimeFormat('en-GB', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value))
  }

  function formatDuration(seconds) {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60

    if (minutes === 0) {
      return `${remainingSeconds} sec`
    }

    return `${minutes} min ${remainingSeconds} sec`
  }

  async function handleDelete(session) {
    setDeleteError(null)
    setDeletingId(session.id)

    try {
      await deleteTrainingSession(session.id)
      setState((current) => ({
        ...current,
        sessions: current.sessions.filter((item) => item.id !== session.id),
      }))
      setConfirmingDeleteId(null)
    } catch (error) {
      setDeleteError(error.message)
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">History</h2>
        <p className="text-secondary mb-3">
          Previous training sessions with exercises, notes, and set data.
        </p>

        {state.loading && <p className="mb-0">Loading history...</p>}

        {state.error && (
          <>
            <p className="status-pill error mb-2">Offline</p>
            <p className="text-danger mb-0">{state.error}</p>
          </>
        )}

        {deleteError && <p className="text-danger mb-3">{deleteError}</p>}

        {!state.loading && !state.error && state.sessions.length === 0 && (
          <p className="mb-0">No saved sessions yet.</p>
        )}

        {!state.loading && !state.error && state.sessions.length > 0 && (
          <div className="d-grid gap-3">
            {state.sessions.map((session) => (
              <article className="border rounded p-3" key={session.id}>
                <div className="d-flex flex-column flex-md-row justify-content-md-between gap-2 mb-3">
                  <div>
                    <h3 className="h5 mb-1">{session.templateName}</h3>
                    <p className="text-secondary mb-0">{formatDate(session.startedAt)}</p>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <span className="text-secondary small">
                      {formatDuration(session.durationSeconds)}
                    </span>
                    {confirmingDeleteId === session.id ? (
                      <>
                        <button
                          className="btn btn-sm btn-danger"
                          disabled={deletingId === session.id}
                          onClick={() => handleDelete(session)}
                          type="button"
                        >
                          {deletingId === session.id ? 'Deleting...' : 'Confirm'}
                        </button>
                        <button
                          className="btn btn-sm btn-outline-secondary"
                          disabled={deletingId === session.id}
                          onClick={() => setConfirmingDeleteId(null)}
                          type="button"
                        >
                          Cancel
                        </button>
                      </>
                    ) : (
                      <button
                        aria-label={`Delete session from ${formatDate(session.startedAt)}`}
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => setConfirmingDeleteId(session.id)}
                        title="Delete session"
                        type="button"
                      >
                        <Trash2 aria-hidden="true" size={16} />
                      </button>
                    )}
                  </div>
                </div>

                <div className="d-grid gap-2">
                  {session.exercises.map((exercise) => (
                    <div className="border rounded px-2 py-1" key={exercise.id}>
                      <div className="d-flex align-items-center gap-2 flex-wrap mb-1">
                        <span className="text-secondary small">#{exercise.orderIndex}</span>
                        <span className="fw-semibold">{exercise.exerciseName}</span>
                        <span className="text-secondary small">{exercise.muscleGroup}</span>
                      </div>

                      {exercise.note && (
                        <p className="text-secondary small mb-1">{exercise.note}</p>
                      )}

                      <div className="d-flex flex-wrap gap-2">
                        {exercise.sets.map((set) => (
                          <span className="border rounded px-2 py-1 small" key={set.id}>
                            Set {set.setNumber}: {set.weight ?? '-'} kg x {set.reps}
                          </span>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

export default HistoryPage
