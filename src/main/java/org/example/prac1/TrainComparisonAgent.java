package org.example.prac1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Interactive console agent that compares Indian trains on a given route.
 *
 * Workflow:
 *   1. Ask the user for a source and destination station.
 *   2. Show the trains available on that route.
 *   3. Ask the user to pick at least two trains to compare.
 *   4. Print a side-by-side comparison (comfort, service, price, availability, speed)
 *      plus a recommended pick.
 *   5. Repeat from step 1 until the user types "bye" at any prompt.
 */
public class TrainComparisonAgent {

    /** A single train option on a route, with the attributes the agent compares on. */
    record Train(
            String name,
            String number,
            String source,
            String destination,
            int comfort,      // 1 (basic) - 5 (excellent)
            int service,       // 1 (basic) - 5 (excellent)
            double seatPrice,  // INR, average across classes
            String availability, // Available / RAC / Waiting List
            int speedKmph,
            double durationHours
    ) {
        boolean runsOn(String src, String dst) {
            return source.equalsIgnoreCase(src) && destination.equalsIgnoreCase(dst);
        }
    }

    private static final List<Train> TRAINS = List.of(
            new Train("Rajdhani Express", "12951", "Delhi", "Mumbai", 4, 4, 2450, "Available", 85, 15.5),
            new Train("Duronto Express", "12213", "Delhi", "Mumbai", 3, 3, 1850, "RAC", 80, 16.0),
            new Train("Mumbai Rajdhani", "12953", "Delhi", "Mumbai", 5, 5, 2900, "Available", 90, 14.5),

            new Train("Howrah Rajdhani", "12301", "Delhi", "Kolkata", 5, 4, 2600, "Available", 88, 17.0),
            new Train("Poorva Express", "12303", "Delhi", "Kolkata", 3, 3, 1600, "Waiting List", 65, 22.5),

            new Train("Vande Bharat Express", "22436", "Delhi", "Bangalore", 5, 5, 3200, "Available", 130, 20.0),
            new Train("Karnataka Express", "12627", "Delhi", "Bangalore", 3, 3, 1750, "RAC", 68, 33.0),

            new Train("Shatabdi Express", "12027", "Chennai", "Bangalore", 4, 4, 950, "Available", 75, 5.0),
            new Train("Lalbagh Express", "12608", "Chennai", "Bangalore", 3, 3, 550, "Available", 60, 6.5),
            new Train("Brindavan Express", "12639", "Chennai", "Bangalore", 3, 2, 480, "Waiting List", 58, 6.0),

            new Train("Deccan Queen", "12123", "Mumbai", "Pune", 4, 4, 380, "Available", 70, 3.25),
            new Train("Pragati Express", "12125", "Mumbai", "Pune", 3, 3, 260, "RAC", 65, 3.5),
            new Train("Indrayani Express", "11301", "Mumbai", "Pune", 2, 3, 180, "Available", 55, 4.0),

            new Train("Shatabdi Express", "12009", "Mumbai", "Ahmedabad", 4, 4, 1150, "Available", 82, 6.5),
            new Train("Gujarat Mail", "19016", "Mumbai", "Ahmedabad", 3, 3, 650, "Waiting List", 62, 9.0),

            new Train("Double Decker Express", "12981", "Delhi", "Jaipur", 3, 4, 520, "Available", 78, 4.5),
            new Train("Ajmer Shatabdi", "12015", "Delhi", "Jaipur", 4, 4, 780, "Available", 90, 4.0),

            new Train("Garib Rath Express", "12216", "Kolkata", "Delhi", 2, 2, 950, "RAC", 70, 17.5),
            new Train("Vande Bharat Express", "22350", "Kolkata", "Delhi", 5, 5, 2950, "Available", 120, 12.5)
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=====================================================");
        System.out.println(" Indian Train Comparison Agent");
        System.out.println(" Compares trains by comfort, service, price,");
        System.out.println(" availability and speed. Type 'bye' any time to quit.");
        System.out.println("=====================================================");

        while (true) {
            String source = ask(scanner, "\nEnter source station: ");
            if (isBye(source)) break;

            String destination = ask(scanner, "Enter destination station: ");
            if (isBye(destination)) break;

            if (source.trim().equalsIgnoreCase(destination.trim())) {
                System.out.println("Source and destination can't be the same. Let's try again.");
                continue;
            }

            List<Train> route = findTrains(source.trim(), destination.trim());
            if (route.isEmpty()) {
                System.out.printf("No trains found from %s to %s in my records. Try another route.%n",
                        source.trim(), destination.trim());
                continue;
            }

            System.out.printf("%nTrains from %s to %s:%n", source.trim(), destination.trim());
            for (int i = 0; i < route.size(); i++) {
                System.out.printf("  [%d] %s (%s)%n", i + 1, route.get(i).name(), route.get(i).number());
            }

            List<Train> selected = selectTrains(scanner, route);
            if (selected == null) break; // user said bye during selection

            printComparison(selected);
        }

        System.out.println("\nGoodbye! Safe travels.");
        scanner.close();
    }

