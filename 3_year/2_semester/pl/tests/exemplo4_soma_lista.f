      PROGRAM SOMAARR
      INTEGER NUMS(5)
      INTEGER I, SOMA
      SOMA = 0
      PRINT *, 'Introduza 5 numeros inteiros:'
      DO 30 I = 1,5
      READ *, NUMS(I)
      SOMA = SOMA + NUMS(I)
   30 CONTINUE
      PRINT *, 'A soma dos numeros e: ', SOMA
      END
