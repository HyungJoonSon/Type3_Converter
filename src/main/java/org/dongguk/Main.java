package org.dongguk;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("input a regular expression");
        String regEx = in.nextLine();

        NonDeterministicFiniteAutomata nfa = new NonDeterministicFiniteAutomata();
        nfa.setRegEx(regEx);
        nfa.re2nfa();
        nfa.print();

        DeterministicFiniteAutomata dfa = new DeterministicFiniteAutomata();
        dfa.setInformationByNFA(nfa.getGraph(), nfa.getSymbols());
        dfa.createDFA();
        dfa.printDFA();
    }
}