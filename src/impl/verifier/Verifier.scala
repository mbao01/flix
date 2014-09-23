package impl.verifier

import impl.logic._
import impl.runtime.{Unification, Interpreter}
import syntax.Terms.RichTerm

object Verifier {

  /**
   * Constraints over integers.
   */
  sealed trait Constraint {
    def fmt: String = ???
  }

  object Constraint {

    case class Var(x: Symbol.VariableSymbol) extends Constraint

    /**
     * Negation.
     */
    case class Not(c: Constraint) extends Constraint

    /**
     * Conjunction.
     */
    case class And(c1: Constraint, c2: Constraint) extends Constraint

    /**
     * Disjunction.
     */
    case class Or(c1: Constraint, c2: Constraint) extends Constraint

    case class Eq() extends Constraint

    case class Neq() extends Constraint

  }


  import Constraint._


  def verify(program: Program): Unit = {
    // TODO: Need a notion of lattices. ..

    for (declaration <- program.declarations) {
      declaration match {
        case Declaration.DeclareBot(t, typ) =>
          val leq = program.lookupLeq(typ).get
          tautology("leastelm", Property.leastElement(leq, t))

        case Declaration.DeclareLeq(leq, typ) =>
          tautology("refl", Property.reflexivity(leq))
          tautology("antisymm", Property.antiSymmetri(leq))
          tautology("trans", Property.transitivity(leq))

        case Declaration.DeclareLub(lub, typ) =>
          val baseType = typ.resultType
          val leq = program.lookupLeq(baseType).get
          //upperBound(leq, lub)

        case Declaration.DeclareHeight(t, typ) =>
      }
    }
  }


  /**
   * Verifies whether the given term `t1` evaluates to `true` for all inputs.
   */
  def tautology(name: String, t0: Term): Unit = {
    def iterate(t: Term): Unit = {
      val abs = evaluate(t)
      abs match {
        case Term.Bool(true) => println("OK")
        case Term.Bool(false) => println("NOTOK"); ??? // TODO: Need ADT

        case x: Term.Abs =>
          for (a <- enumerate(x.typ)) {
            print("  Elm: " + a.fmt + " ")
            val r = evaluate(Term.App(x, a))
            iterate(r)
          }

        case x => ???
        //          if (isNormalForm(x))
        //            genVc(x)
        //          else ???
      }
    }
    println(name)
    iterate(t0)
  }

  /**
   * Returns the partial evaluation of the given term `t`.
   */
  def evaluate(t: Term): Term = t match {
    case Term.Unit => Term.Unit
    case Term.Bool(b) => Term.Bool(b)
    case Term.Int(i) => Term.Int(i)
    case Term.Str(s) => Term.Str(s)
    case Term.Set(xs) => Term.Set(xs.map(evaluate))

    case Term.Var(x) => Term.Var(x)
    case Term.Abs(s, typ, t1) => Term.Abs(s, typ, t1)

    case Term.App(t1, t2) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)

      r1 match {
        case Term.Abs(x, _, tb) =>
          val y = Symbol.freshVariableSymbol(x)
          val r = tb.rename(x, y).substitute(y, r2)
          evaluate(r)
        case _ => throw new RuntimeException()
      }

    case Term.Match(t1, rules) =>
      val r1 = evaluate(t1)
      matchRule(rules, r1) match {
        case None => throw new RuntimeException(s"Unmatched value ${r1.fmt}")
        case Some((t2, env)) => evaluate(t2.substitute2(env))
      }

    case Term.IfThenElse(t1, t2, t3) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      val r3 = evaluate(t3)

      r1.asValue match {
        case None => Term.IfThenElse(r1, r2, r3)
        case Some(v) => if (v.toBool) r2 else r3
      }

    //    case Term.Match

