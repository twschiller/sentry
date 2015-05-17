import java.net.URL

/*
 * Copyright (c) 2015 Todd Schiller. This file is part of Sentry.
 *
 * Sentry is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later version.
 *
 * Sentry is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Sentry.  If not, see <http://www.gnu.org/licenses/>.
 */

class License(val name : String, val url: Option[URL]) {
  def fuzzyMatch(other: License): Boolean = {
    this.name == other.name
  }

  override def toString = name
}

object License {

  // TODO: model superceded licenses http://opensource.org/licenses/category
  // TODO: model retired licenses http://opensource.org/licenses/category

  def safeMake(name : String, url : Option[String]): License ={
    try {
      new License(name, if (url.nonEmpty) Some(new URL(url.get)) else None)
    } catch {
      case _ : Exception => new License(name, None)
    }
  }

  /** Returns the canonical license entry */
  def findCanonical(name: String) : Option[License] = {
    val query = new License(name, None)
    if (!isModified(query)) findCanonical(query) else None
  }

  /** true if the license appears to be modified / customized */
  def isModified(license: License) : Boolean = {
    license.name.toUpperCase.contains(" UNLESS ")
  }

  val Apache2 = new License("Apache-2.0", Some(new URL("http://opensource.org/licenses/Apache-2.0"))) {
    override def fuzzyMatch(other: License) = {
      super.fuzzyMatch(other) || ((other.name.contains("Apache") || other.name.contains("ASF")) && other.name.contains("2.0"))
    }
  }

  val MIT = new License("MIT", Some(new URL("http://opensource.org/licenses/MIT"))) {
    override def fuzzyMatch(other: License) = {
      super.fuzzyMatch(other) || other.name.contains("MIT")
    }
  }

  val BSD2 = new License("BSD-2-Clause", Some(new URL("http://opensource.org/licenses/bsd-license"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || upper == "BSD" || upper == "BSD LICENSE"
    }
  }

  val BSD3 = new License("BSD-3-Clause", Some(new URL("http://opensource.org/licenses/BSD-3-Clause"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || upper.contains("BSD NEW") || upper.contains("BSD SIMPLIFIED") || upper.contains("NEW BSD")
    }
  }

  val CDDL = new License("CDDL-1.0", Some(new URL("http://opensource.org/licenses/CDDL-1.0"))) {
    override def fuzzyMatch(other: License) = {
      super.fuzzyMatch(other) || other.name == "CDDL" || other.name.toUpperCase.contains("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE")
    }
  }

  val GPL2 = new License("GPL-3.0", Some(new URL("http://opensource.org/licenses/GPL-2.0"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase

      super.fuzzyMatch(other) ||
        (upper.contains("GNU GENERAL PUBLIC LICENSE") && upper.contains("VERSION 2") && !upper.contains("EXCEPTION"))
    }
  }

  val LGPL3 = new License("LGPL-3.0", Some(new URL("http://opensource.org/licenses/LGPL-3.0"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || upper.contains("GNU LESSER GENERAL PUBLIC LICENSE")
    }
  }

  val LGPL2 = new License("LGPL-2.1", Some(new URL("http://opensource.org/licenses/LGPL-2.1"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || (upper.contains("LGPL") && upper.contains("2.1"))
    }
  }

  val MPL1 = new License("MPL-1.1", None) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || (upper.contains("MPL") && upper.contains("1.1"))
    }
  }

  val MPL2 = new License("MPL-2.0", None) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase
      super.fuzzyMatch(other) || ((upper.contains("MPL") || upper.contains("MOZILLA PUBLIC LICENSE")) && upper.contains("2.0"))
    }
  }

  val GPL2CE = new License("GPL-2.0 CE", Some(new URL("http://openjdk.java.net/legal/gplv2+ce.html"))) {
    override def fuzzyMatch(other: License) = {
      val upper = other.name.toUpperCase

      super.fuzzyMatch(other) ||
        other.name == "GPLv2+CE" ||
        (upper.contains("GNU GENERAL PUBLIC LICENSE") && upper.contains("VERSION 2") && upper.contains("CLASSPATH EXCEPTION"))
    }
  }

  val GPL3 = new License("GPL-2.0", Some(new URL("http://opensource.org/licenses/GPL-3.0"))) {
    override def fuzzyMatch(other: License) = {
      super.fuzzyMatch(other) || (other.name.toUpperCase.contains("GNU GENERAL PUBLIC LICENSE") && other.name.contains("Version 3"))
    }
  }

  val EPL1 = new License("EPL-1.0", Some(new URL("http://opensource.org/licenses/EPL-1.0"))) {
    override def fuzzyMatch(other: License) = {
      super.fuzzyMatch(other) || (other.name.contains("Eclipse Public License") && other.name.contains("1.0"))
    }
  }

  val CANONICAL = List(
    Apache2,
    MIT,
    CDDL,
    GPL2,
    GPL2CE,
    GPL3,
    EPL1,
    BSD2,
    BSD3,
    LGPL2,
    LGPL3,
    MPL1,
    MPL2
  )

  /** Returns the canonical license entry **/
  def findCanonical(license : License) : Option[License] = {
    val matches = CANONICAL.filter(l => l.fuzzyMatch(license))
    if (matches.length == 1) {
      Some(matches.head)
    } else if (matches.length > 1){
      println(s"Found multiple matches for license with name ${license.name}")
      None
    } else{
      None
    }
  }
}




