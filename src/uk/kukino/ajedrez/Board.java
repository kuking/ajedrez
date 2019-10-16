package uk.kukino.ajedrez;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

public class Board {

    static Buffers<byte[]> BUFFERS; //= new Buffers<>(100, () -> new byte[100]);

    private byte[] contents;
    public final int m, n;

    public Board(int m, int n) {
        if (BUFFERS == null) {
            BUFFERS = new Buffers<>(100, () -> new byte[m * n]);
        }
        this.contents = BUFFERS.lease();
        for (int i = 0; i < m * n; i++) {
            this.contents[i] = Piece.EMPTY.b;
        }

        this.m = m;
        this.n = n;
    }

    public Board(int m, int n, byte[] content) {
        this.contents = content;
        this.m = m;
        this.n = n;
    }

    public int empties(int[] empties) {
        int count = 0;
        for (int i = 0; i < m * n; i++) {
            if (contents[i] == Piece.EMPTY.b) {
                empties[count++] = i;
            }
        }
        return count;
    }

    public String hash() {
        if (contents.length == m * n) {
            return DigestUtils.md5Hex(contents);
//            return DigestUtils.sha1Hex(contents);
        }
        return DigestUtils.md5Hex(Arrays.copyOf(contents, m * n));
//        return DigestUtils.sha1Hex(Arrays.copyOf(contents, m * n));
    }

    public int x(final int idx) {
        return idx % m;
    }

    public int y(final int idx) {
        return idx / m;
    }

    public int idx(final int x, final int y) {
        return y * m + x;
    }

    public boolean isValid(final int x, final int y) {
        return !(x < 0 || y < 0 || x >= m || y >= n);
    }

    public boolean canPlace(int[] attacks, final int attacksM) {
        for (int i = 0; i < attacksM; i++) {
            if (contents[attacks[i]] != Piece.EMPTY.b && contents[attacks[i]] != Piece.BLOCKED.b) {
                return false;
            }
        }
        return true;
    }

    public void place(int[] attacks, final int attacksM, final Piece piece, final int idx) {
        for (int i = 0; i < attacksM; i++) {
            if (contents[attacks[i]] == Piece.EMPTY.b) {
                contents[attacks[i]] = Piece.BLOCKED.b;
            }
        }
        contents[idx] = piece.b;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(m).append('x').append(n).append('\n');
        for (int nn = 0; nn < n; nn++) {
            for (int mm = 0; mm < m; mm++) {
                sb.append(Piece.values()[contents[idx(mm, nn)]].c).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public Board copy() {
        byte[] buffer = BUFFERS.lease();
        System.arraycopy(this.contents, 0, buffer, 0, m * n);
        return new Board(m, n, buffer);
    }

    public void flipVertical() {
        for (int mm = 0; mm < m / 2; mm++) {
            for (int nn = 0; nn < n; nn++) {
                byte tmp = contents[idx(mm, nn)];
                contents[idx(mm, nn)] = contents[idx(m - mm - 1, nn)];
                contents[idx(m - mm - 1, nn)] = tmp;
            }
        }
    }

    public void flipHorizontal() {
        for (int mm = 0; mm < m; mm++) {
            for (int nn = 0; nn < n / 2; nn++) {
                byte tmp = contents[idx(mm, nn)];
                contents[idx(mm, nn)] = contents[idx(mm, n - nn - 1)];
                contents[idx(mm, n - nn - 1)] = tmp;
            }
        }
    }

    public Piece get(final int x, final int y) {
        return Piece.values()[contents[idx(x, y)]];
    }

    public void release() {
        BUFFERS.ret(this.contents);
        this.contents = null;
    }
}
