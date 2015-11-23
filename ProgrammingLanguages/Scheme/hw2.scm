;; (fromTo k n) returns the list of integers from k to n. The size
;; of the problem can be seen as the difference
;; between k and n.
;; Base Case: if k = n (i.e. if the size of the problem is 0), then
;; the result is the list containing only n.
;; Hypothesis: Assume (fromTo (+ k 1) n) returns the list of integers
;; from k+1 to n.
;; Recursive step: (fromTo k n) = (cons k (FromTo (+ k 1) n)

(define (fromTo k n)
  (cond ((= k n) (list n))
        (else (cons k (fromTo (+ k 1) n)))))

;; (removeMults m L) returns a list containing all the elements of L that
;; are not multiples of m.
;; Base Case: if L is null, then end of list is reached, return empty list
;; Hypothesis: Assume (cons (CAR L) (removeMults m (CDR L)) adds the first
;; element of L to the new list, but only if (CAR L) mod m is NOT 0
;; Recursion step: (removeMults m (CDR L))

(define (removeMults m L)
  (cond ((null? L) '())
        ((not (= (modulo (CAR L) m) 0)) (cons (CAR L) (removeMults m (CDR L))))
        (else (removeMults m (CDR L)))))

;; (removeAllMults L) returns a list containing all elements that are not multiples
;; of each other
;; Base Case: if L is null, end of list is reached, return the empty list
;; Hypothesis: (removeAllMults (removeMults (CAR L) (CDR L))) calls removeMults to
;; remove all multiples of the first element of the list from the rest, returns a new
;; list with all multiples removed.. then recurse on removeAllMults with shorter list
;; Recursion step: (cons (CAR L) (removeAllMults (removeMults (CAR L) (CDR L)))
;; appends the first number to the list of the result with all of its multiples removed

(define (removeAllMults L)
  (cond ((null? L) '())
        (else (cons (CAR L) (removeAllMults (removeMults (CAR L) (CDR L)))))))

;; (primes n) computes the list of all primes less than or equal to n.
;; Base Case: None -- this program itself is not recursive, but it calls recursive
;; functions such as fromTo and removeAllMults
;; Hypothesis: fromTo returns a list of numbers from n to k, the removeAllMults should
;; remove all non-primes from the list leaving you with only prime numbers

(define (primes n)
  (removeAllMults (fromTo 2 n)))

;; (maxDepth L) returns the maximum nesting epth of any element within L
;; Base case: when L is not a pair (non empty list) return -1 because
;; the first recursive case adds 1
;; Hypothesis: the answer is the max of the CAR of the list and the CDR of the list
;; recursed over
;; Recursion step: (max (+ (maxDepth (CAR L)) 1) (maxDepth (CDR L))

(define (maxDepth L)
  (cond ((not (pair? L)) -1)
        (else (max (+ (maxDepth (CAR L)) 1) (maxDepth (CDR L))))))

;; (prefix L) converts from infix to prefix
;; Base Case: if L is a number and not a list, append it
;; Hypothesis: (list (CAR (CDR L)) (prefix (CAR L)) (prefix (CDR (CDR L)))))
;; creates a list with the operator first, followed by the recursive solution of left
;; and right operands
;; Recursive step: (list (CAR (CDR L)) (prefix (CAR L)) (prefix (CDR (CDR L)))))

(define (prefix L)
  (if (list? L)
      (if (null? (CDR L))
          (CAR L)
          (list (CAR (CDR L))
                (prefix (CAR L))
                (prefix (CDR (CDR L)))))
      L))

;; (composition fns) takes a list of functions fns and returns a function that is the
;; composition of the functions in fns.
;; Base Case: if fns is null, return x, done. Otherwise, apply the first function in
;; the list to the recursion of all the rest of the functions
;; In which case, the last function wil be applied first, etc. until the first function
;; is applied to x.
;; lambda function calls the defined doWork method.
;; Recursive step: ((CAR fns) (doWork (CDR fns) x))))

(define (composition fns)
  (define (doWork fns x)
    (if (null? fns)
        x
        ((CAR fns) (doWork (CDR fns) x))))
  (lambda (x) (doWork fns x)))
    
