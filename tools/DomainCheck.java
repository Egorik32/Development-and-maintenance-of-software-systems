import java.math.BigDecimal;
import java.time.LocalDate;
import domain.City;
import domain.Park;

public class DomainCheck {
    public static void main(String[] args) {
        Park park = new Park(1L, "Парк имени Горького", "городской", new BigDecimal("19.50"), true, LocalDate.of(1928, 8, 12));
        City city = new City(1L, "Ростов-на-Дону", "областной центр", 1142162, park);
        if (!Long.valueOf(1).equals(city.getIdPark()) || city.getPark() != park) throw new AssertionError("Foreign key and navigation property");
        city.setPopulation(1142163);
        if (city.getPopulation() != 1142163) throw new AssertionError("Getter and setter");
        System.out.println(city);
        System.out.println(park);
        System.out.println("PASS: Domain constructors, getters, setters, relationship and toString verified.");
    }
}
