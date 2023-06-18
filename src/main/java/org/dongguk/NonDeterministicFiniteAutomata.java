package org.dongguk;


import org.dongguk.common.Cell;
import org.dongguk.common.Pair;

import java.util.*;

public class NonDeterministicFiniteAutomata {

    private String re;
    private String reJoined;
    private String rePostFix;

    private List<Character> symbols;
    private Pair pair;

    public NonDeterministicFiniteAutomata(String re) {
        this.re = re;
        this.reJoined = null;
        this.rePostFix = null;
        symbols = new ArrayList<>();


        Set<Character> temp_symbols = new HashSet<>();

        // Terminal Symbol Find
        for (int i = 0; i < re.length(); i++) {
            if (!isSymbol(re.charAt(i)))
                continue;
            temp_symbols.add(re.charAt(i));
        }
        symbols.addAll(temp_symbols);
//        symbols.add('ε');

        // Terminal Symbol 출력
        System.out.print("TerminalSet = { ");
        symbols.forEach(symbol -> {
            if (symbol != symbols.get(symbols.size() - 1))
                System.out.print(symbol + ", ");
            else
                System.out.println(symbol + " }");
        });


        reJoined = add_join_symbol();

        rePostFix = postfix();
        System.out.println("add PostFix symbol:" + rePostFix);
    }

    private String add_join_symbol() {
        int length = re.length();
        if(length==1) {
            System.out.println("add join symbol:" + re);
            reJoined = re;
            return re;
        }
        int return_string_length = 0;
        char return_string[] = new char[2 * length + 2];
        char first, second = '0';
        for (int i = 0; i < length - 1; i++) {
            first = re.charAt(i);
            second = re.charAt(i + 1);
            return_string[return_string_length++] = first;
            if (first != '(' && first != '+' && isSymbol(second)) {
                return_string[return_string_length++] = '•';
            }
            else if (second == '(' && first != '+' && first != '(') {
                return_string[return_string_length++] = '•';
            }
        }
        return_string[return_string_length++] = second;
        String rString = new String(return_string, 0, return_string_length);

        reJoined = rString;
        return rString;
    }

    private String postfix() {
        StringBuilder temp = new StringBuilder();
        Stack<Character> stk = new Stack<>();
        for (int i = 0; i < reJoined.length(); i++) {
            char currentChar = reJoined.charAt(i);

            // Symbol 이라면 그대로 출력
            if (isSymbol(currentChar)) {
                temp.append(currentChar);
                continue;
            }

            // Stk 이 비워져있으면 Stack Push 또는 '(' 경우는 우선순위가 가장 높으므로 Push
            if (stk.isEmpty() || currentChar == '(') {
                stk.push(currentChar);
                continue;
            }

            // ')'라면 '(' 만날 때까지 Pop
            if (currentChar == ')') {
                while (!stk.isEmpty() && stk.peek() != '(') {
                    temp.append(stk.pop());
                }
                stk.pop(); // '(' 지우기
            }

            /** 만약 Stack 이 비워져있지 않고
             * [현재의 연산자 우선순위 <= Stack Top 연산자 우선순위]라면,
             * 주석달기
             */
            else {
                while (!stk.isEmpty() && getInComingPriority(currentChar) <= getInComingPriority(stk.peek())) {
                    temp.append(stk.pop());
                }
                stk.push(currentChar);
            }
        }

        while (!stk.isEmpty()) {
            temp.append(stk.pop());
        }

        return temp.toString();
    }

    public void re2nfa() {
        this.pair = new Pair();
        Pair temp = new Pair();
        Pair right, left;
        Constructor constructor = new Constructor();

        Stack<Pair> stack = new Stack<>();
        for (char c : rePostFix.toCharArray()) {
            switch (c) {
                case '*' -> {
                    temp = stack.pop();
                    this.pair = constructor.constructStarClosure(temp);
                    stack.push(pair);
                }
                case '+' -> {
                    right = stack.pop();
                    left = stack.pop();
                    this.pair = constructor.constructNfaForOR(left, right);
                    stack.push(pair);
                }
                case '.' -> {
                    right = stack.pop();
                    left = stack.pop();
                    this.pair = constructor.constructNfaForConnector(left, right);
                    stack.push(pair);
                }
                default -> {
                    this.pair = constructor.constructNfaForSingleCharacter(c);
                    stack.push(pair);
                }
            }
        }
    }

    public void print() {

    }

    // Check Symbol [a-zA-z0-9]
    private boolean isSymbol(char check) {
        return (check >= 'a' && check <= 'z') || (check >= 'A' && check <= 'Z') || (check >= '0' && check <= '9');
    }

    private int getInComingPriority(char c) {
        return switch (c) {
            case '(' -> 0;
            case '+' -> 1;
            case '•' -> 2;
            case '*' -> 3;
            default -> -1;
        };
    }

    private static class Constructor {
        private Manager manager = null;

        public Constructor() {
            this.manager = new Manager();
        }

        public Pair constructStarClosure(Pair pairIn) {
            Pair pairOut = new Pair();
            pairOut.startNode = manager.newNfa();
            pairOut.endNode = manager.newNfa();

            pairOut.startNode.next = pairIn.startNode;
            pairIn.endNode.next = pairOut.endNode;

            pairOut.startNode.next2 = pairOut.endNode;
            pairIn.endNode.next2 = pairIn.startNode;

            pairIn.startNode = pairOut.startNode;
            pairIn.endNode = pairOut.endNode;

            return pairOut;
        }

        public Pair constructNfaForSingleCharacter(char c) {

            Pair pairOut = new Pair();
            pairOut.startNode = manager.newNfa();
            pairOut.endNode = manager.newNfa();
            pairOut.startNode.next = pairOut.endNode;
            pairOut.startNode.setEdge(c);

            return pairOut;
        }

        public Pair constructNfaForOR(Pair left, Pair right) {
            Pair pair = new Pair();
            pair.startNode = manager.newNfa();
            pair.endNode = manager.newNfa();

            pair.startNode.next = left.startNode;
            pair.startNode.next2 = right.startNode;

            left.endNode.next = pair.endNode;
            right.endNode.next = pair.endNode;

            return pair;
        }

        public Pair constructNfaForConnector(Pair left, Pair right) {
            Pair pairOut = new Pair();
            pairOut.startNode = left.startNode;
            pairOut.endNode = right.endNode;

            left.endNode.next = right.startNode;

            return pairOut;
        }
    }

    private static class Manager {
        private final int NFA_MAX = 256;
        private Cell[] nfaStatesArr = null;
        private Stack<Cell> nfaStack = null;
        private int nextAlloc = 0;
        private int nfaStates = 0;

        public Manager()  {
            nfaStatesArr = new Cell[NFA_MAX];
            for (int i = 0; i < NFA_MAX; i++) {
                nfaStatesArr[i] = new Cell();
            }

            nfaStack = new Stack<Cell>();

        }

        public Cell newNfa()  {
            Cell nfa = null;
            if (nfaStack.size() > 0) {
                nfa = nfaStack.pop();
            }
            else {
                nfa = nfaStatesArr[nextAlloc];
                nextAlloc++;
            }

            nfa.clearState();
            nfa.setState(nfaStates++);
            nfa.setEdge(Cell.EPSILON);

            return nfa;
        }

        public void discardNfa(Cell nfaDiscarded) {
            --nfaStates;
            nfaDiscarded.clearState();
            nfaStack.push(nfaDiscarded);
        }
    }
}
