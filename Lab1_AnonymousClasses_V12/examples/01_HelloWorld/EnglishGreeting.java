public class EnglishGreeting implements HelloWorld {
    String name = "world";

    public void greet() {
        greetSomeone("world");
    }

    public void greetSomeone(String someone) {
        name = someone;
        System.out.println("Hello " + name);
    }
}
