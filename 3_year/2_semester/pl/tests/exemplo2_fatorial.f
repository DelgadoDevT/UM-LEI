      PROGRAM FATORIAL
      INTEGER N, I, FAT
      PRINT *, 'Introduza um numero inteiro positivo:'
      READ *, N
      FAT = 1
      DO 10 I = 1, N
      FAT = FAT * I
   10 CONTINUE
      PRINT *, 'Fatorial de ', N, ': ', FAT
      END
