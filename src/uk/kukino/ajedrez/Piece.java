package uk.kukino.ajedrez;

import java.util.Comparator;

public enum Piece {

    EMPTY('.'), BLOCKED(':'), KING('K'), QUEEN('Q'), BISHOP('B'), KNIGHT('H'), ROCK('R');

    final byte b;
    final char c;

    Piece(final char k) {
        b = (byte) ordinal();
        c = k;
    }

    int attacks(int[] positions, final Board b, final int idx) {

        int p = 0;
        int x = b.x(idx);
        int y = b.y(idx);

        if (this == KING) {
            return kingAttack(positions, b, p, x, y);

        } else if (this == KNIGHT) {
            return knightAttack(positions, b, p, x, y);

        } else if (this == ROCK) {
            return rockAttack(positions, b, idx, p);

        } else if (this == BISHOP) {
            return bishopAttack(positions, b, p, x, y);

        } else if (this == QUEEN) {
            p = rockAttack(positions, b, idx, p);
            return bishopAttack(positions, b, p, x, y);
        }
        // EMPTY, BLOCKED
        return 0;
    }

    private int kingAttack(int[] positions, Board b, int p, int x, int y) {
        for (int xx = -1; xx < 2; xx++) {
            for (int yy = -1; yy < 2; yy++) {
                if (!(xx == x && y == yy) && (b.isValid(x + xx, y + yy))) {
                    positions[p++] = b.idx(x + xx, y + yy);
                }
            }
        }
        return p;
    }

    private int knightAttack(int[] positions, Board b, int p, int x, int y) {
        for (int xx = -2; xx < 3; xx++) {
            for (int yy = -2; yy < 3; yy++) {
                if (xx != 0 && yy != 0 && Math.abs(xx) != Math.abs(yy) && b.isValid(x + xx, y + yy)) {
                    positions[p++] = b.idx(x + xx, y + yy);
                }
            }
        }
        return p;
    }

    private int rockAttack(int[] positions, Board b, int idx, int p) {
        for (int mm = 0; mm < b.m; mm++) {
            if (b.isValid(mm, b.y(idx)) && mm != b.m) {
                positions[p++] = b.idx(mm, b.y(idx));
            }
        }
        for (int nn = 0; nn < b.n; nn++) {
            if (b.isValid(b.x(idx), nn) && nn != b.n) {
                positions[p++] = b.idx(b.x(idx), nn);
            }
        }
        return p;
    }

    private int bishopAttack(int[] positions, Board b, int p, int x, int y) {
        int tm = Math.max(b.m, b.n);
        for (int t = 1; t < tm; t++) {
            if (b.isValid(x - t, y - t)) positions[p++] = b.idx(x - t, y - t);
            if (b.isValid(x + t, y - t)) positions[p++] = b.idx(x + t, y - t);
            if (b.isValid(x + t, y + t)) positions[p++] = b.idx(x + t, y + t);
            if (b.isValid(x - t, y + t)) positions[p++] = b.idx(x - t, y + t);
        }
        return p;
    }

    public static Comparator<Piece> COMPARATOR = new Comparator<Piece>() {
        @Override
        public int compare(Piece o1, Piece o2) {
            return o1.ordinal() - o2.ordinal();
        }
    };

}
