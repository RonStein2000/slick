package scala.slick.ql

import TypeMapper._
import scala.slick.ast.{SimpleNode, Node, UnaryNode, BinaryNode}

trait ColumnOps[B1, P1] {
  protected val leftOperand: Node
  import ColumnOps._
  type OM2[B2, BR, P2, R] = OptionMapper2[B1, B2, BR, P1, P2, R]
  type OM2Bin[BR, P2, R] = OM2[B1, BR, P2, R]
  type OM3[B2, B3, BR, P2, P3, R] = OptionMapper3[B1, B2, B3, BR, P1, P2, P3, R]
  type ToOption = OptionMapper2[B1, B1, B1, Option[B1], Option[B1], Option[B1]]
  type ToSame = OM2Bin[B1, P1, P1]
  type Restr2[B, BR, P2, PR] = OptionMapper2[B, B, BR, P1, P2, PR]
  type Restr1[B, BR, PR] = Restr2[B, BR, P1, PR]
  type BaseTM = BaseTypeMapper[B1]
  type Num = BaseTM with NumericTypeMapper

  def is[P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Is(leftOperand, Node(e)))
  def === [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Is(leftOperand, Node(e)))
  def isNot[P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Not(Is(leftOperand, Node(e))))
  @deprecated("Use =!= instead", "0.9.0")
  def != [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Not(Is(leftOperand, Node(e))))
  def =!= [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Not(Is(leftOperand, Node(e))))
  def < [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Relational("<", leftOperand, Node(e)))
  def <= [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Relational("<=", leftOperand, Node(e)))
  def > [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Relational(">", leftOperand, Node(e)))
  def >= [P2, R](e: Column[P2])(implicit om: OM2Bin[Boolean, P2, R]) =
    om(Relational(">=", leftOperand, Node(e)))
  def inSet[R](seq: Traversable[B1])(implicit om: OM2Bin[Boolean, P1, R], tm: BaseTM) =
    om(InSet(leftOperand, seq, false)(tm))
  def inSetBind[R](seq: Traversable[B1])(implicit om: OM2Bin[Boolean, P1, R], tm: BaseTM) =
    om(InSet(leftOperand, seq, true)(tm))
  def between[P2, P3, R](start: Column[P2], end: Column[P3])(implicit om: OM3[B1, B1, Boolean, P2, P3, R]) =
    om(Between(leftOperand, Node(start), Node(end)))
  def ifNull[B2, P2, R](e: Column[P2])(implicit om: OM2[B2, Boolean, P2, R]): Column[P2] =
    e.mapOp(c => EscFunction[P2]("ifnull", leftOperand, Node(c))(e.typeMapper))
  def min(implicit om: ToOption, tm: BaseTM) =
    om(StdFunction[B1]("min", leftOperand))
  def max(implicit om: ToOption, tm: BaseTM) =
    om(StdFunction[B1]("max", leftOperand))

  // NumericTypeMapper only
  def + [P2, R](e: Column[P2])(implicit om: OM2Bin[B1, P2, R], tm: Num) =
    om(Arith[B1]("+", leftOperand, Node(e)))
  def - [P2, R](e: Column[P2])(implicit om: OM2Bin[B1, P2, R], tm: Num) =
    om(Arith[B1]("-", leftOperand, Node(e)))
  def * [P2, R](e: Column[P2])(implicit om: OM2Bin[B1, P2, R], tm: Num) =
    om(Arith[B1]("*", leftOperand, Node(e)))
  def / [P2, R](e: Column[P2])(implicit om: OM2Bin[B1, P2, R], tm: Num) =
    om(Arith[B1]("/", leftOperand, Node(e)))
  def % [P2, R](e: Column[P2])(implicit om: OM2Bin[B1, P2, R], tm: Num) =
    om(EscFunction[B1]("mod", leftOperand, Node(e)))
  def abs(implicit om: ToSame, tm: Num) =
    om(EscFunction[B1]("abs", leftOperand))
  def ceil(implicit om: ToSame, tm: Num) =
    om(EscFunction[B1]("ceiling", leftOperand))
  def floor(implicit om: ToSame, tm: Num) =
    om(EscFunction[B1]("floor", leftOperand))
  def sign[R](implicit om: OM2Bin[Int, P1, R], tm: Num) =
    om(EscFunction[Int]("sign", leftOperand))
  def toDegrees(implicit om: ToSame, tm: Num) =
    om(EscFunction[B1]("degrees", leftOperand))
  def toRadians(implicit om: ToSame, tm: Num) =
    om(EscFunction[B1]("radians", leftOperand))
  def avg(implicit om: ToOption, tm: Num) =
    om(StdFunction[B1]("avg", leftOperand))
  def sum(implicit om: ToOption, tm: Num) =
    om(StdFunction[B1]("sum", leftOperand))

  // Boolean only
  def &&[P2, R](b: Column[P2])(implicit om: Restr2[Boolean, Boolean, P2, R]) =
    om(And(leftOperand, Node(b)))
  def ||[P2, R](b: Column[P2])(implicit om: Restr2[Boolean, Boolean, P2, R]) =
    om(Or(leftOperand, Node(b)))
  def unary_![R](implicit om: Restr1[Boolean, Boolean, R]) =
    om(Not(leftOperand))

