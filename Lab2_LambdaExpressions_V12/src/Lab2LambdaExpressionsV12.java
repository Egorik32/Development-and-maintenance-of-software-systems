import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Lab2LambdaExpressionsV12 {
    static class Park {
        private final String name;
        private final String type;
        private final double area;
        private final boolean hasWater;
        private final LocalDate buildDate;

        Park(String name, String type, double area, boolean hasWater, LocalDate buildDate) {
            this.name = name;
            this.type = type;
            this.area = area;
            this.hasWater = hasWater;
            this.buildDate = buildDate;
        }

        String getName() {
            return name;
        }

        String getType() {
            return type;
        }

        double getArea() {
            return area;
        }

        boolean hasWater() {
            return hasWater;
        }

        LocalDate getBuildDate() {
            return buildDate;
        }
    }

    static class City {
        private final String name;
        private final String status;
        private final int population;
        private final Park park;

        City(String name, String status, int population, Park park) {
            this.name = name;
            this.status = status;
            this.population = population;
            this.park = park;
        }

        String getName() {
            return name;
        }

        String getStatus() {
            return status;
        }

        int getPopulation() {
            return population;
        }

        Park getPark() {
            return park;
        }
    }

    @FunctionalInterface
    interface Metric<T> {
        double calculate(T item);
    }

    public static void main(String[] args) {
        List<City> cities = Arrays.asList(
                new City("Ростов-на-Дону", "областной центр", 1142162,
                        new Park("Парк имени Горького", "городской", 19.5, true, LocalDate.of(1928, 8, 12))),
                new City("Сочи", "курортный город", 466078,
                        new Park("Олимпийский парк", "спортивный", 200.0, true, LocalDate.of(2014, 2, 7))),
                new City("Шахты", "город областного значения", 226452,
                        new Park("Парк Революции", "культурный", 17.0, false, LocalDate.of(1926, 5, 1))));

        Predicate<City> cityWithWaterPark = city -> city.getPark().hasWater();
        Predicate<City> largeCity = city -> city.getPopulation() >= 500000;
        Function<City, String> row = city -> String.format("%s: %s, население %d, парк %s",
                city.getName(), city.getStatus(), city.getPopulation(), city.getPark().getName());
        Metric<City> populationPerParkArea = city -> city.getPopulation() / city.getPark().getArea();

        System.out.println("=== Вариант 12. Лямбда-выражения: города и парки ===");
        cities.stream()
                .filter(cityWithWaterPark)
                .sorted(Comparator.comparing(City::getPopulation).reversed())
                .map(row)
                .forEach(System.out::println);

        System.out.println();
        System.out.println("Крупные города: " + cities.stream()
                .filter(largeCity)
                .map(City::getName)
                .collect(Collectors.joining(", ")));

        System.out.println("Среднее население: " + cities.stream()
                .mapToInt(City::getPopulation)
                .average()
                .orElse(0));

        System.out.println("Нагрузка населения на площадь парка:");
        cities.forEach(city -> System.out.printf("%s: %.2f чел./га%n",
                city.getName(), populationPerParkArea.calculate(city)));

        System.out.println("Самый старый парк: " + cities.stream()
                .map(City::getPark)
                .min(Comparator.comparing(Park::getBuildDate))
                .map(Park::getName)
                .orElse("нет данных"));
    }
}
