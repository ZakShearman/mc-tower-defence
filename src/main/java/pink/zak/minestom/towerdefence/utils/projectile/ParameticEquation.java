package pink.zak.minestom.towerdefence.utils.projectile;

/**
 * @author <a href="https://github.com/iam4722202468">iam4722202468</a>
 */
public record ParameticEquation(double a, double b, double c) {
    public double solve(double x) {
        return a * x * x + b * x + c;
    }

    public static ParameticEquation calculateMovement(double fromX, double fromY, double maxX, double maxY, double toX, double p3y) {
        if (Math.abs(fromX - toX) < 0.01) return new ParameticEquation(0, 0, fromY);

        double h0 = fromY / (fromX * fromX);
        double h1 = 1 / fromX;
        double h2 = 1 / (fromX * fromX);

        double h3 = maxY / maxX;
        double h4 = maxX;
        double h5 = 1 / maxX;

        double h6 = p3y;
        double h7 = toX * toX;
        double h8 = toX;

        double v = h1 * (h4 - h5 * h7) + h5 * h8 + h2 * (h7 - h4 * h8) - 1;

        double a_top = h1 * (h3 - h5 * h6) + h2 * (h6 - h3 * h8) + h0 * (h5 * h8 - 1);
        double a = a_top / v;

        double b_top = (h5 - h2 * h4) * h6 + h3 * (h2 * h7 - 1) + h0 * (h4 - h5 * h7);
        double b = b_top / v;

        double c_top = (h1 * h4 - 1) * h6 + h3 * (h8 - h1 * h7) + h0 * (h7 - h4 * h8);
        double c = c_top / v;

        return new ParameticEquation(a, b, c);
    }
}