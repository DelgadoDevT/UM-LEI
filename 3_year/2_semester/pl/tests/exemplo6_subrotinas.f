      PROGRAM EX6
      DIMENSION ARR(5)
      INTEGER ARR
      ARR(1) = 10
      CALL MYSUB(ARR, 5)
      STOP
      END

      SUBROUTINE MYSUB(A, N)
      DIMENSION A(N)
      INTEGER A, N
      PRINT *, 'VALOR: ', A(1)
      RETURN
      END