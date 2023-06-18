package org.dongguk.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class State {
    public static final int EPSILON = -1;

    private List<State> states;
    private List<Integer> edges;

    private int index;
    private boolean isVisited;

    private boolean isEnd;

    public State(int index) {
        this.states = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.index = index;
        this.isVisited = false;
        this.isEnd = false;
    }

    public void makeEdge(State nextState, int edge) {
        this.states.add(nextState);
        this.edges.add(edge);
    }
}
