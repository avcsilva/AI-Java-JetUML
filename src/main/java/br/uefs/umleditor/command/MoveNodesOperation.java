package br.uefs.umleditor.command;

import java.util.List;

import br.uefs.umleditor.model.Node;

/**
 * Moves nodes, with their children, by the same displacement.
 */
public class MoveNodesOperation implements DiagramOperation {

    private final List<Node> nodes;
    private final double dx;
    private final double dy;

    public MoveNodesOperation(List<Node> nodes, double dx, double dy) {
        this.nodes = List.copyOf(nodes);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        for (Node node : nodes) {
            node.translate(dx, dy);
        }
    }

    @Override
    public void undo() {
        for (Node node : nodes) {
            node.translate(-dx, -dy);
        }
    }
}
