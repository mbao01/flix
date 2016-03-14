package ca.uwaterloo.flix.language.phase

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.ast.ParsedAst.Literal
import ca.uwaterloo.flix.language.ast.{ParsedAst, _}
import org.scalatest.FunSuite

// TODO: Cleanup names. Numbering and remove the Parser. prefix.

class TestParser extends FunSuite {

  /////////////////////////////////////////////////////////////////////////////
  // Root                                                                    //
  /////////////////////////////////////////////////////////////////////////////
  test("Root01") {
    val input = ""
    new Flix().addStr(input).compile().get
  }

  /////////////////////////////////////////////////////////////////////////////
  // Declarations and Definitions                                            //
  /////////////////////////////////////////////////////////////////////////////
  test("Declaration.Namespace01") {
    val input =
      """namespace a {
        |  // comment
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace02") {
    val input =
      """namespace a.b.c {
        |  // comment
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace03") {
    val input =
      """namespace a {
        |  namespace b {
        |    namespace c {
        |      // comment
        |    }
        |  }
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace04") {
    val input =
      """namespace a.b.c {
        |  namespace d.e.f {
        |    namespace h.i.j {
        |      // comment
        |    }
        |  }
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace05") {
    val input =
      """namespace a {
        |  namespace b {
        |    namespace c {
        |      namespace a.b.c {
        |        def f(x: Int): Int = x + 42
        |      }
        |    }
        |  }
        |}
        |
        |def g: Int = a.b.c.a.b.c/f(21)
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace06") {
    val input =
      """namespace a {
        |  namespace b.c {
        |    namespace d {
        |      namespace e.f.g {
        |        def h(x: Int): Int = x + 42
        |      }
        |    }
        |  }
        |}
        |
        |def j: Int = a.b.c.d.e.f.g/h(21)
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace07") {
    val input =
      """namespace a {
        |  namespace b {
        |    namespace c {
        |      def f(x: Int): Int = x + 42
        |    }
        |  }
        |}
        |
        |namespace a.b.c {
        |  def g: Int = f(21)
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace08") {
    val input =
      """namespace a {
        |  namespace b {
        |    namespace c {
        |      def f(x: Int): Int = x + 42
        |      def g: Int = a.b.c/f(21)
        |    }
        |  }
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Declaration.Namespace09") {
    val input =
      """namespace a {
        |  namespace b {
        |    namespace c {
        |      def u(x: Int): Int = x + 42
        |    }
        |  }
        |
        |  namespace b.c {
        |    def v(x: Int): Int = x + 21
        |  }
        |}
        |
        |namespace a.b.c {
        |  def w(x: Int): Int = x + 11
        |}
        |
        |def r: Int = a.b.c/u(1) + a.b.c/v(2) + a.b.c/w(3)
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  /////////////////////////////////////////////////////////////////////////////
  // Imports                                                                 //
  /////////////////////////////////////////////////////////////////////////////
  test("Import.Wildcard01") {
    val input1 =
      """namespace a.b.c {
        |  def f: Int = 42
        |}
      """.stripMargin
    val input2 =
      """import a.b.c/_
        |def g: Int = f() + 42
      """.stripMargin
    new Flix().addStr(input1).addStr(input2).compile().get
  }

  test("Import.Definition01") {
    val input1 =
      """namespace a.b.c {
        |  def f: Int = 42
        |}
      """.stripMargin
    val input2 =
      """import a.b.c/f
        |def g: Int = f() + 42
      """.stripMargin
    new Flix().addStr(input1).addStr(input2).compile().get
  }

  test("Import.Namespace01") {
    val input1 =
      """namespace a.b.c {
        |  def f: Int = 42
        |}
      """.stripMargin
    val input2 =
      """import a.b.c
        |def g: Int = c/f() + 42
      """.stripMargin
    new Flix().addStr(input1).addStr(input2).compile().get
  }

  test("Definition.Function01") {
    val input = "def foo(): Int = 42"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Definition.Function02") {
    val input = "def foo(x: Int): Int = 42"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Definition.Function03") {
    val input = "def foo(x: Int, y: Int, z: Int): Int = x + y + z"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Definition.Enum01") {
    val input =
      """enum A {
        |  case B
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Enum])
  }

  test("Definition.Enum02") {
    val input =
      """enum A {
        |  case B(Int)
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Enum])
  }

  test("Definition.Enum03") {
    val input =
      """enum A {
        |  case B,
        |  case C(Int),
        |  case D(Bool, Int, Str)
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Enum])
  }

  // TODO: Allow naming of the enum attributes.

  test("Definition.BoundedLattice01") {
    val input = "let a<> = (Tag.Bot, Tag.Top, leq, lub, glb)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.BoundedLattice])
  }

  test("Definition.BoundedLattice02") {
    val input = "let a<> = (Tag.Bot, Tag.Top, foo/leq, bar/lub, baz.qux/glb)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.BoundedLattice])
  }

  test("Definition.BoundedLattice03") {
    val input = "let a<> = (Tag.Bot, Tag.Top, fn (x: Int, y: Int): Bool = true, bar/lub, baz.qux/glb)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.BoundedLattice])
  }

  test("Definition.Relation01") {
    val input = "rel A(b: B)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Relation])
  }

  test("Definition.Relation02") {
    val input = "rel A(b: B, c: C, d: D)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Relation])
  }

  test("Definition.Lattice01") {
    val input = "lat A(b: B)"
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Lattice])
  }

  test("Definition.Index01") {
    val input = "index A({x});"
    val result = new Parser(SourceInput.Str(input)).IndexDefinition.run()
    assert(result.isSuccess)
  }

  test("Definition.Index02") {
    val input = "index A({x}, {x, y});"
    val result = new Parser(SourceInput.Str(input)).IndexDefinition.run()
    assert(result.isSuccess)
  }

  test("Definition.Index03") {
    val input = "index A({x}, {y}, {x, w}, {x, y, z, w});"
    val result = new Parser(SourceInput.Str(input)).IndexDefinition.run()
    assert(result.isSuccess)
  }

  test("Definition.Class01") {
    val input =
      """class Eq[A] {
        |  fn eq(x: A, y: B): Bool
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Class])
  }

  test("Definition.Class02") {
    val input =
      """class Coerce[A, B] {
        |  fn coerce(a: A): B
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Class])
  }

  test("Definition.Class03") {
    val input =
      """class Ord[A] => Eq[A] {
        |  fn eq(x: A, y: A): Bool
        |  fn lessEq(x: A, y: A): Bool
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Class])
  }

  test("Definition.Class04") {
    val input =
      """class Eq[A] => PartialOrd[A], PreOrd[A] {
        |  /* ... */
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Class])
  }

  test("Definition.Law01") {
    val input = "law f(): Bool = true"
    val result = new Parser(SourceInput.Str(input)).LawDefinition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Law])
  }

  test("Definition.Law02") {
    val input = "law f(x: Int): Bool = x % 2 == 0"
    val result = new Parser(SourceInput.Str(input)).LawDefinition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Law])
  }

  test("Definition.Law03") {
    val input = "law f(x: Int, y: Int): Bool = x > y"
    val result = new Parser(SourceInput.Str(input)).LawDefinition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Law])
  }

  test("Definition.Impl01") {
    val input =
      """impl Eq[Int] {
        |  /* ... */
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Impl])
  }

  test("Definition.Impl02") {
    val input =
      """impl Eq[(Int, Int)] {
        |  /* ... */
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Impl])
  }

  test("Definition.Impl03") {
    val input =
      """impl Ord[Int] <= Eq[Int] {
        |  /* ... */
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Impl])
  }

  test("Definition.Impl04") {
    val input =
      """impl A[Int, Int] <= B[Int], C[Int] {
        |  /* ... */
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Definition.run().get
    assert(result.isInstanceOf[ParsedAst.Definition.Impl])
  }

  /////////////////////////////////////////////////////////////////////////////
  // Expressions                                                             //
  /////////////////////////////////////////////////////////////////////////////

  test("Expression.Char01") {
    val input = "'a'"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Char])
  }

  test("Expression.Char02") {
    val input = "'x'"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Char])
  }

  test("Expression.Float32.01") {
    val input = "def f: Float32 = 123.456f32"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Float32.02") {
    val input = "def f: Float32 = -123.456f32"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Float64.01") {
    val input = "def f: Float64 = 123.456f64"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Float64.02") {
    val input = "def f: Float64 = -123.456f64"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Int8") {
    val input = "123i8"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Int8])
  }

  test("Expression.Int16") {
    val input = "123i16"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Int16])
  }

  test("Expression.Int32") {
    val input = "123i32"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Int32])
  }

  test("Expression.Int64") {
    val input = "123i64"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Int64])
  }

  test("Expression.LogicalExp01") {
    val input = "true && false"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.LogicalAnd)(result.op)
  }

  test("Expression.LogicalExp02") {
    val input = "true || false"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.LogicalOr)(result.op)
  }

  test("Expression.LogicalExp03") {
    val input = "1 < 2 && 3 < 4"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.LogicalAnd)(result.op)
  }

  test("Expression.ComparisonExp01") {
    val input = "1 < 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Less)(result.op)
  }

  test("Expression.ComparisonExp02") {
    val input = "1 + 2 > 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Greater)(result.op)
  }

  test("Expression.ComparisonExp03") {
    val input = "1 + 2 > 3 + 4"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Greater)(result.op)
  }

  test("Expression.MultiplicativeExp01") {
    val input = "1 * 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Times)(result.op)
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
  }

  test("Expression.MultiplicativeExp02") {
    val input = "1 * 2 * 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Times)(result.op)
  }

  test("Expression.MultiplicativeExp03") {
    val input = "1 * 2 + 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Plus)(result.op)
  }

  test("Expression.MultiplicativeExp04") {
    val input = "1 + 2 * 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Plus)(result.op)
  }

  test("Expression.AdditiveExp01") {
    val input = "1 + 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Plus)(result.op)
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
  }

  test("Expression.AdditiveExp02") {
    val input = "1 + 2 + 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Plus)(result.op)
  }

  test("Expression.AdditiveExp03") {
    val input = "1 - 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Minus)(result.op)
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
  }

  test("Expression.AdditiveExp04") {
    val input = "1 - 2 - 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Minus)(result.op)
    val e1 = result.e1.asInstanceOf[ParsedAst.Expression.Binary]
    assertResult(BinaryOperator.Minus)(e1.op)
    assert(e1.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(e1.e2.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
  }

  test("Expression.AdditiveExp05") {
    val input = "1 + 2 - 3 + 4 - 5 + 6"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
  }

  test("Expression.Infix01") {
    val input = "1 `plus` 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Infix]
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
    //assertResult(Seq("plus"))(result.name.parts)
  }

  test("Expression.Infix02") {
    val input = "1 `foo.bar.baz/plus` 2"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Infix]
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Lit])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Lit])
    //assertResult(Seq("foo", "bar", "baz", "plus"))(result.name.parts)
  }

  ignore("Expression.Infix03") {
    val input = "+1 `plus` -1"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Infix]
    assert(result.e1.isInstanceOf[ParsedAst.Expression.Unary])
    assert(result.e2.isInstanceOf[ParsedAst.Expression.Unary])
  }

  test("Expression.UnaryExp01") {
    val input = "+ 1"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Unary]
    assertResult(UnaryOperator.Plus)(result.op)
  }

  test("Expression.UnaryExp02") {
    val input = "- 1"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Unary]
    assertResult(UnaryOperator.Minus)(result.op)
  }

  test("Expression.UnaryExp03") {
    val input = "!! true"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Unary]
    assertResult(UnaryOperator.LogicalNot)(result.op)
  }

  test("Expression.Ascribe01") {
    val input = "true: Bool"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Ascribe])
  }

  test("Expression.Ascribe02") {
    val input = "x: Bool -> Int"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Ascribe])
  }

  test("Expression.LiteralExp01") {
    val input = "true"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assertResult("true")(result.lit.asInstanceOf[ParsedAst.Literal.Bool].lit)
  }

  test("Expression.LiteralExp02") {
    val input = "42"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assertResult("42")(result.lit.asInstanceOf[ParsedAst.Literal.Int32].lit)
  }

  test("Expression.LiteralExp03") {
    val input = "\"foo\""
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assertResult("foo")(result.lit.asInstanceOf[ParsedAst.Literal.Str].lit)
  }

  test("Expression.LetMatch01") {
    val input = "let x = 42 in x"
    new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.LetMatch]
  }

  test("Expression.LetMatch02") {
    val input = "let x' = f(1, 2, 3) in g(4, 5, 6)"
    new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.LetMatch]
  }

  test("Expression.LetMatch03") {
    val input =
      """let x = 1 in
        |let y = 2 in
        |let z = 3 in
        |  42""".stripMargin
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    val l1 = result.get.asInstanceOf[ParsedAst.Expression.LetMatch]
    val l2 = l1.body.asInstanceOf[ParsedAst.Expression.LetMatch]
    val l3 = l2.body.asInstanceOf[ParsedAst.Expression.LetMatch]
  }

  test("Expression.IfThenElseExp01") {
    val input = "if (1) 2 else 3"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.IfThenElse])
  }

  test("Expression.IfThenElseExp02") {
    val input = "if (f(1, 2, 3)) g(4, 5, 6) else h(7, 8, 9)"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.IfThenElse])
  }

  test("Expression.IfThenElseExp03") {
    val input = "if ((1)) (2) else (3)"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.IfThenElse])
  }

  test("Expression.Switch01") {
    val input =
      """fn f(x: Int): Int = switch {
        |  case true  => 1
        |}
      """.stripMargin
    val result = new Flix().addStr(input).compile()
    assert(result.isSuccess)
  }

  test("Expression.Switch02") {
    val input =
      """fn f(x: Int): Int = switch {
        |  case x < 0  => 1
        |}
      """.stripMargin
    val result = new Flix().addStr(input).compile()
    assert(result.isSuccess)
  }

  test("Expression.Switch03") {
    val input =
      """fn f(x: Int): Int = switch {
        |  case x < 0  => 1
        |  case x > 0  => 2
        |  case x == 0 => 3
        |}
      """.stripMargin
    val result = new Flix().addStr(input).compile()
    assert(result.isSuccess)
  }

  test("Expression.MatchExp01") {
    val input =
      """match 1 with {
        |  case 2 => 3
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Match])
  }

  test("Expression.MatchExp02") {
    val input =
      """match 1 with {
        |  case 2 => 3
        |  case 4 => 5
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Match])
  }

  test("Expression.MatchExp03") {
    val input =
      """match 1 with {
        |  case 2 => match 3 with {
        |    case 4 => 5
        |  }
        |  case 6 => 7
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    val m1 = result.asInstanceOf[ParsedAst.Expression.Match]
    val m2 = m1.rules.head._2.asInstanceOf[ParsedAst.Expression.Match]
    val l = m2.rules.head._2.asInstanceOf[ParsedAst.Expression.Lit]
    assertResult("5")(l.lit.asInstanceOf[ParsedAst.Literal.Int32].lit)
  }

  test("Expression.MatchExp04") {
    val input =
      """match
        |  match 1 with {
        |    case 2 => 3
        |  } with {
        |    case 4 => 5
        |}
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    val m1 = result.asInstanceOf[ParsedAst.Expression.Match]
    val m2 = m1.e.asInstanceOf[ParsedAst.Expression.Match]
    val l = m2.rules.head._2.asInstanceOf[ParsedAst.Expression.Lit]
    assertResult("3")(l.lit.asInstanceOf[ParsedAst.Literal.Int32].lit)
  }

  test("Expression.CallExp01") {
    val input = "f()"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Apply])
  }

  test("Expression.CallExp02") {
    val input = "f(1, 2, 3)"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Apply])
  }

  test("Expression.CallExp03") {
    val input = "f(f(1), f(f(2)))"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Apply])
  }

  test("Expression.CallExp04") {
    val input = "foo.bar.baz/f(1, 2, 3)"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Apply])
  }

  test("Expression.Tag01") {
    val input = "Foo.Bar"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Tag])
  }

  test("Expression.Tag02") {
    val input = "Foo.Bar ()"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Tag])
  }

  test("Expression.Tag03") {
    val input = "Foo.Bar Baz.Qux"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Tag])
  }

  test("Expression.Tag04") {
    val input = "Foo.Bar (x, y)"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Tag])
  }

  test("Expression.Tag05") {
    val input = "foo.bar/Baz.Qux (42, x, (3, 4))"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get
    assert(result.isInstanceOf[ParsedAst.Expression.Tag])
  }

  test("Expression.Tuple01") {
    val input = "()"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    assert(result.lit.isInstanceOf[ParsedAst.Literal.Unit])
  }

  test("Expression.Tuple02") {
    val input = "(1)"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lit]
    val literal = result.lit.asInstanceOf[Literal.Int32]
    assertResult("1")(literal.lit)
  }

  test("Expression.Tuple03") {
    val input = "(1, x)"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Tuple]
    assertResult(2)(result.elms.size)
  }

  test("Expression.Tuple04") {
    val input = "(1, 2, x, 4, 5, 6)"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Tuple]
    assertResult(6)(result.elms.size)
  }

  test("Expression.Tuple05") {
    val input = "((1, 2), (x, (4, 5)))"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Tuple]
    assertResult(2)(result.elms.size)
  }

  test("Expression.Opt01") {
    val input = "def f: Opt[Char] = None"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Opt02") {
    val input = "def f: Opt[Int] = None"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Opt03") {
    val input = "def f: Opt[Char] = Some('a')"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Opt04") {
    val input = "def f: Opt[Int] = Some(42)"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Opt05") {
    val input = "def f: Opt[(Char, Int)] = None"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Opt06") {
    val input = "def f: Opt[(Char, Int)] = Some(('a', 42))"
    new Flix().addStr(input).compile().get
  }

  test("Expression.List01") {
    val input = "def f: List[Int] = Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.List02") {
    val input = "def f: List[Int] = 1 :: Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.List03") {
    val input = "def f: List[Int] = 1 :: 2 :: Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.List04") {
    val input = "def f: List[(Int, Int)] = Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.List05") {
    val input = "def f: List[(Int, Int)] = (1, 2) :: (3, 4) :: Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.ListList01") {
    val input = "def f: List[List[Int]] = Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.ListList02") {
    val input = "def f: List[List[Int]] = (1 :: Nil) :: Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.ListList03") {
    val input = "def f: List[List[Int]] = (Nil) :: (1 :: Nil) :: (2 :: 3 :: 4 :: Nil) :: Nil"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set01") {
    val input = "def f: Set[Int] = #{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set02") {
    val input = "def f: Set[Int] = #{1}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set03") {
    val input = "def f: Set[Int] = #{1, 2}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set04") {
    val input = "def f: Set[Int] = #{1, 2, 3}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set05") {
    val input = "def f: Set[(Int, Int)] = #{(1, 2)}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set06") {
    val input = "def f: Set[(Int, Int)] = #{(1, 2), (3, 4)}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Set07") {
    val input = "def f: Set[Int] = #{1 + 2, 3 + 4, 5 + 6}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.SetSet01") {
    val input = "def f: Set[Set[Int]] = #{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.SetSet02") {
    val input = "def f: Set[Set[Int]] = #{#{}}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.SetSet03") {
    val input = "def f: Set[Set[Int]] = #{#{1, 2}, #{3, 4}, #{5, 6}}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map01") {
    val input = "def f: Map[Char, Int] = @{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map02") {
    val input = "def f: Map[Char, Int] = @{'a' -> 1}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map03") {
    val input = "def f: Map[Char, Int] = @{'a' -> 1, 'b' -> 2}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map04") {
    val input = "def f: Map[Char, Int] = @{'a' -> 1, 'b' -> 2, 'c' -> 3}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map05") {
    val input = "def f: Map[(Int8, Int16), (Int32, Int64)] = @{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Map06") {
    val input = "def f: Map[(Int8, Int16), (Int32, Int64)] = @{(1i8, 2i16) -> (3i32, 4i64)}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapMap01") {
    val input = "def f: Map[Int, Map[Int, Char]] = @{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapMap02") {
    val input = "def f: Map[Int, Map[Int, Char]] = @{1 -> @{}}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapMap03") {
    val input = "def f: Map[Int, Map[Int, Char]] = @{1 -> @{}, 2 -> @{3 -> 'a', 4 -> 'b'}}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapList01") {
    val input = "def f: Map[Int, List[Int]] = @{1 -> 2 :: 3 :: Nil, 4 -> 5 :: 6 :: Nil}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapListSet01") {
    val input = "def f: Map[Int, List[Set[Int]]] = @{}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapListSet02") {
    val input = "def f: Map[Int, List[Set[Int]]] = @{1 -> Nil}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.MapListSet04") {
    val input = "def f: Map[Int, List[Set[Int]]] = @{1 -> #{1, 2, 3} :: #{4, 5, 6} :: Nil}"
    new Flix().addStr(input).compile().get
  }

  test("Expression.Var01") {
    val input = "x"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Var])
  }

  test("Expression.Lambda01") {
    val input = "fn(x: Int): Int = 42"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lambda]
    assert(result.body.isInstanceOf[ParsedAst.Expression.Lit])
  }

  test("Expression.Lambda02") {
    val input = "fn(x: Bool, y: Int, z: Str): Str = x + y + z"
    val result = new Parser(SourceInput.Str(input)).Expression.run().get.asInstanceOf[ParsedAst.Expression.Lambda]
    assert(result.body.isInstanceOf[ParsedAst.Expression.Binary])
  }

  test("Expression.ErrorExp01") {
    val input = "??? : Int"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.UserError])
  }

  test("Expression.Bot01") {
    val input = "⊥"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Bot])
  }

  test("Expression.Top01") {
    val input = "⊤"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Top])
  }

  test("Expression.Leq01") {
    val input = "x ⊑ y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Leq)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.Lub01") {
    val input = "x ⊔ y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Lub)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.Glb1") {
    val input = "x ⊓ y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Glb)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.Widen01") {
    val input = "x ▽ y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Widen)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.Narrow01") {
    val input = "x △ y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Narrow)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.BotLeqTop") {
    val input = "⊥ ⊑ ⊤"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.ExtendedBinary])
    assertResult(ExtendedBinaryOperator.Leq)(result.get.asInstanceOf[ParsedAst.Expression.ExtendedBinary].op)
  }

  test("Expression.Existential01") {
    val input = "∃(x: Bool). true"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Existential])
  }

  test("Expression.Existential02") {
    val input = "∃(x: Int, y: Int). x == y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Existential])
  }

  test("Expression.Existential03") {
    val input = "\\exists(x: Bool). true"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Existential])
  }

  test("Expression.Existential04") {
    val input = "\\exists(x: Int, y: Int). x == y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Existential])
  }

  test("Expression.Universal01") {
    val input = "∀(x: Bool). true"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Universal])
  }

  test("Expression.Universal02") {
    val input = "∀(x: Int, y: Int). x == y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Universal])
  }

  test("Expression.Universal03") {
    val input = "\\forall(x: Bool). true"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Universal])
  }

  test("Expression.Universal04") {
    val input = "\\forall(x: Int, y: Int). x == y"
    val result = new Parser(SourceInput.Str(input)).Expression.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Expression.Universal])
  }


  /////////////////////////////////////////////////////////////////////////////
  // Patterns                                                                //
  /////////////////////////////////////////////////////////////////////////////
  test("Pattern.Wildcard") {
    val input = "_"
    val result = new Parser(SourceInput.Str(input)).Pattern.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Pattern.Wildcard])
  }

  test("Pattern.Var01") {
    val input = "x"
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Var]
    assertResult("x")(result.ident.name)
  }

  test("Pattern.Var02") {
    val input = "foo_Bar'"
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Var]
    assertResult("foo_Bar'")(result.ident.name)
  }

  test("Pattern.Literal01") {
    val input = "true"
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Lit]
    assertResult("true")(result.lit.asInstanceOf[ParsedAst.Literal.Bool].lit)
  }

  test("Pattern.Literal02") {
    val input = "42"
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Lit]
    assertResult("42")(result.lit.asInstanceOf[ParsedAst.Literal.Int32].lit)
  }

  test("Pattern.Literal03") {
    val input = "\"foo\""
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Lit]
    assertResult("foo")(result.lit.asInstanceOf[ParsedAst.Literal.Str].lit)
  }

  test("Pattern.Tag01") {
    val input = "Const.Bot"
    val result = new Parser(SourceInput.Str(input)).Pattern.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Pattern.Tag])
  }

  test("Pattern.Tag02") {
    val input = "Const.Cst(5)"
    val result = new Parser(SourceInput.Str(input)).Pattern.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Pattern.Tag])
  }

  test("Pattern.Tag03") {
    val input = "Foo.Bar (x, _, z)"
    val result = new Parser(SourceInput.Str(input)).Pattern.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Pattern.Tag])
  }

  test("Pattern.Tag04") {
    val input = "foo.bar/baz.Foo(x, y, z)"
    val result = new Parser(SourceInput.Str(input)).Pattern.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[ParsedAst.Pattern.Tag])
  }

  test("Pattern.Tuple01") {
    val input = "(x, y, true)"
    val result = new Parser(SourceInput.Str(input)).Pattern.run().get.asInstanceOf[ParsedAst.Pattern.Tuple]
    assertResult(3)(result.pats.size)
  }

  test("Pattern.Opt01") {
    val input =
      """def f(o: Opt[Int]): Int = match o with {
        |  case None => 0
        |  case Some(x) => x
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Opt02") {
    val input =
      """def f(o: Opt[Int]): Int = match o with {
        |  case None => 0
        |  case Some(1) => 1
        |  case Some(2) => 2
        |  case Some(x) => x + x
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Opt03") {
    val input =
      """def f(o: Opt[Char]): Int = match o with {
        |  case None => 0
        |  case Some('a') => 1
        |  case Some('b') => 2
        |  case Some(c)   => 3
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List01") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case Nil => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List02") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case 1 :: Nil => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List03") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case 1 :: 2 :: Nil => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List04") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case 1 :: 2 :: 3 :: Nil => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List05") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case x :: Nil => x
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List06") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case x :: y :: Nil => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List07") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case Nil => 0
        |  case x :: rs => 1 + f(rs)
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List08") {
    val input =
      """def f(xs: List[Int]): Bool = match xs with {
        |  case Nil => true
        |  case x :: y :: rs => f(rs)
        |  case _ => false
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List09") {
    val input =
      """def f(xs: List[Int]): Int = match xs with {
        |  case Nil => 0
        |  case x :: Nil => x
        |  case x :: y :: Nil => x + y
        |  case xs => 42
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List10") {
    val input =
      """def f(xs: List[(Char, Int)]): Int = match xs with {
        |  case Nil => 0
        |  case (c, i) :: Nil => i
        |  case (c1, i1) :: (c2, i2) :: Nil => i1 + i2
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.List11") {
    val input =
      """def f(xs: List[(Char, Int)]): Int = match xs with {
        |  case Nil => 0
        |  case (c, 42) :: Nil => 1
        |  case ('a', i1) :: (c2, 21) :: Nil => 2
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.ListList01") {
    val input =
      """def f(xs: List[List[Int]]): Int = match xs with {
        |  case Nil => 0
        |  case (x :: Nil) :: (y :: Nil) :: Nil => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.ListList02") {
    val input =
      """def f(xs: List[List[Int]]): Int = match xs with {
        |  case Nil => 0
        |  case (x :: y :: Nil) :: (z :: w :: Nil) :: Nil => x + y + z + w
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.ListList03") {
    val input =
      """def f(xs: List[List[Int]]): Int = match xs with {
        |  case Nil => 0
        |  case (x :: xs) :: (y :: ys) :: (z :: zs) :: Nil => x + y + z
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set01") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set02") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{1} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set03") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{1, 2, 3} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set04") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{1, 2, 3, rs...} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set05") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{x} => x
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set06") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{x, y} => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set07") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{x, y, z} => x + y + z
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set08") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{} => 0
        |  case #{x, y, z, rs...} => f(rs)
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Set09") {
    val input =
      """def f(xs: Set[Int]): Int = match xs with {
        |  case #{} => 0
        |  case #{x} => x
        |  case #{x, rs...} => x + fs(rs)
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.SetSet01") {
    val input =
      """def f(xs: Set[Set[Int]]): Int = match xs with {
        |  case #{} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.SetSet02") {
    val input =
      """def f(xs: Set[Set[Int]]): Int = match xs with {
        |  case #{#{x}, #{y}, rs...} => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.SetSet03") {
    val input =
      """def f(xs: Set[Set[Int]]): Int = match xs with {
        |  case #{#{x, y, as...}, #{z, w, bs...}, rs...} => x + y + z + w
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map01") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map02") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> 42} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map03") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> 42, 'b' -> 21} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map04") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> 42, 'b' -> 21, c -> 11} => 0
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map05") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> x} => x
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map06") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> x, 'b' -> y} => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map07") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{'a' -> x, 'b' -> y, rs...} => x + y
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  test("Pattern.Map08") {
    val input =
      """def f(xs: Map[Char, Int]): Int = match xs with {
        |  case @{} => 0
        |  case @{'a' -> x} => x
        |  case @{'a' -> x, rs...} => f(rs)
        |}
      """.stripMargin
    new Flix().addStr(input).compile().get
  }

  /////////////////////////////////////////////////////////////////////////////
  // Facts and Rules                                                         //
  /////////////////////////////////////////////////////////////////////////////
  test("FactDeclaration01") {
    val input = "P(42)."
    val result = new Parser(SourceInput.Str(input)).FactDeclaration.run()
    assert(result.isSuccess)
  }

  test("FactDeclaration02") {
    val input = "P(\"foo\")."
    val result = new Parser(SourceInput.Str(input)).FactDeclaration.run()
    assert(result.isSuccess)
  }

  test("FactDeclaration03") {
    val input = "P(f(1, 2, 3))."
    val result = new Parser(SourceInput.Str(input)).FactDeclaration.run()
    assert(result.isSuccess)
  }

  test("RuleDeclaration01") {
    val input = "P(x) :- A(x)."
    val result = new Parser(SourceInput.Str(input)).RuleDeclaration.run()
    assert(result.isSuccess)
    assertResult(1)(result.get.body.size)
  }

  test("RuleDeclaration02") {
    val input = "P(x, y, z) :- A(x), B(y), C(z)."
    val result = new Parser(SourceInput.Str(input)).RuleDeclaration.run()
    assert(result.isSuccess)
    assertResult(3)(result.get.body.size)
  }

  test("RuleDeclaration03") {
    val input = "P(f(x), g(y, z)) :- isFoo(x, y), isBar(y, z), A(x), B(y), C(z)."
    val result = new Parser(SourceInput.Str(input)).RuleDeclaration.run()
    assert(result.isSuccess)
    assertResult(5)(result.get.body.size)
  }

  test("Rule.Loop01") {
    val input = "P(x, z) :- A(x, y), z <- f(y)."
    val result = new Parser(SourceInput.Str(input)).RuleDeclaration.run()
    assert(result.isSuccess)
  }

  test("Predicate.Alias01") {
    val input = "r := 42"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate.Alias])
  }

  ignore("Predicate.Alias02") {
    val input = "r := (true, 42, \"foo\")"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate.Alias])
  }

  test("Predicate.Alias03") {
    val input = "r := f(x, g(y, z))"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate.Alias])
  }

  test("Predicate.Loop01") {
    val input = "y <- f(x)"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate.Loop])
  }

  test("Predicate.Loop02") {
    val input = "x <- f(1, 2, 3)"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate.Loop])
  }

  test("Predicate.NotEqual01") {
    val input = "x != y"
    val result = new Parser(SourceInput.Str(input)).Predicate.run().get
    assert(result.isInstanceOf[ParsedAst.Predicate])
  }

  /////////////////////////////////////////////////////////////////////////////
  // Terms                                                                   //
  /////////////////////////////////////////////////////////////////////////////
  test("Term01") {
    val input = "_"
    val result = new Parser(SourceInput.Str(input)).Term.run().get
    assert(result.isInstanceOf[ParsedAst.Term.Wildcard])
  }

  test("Term02") {
    val input = "x"
    val result = new Parser(SourceInput.Str(input)).Term.run().get.asInstanceOf[ParsedAst.Term.Var]
    assertResult("x")(result.ident.name)
  }

  test("Term03") {
    val input = "42"
    val result = new Parser(SourceInput.Str(input)).Term.run().get.asInstanceOf[ParsedAst.Term.Lit]
    assertResult("42")(result.lit.asInstanceOf[ParsedAst.Literal.Int32].lit)
  }

  test("Term04") {
    val input = "foo(x)"
    val result = new Parser(SourceInput.Str(input)).Term.run().get.asInstanceOf[ParsedAst.Term.Apply]
    // assertResult(Seq("foo"))(result.name.parts)
  }

  test("Term05") {
    val input = "foo/bar(x, y, z)"
    val result = new Parser(SourceInput.Str(input)).Term.run().get.asInstanceOf[ParsedAst.Term.Apply]
    //assertResult(Seq("foo", "bar"))(result.name.parts)
    //assertResult(Seq("x", "y", "z"))(result.args.map(_.asInstanceOf[ParsedAst.Term.Var].ident.name))
  }

  /////////////////////////////////////////////////////////////////////////////
  // Types                                                                   //
  /////////////////////////////////////////////////////////////////////////////
  test("Type.Lambda01") {
    val input = "(A) -> B"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda02") {
    val input = "(A, B) -> C"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda03") {
    val input = "((A, B)) -> C"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda04") {
    val input = "(A) -> (B, C)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda05") {
    val input = "(A) -> (B) -> C"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda06") {
    val input = "(A) -> ((B) -> C)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda07") {
    val input = "((A) -> B) -> C"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda08") {
    val input = "(A, B, C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda09") {
    val input = "((A, B), C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args(0).isInstanceOf[Type.Tuple])
    assert(result.args(0).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.args(1).isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda10") {
    val input = "(((A, B), C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(0).isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(0).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(1).isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda11") {
    val input = "(A, (B, C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args(0).isInstanceOf[Type.Unresolved])
    assert(result.args(1).isInstanceOf[Type.Tuple])
    assert(result.args(1).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda12") {
    val input = "((A, (B, C))) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(0).isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(1).isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms(1).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda13") {
    val input = "((A, B, C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda14") {
    val input = "(A, B) -> (C, D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda15") {
    val input = "((A, B)) -> (C, D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda16") {
    val input = "(A) -> (B, C, D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda17") {
    val input = "(A) -> ((B, C), D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(0).isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(0).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(1).isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda18") {
    val input = "(A) -> (B, (C, D))"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(0).isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(1).isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms(1).asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda19") {
    val input = "(A, B) -> (C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda20") {
    val input = "(A, B) -> ((C) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda21") {
    val input = "((A, B) -> C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda22") {
    val input = "((A, B)) -> (C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda23") {
    val input = "((A, B)) -> ((C) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda24") {
    val input = "(((A, B)) -> C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda25") {
    val input = "(A) -> (B, C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda26") {
    val input = "(A) -> ((B, C) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda27") {
    val input = "((A) -> (B, C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Tuple])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda28") {
    val input = "(A) -> ((B, C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda29") {
    val input = "(A) -> (((B, C)) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda30") {
    val input = "(A) -> (B) -> (C, D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda31") {
    val input = "(A) -> ((B) -> (C, D))"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda32") {
    val input = "((A) -> (B)) -> (C, D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Tuple])
    assert(result.retTpe.asInstanceOf[Type.Tuple].elms.forall(_.isInstanceOf[Type.Unresolved]))
  }

  test("Type.Lambda33") {
    val input = "(A) -> (B) -> (C) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda34") {
    val input = "(A) -> ((B) -> (C) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda35") {
    val input = "(A) -> ((B) -> ((C) -> D))"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda36") {
    val input = "(A) -> (((B) -> (C)) -> D)"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda37") {
    val input = "((A) -> (B) -> (C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda38") {
    val input = "((A) -> ((B) -> (C))) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda39") {
    val input = "(((A) -> (B)) -> (C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Lambda])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Lambda40") {
    val input = "(A) -> ((B) -> (C)) -> D"
    val result = new Parser(SourceInput.Str(input)).Type.run().get.asInstanceOf[Type.Lambda]
    assert(result.args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Lambda])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].args.head.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].args.head.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
    assert(result.retTpe.asInstanceOf[Type.Lambda].retTpe.isInstanceOf[Type.Unresolved])
  }

  test("Type.Tuple01") {
    val input = "()"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assertResult(result.get)(Type.Unit)
  }

  test("Type.Tuple02") {
    val input = "(A)"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Unresolved])
  }

  test("Type.Tuple03") {
    val input = "(A, B)"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Tuple])
    assertResult(2)(result.get.asInstanceOf[Type.Tuple].elms.length)
  }

  test("Type.Tuple04") {
    val input = "(A, B, C)"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Tuple])
    assertResult(3)(result.get.asInstanceOf[Type.Tuple].elms.length)
  }

  test("Type.Parametric01") {
    val input = "A[B]"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Parametric])
  }

  test("Type.Parametric02") {
    val input = "A[B, C]"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Parametric])
  }

  test("Type.Parametric03") {
    val input = "A[B, C[D, E]]"
    val result = new Parser(SourceInput.Str(input)).Type.run()
    assert(result.isSuccess)
    assert(result.get.isInstanceOf[Type.Parametric])
  }

  /////////////////////////////////////////////////////////////////////////////
  // Identifiers & Names                                                     //
  /////////////////////////////////////////////////////////////////////////////
  test("Ident01") {
    val input = "x"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("x")(result.name)
  }

  test("Ident02") {
    val input = "y"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("y")(result.name)
  }

  test("Ident03") {
    val input = "x0"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("x0")(result.name)
  }

  test("Ident04") {
    val input = "x'"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("x'")(result.name)
  }

  test("Ident05") {
    val input = "foobar"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("foobar")(result.name)
  }

  test("Ident06") {
    val input = "fooBar"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("fooBar")(result.name)
  }

  test("Ident07") {
    val input = "foo_bar"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("foo_bar")(result.name)
  }

  test("Ident08") {
    val input = "f00_BAR'''"
    val result = new Parser(SourceInput.Str(input)).Ident.run().get
    assertResult("f00_BAR'''")(result.name)
  }

  test("Ident09") {
    val input = "1"
    val result = new Parser(SourceInput.Str(input)).Ident.run()
    assert(result.isFailure)
  }

  test("Ident10") {
    val input = "'"
    val result = new Parser(SourceInput.Str(input)).Ident.run()
    assert(result.isFailure)
  }

  test("Ident11") {
    val input = "_"
    val result = new Parser(SourceInput.Str(input)).Ident.run()
    assert(result.isFailure)
  }

  test("QName01") {
    val input = "x"
    val result = new Parser(SourceInput.Str(input)).QName.run().get
    //assertResult(Seq("x"))(result.parts)
  }

  test("QName02") {
    val input = "x::y"
    val result = new Parser(SourceInput.Str(input)).QName.run().get
    //assertResult(Seq("x", "y"))(result.parts)
  }

  test("QName03") {
    val input = "x::y::z"
    val result = new Parser(SourceInput.Str(input)).QName.run().get
    //assertResult(Seq("x", "y", "z"))(result.parts)
  }

  test("QName04") {
    val input = "abc::def::hij"
    val result = new Parser(SourceInput.Str(input)).QName.run().get
    //assertResult(Seq("abc", "def", "hij"))(result.parts)
  }

  /////////////////////////////////////////////////////////////////////////////
  // Literals                                                                //
  /////////////////////////////////////////////////////////////////////////////
  test("Literal (true)") {
    val input = "true"
    val result = new Parser(SourceInput.Str(input)).Literal.run().get.asInstanceOf[ParsedAst.Literal.Bool]
    assertResult("true")(result.lit)
  }

  test("Literal (false)") {
    val input = "false"
    val result = new Parser(SourceInput.Str(input)).Literal.run().get.asInstanceOf[ParsedAst.Literal.Bool]
    assertResult("false")(result.lit)
  }

  test("Literal (123)") {
    val input = "123"
    val result = new Parser(SourceInput.Str(input)).Literal.run().get.asInstanceOf[ParsedAst.Literal.Int32]
    assertResult("123")(result.lit)
  }

  test("Literal (\"\")") {
    val input = "\"\""
    val result = new Parser(SourceInput.Str(input)).Literal.run().get.asInstanceOf[ParsedAst.Literal.Str]
    assertResult("")(result.lit)
  }

  test("Literal (\"foo\")") {
    val input = "\"foo\""
    val result = new Parser(SourceInput.Str(input)).Literal.run().get.asInstanceOf[ParsedAst.Literal.Str]
    assertResult("foo")(result.lit)
  }

  /////////////////////////////////////////////////////////////////////////////
  // Operators                                                               //
  /////////////////////////////////////////////////////////////////////////////
  test("Operator.Unary !") {
    val input = "!"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.UnaryOp).get
    assertResult(UnaryOperator.LogicalNot)(result)
  }

  test("Operator.Unary +") {
    val input = "+"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.UnaryOp).get
    assertResult(UnaryOperator.Plus)(result)
  }

  test("Operator.Unary -") {
    val input = "-"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.UnaryOp).get
    assertResult(UnaryOperator.Minus)(result)
  }

  test("Operator.Unary ~") {
    val input = "~"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.UnaryOp).get
    assertResult(UnaryOperator.BitwiseNegate)(result)
  }

  test("Operator.Binary.LogicalOp &&") {
    val input = "&&"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.LogicalAnd)(result)
  }

  test("Operator.Binary.LogicalOp ||") {
    val input = "||"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.LogicalOr)(result)
  }

  test("Operator.Binary.LogicalOp ->") {
    val input = "==>"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.Implication)(result)
  }

  test("Operator.Binary.LogicalOp <->") {
    val input = "<==>"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.Biconditional)(result)
  }

  test("Operator.Binary.Bitwise &") {
    val input = "&"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.BitwiseAnd)(result)
  }

  test("Operator.Binary.Bitwise |") {
    val input = "|"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.BitwiseOr)(result)
  }

  test("Operator.Binary.Bitwise ^") {
    val input = "^"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.BitwiseXor)(result)
  }

  test("Operator.Binary.Bitwise <<") {
    val input = "<<"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.BitwiseLeftShift)(result)
  }

  test("Operator.Binary.Bitwise >>") {
    val input = ">>"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.BitwiseRightShift)(result)
  }

  test("Operator.Binary.ComparisonOp <") {
    val input = "<"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.Less)(result)
  }

  test("Operator.Binary.ComparisonOp <=") {
    val input = "<="
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.LessEqual)(result)
  }

  test("Operator.Binary.ComparisonOp >") {
    val input = ">"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.Greater)(result)
  }

  test("Operator.Binary.ComparisonOp >=") {
    val input = ">="
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.GreaterEqual)(result)
  }

  test("Operator.Binary.ComparisonOp ==") {
    val input = "=="
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.Equal)(result)
  }

  test("Operator.Binary.ComparisonOp !=") {
    val input = "!="
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.NotEqual)(result)
  }

  test("Operator.Binary.MultiplicativeOp *") {
    val input = "*"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.MultiplicativeOp).get
    assertResult(BinaryOperator.Times)(result)
  }

  test("Operator.Binary.MultiplicativeOp /") {
    val input = "/"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.MultiplicativeOp).get
    assertResult(BinaryOperator.Divide)(result)
  }

  test("Operator.Binary.MultiplicativeOp %") {
    val input = "%"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.MultiplicativeOp).get
    assertResult(BinaryOperator.Modulo)(result)
  }

  test("Operator.Binary.AdditiveOp +") {
    val input = "+"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.AdditiveOp).get
    assertResult(BinaryOperator.Plus)(result)
  }

  test("Operator.Binary.AdditiveOp -") {
    val input = "-"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.AdditiveOp).get
    assertResult(BinaryOperator.Minus)(result)
  }

  test("Operator.ExtendedBinary.Leq ⊑") {
    val input = "⊑"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ExtendedBinaryOp).get
    assertResult(ExtendedBinaryOperator.Leq)(result)
  }

  test("Operator.ExtendedBinary.Lub ⊔") {
    val input = "⊔"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ExtendedBinaryOp).get
    assertResult(ExtendedBinaryOperator.Lub)(result)
  }

  test("Operator.ExtendedBinary.Glb ⊓") {
    val input = "⊓"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ExtendedBinaryOp).get
    assertResult(ExtendedBinaryOperator.Glb)(result)
  }

  test("Operator.ExtendedBinary.Widen ▽") {
    val input = "▽"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ExtendedBinaryOp).get
    assertResult(ExtendedBinaryOperator.Widen)(result)
  }

  test("Operator.ExtendedBinary.Narrow △") {
    val input = "△"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ExtendedBinaryOp).get
    assertResult(ExtendedBinaryOperator.Narrow)(result)
  }

  /////////////////////////////////////////////////////////////////////////////
  // UTF8 Operators                                                          //
  /////////////////////////////////////////////////////////////////////////////
  test("Operator.Unary.UTF8-Negation") {
    val input = "¬"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.UnaryOp).get
    assertResult(UnaryOperator.LogicalNot)(result)
  }

  test("Operator.Binary.UTF8-Equal") {
    val input = "≡"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.ComparisonOp).get
    assertResult(BinaryOperator.Equal)(result)
  }

  test("Operator.Binary.UTF8-Conjunction") {
    val input = "∧"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.LogicalAnd)(result)
  }

  test("Operator.Binary.UTF8-Disjunction") {
    val input = "∨"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.LogicalOr)(result)
  }

  test("Operator.Binary.UTF8-Implication") {
    val input = "→"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.Implication)(result)
  }

  test("Operator.Binary.UTF8-Biconditional") {
    val input = "↔"
    val parser = mkParser(input)
    val result = parser.__run(parser.Operators.LogicalOp).get
    assertResult(BinaryOperator.Biconditional)(result)
  }

  /////////////////////////////////////////////////////////////////////////////
  // Annotations                                                             //
  /////////////////////////////////////////////////////////////////////////////
  test("Annotation @strict") {
    val input = "@strict"
    val parser = mkParser(input)
    val result = parser.__run(parser.Annotation).get
    assert(result.isInstanceOf[ParsedAst.Annotation])
  }

  test("Annotation @monotone") {
    val input = "@monotone"
    val parser = mkParser(input)
    val result = parser.__run(parser.Annotation).get
    assert(result.isInstanceOf[ParsedAst.Annotation])
  }

  test("Annotation.AnnotatedFunction01") {
    val input =
      """@strict
        |fn f(x: Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition)
    assert(result.get.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedFunction02") {
    val input =
      """@monotone
        |fn f(x: Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedFunction03") {
    val input =
      """@strict @monotone
        |fn f(x: Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedFunction04") {
    val input =
      """@strict @monotone @commutative @associative @unsafe @unchecked
        |fn f(x: Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedParameter01") {
    val input =
      """fn f(x: @strict Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedParameter02") {
    val input =
      """fn f(x: @monotone Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedParameter03") {
    val input =
      """fn f(x: @strict @monotone Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  test("Annotation.AnnotatedParameter04") {
    val input =
      """fn f(x: @strict Int, y: Int, z: @monotone Int): Int = x
      """.stripMargin
    val parser = mkParser(input)
    val result = parser.__run(parser.FunctionDefinition).get
    assert(result.isInstanceOf[ParsedAst.Definition.Function])
  }

  /////////////////////////////////////////////////////////////////////////////
  // Whitespace                                                              //
  /////////////////////////////////////////////////////////////////////////////
  test("WhiteSpace (1)") {
    val input = " "
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  test("WhiteSpace (2)") {
    val input = "    "
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  test("WhiteSpace (3)") {
    val input = "\t"
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  test("WhiteSpace (4)") {
    val input = "\n\r"
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  test("WhiteSpace (5)") {
    val input = " // comments are also whitespace "
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  test("WhiteSpace (6)") {
    val input = " /* comments are also whitespace */ "
    val result = new Parser(SourceInput.Str(input)).WS.run()
    assert(result.isSuccess)
  }

  /////////////////////////////////////////////////////////////////////////////
  // Comments                                                                //
  /////////////////////////////////////////////////////////////////////////////
  test("SingleLineComment (1)") {
    val input = "// a comment"
    val result = new Parser(SourceInput.Str(input)).Comment.run()
    assert(result.isSuccess)
  }

  test("SingleLineComment (2)") {
    val input =
      """// a comment
        |// another comment
        |// and yet another
      """.stripMargin
    val result = new Parser(SourceInput.Str(input)).Comment.run()
    assert(result.isSuccess)
  }

  test("MultiLineComment (1)") {
    val input = "/* a comment */"
    val result = new Parser(SourceInput.Str(input)).Comment.run()
    assert(result.isSuccess)
  }

  test("MultiLineComment (2)") {
    val input =
      """/*
        |a comment
        |*/""".stripMargin
    val result = new Parser(SourceInput.Str(input)).Comment.run()
    assert(result.isSuccess)
  }


  /**
    * Returns a parser for the given string `s`.
    */
  private def mkParser(s: String): Parser = new Parser(SourceInput.Str(s))
}
