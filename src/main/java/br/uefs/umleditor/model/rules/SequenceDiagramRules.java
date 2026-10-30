package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;

/**
 * Sequence diagram rules. Only note connectors are created in this diagram
 * for now; calls and returns are checked here once they exist (phase 5).
 * Invalid attempts in sequence diagrams fail silently (RF30).
 */
public class SequenceDiagramRules extends DiagramRules {

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        return ValidationResult.SILENT_FAILURE;
    }
}
