package org.example.prac2;

import java.util.Scanner;

/**
 * Console entry point for the Agentic AI Shopping Assistant.
 *
 * Two capabilities, both backed by live web search through Claude:
 *   - "search <query>"            -> find real products matching the query
 *   - "compare <A> vs <B> [vs C]" -> compare two or more real products
 *
 * Any other input is treated as a free-form request and Claude decides
 * whether it's a search or a comparison. Type "bye" to exit.
 */
public class ShoppingAssistantMain {

    public static void main(String[] args) {

        ShoppingAgent agent = new ShoppingAgent();

        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("   AI Shopping Assistant");
        System.out.println("=================================");
        System.out.println("Search or compare products.");
        System.out.println("Type 'bye' to exit.");

        while (true) {

            System.out.print("\nUser: ");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("bye")) {
                System.out.println("Agent: Goodbye!");
                break;
            }

            if (input.isBlank()) {
                continue;
            }

            try {

                String response =
                        agent.handleUserRequest(input);

                System.out.println("\nAgent:");
                System.out.println(response);

            } catch (Exception e) {

                System.out.println(
                        "\nAgent: Sorry, something went wrong: "
                                + e.getMessage()
                );
            }
        }

        scanner.close();
    }
}
