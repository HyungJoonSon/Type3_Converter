package org.dongguk;

import lombok.Getter;
import lombok.Setter;
import org.dongguk.common.State;
import org.dongguk.common.StatePair;

import java.util.*;

@Getter
@Setter
public class DeterministicFiniteAutomata {
    private StatePair graph;
    private List<Character> symbols;
    private Map<Set<Integer>, Integer> map;
    private Set<Integer> tempset;
    private List<List<Integer>> dfa;
    private List<Integer> startStates;
    private List<Integer> endStates;
    private int stateIndex = 0;

    public DeterministicFiniteAutomata() {
        this.graph = null;
        this.symbols = null;
        this.map = new HashMap<>();
        this.tempset = null;
        this.dfa = new ArrayList<>();
        this.startStates = new ArrayList<>();
        this.endStates = new ArrayList<>();
    }

    public void setInformationByNFA(StatePair nfaPair, List<Character> symbols) {
        this.graph = nfaPair;
        this.symbols = symbols;
    }

    public List<List<Integer>> getDFA() {
        List<List<Integer>> reduceDfa = new ArrayList<>();
        for (List<Integer> dfaLine : dfa) {
            List<Integer> newDfaLine = new ArrayList<>();
            for (Integer state : dfaLine) {
                if (state == null) {
                    newDfaLine.add(null);
                    continue;
                }

                Set<Integer> set = getSet(state);
                if (set == null || set.isEmpty())
                    newDfaLine.add(null);
                else
                    newDfaLine.add(state);
            }
            reduceDfa.add(newDfaLine);
        }
        return reduceDfa;
    }

