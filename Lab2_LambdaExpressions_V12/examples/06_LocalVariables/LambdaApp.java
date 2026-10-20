public class LambdaApp {
    public static void main(String[] args) {
        int n = 70;
        int m = 30;
        Operation op = () -> {
            // n = 100; is forbidden: a captured local variable must be effectively final.
            return m + n;
        };
        System.out.println(op.calculate());
    }
}
