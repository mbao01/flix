///
/// A Partial Order is a function ⊑ which satisfies three properties: reflexivity, anti-symmetry, and transitivity.
///
namespace PartialOrder {

    ///
    /// The monotonicity law asserts that a function `f` is order-preserving w.r.t.
    /// the partial order `dom` on its domain and the partial order `codom` on its co-domain.
    ///
    /// NB: This particular law is for unary functions.
    ///
    law monotone1[a, b](f: a -> b, dom: (a, a) -> Bool, codom: (b, b) -> Bool): Bool =
        use Bool.→;
        ∀(x: a, y: a). (x `dom` y) → (f(x) `codom` f(y))

    ///
    /// The monotonicity law asserts that a function `f` is order-preserving w.r.t.
    /// the partial order `dom` on its domain and the partial order `codom` on its co-domain.
    ///
    /// NB: This particular law is for binary functions.
    ///
    law monotone2[a, b, c](f: (a, b) -> c, dom1: (a, a) -> Bool, dom2: (b, b) -> Bool, codom: (c, c) -> Bool): Bool =
        use Bool.{∧, →};
        ∀(x1: a, x2: a, y1: b, y2: b). ((x1 `dom1` y1) ∧ (x2 `dom2` y2)) → (f(x1, x2) `codom` f(y1, y2))

    ///
    /// The safety law asserts that the abstract function `fa` is an over-approximation of the concrete
    /// function `fc` according to the partial order `leq` and abstraction function `alpha`.
    ///
    /// NB: This particular law is for unary functions.
    ///
    law safe1[a1, a2, c1, c2](fa: a1 -> a2, fc: c1 -> c2, alpha1: c1 -> a1, alpha2: c2 -> a2, leq: (a2, a2) -> Bool): Bool =
        ∀(x: c1). alpha2(fc(x)) `leq` fa(alpha1(x))

    ///
    /// The safety law asserts that the abstract function `fa` is an over-approximation of the concrete
    /// function `fc` according to the partial order `leq` and abstraction functions `alpha1` and `alpha2`.
    ///
    /// NB: This particular law is for binary functions.
    ///
    law safe2[a1, a2, c1, c2](fa: (a1, a1) -> a2, fc: (c1, c1) -> c2, alpha1: c1 -> a1, alpha2: c2 -> a2, leq: (a2, a2) -> Bool): Bool =
        ∀(x: c1, y: c1). alpha2(fc(x, y)) `leq` fa(alpha1(x), alpha1(y))

}
