      PROGRAM TESTARR
      INTEGER A
      DIMENSION A(5)
      INTEGER B(5)
      INTEGER I

      DO 10 I = 1, 5
      A(I) = I * 10
      B(I) = I * 20
   10 CONTINUE

      DO 20 I = 1, 5
      PRINT *, 'A(', I, ') = ', A(I)
      PRINT *, 'B(', I, ') = ', B(I)
   20 CONTINUE

      END
