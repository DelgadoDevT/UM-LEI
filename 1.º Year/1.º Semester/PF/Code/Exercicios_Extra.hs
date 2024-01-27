module Ficha10 where

maximumMB :: Ord a => [Maybe a] -> Maybe a
maximumMB [] = Nothing
maximumMB (Nothing:xs) = maximumMB xs
maximumMB (Just x:xs) = case maximumMB xs of
                        Nothing -> Just x
                        Just y -> Just (max x y)

type Mat = [[Int]]

stringToMap :: String -> Mat
stringToMap "" = []
stringToMap l = map (\l -> read ("["++l++"]")) (lines l)

data Btree a = Empty
               | Node a (Btree a) (Btree a)
               deriving Show

arv = (Node 5 (Node 7 (Node 3 Empty Empty)
                      (Node 2 (Node 10 Empty Empty) Empty)
                    )
                    (Node 1 (Node 12 Empty Empty)
                            (Node 4 Empty (Node 8 Empty Empty))
              )
      )
            
numera :: Btree a -> Btree (a, Int)
numera t = fst $ numera' t 1

numera' :: Btree a -> Int -> (Btree (a,Int),Int)
numera' Empty n = (Empty,n)
numera' (Node i e d) n = (Node (i,n) e' d',nd)
   where
        (e',ne) = numera' e (n+1)
        (d',nd) = numera' d ne    

  




