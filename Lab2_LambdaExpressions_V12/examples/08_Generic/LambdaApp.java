public class LambdaApp {
    public static void main(String[] args) {
        Operationable<Integer> operation1 = (x, y) -> x + y;
        Operationable<String> operation2 = (x, y) -> x + y;
        System.out.println(operation1.calculate(20, 10));
        System.out.println(operation2.calculate("20", "10"));
    }
}
