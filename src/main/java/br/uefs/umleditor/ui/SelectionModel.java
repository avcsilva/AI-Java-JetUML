package br.uefs.umleditor.ui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.uefs.umleditor.model.DiagramElement;

/**
 * The elements, nodes and edges, currently selected in a diagram (RF32, RF33).
 */
public class SelectionModel {

    private final Set<DiagramElement> selected = new LinkedHashSet<>();

    public List<DiagramElement> getSelected() {
        return List.copyOf(selected);
    }

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public boolean contains(DiagramElement element) {
        return selected.contains(element);
    }

    public void clear() {
        selected.clear();
    }

    /**
     * Replaces the selection with a single element.
     */
    public void set(DiagramElement element) {
        selected.clear();
        selected.add(element);
    }

    public void setAll(Collection<? extends DiagramElement> elements) {
        selected.clear();
        selected.addAll(elements);
    }

    public void addAll(Collection<? extends DiagramElement> elements) {
        selected.addAll(elements);
    }

    /**
     * Adds the element if it is not selected, or removes it otherwise (Ctrl+click).
     */
    public void toggle(DiagramElement element) {
        if (!selected.remove(element)) {
            selected.add(element);
        }
    }
}
