public class LambdaApp {
    public static void main(String[] args) {
        Operationable op = new Operationable() {
            public int calculate(int x, int y) {
                return x + y;
            }
        };
        int z = op.calculate(20, 10);
        System.out.println(z);
    }
}