  // String only
  def length[R](implicit om: Restr1[String, Int, R]) =
    om(EscFunction[Int]("length", leftOperand))
  def like[P2, R](e: Column[P2])(implicit om: Restr2[String, Boolean, P2, R]) =
    om(Like(leftOperand, Node(e), None))
  def like[P2, R](e: Column[P2], esc: Char)(implicit om: Restr2[String, Boolean, P2, R]) =
    om(Like(leftOperand, Node(e), Some(esc)))
  def ++[P2, R](e: Column[P2])(implicit om: Restr2[String, String, P2, R]) =
    om(EscFunction[String]("concat", leftOperand, Node(e)))
  def startsWith[R](s: String)(implicit om: Restr1[String, Boolean, R]) =
    om(new StartsWith(leftOperand, s))
  def endsWith[R](s: String)(implicit om: Restr1[String, Boolean, R]) =
    om(new EndsWith(leftOperand, s))
  def toUpperCase[R](implicit om: Restr1[String, String, R]) =
    om(EscFunction[String]("ucase", leftOperand))
  def toLowerCase[R](implicit om: Restr1[String, String, R]) =
    om(EscFunction[String]("lcase", leftOperand))
  def ltrim[R](implicit om: Restr1[String, String, R]) =
    om(EscFunction[String]("ltrim", leftOperand))
  def rtrim[R](implicit om: Restr1[String, String, R]) =
    om(EscFunction[String]("rtrim", leftOperand))
  def trim[R](implicit om: Restr1[String, String, R]) =
    om(EscFunction[String]("ltrim", EscFunction[String]("rtrim", leftOperand)))
}

object ColumnOps {
  final case class In(left: Node, right: Node) extends OperatorColumn[Boolean] with SimpleBinaryOperator {
    val name = "in"
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
  }
  final case class CountAll(child: Node) extends OperatorColumn[Int] with UnaryNode {
    protected[this] def nodeRebuild(child: Node): Node = copy(child = child)
  }

  final case class Relational(name: String, left: Node, right: Node) extends OperatorColumn[Boolean] with SimpleBinaryOperator {
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
  }
  final case class Arith[T : TypeMapper](name: String, left: Node, right: Node) extends OperatorColumn[T] with SimpleBinaryOperator {
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy[T](left = left, right = right)
  }

  final case class Is(left: Node, right: Node) extends OperatorColumn[Boolean] with BinaryNode {
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
  }
  final case class CountDistinct(child: Node) extends OperatorColumn[Int] with UnaryNode {
    protected[this] def nodeRebuild(child: Node): Node = copy(child = child)
  }
  final case class InSet[T](child: Node, seq: Traversable[T], bind: Boolean)(val tm: TypeMapper[T]) extends OperatorColumn[Boolean] with UnaryNode {
    protected[this] def nodeRebuild(child: Node): Node = copy[T](child = child)()
  }

  final case class Between(left: Node, start: Node, end: Node) extends OperatorColumn[Boolean] with SimpleNode {
    protected[this] def nodeChildGenerators = Seq(left, start, end)
    protected[this] def nodeRebuild(ch: IndexedSeq[Node]): Node = Between(ch(0), ch(1), ch(2))
  }

  final case class AsColumnOf[T : TypeMapper](child: Node, typeName: Option[String]) extends Column[T] with UnaryNode {
    protected[this] def nodeRebuild(child: Node): Node = copy[T](child = child)
  }

  // Boolean
  final case class And(left: Node, right: Node) extends OperatorColumn[Boolean] with SimpleBinaryOperator {
    val name = "and"
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
  }
  final case class Or(left: Node, right: Node) extends OperatorColumn[Boolean] with SimpleBinaryOperator {
    val name = "or"
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
  }
  final case class Not(child: Node) extends OperatorColumn[Boolean] with UnaryNode {
    protected[this] def nodeRebuild(child: Node): Node = copy(child = child)
  }

  // String
  sealed case class Like(left: Node, right: Node, esc: Option[Char]) extends OperatorColumn[Boolean] with BinaryNode {
    protected[this] def nodeRebuild(left: Node, right: Node): Node = copy(left = left, right = right)
    override def equals(o: Any) = getClass == o.asInstanceOf[AnyRef].getClass && super.equals(o)
  }
  final class StartsWith(n: Node, s: String) extends Like(n, ConstColumn(likeEncode(s)+'%'), Some('^')) {
    protected[this] override def nodeRebuild(left: Node, right: Node): Node = new StartsWith(left, s)
  }
  final class EndsWith(n: Node, s: String) extends Like(n, ConstColumn('%'+likeEncode(s)), Some('^')) {
    protected[this] override def nodeRebuild(left: Node, right: Node): Node = new EndsWith(left, s)
  }

  def likeEncode(s: String) = {
    val b = new StringBuilder
    for(c <- s) c match {
      case '%' | '_' | '^' => b append '^' append c
      case _ => b append c
    }
    b toString
  }
}
