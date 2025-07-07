import jakarta.persistence.*

@Entity
@Table(name = "places")
data class Place(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isFree: Boolean,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    val city: City
) 