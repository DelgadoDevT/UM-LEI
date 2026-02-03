FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    texlive-latex-extra \
    texlive-fonts-extra \
    lhs2tex \
    make \
    vim \
    curl \
    build-essential \
    libgmp-dev

RUN curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org | sh && \
    echo ". ~/.ghcup/env" >> ~/.bashrc && \
    . ~/.ghcup/env && \
    ghcup install ghc && \
    ghcup install cabal

RUN . ~/.ghcup/env && \
    cabal update && \
    cabal install --lib random && \
    cabal install --lib process && \
    cabal install --lib split && \
    cabal install --lib matrix

WORKDIR /cp2526t
