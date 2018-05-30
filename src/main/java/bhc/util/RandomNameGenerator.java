package bhc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for generating better random names
 *
 * Created by MVW on 5/28/2018.
 */
public class RandomNameGenerator {

    private final List<String> FIRST_NAMES = new ArrayList<>();
    private final List<String> LAST_NAMES = new ArrayList<>();

    public RandomNameGenerator() {
        try (InputStream in = getClass().getResourceAsStream("/firstNames.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))
        ) {
            String name = reader.readLine();
            while (name != null) {
                FIRST_NAMES.add(name);
                name = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream in = getClass().getResourceAsStream("/lastNames.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))
        ) {
            String name = reader.readLine();
            while (name != null) {
                LAST_NAMES.add(name);
                name = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String randomName() {
        int firstNameIndex = (int)(Math.random() * FIRST_NAMES.size());
        int lastNameIndex = (int)(Math.random() * LAST_NAMES.size());
        return FIRST_NAMES.get(firstNameIndex) + "_" + LAST_NAMES.get(lastNameIndex);
    }

    public static void main(String[] args) {
        RandomNameGenerator randomNameGenerator = new RandomNameGenerator();
        for (int i = 0; i < 100; i++) {
            System.out.println(randomNameGenerator.randomName());
        }
    }
}
