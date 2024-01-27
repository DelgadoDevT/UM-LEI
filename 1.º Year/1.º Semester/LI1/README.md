# PrimateKong

## Overview

Welcome to the Computer Science Labs I repository! This project is an exciting recreation of the classic Donkey Kong game, featuring a nostalgic gaming experience along with a hidden secret mode. To unlock the secret mode, simply press the keys S, T, A, and R simultaneously.

## Contributors

* Ricardo Morais

## Executable

You can compile and run the program using the build and run commands of cabal.

```bash
cabal run primate-kong
```

## Interpreter

You can open the Haskell interpreter (GHCi) using cabal with the project automatically loaded.

```bash
cabal repl
```

## Tests

The project uses the [HUnit](https://hackage.haskell.org/package/HUnit) library for unit testing.

You can run the tests using the following command

```bash
cabal test
```

If you want to run examples from the documentation as unit tests, use the [Doctest](https://hackage.haskell.org/package/doctest) library.

```bash
cabal repl --build-depends=QuickCheck,doctest --with-ghc=doctest
```

## Documentation

You can generate documentation using [Haddock](https://haskell-haddock.readthedocs.io/).

```bash
cabal haddock
```
