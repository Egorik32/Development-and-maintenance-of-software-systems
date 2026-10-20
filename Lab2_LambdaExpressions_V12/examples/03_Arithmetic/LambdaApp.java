public class LambdaApp {
    public static void main(String[] args) {
        Operationable operation1 = (int x, int y) -> x + y;
        Operationable operation2 = (int x, int y) -> x - y;
        Operationable operation3 = (int x, int y) -> x * y;
        System.out.println(operation1.calculate(20, 10));
        System.out.println(operation2.calculate(20, 10));
        System.out.println(operation3.calculate(20, 10));
    }
}
