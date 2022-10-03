import dev.lexoland.jda.api.Holder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Scanner;

public class HolderExample {

    public static void main(String[] args) {
        //Create a new JDA instance
        JDA jda = JDABuilder.createDefault(args[0])
                .setHolder(TestHolder::new)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            jda.shutdown();
        }));

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("exit")) {
                    System.exit(0);
                    break;
                }
            }
        }).start();
    }

    public static class TestHolder implements Holder {

        private final Guild guild;

        public TestHolder(Guild g) {
            this.guild = g;
        }

        @Override
        public void onInitialized() {
            System.out.println("Initialized! " + guild.getName());
        }

        @Override
        public void onDestruct() {
            System.out.println("Destructed! " + guild.getName());
        }
    }

}
