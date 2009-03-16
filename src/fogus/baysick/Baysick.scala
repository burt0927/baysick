package fogus.baysick {
  import scala.collection.mutable.HashMap

  class Baysick {
    abstract sealed class BasicLine
    case class PrintString(num: Int, s: String) extends BasicLine
    case class PrintResult(num:Int, fn:Function0[String]) extends BasicLine
    case class PrintVariable(num: Int, s: Symbol) extends BasicLine
    case class PrintLine(num: Int, str: String, name: Symbol) extends BasicLine
    case class PrintNumber(num: Int, number: BigInt) extends BasicLine
    case class GotoLine(num: Int, to: Int) extends BasicLine
    case class InputLine(num: Int, name: Symbol) extends BasicLine
    case class EndLine(num: Int) extends BasicLine

    val lines = new HashMap[Int, BasicLine]
    val binds = new HashMap[Symbol, Any]

    case class Appendr(str: String) {
      var append = str

      def %(name:Symbol):Function0[String] = {
        return new Function0[String] {
          def apply():String = {
            return str.concat(binds(name).toString)
          }
        }
      }
    }

    case class LineBuilder(num: Int) {
      def END() = lines(num) = EndLine(num)

      object PRINT {
        def apply(str:String) = lines(num) = PrintString(num, str)
        def apply(number: BigInt) = lines(num) = PrintNumber(num, number)
        def apply(s: Symbol) = lines(num) = PrintVariable(num, s)
        def apply(str: String, name: Symbol) = lines(num) = PrintLine(num, str, name)
        def apply(fn:Function0[String]) = lines(num) = PrintResult(num, fn)
      }

      object INPUT {
        def apply(name: Symbol) = lines(num) = InputLine(num, name)
      }

      object GOTO {
        def apply(to: Int) = lines(num) = GotoLine(num, to)
      }
    }

    private def gotoLine(line: Int) {
      lines(line) match {
        case PrintLine(_, str: String, name: Symbol) => {
          val value = binds(name)
          println(str + value)
          gotoLine(line + 10)
        }
        case PrintNumber(_, number:BigInt) => {
          println(number)
          gotoLine(line + 10)
        }
        case PrintString(_, s:String) => {
          println(s)
          gotoLine(line + 10)
        }
        case PrintResult(_, fn:Function0[String]) => {
          println(fn())
          gotoLine(line + 10)
        }
        case PrintVariable(_, s:Symbol) => {
          val value = binds(s)
          println(value)
          gotoLine(line + 10)
        }
        case InputLine(_, name) => {
          val entry = readLine
          binds(name) = entry
          gotoLine(line + 10)
        }
        case GotoLine(_, to) => gotoLine(to)
        case EndLine(_) => {
          println("-- Done at line " + line)
        }
      }
    }

    def RUN {
      gotoLine(lines.keys.toList.sort((l,r) => l < r).first)
    }

    implicit def int2LineBuilder(i: Int) = LineBuilder(i)
    implicit def string2Appendr(s:String) = Appendr(s)
  }
}
