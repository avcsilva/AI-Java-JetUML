package br.uefs.umleditor.ui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.uefs.umleditor.model.Node;

/**
 * The elements currently selected in a diagram (RF32, RF33).
 */
public class SelectionModel {

    private final Set<Node> selected = new LinkedHashSet<>();

    public List<Node> getSelected() {
        return List.copyOf(selected);
    }

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public boolean contains(Node node) {
        return selected.contains(node);
    }

    public void clear() {
        selected.clear();
    }

    /**
     * Replaces the selection with a single element.
     */
    public void set(Node node) {
        selected.clear();
        selected.add(node);
    }

    public void setAll(Collection<Node> nodes) {
        selected.clear();
        selected.addAll(nodes);
    }

    public void addAll(Collection<Node> nodes) {
        selected.addAll(nodes);
    }

    /**
     * Adds the element if it is not selected, or removes it otherwise (Ctrl+click).
     */
    public void toggle(Node node) {
        if (!selected.remove(node)) {
            selected.add(node);
        }
    }
}
