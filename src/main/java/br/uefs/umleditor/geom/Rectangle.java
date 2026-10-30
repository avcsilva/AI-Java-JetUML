package br.uefs.umleditor.geom;

/**
 * An immutable axis-aligned rectangle in diagram coordinates.
 */
public record Rectangle(double x, double y, double width, double height) {

    /**
     * Creates the rectangle with the given opposite corners, in any order.
     */
    public static Rectangle fromCorners(Point first, Point second) {
        double left = Math.min(first.x(), second.x());
        double top = Math.min(first.y(), second.y());
        return new Rectangle(left, top, Math.abs(first.x() - second.x()), Math.abs(first.y() - second.y()));
    }

    public double maxX() {
        return x + width;
    }

    public double maxY() {
        return y + height;
    }

    public Point center() {
        return new Point(x + width / 2, y + height / 2);
    }

    public boolean contains(Point point) {
        return point.x() >= x && point.x() <= maxX() && point.y() >= y && point.y() <= maxY();
    }

    public boolean contains(Rectangle other) {
        return other.x >= x && other.maxX() <= maxX() && other.y >= y && other.maxY() <= maxY();
    }

    public Rectangle union(Rectangle other) {
        double left = Math.min(x, other.x);
        double top = Math.min(y, other.y);
        return new Rectangle(left, top, Math.max(maxX(), other.maxX()) - left, Math.max(maxY(), other.maxY()) - top);
    }

    public Rectangle translated(double dx, double dy) {
        return new Rectangle(x + dx, y + dy, width, height);
    }

    public Rectangle expanded(double margin) {
        return new Rectangle(x - margin, y - margin, width + 2 * margin, height + 2 * margin);
    }
}
