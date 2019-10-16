#!/usr/bin/env python3

import copy
import hashlib
import pickle
import time
from itertools import chain

config = {
    'M': 5,  # size 6
    'N': 5,  # size 9
    'K': 0,  # Kings = 2
    'Q': 0,  # Queens = 1
    'B': 0,  # Bishops = 1
    'R': 2,  # Rocks = 1
    'H': 4  # Knights (Horses) = 1
}


# ':' are places in the board where a piece can not be placed or it would be threaten
# '.' are empty spaces

def new_board(cfg):
    return ['.' for _ in range(cfg['M'] * cfg['N'])]


def hash_board(b):
    return hashlib.sha1(pickle.dumps(b)).hexdigest()


def print_board(cfg, board):
    M = cfg['M']
    N = cfg['N']
    print("%ix%i:" % (M, N))
    for m in range(M):
        for n in range(N):
            print(board[board_idx(cfg, m, n)] + " ", end='')
        print()


def pieces_left(cfg):
    return sum(cfg[piece] for piece in ['Q', 'R', 'B', 'H', 'K'])


def next_piece(cfg):
    pieces_prio = ['Q', 'R', 'B', 'H', 'K']
    pieces_prio.reverse()
    for piece in pieces_prio:
        for _ in range(cfg[piece]):
            yield piece


def free_slots(cfg, b):
    M = cfg['M']
    for i in range(len(b)):
        if b[i] == '.':
            yield i % M, i // M


def is_valid(cfg, mm, nn):
    return not (mm < 0 or nn < 0 or mm >= cfg['M'] or nn >= cfg['N'])


def board_idx(cfg, m, n):
    M = cfg['M']
    return n * M + m


def mark_if_empty(cfg, b, m, n, mark):
    idx = board_idx(cfg, m, n)
    if b[idx] == '.':
        b[idx] = mark


def king_threats_gen(cfg, m, n):
    for mm in range(-1, 2):
        for nn in range(-1, 2):
            if not (mm == m and nn == n) and is_valid(cfg, m + mm, n + nn):
                yield m + mm, n + nn


def knight_threats_gen(cfg, m, n):
    for x in [-2, -1, 1, 2]:
        for y in [-2, -1, 1, 2]:
            if abs(x) != abs(y) and is_valid(cfg, m + x, n + y):
                yield m + x, n + y


def rock_threats_gen(cfg, m, n):
    for mm in range(cfg['M']):
        if is_valid(cfg, mm, n) and mm != m:
            yield mm, n
    for nn in range((cfg['N'])):
        if is_valid(cfg, m, nn) and nn != n:
            yield m, nn


def bishop_threats_gen(cfg, m, n):
    r = max(cfg['M'], cfg['N'])
    for x in range(1, r):
        if is_valid(cfg, m - x, n - x):
            yield m - x, n - x
        if is_valid(cfg, m + x, n - x):
            yield m + x, n - x
        if is_valid(cfg, m + x, n + x):
            yield m + x, n + x
        if is_valid(cfg, m - x, n + x):
            yield m - x, n + x


def queen_threats_gen(cfg, m, n):
    for x, y in chain(bishop_threats_gen(cfg, m, n), rock_threats_gen(cfg, m, n)):
        yield x, y


threats_gen_for = {
    'K': king_threats_gen,
    'Q': queen_threats_gen,
    'H': knight_threats_gen,
    'R': rock_threats_gen,
    'B': bishop_threats_gen}


def can_place_piece(cfg, b, m, n, piece):
    # not necessary as this is called with free_slots iterator
    # if not is_valid(cfg, m, n):
    #     return False
    # if b[board_idx(cfg, m, n)] != '.':  # self kill or already other piece there
    #     return False
    for x, y in threats_gen_for[piece](cfg, m, n):
        idx = board_idx(cfg, x, y)
        if b[idx] != '.' and b[idx] != ':':  # kill somebody else
            return False
    return True


def place_piece(cfg, b, m, n, piece):
    """ assumed  called after can_place_piece """
    for mm, nn in threats_gen_for[piece](cfg, m, n):
        mark_if_empty(cfg, b, mm, nn, ':')
    b[board_idx(cfg, m, n)] = piece
    cfg[piece] = cfg[piece] - 1


hashes = set()
iters = 0
totals = 0


def iterate(c, b, level):
    global iters, totals, hashes
    iters = iters + 1
    h = hash_board(b)
    if h in hashes:
        return
    hashes.add(h)

    for piece in next_piece(c):
        for m, n in free_slots(c, b):
            if can_place_piece(c, b, m, n, piece):
                next_c = copy.copy(c)
                next_b = copy.copy(b)
                place_piece(next_c, next_b, m, n, piece)
                iterate(next_c, next_b, level + 1)

    if pieces_left(c) == 0:
        totals = totals + 1
        if totals % 50000 == 0:
            print("%i found in %i iterations" % (totals, iters))
            print(hash_board(b))
            print_board(c, b)


start = time.time()
print("%ix%i Board, %i Queens, %i Kings, %i Rocks, %i Bishops & %i Knights" % (
    config['M'], config['N'], config['Q'], config['K'], config['R'], config['B'], config['H']))

iterate(config, new_board(config), 1)
print("Finished, total solutions:", totals, " in %2.2f seconds" % (time.time() - start))
