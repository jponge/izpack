package com.izforge.izpack.compiler

import container.CompilerContainer
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import java.util.Properties
import org.scalatest.{Spec}

/**
 * Created by IntelliJ IDEA.
 * @author Anthonin Bonnefoy
 */

class PackagerTest extends Spec with ShouldMatchers with MockitoSugar {
  var properties = mock[Properties]
  var compilerContainer = mock[CompilerContainer]
  var packagerLisetner = mock[PackagerListener]


  describe("The packager") {
    val packager = new Packager(properties, compilerContainer, packagerLisetner)
    it("should add variables") {

    }

  }

}