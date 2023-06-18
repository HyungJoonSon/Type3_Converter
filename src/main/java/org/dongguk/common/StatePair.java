package org.dongguk.common;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatePair {
    private State startNode;
    private State endNode;
}
