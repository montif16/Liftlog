function HistoryPage() {
  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">History</h2>
        <p className="text-secondary mb-3">
          Historik viser tidligere sessions og løftedata pr. øvelse.
        </p>
        <ul className="mb-0">
          <li>Dato, varighed og template</li>
          <li>Set-data for hver øvelse</li>
          <li>Filtrering på øvelse og periode</li>
        </ul>
      </div>
    </section>
  )
}

export default HistoryPage