    private static List<Train> findTrains(String source, String destination) {
        List<Train> matches = new ArrayList<>();
        for (Train t : TRAINS) {
            if (t.runsOn(source, destination)) matches.add(t);
        }
        return matches;
    }

    /** Repeats until the user picks at least two valid trains, or types bye. */
    private static List<Train> selectTrains(Scanner scanner, List<Train> route) {
        while (true) {
            String input = ask(scanner,
                    "Select at least 2 trains to compare (comma-separated numbers, e.g. 1,2): ");
            if (isBye(input)) return null;

            String[] parts = input.trim().split(",");
            List<Train> chosen = new ArrayList<>();
            boolean valid = true;

            for (String part : parts) {
                String p = part.trim();
                if (p.isEmpty()) continue;
                try {
                    int idx = Integer.parseInt(p);
                    if (idx < 1 || idx > route.size()) {
                        System.out.printf("'%d' is not a valid option. Pick between 1 and %d.%n", idx, route.size());
                        valid = false;
                        break;
                    }
                    Train candidate = route.get(idx - 1);
                    if (!chosen.contains(candidate)) chosen.add(candidate);
                } catch (NumberFormatException e) {
                    System.out.printf("'%s' is not a number. Please enter train numbers like 1,2.%n", p);
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;
            if (chosen.size() < 2) {
                System.out.println("Please select at least 2 different trains for a comparison.");
                continue;
            }
            return chosen;
        }
    }

    private static void printComparison(List<Train> trains) {
        System.out.println("\n--- Comparison ---");
        System.out.printf("%-22s %-8s %-8s %-8s %-10s %-14s %-10s%n",
                "Train", "Comfort", "Service", "Price", "Availability", "Speed(km/h)", "Score");

        double bestScore = Double.NEGATIVE_INFINITY;
        Train best = null;

        for (Train t : trains) {
            double score = score(t, trains);
            System.out.printf("%-22s %-8s %-8s %-8.0f %-14s %-10s %-10.1f%n",
                    t.name(), stars(t.comfort()), stars(t.service()), t.seatPrice(),
                    t.availability(), t.speedKmph(), score);
            if (score > bestScore) {
                bestScore = score;
                best = t;
            }
        }

        if (best != null) {
            System.out.printf("%nRecommended: %s (%s) - best overall balance of comfort, service, "
                    + "price, availability and speed for this route.%n", best.name(), best.number());
        }
    }

    /** Weighted 0-100 score: comfort 25%, service 20%, availability 25%, speed 15%, price 15% (cheaper is better). */
    private static double score(Train t, List<Train> group) {
        double comfortNorm = t.comfort() / 5.0;
        double serviceNorm = t.service() / 5.0;

        double maxSpeed = group.stream().mapToInt(Train::speedKmph).max().orElse(t.speedKmph());
        double speedNorm = maxSpeed == 0 ? 0 : t.speedKmph() / maxSpeed;

        double minPrice = group.stream().mapToDouble(Train::seatPrice).min().orElse(t.seatPrice());
        double priceNorm = t.seatPrice() == 0 ? 1 : minPrice / t.seatPrice(); // cheaper -> closer to 1

        double availabilityNorm = switch (t.availability().toLowerCase(Locale.ROOT)) {
            case "available" -> 1.0;
            case "rac" -> 0.6;
            case "waiting list" -> 0.3;
            default -> 0.0;
        };

        return (comfortNorm * 0.25 + serviceNorm * 0.20 + availabilityNorm * 0.25
                + speedNorm * 0.15 + priceNorm * 0.15) * 100;
    }

    private static String stars(int rating) {
        return "*".repeat(Math.max(0, rating));
    }

    private static String ask(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : "bye";
    }

    private static boolean isBye(String input) {
        return input != null && input.trim().equalsIgnoreCase("bye");
    }
}
