package br.uefs.umleditor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.uefs.umleditor.command.ChangePropertyOperation;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.AssociationEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.Directionality;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectCollaborationEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;

/**
 * Fields of the properties panel for each element (RF20, RF22).
 */
class PropertiesTest {

    private static List<String> names(DiagramElement element) {
        return element.getProperties().stream().map(Property::getName).toList();
    }

    @Test
    void nodePropertiesFollowRequirements() {
        assertEquals(List.of("Name", "Attributes", "Methods"), names(new ClassNode()));
        assertEquals(List.of("Name", "Methods"), names(new InterfaceNode()));
        assertEquals(List.of("Name"), names(new PackageNode()));
        assertEquals(List.of("Name", "Contents"), names(new PackageDescriptionNode()));
        assertEquals(List.of("Name"), names(new ObjectNode()));
        assertEquals(List.of("Name", "Value"), names(new FieldNode()));
        assertEquals(List.of("Name"), names(new ActorNode()));
        assertEquals(List.of("Name"), names(new UseCaseNode()));
        assertEquals(List.of("Name"), names(new StateNode()));
        assertEquals(List.of(), names(new InitialStateNode()));
        assertEquals(List.of(), names(new FinalStateNode()));
        assertEquals(List.of("Name"), names(new ImplicitParameterNode()));
        assertEquals(List.of("Text"), names(new NoteNode()));
    }

    @Test
    void edgePropertiesFollowRequirements() {
        assertEquals(List.of("Start Label", "Middle Label", "End Label", "Directionality"),
                names(new AssociationEdge()));
        assertEquals(List.of("Start Label", "Middle Label", "End Label", "Directionality", "Aggregation Type"),
                names(new AggregationEdge(AggregationEdge.Type.COMPOSITION)));
        assertEquals(List.of("Generalization Type"),
                names(new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE)));
        assertEquals(List.of("Directionality"), names(new DependencyEdge()));
        assertEquals(List.of("Middle Label"), names(new StateTransitionEdge()));
        assertEquals(List.of("Middle Label"), names(new ObjectCollaborationEdge()));
        assertEquals(List.of("Dependency Type"), names(new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.NONE)));
        assertEquals(List.of(), names(new NoteEdge()));
    }

    @Test
    void choicesOfferTheValuesOfTheRequirements() {
        Property directionality = new DependencyEdge().getProperties().get(0);
        assertEquals(List.of(Directionality.UNIDIRECTIONAL, Directionality.BIDIRECTIONAL),
                directionality.getChoices());
        Property associationDirectionality = new AssociationEdge().getProperties().get(3);
        assertEquals(3, associationDirectionality.getChoices().size());
        Property dependencyType = new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.INCLUDE).getProperties().get(0);
        assertEquals("[None, Include, Extend]", dependencyType.getChoices().toString());
    }

    @Test
    void aggregationTypeSwitchesBetweenAggregationAndComposition() {
        AggregationEdge edge = new AggregationEdge(AggregationEdge.Type.AGGREGATION);
        Property type = edge.getProperties().get(4);
        type.set(AggregationEdge.Type.COMPOSITION);
        assertEquals(AggregationEdge.Type.COMPOSITION, edge.getType());
    }

    @Test
    void propertyChangeCanBeUndone() {
        ClassNode node = new ClassNode();
        Property name = node.getProperties().get(0);
        ChangePropertyOperation change = new ChangePropertyOperation(name, "", "Order");
        change.execute();
        assertEquals("Order", node.getName());
        change.undo();
        assertTrue(node.getName().isEmpty());
    }
}
