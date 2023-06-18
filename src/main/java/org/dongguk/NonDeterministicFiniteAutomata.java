package org.dongguk;


import lombok.Getter;
import lombok.Setter;
import org.dongguk.common.State;
import org.dongguk.common.StatePair;

import java.util.*;

@Getter
@Setter
public class NonDeterministicFiniteAutomata {
    private int restate = 0;
    private String regExByPostFix;

    private StatePair startPair;
    private List<Character> symbols;
    private List<Integer> endIndexes;

    public NonDeterministicFiniteAutomata() {
        this.regExByPostFix = null;
        this.startPair = null;
        this.symbols = new ArrayList<>();
        this.endIndexes = new ArrayList<>();
    }

    public void setRegEx(String regEx) {
        Set<Character> temp_symbols = new HashSet<>();

        // Terminal Symbol Find
        for (int i = 0; i < regEx.length(); i++) {
            if (!isSymbol(regEx.charAt(i)))
                continue;
            temp_symbols.add(regEx.charAt(i));
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

        regExByPostFix = postfix(addJoinSymbolToRegEx(regEx));
    }

    public void re2nfa() {
        StatePair temp, right, left = null;

        Stack<StatePair> stack = new Stack<>();
        for (char c : regExByPostFix.toCharArray()) {
            switch (c) {
                case '*' -> {
                    temp = stack.pop();
                    this.startPair = Constructor.constructStarClosure(temp);
                    stack.push(startPair);
                }
                case '+' -> {
                    right = stack.pop();
                    left = stack.pop();
                    this.startPair = Constructor.constructNfaForOR(left, right);
                    stack.push(startPair);
                }
                case '•' -> {
                    right = stack.pop();
                    left = stack.pop();
                    this.startPair = Constructor.constructNfaForConnector(left, right);
                    stack.push(startPair);
                }
                default -> {
                    this.startPair = Constructor.constructNfaForSingleCharacter(c);
                    stack.push(startPair);
                }
            }
        }
    }

    private String addJoinSymbolToRegEx(String regEx) {
        int length = regEx.length();
        if(length==1) {
            return regEx;
        }
        int return_string_length = 0;
        char return_string[] = new char[2 * length + 2];
        char first, second = '0';
        for (int i = 0; i < length - 1; i++) {
            first = regEx.charAt(i);
            second = regEx.charAt(i + 1);
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

        return rString;
    }

    private String postfix(String regExByJoin) {
        StringBuilder temp = new StringBuilder();
        Stack<Character> stk = new Stack<>();
        for (int i = 0; i < regExByJoin.length(); i++) {
            char currentChar = regExByJoin.charAt(i);

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



    public void print() {
        relocationStateIndex(startPair.getStartNode());
        relocationStateVisited(startPair.getStartNode());


        System.out.print("StateSet = { ");
        for (int i = 0; i < restate; i++) {
            String stateName = "q" + String.format("%03d", i);
            if (i != restate - 1)
                System.out.print(stateName + ", ");
            else
                System.out.println(stateName + " }");
        }

        System.out.print("TerminalSet = { ");
        symbols.forEach(symbol -> {
            if (symbol != symbols.get(symbols.size() - 1))
                System.out.print(symbol + ", ");
            else
                System.out.println(symbol + " }");
        });

        System.out.println("DeltaFunctions = { ");
        printState(startPair.getStartNode());
        System.out.println("}");

        System.out.println("StartState = { q" + String.format("%03d", startPair.getStartNode().getIndex()) + " }");
        System.out.print("FinalStateSet = { ");
        for (int endIndex : endIndexes) {
            String endStateName = "q" + String.format("%03d", endIndex);
            if (endIndex != endIndexes.get(endIndexes.size() - 1))
                System.out.print(endStateName + ", ");
            else
                System.out.println(endStateName + " }");
        }
        relocationStateVisited(startPair.getStartNode());
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

    private void relocationStateIndex(State state) {
        if (state == null || state.isVisited()) {
            return;
        }

        state.setVisited(true);
        state.setIndex(restate++);

        if (state.isEnd())
            endIndexes.add(state.getIndex());

        for (State nextState : state.getStates())
            relocationStateIndex(nextState);
    }
    private void relocationStateVisited(State state) {
        if (state == null || !state.isVisited()) {
            return;
        }
        state.setVisited(false);
        for (State nextState : state.getStates())
            relocationStateVisited(nextState);
    }

    private void printState(State state) {
        if (state == null || state.isVisited()) {
            return;
        }

        state.setVisited(true);

        printNfaNode(state);
        for (State nextState : state.getStates())
            printState(nextState);
    }

    private void printNfaNode(State state) {
        List<State> states = state.getStates();
        List<Integer> edges = state.getEdges();

        // 현재 존재하는 edge 모두 검색
        Set<Integer> edgeIndexes = new HashSet<>(edges);

        // edge List 추가
        Map<Integer, List<Integer>> edgeMap = new HashMap<>();
        for (int index : edgeIndexes)
            edgeMap.put(index, new ArrayList<>());

        // edge 마다 다음 State 추가
        for (int i = 0; i < states.size(); i++) {
            edgeMap.get(edges.get(i)).add(states.get(i).getIndex());
        }

        for (int key : edgeMap.keySet()) {
            String stateName = "q" + String.format("%03d", state.getIndex());
            String arcName = (key == -1 ? "ε" : String.valueOf((char) key));
            System.out.print("\t(" + stateName + ", " + arcName + ") = { ");

            List<Integer> nextStates = edgeMap.get(key);
            for (int nextState : nextStates) {
                String nextStateName = "q" + String.format("%03d", nextState);
                if (nextState != nextStates.get(nextStates.size() - 1))
                    System.out.print(nextStateName + ", ");
                else
                    System.out.println(nextStateName + " }");
            }
        }
    }

    private static class Constructor {
        private static int stateIndex = 0;

        public static StatePair constructStarClosure(StatePair pairIn) {
            // State 2개 추가
            StatePair pairOut = new StatePair(new State(stateIndex++), new State(stateIndex++));

            // Arc 4개 추가
            pairOut.getStartNode().makeEdge(pairIn.getStartNode(), State.EPSILON);
            pairIn.getEndNode().makeEdge(pairOut.getEndNode(), State.EPSILON);

            pairOut.getStartNode().makeEdge(pairOut.getEndNode(), State.EPSILON);
            pairIn.getEndNode().makeEdge(pairIn.getStartNode(), State.EPSILON);

            pairIn.getEndNode().setEnd(false);
            pairOut.getEndNode().setEnd(true);

            return pairOut;
        }

        public static StatePair constructNfaForOR(StatePair leftPair, StatePair rightPair) {
            // State 2개 추가
            StatePair pairOut = new StatePair(new State(stateIndex++), new State(stateIndex++));

            // Arc 4개 추가
            pairOut.getStartNode().makeEdge(leftPair.getStartNode(), State.EPSILON);
            pairOut.getStartNode().makeEdge(rightPair.getStartNode(), State.EPSILON);

            leftPair.getEndNode().makeEdge(pairOut.getEndNode(), State.EPSILON);
            rightPair.getEndNode().makeEdge(pairOut.getEndNode(), State.EPSILON);

            // 종결 상태 변경
            leftPair.getEndNode().setEnd(false);
            rightPair.getEndNode().setEnd(false);
            pairOut.getEndNode().setEnd(true);

            return pairOut;
        }

        public static StatePair constructNfaForConnector(StatePair leftPair, StatePair rightPair) {
            // State 0개, Arc 1개 추가
            StatePair pairOut = new StatePair(leftPair.getStartNode(), rightPair.getEndNode());

            // Arc 1개 추가
            leftPair.getEndNode().makeEdge(rightPair.getStartNode(), State.EPSILON);

            // 종결 상태 변경
            leftPair.getEndNode().setEnd(false);
            rightPair.getEndNode().setEnd(true);

            return pairOut;
        }

        public static StatePair constructNfaForSingleCharacter(char c) {
            // State 2개 추가
            StatePair pairOut = new StatePair(new State(stateIndex++), new State(stateIndex++));

            // Arc 1개 추가
            pairOut.getStartNode().makeEdge(pairOut.getEndNode(), c);

            // 종결 상태 변경
            pairOut.getEndNode().setEnd(true);

            return pairOut;
        }
    }
}
