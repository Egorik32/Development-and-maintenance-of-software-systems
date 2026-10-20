public class LambdaApp {
    public static void main(String[] args) {
        Operation func = action(1);
        int a = func.execute(6, 5);
        System.out.println(a);
        int b = action(2).execute(8, 2);
        System.out.println(b);
    }

    private static Operation action(int number) {
        switch (number) {
            case 1: return (x, y) -> x + y;
            case 2: return (x, y) -> x - y;
            case 3: return (x, y) -> x * y;
            default: return (x, y) -> 0;
        }
    }
}
