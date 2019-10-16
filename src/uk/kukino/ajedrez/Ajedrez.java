package uk.kukino.ajedrez;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.kukino.ajedrez.Piece.*;

public class Ajedrez {

    static Buffers<int[]> BUFFERS = new Buffers<>(1000, () -> new int[1000]);

    long iter = 0;
    long totals = 0;

    private void search(Piece[] pieces, int piecesI, Board b, int level) {

        iter++;

        if (piecesI < pieces.length) {
            Piece piece = pieces[piecesI];
            int[] attacks = BUFFERS.lease();
            int[] empties = BUFFERS.lease();

            int emptiesM = b.emptiesAfterLastPiece(empties, piece);
            for (int e = 0; e < emptiesM; e++) {
                int pieceIdx = empties[e];

                int am = piece.attacks(attacks, b, pieceIdx);
                if (b.canPlace(attacks, am)) {
                    Board newB = b.copy();
                    newB.place(attacks, am, piece, pieceIdx);
                    search(pieces, piecesI + 1, newB, level + 1);
                    newB.release();
                }
            }
            BUFFERS.ret(empties);
            BUFFERS.ret(attacks);
        } else {
//            if (!integrity(pieces, b)) {
//                System.out.println("Integrity failed!");
//                System.out.println(b.toString());
//            }

            totals++;
            if (totals % 1000_000 == 0) {
                System.out.println("Found " + totals + " so far");
                System.out.println(b.toString());
            }

        }
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

//        Board b = new Board(6, 6);
//        Piece[] pieces = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING};

        Board b = new Board(7, 7);
        Piece[] pieces = new Piece[]{QUEEN, QUEEN, KNIGHT, KNIGHT, ROOK, ROOK, BISHOP, BISHOP, KING, KING};

        //        Board b = new Board(5, 5);
//        Piece[] pieces = new Piece[]{ROCK, ROCK, KING, KING, KING, KING};
//        Board b = new Board(6, 9);
//        Piece[] pieces = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING, KING};
//        Board b = new Board(6, 8);
//        Piece[] pieces = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING, KING};
//        Board b = new Board(6, 10);
//        Piece[] pieces = new Piece[]{QUEEN, ROCK, BISHOP, KNIGHT, KING, KING};
//        Board b = new Board(4, 4);
//        Piece[] pieces = new Piece[]{ROCK, ROCK, KNIGHT, KNIGHT, KNIGHT, KNIGHT};
//        Board b = new Board(4, 4);
//        Piece[] pieces = new Piece[]{ROCK, ROCK, KNIGHT, KNIGHT};

        ajedrez.search(pieces, 0, b, 1);
        System.out.println("java:" + b.m + "x" + b.n + " " + Arrays.asList(pieces));
        System.out.println("Total found " + ajedrez.totals); // + "  or #" + ajedrez.finalHashes.size()); // 20352869
        System.out.println("Took " + (System.currentTimeMillis() - start) / 1000f + " ms, "
                + ajedrez.iter + " iterations");

    }

}
