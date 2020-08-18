package com.charrey.graph;

public class ContractResult {
    private final MyGraph graph;

    public ContractResult(MyGraph contracted) {
        this.graph = contracted;
    }

    public MyGraph getGraph() {
        return graph;
    }
}