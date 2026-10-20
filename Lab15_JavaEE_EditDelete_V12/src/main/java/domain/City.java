package domain;

public class City {
    private Long id;
    private String name;
    private String status;
    private Integer population;
    private Long idPark;
    private Park park;

    public City() {
    }

    public City(String name, String status, Integer population, Park park) {
        this.name = name;
        this.status = status;
        this.population = population;
        this.park = park;
        this.idPark = park == null ? null : park.getId();
    }

    public City(Long id, String name, String status, Integer population, Park park) {
        this(name, status, population, park);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public Park getPark() {
        return park;
    }

    public void setPark(Park park) {
        this.park = park;
        this.idPark = park == null ? null : park.getId();
    }
    public Long getIdPark() { return idPark; }
    public void setIdPark(Long idPark) { this.idPark = idPark; }
    @Override public String toString() {
        return "City {id=" + id + ", name=" + name + ", status=" + status + ", population=" + population + ", idPark=" + idPark + ", park=" + park + "}";
    }
}
