package org.example.prac3.approval;


import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class HumanApprovalService {

    private final Scanner scanner = new Scanner(System.in);

    public boolean requestApproval(String message) {

        System.out.println();
        System.out.println("===== HUMAN REVIEW =====");
        System.out.println(message);
        System.out.println();
        System.out.print("Approve fix? (yes/no): ");

        String input = scanner.nextLine();

        return "yes".equalsIgnoreCase(input.trim());
    }
}