package org.dongguk;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("input a regular expression");
        String re = in.nextLine();

        NonDeterministicFiniteAutomata nfa = new NonDeterministicFiniteAutomata(re);
        nfa.re2nfa();
    }
}