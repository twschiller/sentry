import org.scalatest.FunSuite

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

/**
 * Created by tschiller on 5/16/15.
 */
class LicenseSpec extends FunSuite {

  def check = (l : String, target : License) => {
    assert(License.findCanonical(l).getOrElse(None) === target)
  }

  test("can match Apache 2.0 licenses") {
    check("The Apache Software License, Version 2.0", License.Apache2)
    check("Apache License, Version 2.0, Version 2.0", License.Apache2)
    check("ASF 2.0", License.Apache2)
  }

  test("can match EPL 1.0 licenses") {
    check("Eclipse Public License 1.0", License.EPL1)
    check("Eclipse Public License - v 1.0", License.EPL1)
  }

  test("can match MIT licenses") {
    check("MIT", License.MIT)
    check("MIT License", License.MIT)
  }

  test("can match BSD 2 licenses") {
    check("BSD", License.BSD2)
    check("BSD License", License.BSD2)
  }

  test("can match BSD 3 licenses") {
    check("New BSD License", License.BSD3)
  }

  test("can match CDDL licenses") {
    check("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0", License.CDDL)
    check("CDDL", License.CDDL)
  }

  test("can match LGPL-3.0 licenses") {
    check("GNU Lesser General Public License", License.LGPL3)
  }

  test("can match LGPL-2.1 licenses") {
    check("LGPL 2.1", License.LGPL2)
  }

  test("can match MPL-1.1 licenses") {
    check("MPL 1.1", License.MPL1)
  }

  test("can match GPLv2+CE") {
    check("GPLv2+CE", License.GPL2CE)
    check("GNU General Public License (GPL), version 2, with the Classpath exception", License.GPL2CE)
  }

  test("don't match mixed licenses") {
    assert(License.findCanonical("CDDL/GPLv2+CE") === None)
  }

  test("don't match licenses with exceptions") {
    assert(License.findCanonical("All files contained in this JAR are licensed under the Apache 2.0 license, unless noted differently in their source (see swing2swt).") === None)
  }
}
