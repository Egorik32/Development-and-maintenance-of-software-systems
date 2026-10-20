public class LambdaApp {
    static int x = 10;
    static int y = 20;

    public static void main(String[] args) {
        Operationable operation = (int x, int y) -> {
            if (y == 0)
                return 0;
            else
                return x / y;
        };
        System.out.println(operation.calculate(20, 10));
        System.out.println(operation.calculate(20, 0));
    }
}