    case Term.UnaryOp(op, t1) =>
      val r1 = evaluate(t1)
      r1.asValue match {
        case None => Term.UnaryOp(op, r1)
        case Some(v1) => Interpreter.apply(op, v1).toTerm
      }
    case Term.BinaryOp(op, t1, t2) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      (r1.asValue, r2.asValue) match {
        case (Some(v1), Some(v2)) => Interpreter.apply(op, v1, v2).toTerm
        case _ => Term.BinaryOp(op, r1, r2)
      }
    case Term.Tag(n, t1, typ) => Term.Tag(n, evaluate(t1), typ)
    case Term.Tuple2(t1, t2) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      Term.Tuple2(r1, r2)
    case Term.Tuple3(t1, t2, t3) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      val r3 = evaluate(t3)
      Term.Tuple3(r1, r2, r3)
    case Term.Tuple4(t1, t2, t3, t4) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      val r3 = evaluate(t3)
      val r4 = evaluate(t4)
      Term.Tuple4(r1, r2, r3, r4)
    case Term.Tuple5(t1, t2, t3, t4, t5) =>
      val r1 = evaluate(t1)
      val r2 = evaluate(t2)
      val r3 = evaluate(t3)
      val r4 = evaluate(t4)
      val r5 = evaluate(t5)
      Term.Tuple5(r1, r2, r3, r4, r5)
  }

  /**
   * Optionally returns a pair of a term and environment for which one of the given `rules` match the given value `v`.
   */
  private def matchRule(rules: List[(Pattern, Term)], t: Term): Option[(Term, Map[Symbol.VariableSymbol, Term])] = rules match {
    case Nil => None
    case (p, t1) :: rest => Unification.unify(p, t) match {
      case None => matchRule(rest, t)
      case Some(env) => Some((t1, env))
    }
  }


  /**
   * Returns *all* terms which inhabit the given type `typ`.
   *
   * If the type is finite then a closed term is returned.
   * Otherwise an open term is returned (i.e. a term with variables).
   */
  def enumerate(typ: Type, sum: Option[Type.Sum] = None): Set[Term] = typ match {
    case Type.Unit => Set(Term.Unit)
    case Type.Bool => Set(Term.Bool(true), Term.Bool(false))
    case Type.Int => Set(Term.Var(Symbol.freshVariableSymbol("i")))

    case Type.Tag(n, typ2) => enumerate(typ2).map(x => Term.Tag(n, x, sum.get))
    case Type.Sum(ts) => ts.flatMap(x => enumerate(x, Some(Type.Sum(ts)))).toSet

    case Type.Tuple2(typ1, typ2) =>
      for (t1 <- enumerate(typ1);
           t2 <- enumerate(typ2))
      yield Term.Tuple2(t1, t2)
    case Type.Tuple3(typ1, typ2, typ3) =>
      for (t1 <- enumerate(typ1);
           t2 <- enumerate(typ2);
           t3 <- enumerate(typ3))
      yield Term.Tuple3(t1, t2, t3)
    case Type.Tuple4(typ1, typ2, typ3, typ4) =>
      for (t1 <- enumerate(typ1);
           t2 <- enumerate(typ2);
           t3 <- enumerate(typ3);
           t4 <- enumerate(typ4))
      yield Term.Tuple4(t1, t2, t3, t4)
    case Type.Tuple5(typ1, typ2, typ3, typ4, typ5) =>
      for (t1 <- enumerate(typ1);
           t2 <- enumerate(typ2);
           t3 <- enumerate(typ3);
           t4 <- enumerate(typ4);
           t5 <- enumerate(typ5))
      yield Term.Tuple5(t1, t2, t3, t4, t5)

    case Type.Set(xs) => throw new UnsupportedOperationException("Unexpected type.")
    case Type.Var(x) => throw new UnsupportedOperationException("Unexpected type.")
    case Type.Function(a, b) => throw new UnsupportedOperationException("Impossible to enumerate functions.")
    case Type.Str => throw new UnsupportedOperationException("Impossible to enumerate strings.")
  }


  //  /**
  //   * Returns `true` iff the given term `t` is in normal form.
  //   */
  //  def isNormalForm(t: Term): Boolean = {
  //    def isInt(t: Term): Boolean = t match {
  //      case Term.Int(i) => true
  //
  //    }
  //
  //    t match {
  //      case Term.Unit => false
  //      case Term.Bool(b) => true
  //      case Term.Int(i) => false
  //      case Term.Str(s) => false
  //
  //
  //      case _: Term.Tag => false
  //      case _: Term.Tuple2 => false
  //      case _: Term.Tuple3 => false
  //      case _: Term.Tuple4 => false
  //      case _: Term.Tuple5 => false
  //    }
  //  }
  //
  //
  //  def genVc(t: Term): Constraint = t match {
  //    case Term.BinaryOp(op, t2, t3) => ???
  //
  //    case Term.IfThenElse(t1, t2, t3) =>
  //      val c1 = genVc(t1)
  //      val c2 = genVc(t2)
  //      val c3 = genVc(t3)
  //      Or(And(c1, c2), And(Not(c1), c3))
  //
  //    case Term.BinaryOp(BinaryOperator.Equal, t1, t2) => ???
  //  }


}
