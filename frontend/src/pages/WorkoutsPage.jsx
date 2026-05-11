function WorkoutsPage() {
  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">Workouts</h2>
        <p className="text-secondary mb-3">
          Her skal brugeren kunne oprette færdige workouts, for eksempel Upper Body Day,
          og tilføje flere øvelser til dem.
        </p>
        <ul className="mb-0">
          <li>Opret workout template</li>
          <li>Tilføj øvelser fra øvelseslisten</li>
          <li>Angiv target sets og reps pr. øvelse</li>
          <li>Brug workouten som udgangspunkt for en aktiv session</li>
        </ul>
      </div>
    </section>
  )
}

export default WorkoutsPage
