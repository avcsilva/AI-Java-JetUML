package br.uefs.umleditor.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.uefs.umleditor.command.MoveNodesOperation;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.GridSnap;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.model.builder.DiagramBuilder;
import br.uefs.umleditor.model.nodes.NodeFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Handles mouse and keyboard gestures on the drawing area: node creation
 * (RF11), selection by click, Ctrl+click and lasso (RF32), moving nodes with
 * grid snapping (RF36) and attaching or detaching nodes with Shift (RF15).
 */
public class CanvasController {

    private final DiagramTab tab;
    private final DiagramBuilder builder;
    private final SelectionModel selection;
    private final DiagramCanvas canvas;
    private final ToolPalette palette;

    private List<Node> draggedNodes = List.of();
    private Point dragStart;
    private Point anchorStart;
    private double movedX;
    private double movedY;

    private Point lassoStart;
    private List<Node> selectionBeforeLasso = List.of();

    private boolean shiftDown;

    public CanvasController(DiagramTab tab, DiagramBuilder builder, SelectionModel selection, DiagramCanvas canvas,
                            ToolPalette palette) {
        this.tab = tab;
        this.builder = builder;
        this.selection = selection;
        this.canvas = canvas;
        this.palette = palette;
        canvas.getCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        canvas.getCanvas().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        canvas.getCanvas().addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        canvas.getCanvas().addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        canvas.getCanvas().addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
    }

    private void mousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        canvas.getCanvas().requestFocus();
        Point point = canvas.toDiagramPoint(event);
        ToolType tool = palette.getSelectedTool();
        if (tool == ToolType.SELECT) {
            startSelection(point, event.isShortcutDown());
        } else {
            NodeFactory.create(tool).ifPresent(node -> createNode(node, point));
        }
        canvas.refresh();
    }

    private void createNode(Node node, Point point) {
        builder.createNode(node, point).ifPresent(operation -> {
            tab.perform(operation);
            selection.set(node);
        });
    }

    private void startSelection(Point point, boolean toggle) {
        Optional<Node> hit = builder.findNodeAt(point);
        if (hit.isEmpty()) {
            if (!toggle) {
                selection.clear();
            }
            lassoStart = point;
            selectionBeforeLasso = selection.getSelected();
            return;
        }
        Node node = hit.get();
        if (toggle) {
            selection.toggle(node);
            return;
        }
        if (!selection.contains(node)) {
            selection.set(node);
        }
        draggedNodes = DiagramBuilder.movableNodes(selection.getSelected());
        if (!draggedNodes.isEmpty()) {
            dragStart = point;
            Node anchor = draggedNodes.contains(node) ? node : draggedNodes.get(0);
            anchorStart = new Point(anchor.getX(), anchor.getY());
            draggedNodes = moveAnchorFirst(draggedNodes, anchor);
            movedX = 0;
            movedY = 0;
        }
    }

    private static List<Node> moveAnchorFirst(List<Node> nodes, Node anchor) {
        ArrayList<Node> ordered = new ArrayList<>(nodes);
        ordered.remove(anchor);
        ordered.add(0, anchor);
        return ordered;
    }

    private void mouseDragged(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        Point point = canvas.toDiagramPoint(event);
        if (dragStart != null) {
            dragNodes(point);
        } else if (lassoStart != null) {
            Rectangle region = Rectangle.fromCorners(lassoStart, point);
            canvas.setLasso(region);
            selection.setAll(selectionBeforeLasso);
            selection.addAll(builder.findNodesInside(region));
        }
        canvas.refresh();
    }

    /**
     * Moves the dragged nodes so that the node under the mouse stays aligned
     * to the grid, keeping everything inside the diagram.
     */
    private void dragNodes(Point point) {
        Node anchor = draggedNodes.get(0);
        double targetX = GridSnap.snap(anchorStart.x() + point.x() - dragStart.x());
        double targetY = GridSnap.snap(anchorStart.y() + point.y() - dragStart.y());
        translateDragged(targetX - anchor.getX(), targetY - anchor.getY());
        Point correction = builder.correctionToKeepInside(draggedNodes);
        translateDragged(correction.x(), correction.y());
    }

    private void translateDragged(double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        for (Node node : draggedNodes) {
            node.translate(dx, dy);
        }
        movedX += dx;
        movedY += dy;
    }

    private void mouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (dragStart != null && (movedX != 0 || movedY != 0)) {
            tab.record(new MoveNodesOperation(draggedNodes, movedX, movedY));
        }
        dragStart = null;
        draggedNodes = List.of();
        lassoStart = null;
        canvas.setLasso(null);
        canvas.refresh();
    }

    private void keyPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.SHIFT || shiftDown) {
            return;
        }
        shiftDown = true;
        if (palette.getSelectedTool() == ToolType.SELECT && !selection.isEmpty()) {
            builder.toggleParent(selection.getSelected()).ifPresent(operation -> {
                tab.perform(operation);
                canvas.refresh();
            });
        }
    }

    private void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) {
            shiftDown = false;
        }
    }
}
