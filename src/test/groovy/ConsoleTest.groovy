import org.anc.lapps.dsl.TextDevice

/**
 * @author Keith Suderman
 */
class ConsoleTest {

    void run() {
        TextDevice io = TextDevice.create()
        boolean running = true
        while (running) {
            io.printf("Enter something: ")
            String response = io.readLine()
            switch(response) {
                case 'password':
                    io.printf("Enter a password: ")
                    char[] chars = io.readPassword()
                    io.printf("Your password is: %s\n", new String(chars))
                    break
                case 'help':
                    io.printf("Sorry, there is not help available at this time.\n")
                    break
                case 'exit':
                    running = false;
                    break
                default:
                    io.printf("You entered: ${response}\n")
                    break

            }
        }
        println "Done."
    }
    public static void main(args) {
        new ConsoleTest().run()
    }
}

