import { useEffect, useState } from 'react'
import { getHealth } from '../services/api'

function HomePage() {
  const [healthState, setHealthState] = useState({
    loading: true,
    data: null,
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    async function loadHealth() {
      try {
        const health = await getHealth(controller.signal)
        setHealthState({ loading: false, data: health, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setHealthState({ loading: false, data: null, error: error.message })
        }
      }
    }

    loadHealth()
    return () => controller.abort()
  }, [])

  return (
    <section className="row g-3">
      <div className="col-12">
        <div className="card shadow-sm">
          <div className="card-body">
            <h2 className="h4 mb-2">Interactive Workout Tracker</h2>
            <p className="text-secondary mb-0">
              Næste milestone er templates, aktiv session, historik og progression.
            </p>
          </div>
        </div>
      </div>

      <div className="col-12 col-lg-6">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h3 className="h6 text-uppercase text-secondary mb-3">Backend status</h3>
            {healthState.loading && <p className="mb-0">Checking API...</p>}
            {healthState.error && (
              <>
                <p className="status-pill error mb-2">Offline</p>
                <p className="text-danger mb-0">{healthState.error}</p>
              </>
            )}
            {healthState.data && (
              <>
                <p className="status-pill ok mb-2">Online</p>
                <div className="small text-secondary">
                  <div>App: {healthState.data.application}</div>
                  <div>Status: {healthState.data.status}</div>
                  <div>Timestamp: {healthState.data.timestamp}</div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="col-12 col-lg-6">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h3 className="h6 text-uppercase text-secondary mb-3">Scope</h3>
            <ul className="mb-0">
              <li>Workout templates</li>
              <li>Aktiv træningssession</li>
              <li>Set/reps/vægt logning</li>
              <li>Rest timer</li>
              <li>Historik og progression</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  )
}

export default HomePage
