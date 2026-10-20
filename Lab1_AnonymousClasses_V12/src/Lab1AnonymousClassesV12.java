import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Lab1AnonymousClassesV12 {
    interface Checker<T> {
        boolean check(T item);
    }

    interface Formatter<T> {
        String format(T item);
    }

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

    public static void main(String[] args) {
        List<City> cities = Arrays.asList(
                new City("Ростов-на-Дону", "областной центр", 1142162,
                        new Park("Парк имени Горького", "городской", 19.5, true, LocalDate.of(1928, 8, 12))),
                new City("Сочи", "курортный город", 466078,
                        new Park("Олимпийский парк", "спортивный", 200.0, true, LocalDate.of(2014, 2, 7))),
                new City("Шахты", "город областного значения", 226452,
                        new Park("Парк Революции", "культурный", 17.0, false, LocalDate.of(1926, 5, 1))));

        final int largeCityLimit = 500000;

        Checker<City> largeCityChecker = new Checker<City>() {
            @Override
            public boolean check(City city) {
                return city.getPopulation() >= largeCityLimit;
            }
        };

        Checker<City> waterParkChecker = new Checker<City>() {
            @Override
            public boolean check(City city) {
                return city.getPark().hasWater();
            }
        };

        Formatter<City> cityFormatter = new Formatter<City>() {
            @Override
            public String format(City city) {
                String size = largeCityChecker.check(city) ? "крупный город" : "средний город";
                String water = waterParkChecker.check(city) ? "есть водоем" : "водоема нет";
                return String.format("%s (%s): население %d, %s; парк \"%s\", тип %s, площадь %.1f га, %s",
                        city.getName(),
                        city.getStatus(),
                        city.getPopulation(),
                        size,
                        city.getPark().getName(),
                        city.getPark().getType(),
                        city.getPark().getArea(),
                        water);
            }
        };

        System.out.println("=== Вариант 12. Анонимные классы: города и парки ===");
        for (City city : cities) {
            System.out.println(cityFormatter.format(city));
        }
    }
}
