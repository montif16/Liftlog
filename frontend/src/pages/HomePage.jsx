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
  )
}

export default HomePage
