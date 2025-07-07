import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long>
