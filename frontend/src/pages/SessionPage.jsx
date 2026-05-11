function SessionPage() {
  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">Session</h2>
        <p className="text-secondary mb-3">
          Aktiv session bliver den mest interaktive del af appen.
        </p>
        <ul className="mb-0">
          <li>Start session fra template</li>
          <li>Log set/reps/vægt live</li>
          <li>Rest timer mellem sæt</li>
          <li>Afslut og gem session</li>
        </ul>
      </div>
    </section>
  )
}

export default SessionPage
