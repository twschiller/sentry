object HelloWorld {
  def main(args: Array[String]) {
    val now = new org.joda.time.DateTime()
    println("Hi SBT, the time is " + now.toString("hh:mm aa"))
  }
}
