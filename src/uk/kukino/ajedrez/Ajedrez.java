package uk.kukino.ajedrez;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static uk.kukino.ajedrez.Piece.*;

public class Ajedrez {

    static Buffers<int[]> BUFFERS = new Buffers<>(1000, () -> new int[1000]);

    long iter = 0;
    long totals = 0;
    HashSet<String> hashes = new HashSet<>(50_000_000);

    private void search(Piece[] left, int leftI, Board b, int level) {

        iter++;
        if (level != 1) {
            if (checkHash(b)) return;
//            Board bf = b.copy();
//            bf.flipHorizontal();
//            if (checkHash(bf)) {
//                bf.release();
//                return;
//            }
//            bf.flipVertical();
//            if (checkHash(bf)) {
//                bf.release();
//                return;
//            }
//            bf.flipHorizontal();
//            if (checkHash(bf)) {
//                bf.release();
//                return;
//            }
//            bf.release();
        }

        if (leftI < left.length) {
            Piece piece = left[leftI];
            int[] attacks = BUFFERS.lease();
            int[] empties = BUFFERS.lease();
            int emptiesM = b.empties(empties);
            for (int e = 0; e < emptiesM; e++) {
                int am = piece.attacks(attacks, b, empties[e]);
                if (b.canPlace(attacks, am)) {
                    Board newB = b.copy();
                    newB.place(attacks, am, piece, empties[e]);
                    search(left, leftI + 1, newB, level + 1);
                    newB.release();
                }
            }
            BUFFERS.ret(empties);
            BUFFERS.ret(attacks);
        }

        if (leftI == left.length) {
//            if (!integrity(left, b)) {
//                System.out.println("Integrity failed!");
//                System.out.println(b.toString());
//            }

            totals += 1; // + 4 if mirroring boards
            if (totals % 1000_000 == 0) {
                System.out.println("Found " + totals + " so far");
                System.out.println(b.toString());
            }

        }
    }

    private boolean checkHash(Board b) {
        String hash = b.hash();
        if (hashes.contains(hash)) {
            return true;
        }
        hashes.add(hash);
        return false;
    }

    private boolean integrity(Piece[] left, Board b) {
        List<Piece> expected = new ArrayList<>();
        List<Piece> whatis = new ArrayList<>();
        for (Piece piece : left) {
            expected.add(piece);
        }
        for (int m = 0; m < b.m; m++) {
            for (int n = 0; n < b.n; n++) {
                Piece piece = b.get(m, n);
                if (piece != EMPTY && piece != BLOCKED) {
                    whatis.add(b.get(m, n));
                }
            }
        }
        expected.sort(COMPARATOR);
        whatis.sort(COMPARATOR);
        return expected.equals(whatis);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Ajedrez ajedrez = new Ajedrez();

        Board b = new Board(5, 5);
        Piece[] lefts = new Piece[]{ROCK, ROCK, KING, KING, KING, KING};
//        Piece[] = new Board(6, 9);
//        Piece[] lefts = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING, KING};
//        Board b = new Board(6, 8);
//        Piece[] lefts = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING, KING};
//        Board b = new Board(4, 4);
//        Piece[] lefts = new Piece[]{ROCK, ROCK, KNIGHT, KNIGHT, KNIGHT, KNIGHT};

        ajedrez.search(lefts, 0, b, 1);
        System.out.println(b.m + "x" + b.n + " " + Arrays.asList(lefts));
        System.out.println("Total found " + ajedrez.totals); // 20352869
        System.out.println("Took " + (System.currentTimeMillis() - start) / 1000f + " ms. and  " + ajedrez.iter + " iterations."); // 43s,

//        Total found 20352869
//        Took 86.0 ms. and  45751933 iterations.

    }

    /*
    6x8 [QUEEN, ROCK, BISHOP, KNIGHT, KING, KING]
    Total found 5629780
    Took 14.509 ms. and  13210662 iterations.

    vv with mirroring
    6x8 [QUEEN, ROCK, BISHOP, KNIGHT, KING, KING]
    Total found 5563272
    Took 10.712 ms. and  3267139 iterations.


    6x9 [QUEEN, ROCK, BISHOP, KNIGHT, KING, KING]
    Total found 20352869
    Took 57.95 ms. and  45751933 iterations.

    vv with mirroring
    6x9 [QUEEN, ROCK, BISHOP, KNIGHT, KING, KING]
    Total found 18352200
    Took 43.723 ms. and  10317126 iterations.
     */
}