    public void printDFA() {
        System.out.println();
        System.out.println("--------DFA--------");
        System.out.print("StateSet = { ");
        for (int i = 0; i < stateIndex; i++) {
            String stateName = "q`" + String.format("%03d", i);
            if (i != stateIndex - 1)
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

        List<List<Integer>> dfalist = getDFA();
        System.out.println("DeltaFunctions = { ");

        for (int i = 0; i < dfa.size(); i++) {
            String stateName = "q`" + String.format("%03d", i);

            List<Integer> dfaLine = dfa.get(i);
            for (int j = 0; j < dfaLine.size(); j++) {
                if (dfaLine.get(j) == null) {
                    continue;
                }
                String nextName = "q`" + String.format("%03d", dfaLine.get(j));
                System.out.println("\t(" + stateName + ", " + symbols.get(j) + ") = { " + nextName + " }");
            }
        }
        System.out.println("}");
        makeStart();
        System.out.print("StartStateSet = { ");
        for (int startIndex : startStates) {
            String endStateName = "q`" + String.format("%03d", startIndex);
            if (startIndex != startStates.get(startStates.size() - 1))
                System.out.print(endStateName + ", ");
            else
                System.out.println(endStateName + " }");
        }
        relocationStateVisited(graph.getStartNode());
        makeEnd();
        System.out.print("FinalStateSet = { ");
        for (int endState : endStates) {
            String endStateName = "q`" + String.format("%03d", endState);
            if (endState != endStates.get(endStates.size() - 1))
                System.out.print(endStateName + ", ");
            else
                System.out.println(endStateName + " }");
        }

        System.out.println("--------DFA--------");
    }

    public void createDFA() {
        tempset = new HashSet<>();
        
        // 시작 심볼에서 갈 수 있는 모든 State 를 찾음
        Set<Integer> startStates = moveState(graph.getStartNode(), State.EPSILON);
        map.put(startStates, stateIndex);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(stateIndex++);

        while(!queue.isEmpty()) {
            List<Integer> dfaLine = new ArrayList<>();

            // 현재 closure 에서 갈 수 있는 State 들(즉, 클로저(상태의 집합) 함수의 출력 값)
            Set<Integer> accessStates = getSet(queue.poll());

            for(int i = 0; i < symbols.size(); i++) {
                tempset = new HashSet<>();

                // (현재 State 에서 갈 수 있는 State 들 중) -> 해당 symbol 로 갈 수 있는 State 들
                Set<Integer> midSet = new HashSet<>();
                for (int accessState : accessStates) {
                    // 시작 State 에서 갈 수 있는 State 인지 확인
                    State state = getState(graph.getStartNode(), accessState);
                    relocationStateVisited();

                    if (state == null)
                        continue;

                    // 갈 수 있는 State 에서 symbol 을 보고 다른 State 로 갈 수 있다면 midSet 에 추가
                    List<Integer> edges = state.getEdges();
                    List<Integer> edgeIndex = new ArrayList<>();

                    for (int j = 0; j < edges.size(); j++) {
                        if (edges.get(j) != (int) symbols.get(i))
                            continue;
                        edgeIndex.add(j);
                    }

                    for (int index : edgeIndex) {
                        midSet.add(state.getStates().get(index).getIndex());
                    }
                }

                // 현재 클루저에서 symbol 을 보고 갈 수 있는 State(midSet)에 대해서
                for (Integer accessState : midSet) {
                    // 시작 State 에서 갈 수 있는 State 인지 확인
                    State state = getState(graph.getStartNode(), accessState);
                    relocationStateVisited();
                    
                    // State 에서 갈 수 있는 모든 State 탐색
                    moveState(state, State.EPSILON);
                }

                // 현재 tempSet 에는 midSet 에서 갈 수 있는 State 의 집합이 있고
                // tempSet 이 기존에 (Map)에 있는지 확인
                Integer tempIndex = getCharacter(tempset);
                
                // 기존에 없는 경우(tempIndex = null)
                if(tempIndex == null) {
                    // 입실론을 타고 갈 수 있는 곳이 없는 경우
                    if(tempset.isEmpty()) {
                        dfaLine.add(null);
                        map.put(tempset, -1);
                    }
                    // 입실론을 타고 갈 수 있는 곳이 있는 경우
                    else {
                        queue.add(stateIndex);
                        dfaLine.add(stateIndex);
                        map.put(tempset, stateIndex++);
                    }
                }
                // 기존에 있는 경우
                else {
                    // 입실론을 타고 못가는 경우
                    if(tempIndex == -1) {
                        dfaLine.add(null);
                    }
                    else {
                        dfaLine.add(tempIndex);
                    }
                }
            }
            dfa.add(dfaLine);
        }
    }

    // moveState, connectState 현재 State 에서 symbol 을 보고 갈 수 있는 모든 State 를 찾음
    private Set<Integer> moveState(State currentState, int symbol) {
        connectState(currentState, symbol);
        relocationStateVisited();
        return tempset;
    }

    private void connectState(State state, int symbol) {
        if(state == null || state.isVisited())
            return;

        state.setVisited(true);
        tempset.add(state.getIndex());

        List<State> nextStates = state.getStates();
        List<Integer> symbols = state.getEdges();
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i) != symbol) {
                continue;
            }
            connectState(nextStates.get(i), symbol);
        }
    }

    // 해당 stateIndex 에 해당하는 클로저 반환
    private Set<Integer> getSet(int stateIndex) {
        for (Map.Entry<Set<Integer>, Integer> m : map.entrySet())  {
            if(m.getValue() == stateIndex)
                return m.getKey();
        }
        return null;
    }

    private State getState(State state, int startState) {
        if (state == null || state.isVisited())
            return null;

        state.setVisited(true);

        if (state.getIndex() == startState)
            return state;
        
        if (state.getIndex() > startState)
            return null;

        List<State> nextStates = state.getStates();

        for (int i = 0; i < nextStates.size(); i++) {
            State temp = getState(nextStates.get(i), startState);
            if (temp != null)
                return temp;
        }

        return null;
    }

    private void makeStart() {
        for (Map.Entry<Set<Integer>, Integer> entry : map.entrySet()) {
            if(entry.getValue() == -1)
                continue;

            for (Integer integer : entry.getKey()) {
                if(integer == graph.getStartNode().getIndex()) {
                    startStates.add((getCharacter(entry.getKey()).intValue()));
                }
            }
        }
    }

    private void makeEnd() {
        for (Map.Entry<Set<Integer>, Integer> entry : map.entrySet()) {
            if(entry.getValue() == -1)
                continue;

            for (Integer integer : entry.getKey()) {
                if(integer == graph.getEndNode().getIndex()) {
                    endStates.add((getCharacter(entry.getKey()).intValue()));
                }
            }
        }
    }

    private Integer getCharacter(Set<Integer> set) {
        return map.get(set);
    }

    private void relocationStateVisited(State state) {
        if (state == null || !state.isVisited()) {
            return;
        }
        state.setVisited(false);
        for (State nextState : state.getStates())
            relocationStateVisited(nextState);
    }

    private void relocationStateVisited() {
        graph.getStartNode().setVisited(false);

        for (State nextState : graph.getStartNode().getStates())
            relocationStateVisited(nextState);
    }
}
