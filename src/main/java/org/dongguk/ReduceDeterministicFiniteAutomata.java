package org.dongguk;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ReduceDeterministicFiniteAutomata {
    private List<List<Integer>> dfa;
    private List<List<Integer>> mfa = new ArrayList<>();
    private List<Character> symbols;
    private List<Integer> endStates;
    private Set<Set<Integer>> statesSet = new HashSet<>();
    private Map<Integer, Integer> map = new HashMap<>();

    public ReduceDeterministicFiniteAutomata() {
        this.mfa = new ArrayList<>();
        this.statesSet = new HashSet<>();
        this.map = new HashMap<>();
    }


    public void setInformationByDFA(List<List<Integer>> dfa,  List<Character> symbols, List<Integer> endState) {
        this.dfa = dfa;
        this.symbols = symbols;
        this.endStates = endState;
    }

    public void printMFA() {
        System.out.println("--------MFA--------");
        System.out.print("StateSet = { ");
//        List<Integer> stateSet = new ArrayList<>();
//        for (int i = 0; i < dfa.size(); i++) {
//            if (map.get(i) == null)
//                stateSet.add(i);
//        }

        for (int i = 0; i < mfa.size(); i++) {
            String stateName = "q``" + String.format("%03d", i);
            if (i != mfa.size() - 1)
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

        List<List<Integer>> mfaList = mfa;
        for (int i = 0; i < mfaList.size(); i++) {
            String stateName = "q``" + String.format("%03d", i);

            List<Integer> mfaLine = mfaList.get(i);
            for (int j = 0; j < mfaLine.size(); j++) {
                if (mfaLine.get(j) == null) {
                    continue;
                }
                String nextName = "q``" + String.format("%03d", mfaLine.get(j));
                System.out.println("\t(" + stateName + ", " + symbols.get(j) + ") = { " + nextName + " }");
            }
        }
        System.out.println("}");
        System.out.println("start state: [0]");
        System.out.println("end state: " + endStates);
        System.out.println("--------MFA--------");
    }

    public void minimize() {
        init(statesSet);
        int count = 0;
        while (true) {
            if (count == statesSet.size())
                break;
            else
                count = 0;
            Set<Set<Integer>> copyOfstatesSet = new HashSet<>(statesSet);
            for (Set<Integer> set : copyOfstatesSet) {
                if (isIndivisible(set)) {
                    count++;
                    continue;
                } else {
                    minimize(set);
                }
            }
        }
    }

    private void minimize(Set<Integer> states) {
        statesSet.remove(states);

        Map <List<Integer>, List<Integer>> map = new HashMap<>();
        for (int state : states) {
            // 다음 (States)를 찾음
            List<Integer> nextStates = new ArrayList<>();
            for (Character symbol : symbols) {
                nextStates.add(moveState(state, symbol));
            }

            // 다음 (States)를 갖는 현재 (States)를 찾음
            List<Integer> currentState = map.get(nextStates);
            if(currentState == null) {
                List<Integer> temp = new ArrayList<>();
                temp.add(state);
                map.put(nextStates, temp);
            }
            else {
                currentState.add(state);
                map.put(nextStates, currentState);
            }
        }

        for(List<Integer> currentStates : map.values()) {
            Set<Integer> set = new HashSet<>(currentStates);
            statesSet.add(set);
        }
    }

    private boolean isEqualMove(Set<Integer> temp) {
        // temp 가 비워져 있다면 둘다 null 로 전이 되니 끝(전이가 안된다는 말)
        if(temp.isEmpty())
            return true;

        // 각 (state)가 속한 (states)의 (index)를 찾는다.
        Set<Integer> indexes = new HashSet<>();
        for (Integer state : temp) {
            indexes.add(getSetIndexInStatesSet(state));
        }
        
        // 같은 (index)로 간다면 그 둘은 같은 전이
        return indexes.size() == 1;
    }

    private int getSetIndexInStatesSet(Integer currentState) {
        int i = 0;
        for (Set<Integer> states : statesSet) {
            for (Integer state : states) {
                if(state.equals(currentState))
                    return i;
            }
            i++;
        }
        return -1;
    }

    private void init(Set<Set<Integer>> statesSet) {
        Set<Integer> finishState = new HashSet<>();
        Set<Integer> nonFinishState = new HashSet<>();
        for (int i = 0; i < dfa.size(); i++) {
            if (isEndState(i))
                finishState.add(i);
            else
                nonFinishState.add(i);
        }
        statesSet.add(nonFinishState);
        statesSet.add(finishState);
    }

    private boolean isEndState(Integer state) {
        for (Integer endState : endStates) {
            if (state.equals(endState))
                return true;
        }
        return false;
    }

    private boolean isIndivisible(Set<Integer> set) {
        if (set.size() == 1)
            return true;

        // 각 심볼에 대해서
        for (Character symbol : symbols) {
            // 전이 시킨 (State)를 담을 임시 Set
            Set<Integer> nextStates = new HashSet<>();

            for (Integer state : set) {
                nextStates.add(moveState(state, symbol));
            }

            // 전이를 시켰을 때, 같은 전이라면 넘어가고 아니라면 false
            if (isEqualMove(nextStates))
                continue;
            else {
                return false;
            }
        }
        return true;
    }

    public void merge(){
        Map<Integer, Integer> indexMap = new HashMap<>();
        int index = 0;
        for (Set<Integer> states : statesSet) {
            if(states.size() == 1) {
                indexMap.put(states.iterator().next(), index++);
                continue;
            }
            else {
                int i = 0;
                int key = 0;
                // 처음을 제외하고는 (state)를 처음 (State)로 Mapping
                for (Integer state : states) {
                    if(i++ == 0) {
                        indexMap.put(state, index++);
                        key = state;
                    }
                    else
                        map.put(state, key);
                }
            }
        }

        // 임시 MFA 작성
        List<List<Integer>> tempMFA = new ArrayList<>();
        for (int i = 0; i < dfa.size(); i++) {
            // 만약 해당 해당 (State)가 합칠 필요가 없다면(즉, States Size == 1)
            if(isSetSizeOverTwo(i)) {
                continue;
            }

            List<Integer> dfaLine = dfa.get(i);
            List<Integer> newNextStates = new ArrayList<>();
            for (Integer nextState : dfaLine) {
                // 상태가 바껴야 한다면
                if(needReplace(nextState))
                    newNextStates.add(indexMap.get(map.get(nextState)));
                // 상태가 바뀌지 않아도 된다면
                else {
                    newNextStates.add(indexMap.get(nextState));
                }
            }
            tempMFA.add(newNextStates);
        }


        List<Integer> finalStates = new ArrayList<>();
        for (int i = 0; i < tempMFA.size(); i++) {
            List<Integer> mfaLine = tempMFA.get(i);

            if (mfaLine.isEmpty() || finalStates.contains(i)) {
                continue;
            } else {
                finalStates.add(i);
                mfa.add(mfaLine);
            }
        }

        makeEndStates(indexMap);
    }
    private boolean needReplace(Integer state) {
        Integer value = map.get(state);
        return value != null || value == state;
    }


    private boolean isSetSizeOverTwo(Integer state) {
        for (Map.Entry<Integer, Integer> m : map.entrySet())  {
            if(m.getKey().equals(state))
                return true;
        }
        return false;
    }

    private Integer moveState(Integer state, Character symbol) {
        List<Integer> dfaLine = dfa.get(state);

        if (dfaLine == null || dfaLine.isEmpty())
            return null;

        int symbolIndex = getSymbolIndex(symbol);
        return dfaLine.get(symbolIndex) != null ? dfaLine.get(symbolIndex) : null;
    }

    private int getSymbolIndex(char input) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i) == input)
                return i;
        }
        return -1;
    }

    private void makeEndStates(Map<Integer, Integer> indexMap) {
        Set<Integer> set = new HashSet<>();
        for (Set<Integer> states : statesSet) {
            for (Integer state : states) {
                for (Integer endState : endStates) {
                    if (!state.equals(endState))
                        continue;
                    if (needReplace(state))
                        set.add(indexMap.get(map.get(state)));
                    else
                        set.add(indexMap.get(state));
                }
            }
        }

        endStates = new ArrayList<>(set);
    }
}
